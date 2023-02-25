package com.russell.smb

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import java.io.InputStream


//todo 优化加载速度,在外边列表就可以开始加载不需要在加载一次
class ImageActivity : BseActivity() {
    private lateinit var viewPager :ViewPager2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
        viewPager = findViewById(R.id.view_pages)

         SmbClient.instance.listFiles(path,FileType.IMAGE,object :OnReadFileCallback{
            override fun onSuccess(list: List<FileInfo>) {
                Log.d(Utils.SMB_TAG, "onSuccess() called size = "+list.size)
                var indexPage = 0
                list.forEachIndexed { index, fileInfo ->
                    run {
                        if (fileInfo.fileName == name) {
                            indexPage = index
                        }
                    }
                }
                val adapter= ImageAdapter(path,list)
                runOnUiThread{
                    viewPager.adapter = adapter
                    viewPager.setCurrentItem(indexPage,false)
                }
            }

            override fun onFail(message: String) {
                Log.d(Utils.SMB_TAG, "null() called $message");
            }
        })
    }
}

class ImageAdapter (private  val path:String,private  val list: List<FileInfo>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =LayoutInflater.from(parent.context).inflate(R.layout.item_image,parent,false)
        return object : RecyclerView.ViewHolder(view) {}
    }

    override fun getItemCount()=list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        SmbClient.instance.openFile(path,list[position].fileName,object :OnReadFileInputStreamCallback{
            override fun onSuccess(file: InputStream) {
                val image = BitmapFactory.decodeStream(file)
                holder.itemView.post {
                    val imageView = holder.itemView.findViewById<ImageView>(R.id.image)
                    imageView.setImageBitmap(image)
                }
            }

            override fun onFail(message: String) {
                Log.d(Utils.SMB_TAG, "null() called");
            }

        })
    }


}

