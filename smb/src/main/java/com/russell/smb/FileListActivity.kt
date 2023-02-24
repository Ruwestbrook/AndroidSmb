package com.russell.smb

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.russell.smb.Utils.Companion.SMB_TAG

class FileListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_list)
        FileFragment.pageClick = object : PageClick {
            override fun click(path: String, file: FileInfo) {
                if(file.isDirectory){
                    replaceFragment(path + "/" + file.fileName)
                }else if(file.fileType == FileType.TEXT || file.fileType == FileType.PROPERTIES){
                    TextFileActivity.startTextActivity(this@FileListActivity,path,file.fileName)
                }

            }

        }

        SmbClient.instance.clientStatus= object : SmbClient.ClientStatus {
            override fun initStatus(status: SmbClient.Status) {
                Log.d(SMB_TAG, "initStatus() called with: status = $status")
                replaceFragment("")
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
                    Log.d(Utils.SMB_TAG, "onFail() called with: message = $message")
                }

            })
        }.start()


    }




    companion object {
        fun startActivity(
            context: Context,
            ip: String,
            username: String,
            password: String,
            fileName: String
        ) {
            val intent = Intent(context, FileListActivity::class.java)
            intent.putExtra("ip", ip)
            intent.putExtra("username", username)
            intent.putExtra("password", password)
            intent.putExtra("fileName", fileName)
            context.startActivity(intent)
        }
    }
}