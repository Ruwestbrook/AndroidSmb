package com.russell.androidsmb

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.russell.smb.FileListActivity
import com.russell.smb.SmbClient

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SmbClient.setSmbConfig(BuildConfig.ip,BuildConfig.username,BuildConfig.password,BuildConfig.fileName)
        startActivity(Intent(this, FileListActivity::class.java))
    }

}