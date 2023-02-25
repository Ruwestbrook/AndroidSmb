package com.russell.smb


import android.os.Bundle
import android.widget.TextView

class TextActivity : BseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text)
        val textView = findViewById<TextView>(R.id.text)
        textView.text= SmbClient.instance.openTextFileAsync(path,name)
    }

}