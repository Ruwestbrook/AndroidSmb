package com.russell.smb

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BseActivity :AppCompatActivity() {

    var path = ""
    var name = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        path = if( intent.getStringExtra("path") == null ) "" else intent.getStringExtra("path")!!
        name = if( intent.getStringExtra("name") == null ) "" else intent.getStringExtra("name")!!
    }
}