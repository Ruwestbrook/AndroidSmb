package com.russell.smb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.russell.smb.Utils.Companion.SMB_TAG

class FileListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_list)
        Log.d(SMB_TAG, "onCreate: stringFromJNI="+stringFromJNI())
        FileFragment.pageClick = object : PageClick {
            override fun click(path: String, file: FileInfo) {
                if(file.isDirectory){
                    replaceFragment(path + "/" + file.fileName)
                }else if(file.fileType == FileType.TEXT || file.fileType == FileType.PROPERTIES){
                    Utils.startFileActivity(this@FileListActivity,path,file.fileName,TextActivity::class.java)
                }else if(file.fileType == FileType.IMAGE){
                    Utils.startFileActivity(this@FileListActivity,path,file.fileName,ImageActivity::class.java)
                }else if(file.fileType == FileType.VIDEO){
                    Utils.startFileActivity(this@FileListActivity,path,file.fileName,VideoActivity::class.java)
                }

            }

        }

        SmbClient.instance.clientStatus= object : SmbClient.ClientStatus {
            override fun initStatus(status: SmbClient.Status) {
                Log.d(SMB_TAG, "initStatus() called with: status = $status")
                if(status == SmbClient.Status.ConnectSuccess){
                    replaceFragment("")
                }
            }
        }

    }


    private fun replaceFragment(path: String) {
        Log.d(SMB_TAG, "replaceFragment() called with: path = $path")
        Thread {
            SmbClient.instance.listFiles(path, object : OnReadFileCallback {
                override fun onSuccess(list: List<FileInfo>) {

                    runOnUiThread {
                        val fragment = FileFragment(path, list)
                        val transaction = supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.container, fragment)
                        transaction.addToBackStack(null)
                        transaction.commit()
                    }
                }

                override fun onFail(message: String) {
                    Log.d(SMB_TAG, "onFail() called with: message = $message")
                }

            })
        }.start()


    }

    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'ffmpeg' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}