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
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit

class SmbClient() {
    private var connectShare: DiskShare? = null
    private val executorService = Executors.newFixedThreadPool(2)
    var clientStatus: ClientStatus? = null
        set(value) {
            field = value
            field?.initStatus(connectStatus)
        }

    @set:Synchronized
    private var connectStatus = Status.Initialization
    private var initTime = 0
    fun listFiles(path: String, callback: OnReadFileCallback) {
        listFiles(path,null,callback)
    }


    fun listFiles(path: String,fileType: FileType?, callback: OnReadFileCallback) {
        executorService.submit {
            try {
                initTime = 0
                val fileNameList = mutableListOf<FileInfo>()
                Log.d(SMB_TAG, "listFiles: ")
                val list = connectShare!!.list(path, null)
                for (information in list) {
                    if (information.fileName == "." || information.fileName == "..") {
                        continue
                    }
                    connectShare!!.getFileInformation(path + "/" + information.fileName).standardInformation.isDirectory
                    val isDirectory = isDirectory(path + "/" + information.fileName)
                    val fileInfo = FileInfo(information.fileName, isDirectory, FileType.UNKNOWN, 0)
                    Utils.getFileType(information.fileName, isDirectory, fileInfo)
                    if(fileType != null){
                        if(fileInfo.fileType == fileType){
                            fileNameList.add(fileInfo)
                        }
                    }else{
                        fileNameList.add(fileInfo)
                    }
                }
                callback.onSuccess(fileNameList)
            } catch (e: Exception) {
                e.printStackTrace()
                callback.onFail(e.message ?: "获取文件名失败")
            }
        }
    }


    fun openTextFileAsync(path: String, name: String): String? {
        val  result = FutureTask {
            val stringBuilder = StringBuilder()
            try {
                connectShare!!.getFileInformation("/local.properties")
                val file: File = connectShare!!.openFile(
                    "$path/$name",
                    EnumSet.of(AccessMask.GENERIC_READ), null,
                    SMB2ShareAccess.ALL,
                    SMB2CreateDisposition.FILE_OPEN, null
                )

                val reader = InputStreamReader(file.inputStream)
                val bufferedReader = BufferedReader(reader)

                var line = bufferedReader.readLine()
                Log.d(SMB_TAG, "openFile: $line")
                while (line != null) {
                    Log.d(SMB_TAG, "openFile: $line")
                    stringBuilder.append(line).append("\n")
                    line = bufferedReader.readLine()
                }
            } catch (e: Exception) {
                Log.d(SMB_TAG, "openTextFile: $e")
            }
            return@FutureTask stringBuilder.toString()
        }

        executorService.submit(result)
        return result.get()

    }



    fun openTextFile(path: String, name: String, callback: OnReadTextFileCallback) {

        executorService.submit {
            val stringBuilder = StringBuilder()
            try {
                connectShare!!.getFileInformation("/local.properties")
                val file: File = connectShare!!.openFile(
                    "$path/$name",
                    EnumSet.of(AccessMask.GENERIC_READ), null,
                    SMB2ShareAccess.ALL,
                    SMB2CreateDisposition.FILE_OPEN, null
                )

                val reader = InputStreamReader(file.inputStream)
                val bufferedReader = BufferedReader(reader)

                var line = bufferedReader.readLine()
                Log.d(SMB_TAG, "openFile: $line")
                while (line != null) {
                    Log.d(SMB_TAG, "openFile: $line")
                    stringBuilder.append(line).append("\n")
                    line = bufferedReader.readLine()
                }
                callback.onSuccess(stringBuilder.toString())
            } catch (e: Exception) {
                Log.d(SMB_TAG, "openTextFile: $e")
                callback.onFail(e.message ?: "")
            }

        }
    }


    fun openFile(path: String, name: String, callback: OnReadFileInputStreamCallback) {
        executorService.submit {
            try {
                connectShare!!.getFileInformation("/local.properties")
                val file: File = connectShare!!.openFile(
                    "$path/$name",
                    EnumSet.of(AccessMask.GENERIC_READ), null,
                    SMB2ShareAccess.ALL,
                    SMB2CreateDisposition.FILE_OPEN, null
                )

                callback.onSuccess(file.inputStream)


            } catch (e: Exception) {
                Log.e(SMB_TAG, "openTextFile: $e")
                callback.onFail(e.message ?: "")
            }

        }
    }


    private fun connection(ip: String, username: String, password: String, fileName: String) {
        executorService.submit {
            setStatus(Status.Connecting)
            val config =
                SmbConfig.builder().withSoTimeout(Int.MAX_VALUE.toLong(), TimeUnit.MILLISECONDS)
                    .withReadTimeout(60L, TimeUnit.SECONDS).withTimeout(60L, TimeUnit.SECONDS)
                    .build()
            val client = SMBClient(config)
            val connection = client.connect(ip)
            val authContext = AuthenticationContext(username, password.toCharArray(), null)
            val sessionShare = connection.authenticate(authContext)
            if (sessionShare?.connectShare(fileName) == null) {
                setStatus(Status.ConnectFalse)
                throw Exception("请检查文件夹名称")
            }
            connectShare = sessionShare.connectShare(fileName) as DiskShare
            setStatus(Status.ConnectSuccess)
            Log.d(SMB_TAG, "setStatus( Status.ConnectSuccess):$clientStatus")
        }
    }


    private fun isDirectory(path: String): Boolean =
        connectShare?.getFileInformation(path)?.standardInformation?.isDirectory == true

    private fun setStatus(status: Status) {

        connectStatus = status
        clientStatus?.initStatus(status)
    }


    private object SingletonHolder {
        val holder = SmbClient()

    }

    companion object {


        val instance = SingletonHolder.holder


        fun setSmbConfig(ip: String, username: String, password: String, fileName: String) {
            instance.connection(ip, username, password, fileName)
        }

    }

    enum class Status(status: Int) {
        Initialization(0), Connecting(1), ConnectFalse(2), ConnectSuccess(3)
    }

    interface ClientStatus {
        fun initStatus(status: Status) {

        }
    }

}