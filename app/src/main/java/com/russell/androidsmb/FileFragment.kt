package com.russell.androidsmb

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.russell.smb.FileInfo

class FileFragment(private val path:String,private val list: List<FileInfo>) : Fragment() {
      private lateinit var mRecyclerView :RecyclerView

      companion object{

       var pageClick:PageClick? =null

      }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view= inflater.inflate(R.layout.file_list_page,container,false)
        mRecyclerView = view.findViewById(R.id.file_list)
        mRecyclerView.layoutManager = GridLayoutManager(context,3)
        mRecyclerView.adapter = FileAdapter(list,object:FileClick{
            override fun click(file: FileInfo) {
                    pageClick?.click(path,file)
            }
        } )
        return view
    }


}

class  FileAdapter(private val list: List<FileInfo>, private val clickListener:FileClick): RecyclerView.Adapter<FileViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = FileViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_file,parent,false))

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
            holder.itemView.setOnClickListener{
                clickListener.click(list[position])
            }
            holder.setData(list[position])
    }

}


class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var mFileImage:ImageView
    private var mFileName:TextView
    fun setData(fileInfo :FileInfo){
        mFileImage.setImageDrawable(ContextCompat.getDrawable(itemView.context,
        if (fileInfo.isDirectory){
            R.drawable.directory
        }else{
            R.drawable.text_file
        }
            ))

        mFileName.text = fileInfo.FileName
    }

    init {
        mFileImage = itemView.findViewById(R.id.file_image)
        mFileName = itemView.findViewById(R.id.file_name)
    }


}

interface FileClick{

    fun click(file:FileInfo)

}

interface PageClick{

    fun click(path:String,file:FileInfo)

}
