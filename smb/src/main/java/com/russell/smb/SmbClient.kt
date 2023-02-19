package com.russell.smb

import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.SmbConfig
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import java.util.concurrent.TimeUnit

class SmbClient(private val builder:Build) {

    private lateinit var connectShare: DiskShare
    private  var fileName =""
    private    var clientStatus:ClientStatus? = null
    @set:Synchronized
    private  var  connectStatus = Status.initialization


    private var onReadFileCallback:OnReadFileCallback? = null
    var onReadFilePath :String = ""
    var initTime = 0
    fun listFiles(path :String,callback :OnReadFileCallback){
        if(connectStatus != Status.connectSuccess){
            onReadFileCallback=callback
            onReadFilePath=path
            return
        }
        if(!connectShare.isConnected){
            if(initTime >2){
                callback.onFail( "获取文件名失败")
                clientStatus?.log("获取文件名失败")
                initTime=0
                return
            }
            connection()
            onReadFileCallback=callback
            onReadFilePath=path
            initTime ++
            return
        }
        try {
            initTime=0
            val fileNameList = mutableListOf<FileInfo>()
            clientStatus?.log("path="+path)
            val list = connectShare.list(path, null)
            clientStatus?.log("list="+list)
            list.forEach {

            }
            for (information in list) {
                if(information.fileName == "." || information.fileName ==".."){
                    continue
                }
                connectShare.getFileInformation(path+"/"+information.fileName).standardInformation.isDirectory
                fileNameList.add(FileInfo(information.fileName,isDirectory(path+"/"+information.fileName)))
            }
            callback.onSuccess(fileNameList)
            clientStatus?.log("listFiles apter connectShare .isConnected= "+connectShare.isConnected)
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onFail(e.message ?: "获取文件名失败")
        }

    }

    private fun connection(){
        Thread{
            clientStatus = builder.clientStatus
            setStatus(Status.connecting)
            val config = SmbConfig.builder().withSoTimeout(builder.timeOut.toLong(),TimeUnit.SECONDS)
                .withReadTimeout(builder.timeOut.toLong(),TimeUnit.SECONDS).withTimeout(builder.timeOut.toLong(),TimeUnit.SECONDS).build()
            val client = SMBClient(config)
            val connection = client.connect(builder.ip)
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

    fun getConnection() = connectShare

    private fun isDirectory(path :String):Boolean=  connectShare.getFileInformation(path).standardInformation.isDirectory
    private fun setStatus(status: Status){

        connectStatus =status
        clientStatus?.initStatus(status)
    }


    companion object{
       class Build {
            var timeOut  = 6000
            var ip = ""
            var username = ""
            var password =""
            var fileName = ""
            var clientStatus:ClientStatus? = null
            fun setTimeOut(time:Int):Build{
                timeOut = time
                return this
            }

           fun setClientStatus(clientStatus:ClientStatus):Build{
               this.clientStatus = clientStatus
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
                val smbClient = SmbClient(this)
                smbClient.connection()
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

    interface ClientStatus{
        fun initStatus(status:Status) {

        }
        fun log(message:String)
    }

}