package com.russell.smb

import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.SmbConfig
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.share.DiskShare
import java.util.concurrent.TimeUnit

class SmbClient {

    private lateinit var connectShare: DiskShare
    private lateinit var connection: Connection

    @set:Synchronized
    private  var  connectStatus = Status.initialization

    fun init(builder:Build) {
        Thread{
            connectStatus = Status.connecting
            val config = SmbConfig.builder().withSoTimeout(builder.timeOut)
                .withReadTimeout(builder.timeOut.toLong(),TimeUnit.SECONDS).withTimeout(builder.timeOut.toLong(),TimeUnit.SECONDS).build()
            val client = SMBClient(config)
            connection = client.connect(builder.ip)
            val authContext = AuthenticationContext(builder.username, builder.password.toCharArray(), null)
            val session = connection.authenticate(authContext)
            if(session?.connectShare(builder.fileName) == null){
                connectStatus = Status.connectFalse
                throw Exception("请检查文件夹名称")
            }
            connectShare = session.connectShare(builder.fileName) as DiskShare
            connectStatus = Status.connectSuccess
            if(onReadFileCallback!=null){
                listFiles(onReadFilePath,onReadFileCallback!!)
            }
        }.start()
    }

    var onReadFileCallback:OnReadFileCallback? = null
    var onReadFilePath :String = ""
    fun listFiles(path :String,callback :OnReadFileCallback){
        if(connectStatus != Status.connectSuccess){
            callback.onFail("暂未连接，稍后再试")
            onReadFileCallback=callback
            onReadFilePath=path
            return
        }
        try {
            val fileNameList = mutableListOf<FileInfo>()
            val list = connectShare.list(path, null)
            for (information in list) {
                if(information.fileName == "." || information.fileName ==".."){
                    continue
                }
                connectShare.getFileInformation(information.fileName).standardInformation.isDirectory
                fileNameList.add(FileInfo(information.fileName,isDirectory(information.fileName)))
            }
            callback.onSuccess(fileNameList)
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onFail(e.message ?: "获取文件名失败")
        }

    }

    private fun isDirectory(path :String):Boolean=  connectShare.getFileInformation(path).standardInformation.isDirectory

    fun openDirectory(path :String,callback :OnReadFileCallback){
        try {
            val fileNameList = mutableListOf<FileInfo>()
            val list = connectShare.list(path, null)
            for (information in list) {
                if(information.fileName == "." || information.fileName ==".."){
                    continue
                }
                fileNameList.add(FileInfo(information.fileName,isDirectory(path+"\\"+information.fileName)))
            }
            callback.onSuccess(fileNameList)
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onFail(e.message ?: "获取文件名失败")
        }
    }


    companion object{
       class Build {
            var timeOut  = 60
            var ip = ""
            var username = ""
            var password =""
            var fileName = ""

            fun setTimeOut(time:Int):Build{
                timeOut = time
                return this
            }

            fun setIp(ip:String):Build{
                this.ip = ip
                return this
            }
            fun setUsername(username:String):Build{
                this.ip = username
                return this
            }
            fun setPassword(password:String):Build{
                this.password = password
                return this
            }
            fun setFileName(fileName:String):Build{
                this.fileName = fileName
                return this
            }

            fun setSmbConfig(ip:String,username:String,password:String,fileName:String):Build{
                this.ip = ip
                this.username = username
                this.password = password
                this.fileName = fileName
                return this
            }

            fun build():SmbClient{
                val smbClient = SmbClient()
                smbClient.init(this)
                return smbClient
            }

        }


        @JvmStatic
        fun Builder(): Build {
            return Build()
        }

    }

    enum class Status(status:Int){
        initialization(0), connecting(1),connectFalse(2),connectSuccess(2)
    }

}