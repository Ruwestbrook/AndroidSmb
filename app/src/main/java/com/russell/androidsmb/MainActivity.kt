package com.russell.androidsmb

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.russell.smb.FileInfo
import com.russell.smb.OnReadFileCallback
import com.russell.smb.SmbClient


class MainActivity : AppCompatActivity() {
    private  val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate: "+BuildConfig.BUILD_TYPE)
        val smbClient = SmbClient.Builder().setSmbConfig(BuildConfig.ip,BuildConfig.username,BuildConfig.password,BuildConfig.fileName).build()
        smbClient.listFiles("",object : OnReadFileCallback {
            override fun onSuccess(list: List<FileInfo>) {
               list.forEach {
                   Log.d(TAG, "onSuccess: ${it.FileName},${it.isDirectory}")

//                   if(it.isDirectory){
//
//                       smbClient.openDirectory("${it.FileName}/",object :OnReadFileCallback{
//                           override fun onSuccess(list: List<FileInfo>) {
//                               Log.d(TAG, "onSuccess: -------------------------------------------------")
//
//                               list.forEach {
//                                   Log.d(TAG, "onSuccess: ${it.FileName},${it.isDirectory}")
//                               }
//
//                           }
//
//                           override fun onFail(message: String) {
//
//                           }
//
//                       })
//                   }
               }



            }

            override fun onFail(message: String) {
                Log.d(TAG, "onFail() called with: message = $message")
            }

        })
    }

}