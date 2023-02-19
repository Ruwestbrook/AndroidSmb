package com.russell.androidsmb

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.russell.smb.FileInfo
import com.russell.smb.OnReadFileCallback
import com.russell.smb.SmbClient


class MainActivity : AppCompatActivity() {
    private  val TAG = "MainActivity"
    private lateinit var smbClient:SmbClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate: "+BuildConfig.BUILD_TYPE)
        FileFragment.pageClick = object :PageClick{
            override fun click(path: String, file: FileInfo) {
                replaceFragment(path+"/"+file.FileName)
            }

        }
        smbClient = SmbClient.Builder().setSmbConfig(BuildConfig.ip,BuildConfig.username,BuildConfig.password,BuildConfig.fileName).setClientStatus(object :SmbClient.ClientStatus{
            override fun initStatus(status: SmbClient.Status) {
                Log.d(TAG, "initStatus() called with: status = $status")

            }

            override fun log(message: String) {
                Log.d(TAG, "log() called with: message = $message")
            }
        }).build()
        replaceFragment("")

    }


    private fun replaceFragment(path:String){
        Log.d(TAG, "replaceFragment() called with: path = $path")
        Thread{
            smbClient.listFiles(path,object : OnReadFileCallback {
                override fun onSuccess(list: List<FileInfo>) {

                   runOnUiThread {
                       val fragment =FileFragment(path,list)
                       val transaction = supportFragmentManager.beginTransaction()
                       transaction.replace(R.id.container,fragment)
                       transaction.commit()
                   }
                }

                override fun onFail(message: String) {
                    Log.d(TAG, "onFail() called with: message = $message")
                }

            })
        }.start()


    }
}