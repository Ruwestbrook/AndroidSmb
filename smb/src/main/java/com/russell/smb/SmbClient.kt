package com.russell.smb

import android.util.Log
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2CreateOptions
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.SmbConfig
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import com.hierynomus.smbj.share.File
import com.russell.smb.Utils.Companion.SMB_TAG
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.util.*
import java.util.concurrent.TimeUnit

class SmbClient() {
    private  var connectShare: DiskShare? = null
    private  var sessionShare: Session?= null
    var clientStatus:ClientStatus? = null
    set(value) {
        field= value
        field?.initStatus(connectStatus)
    }
    @set:Synchronized
    private  var  connectStatus = Status.Initialization
    private  var initTime = 0
    fun listFiles(path :String,callback :OnReadFileCallback){
        try {
            initTime=0
            val fileNameList = mutableListOf<FileInfo>()
            Log.d(SMB_TAG, "listFiles: ")
            val list = connectShare!!.list(path, null)
            for (information in list) {
                if(information.fileName == "." || information.fileName ==".."){
                    continue
                }
                connectShare!!.getFileInformation(path+"/"+information.fileName).standardInformation.isDirectory
                val isDirectory = isDirectory(path+"/"+information.fileName)
                val fileInfo =FileInfo(information.fileName,isDirectory,FileType.UNKNOWN,0)
                Utils.getFileType(information.fileName,isDirectory,fileInfo)
                fileNameList.add(fileInfo)
            }
            callback.onSuccess(fileNameList)
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onFail(e.message ?: "获取文件名失败")
        }

    }


    fun openTextFile(path :String,name:String,callback: OnReadTextFileCallback){
        Log.d(SMB_TAG, "openTextFile() called with: path = $connectShare")
        Log.d(SMB_TAG, "openTextFile() called with: path = ${connectShare?.isConnected}")
        Thread{
            try {
                Log.d(SMB_TAG, "openTextFile() called with: path ="+connectShare!!.getFileInformation("/local.properties"))
                connectShare!!.getFileInformation("/local.properties")
                val file :File = connectShare!!.openFile(
                    "local.properties",
                    EnumSet.of(AccessMask.GENERIC_READ), null,
                    SMB2ShareAccess.ALL,
                    SMB2CreateDisposition.FILE_OPEN, null)


                val reader= InputStreamReader(file.inputStream)
                val bufferedReader = BufferedReader(reader)
                val stringBuilder = StringBuilder()
                var line = bufferedReader.readLine()
                Log.d(SMB_TAG, "openFile: $line")
                while (line != null){
                    Log.d(SMB_TAG, "openFile: $line")
                    stringBuilder.append(line).append("\n")
                    line = bufferedReader.readLine()
                }
                callback.onSuccess(stringBuilder.toString())
               //
            }catch (e:Exception){
                callback.onFail(e.message ?:"")
                Log.d(SMB_TAG, "openTextFile: e")

            }
        }.start()

    }

    var fileName:String = ""
    private fun connection(ip:String,username:String,password:String,fileName:String){
        this.fileName= fileName
        Thread{
            setStatus(Status.Connecting)
            val config = SmbConfig.builder().withSoTimeout(Int.MAX_VALUE.toLong(),TimeUnit.MILLISECONDS)
                .withReadTimeout(60L,TimeUnit.SECONDS).withTimeout(60L,TimeUnit.SECONDS).build()
            val client = SMBClient(config)
            val connection = client.connect(ip)
            val authContext = AuthenticationContext(username, password.toCharArray(), null)
            sessionShare = connection.authenticate(authContext)
            if(sessionShare?.connectShare(fileName) == null){
                setStatus(Status.ConnectFalse)
                throw Exception("请检查文件夹名称")
            }
            connectShare = sessionShare!!.connectShare(fileName) as DiskShare
            setStatus( Status.ConnectSuccess)
            Log.d(SMB_TAG, "setStatus( Status.ConnectSuccess):$clientStatus")
        }.start()
    }

    fun getConnection() = connectShare

    private fun isDirectory(path :String):Boolean= connectShare?.getFileInformation(path)?.standardInformation?.isDirectory == true
    private fun setStatus(status: Status){

        connectStatus =status
        clientStatus?.initStatus(status)
    }



    private object SingletonHolder {
        val holder = SmbClient()

    }

    companion object{


        val instance = SingletonHolder.holder


        fun setSmbConfig(ip:String,username:String,password:String,fileName:String){
            instance.connection(ip,username,password,fileName)
        }

    }

    enum class Status(status:Int){
        Initialization(0), Connecting(1),ConnectFalse(2),ConnectSuccess(3)
    }

    interface ClientStatus{
        fun initStatus(status:Status) {

        }
    }

}