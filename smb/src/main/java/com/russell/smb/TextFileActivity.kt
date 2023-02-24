package com.russell.smb

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.TextView

class TextFileActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text)
        Log.d(Utils.SMB_TAG, "TextActivity onCreate:")
        val path = if( intent.getStringExtra("path") == null ) "" else intent.getStringExtra("path")!!
        val name = intent.getStringExtra("name")!!
        Log.d(Utils.SMB_TAG, "onCreate: $path")
        Log.d(Utils.SMB_TAG, "onCreate: $name")
        SmbClient.instance.openTextFile(path,name,object :OnReadTextFileCallback{
            override fun onSuccess(file: String) {
                runOnUiThread{
                    val textView = findViewById<TextView>(R.id.text)
                    textView.text= file
                }
            }

            override fun onFail(message: String) {
            }

        })

    }


    companion object {

        fun startTextActivity(
            context: Context,
            path: String,
            name: String,
        ) {
            val intent = Intent(context, TextFileActivity::class.java)
            intent.putExtra("path", path)
            intent.putExtra("name", name)
            Log.d(Utils.SMB_TAG, "startTextActivity: $path")
            Log.d(Utils.SMB_TAG, "startTextActivity: $name")
            context.startActivity(intent)
        }
    }

}