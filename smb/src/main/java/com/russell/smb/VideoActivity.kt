package com.russell.smb

import android.media.MediaDataSource
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.hierynomus.msfscc.fileinformation.FileStandardInformation
import com.hierynomus.smbj.share.File
import com.russell.smb.Utils.Companion.SMB_TAG
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.InputStream


class VideoActivity : AppCompatActivity(),  SurfaceHolder.Callback{

    lateinit var mSurfaceView: SurfaceView
    var player: MediaPlayer? = null
    var isDownload = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        mSurfaceView= findViewById(R.id.play_view)
        val mSurfaceHolder = mSurfaceView.holder
        mSurfaceHolder?.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if(isDownload){
            player?.setDisplay(holder)
            player?.start()
        }
        player = MediaPlayer()
        player?.setDisplay(holder)
        SmbClient.instance.openFile("","demo.mp4",object :OnReadFileInputStreamCallback{
            override fun onSuccess(file: InputStream) {

                val video = VideoDataSource(file,object :VideoDownloadListener{
                    override fun onVideoDownloaded() {
                        Log.d(SMB_TAG, "onVideoDownloaded() called")
                        player?.prepareAsync()
                    }

                    override fun onVideoDownloadError(e: Exception?) {
                        Log.d(SMB_TAG, "onVideoDownloadError() called with: e = $e")
                    }

                })
                player?.setOnPreparedListener {
                    Log.d(SMB_TAG, "setOnPreparedListener onSuccess() called")
                    isDownload= true
                    player?.start()
                }
                player?.setDataSource(video)
            }

            override fun onFail(message: String) {
                Log.d(SMB_TAG, "onFail() called with: message = $message")
            }

        })

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
            player?.stop()
        player?.release()
    }


    class VideoDataSource(inputStream: InputStream, listener: VideoDownloadListener): MediaDataSource() {
        @Volatile
        lateinit var  videoBuffer:ByteArray
        init {

            Thread{
               try {
                   val byteArrayOutputStream = ByteArrayOutputStream()
                   var read =0
                   while (read != -1){
                       read = inputStream.read()
                       byteArrayOutputStream.write(read)
                   }
                   inputStream.close()
                   byteArrayOutputStream.flush()
                   videoBuffer = byteArrayOutputStream.toByteArray()
                   byteArrayOutputStream.close()
                   listener.onVideoDownloaded()
               }catch (e:Exception){
                   listener.onVideoDownloadError(e)
               }
            }.start()
        }
        override fun close() {

        }

        override fun readAt(position: Long, buffer: ByteArray?, offset: Int, size: Int): Int {
            synchronized (videoBuffer) {
                val length = videoBuffer.size
                if(position >= length){
                    return -1
                }
                var readLength = size
                if(position + size >length){
                    readLength = (length-position).toInt()
                }
                System.arraycopy(
                    videoBuffer,
                    position.toInt(), buffer, offset, readLength
                )

                return readLength
            }
        }

        override fun getSize(): Long {
            synchronized (videoBuffer) {
                return videoBuffer.size.toLong()
            }
        }

    }

    interface VideoDownloadListener {
        fun onVideoDownloaded()
        fun onVideoDownloadError(e: Exception?)
    }
}