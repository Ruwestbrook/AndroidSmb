package com.russell.smb

import java.util.*

class Utils {

    companion object{

        private val videoTypeList = mutableListOf("mp4","mkv","wmv")
        private val musicTypeList = mutableListOf("mp3","m4a","wav","flac")
        private val zipTypeList = mutableListOf("rar","zip","tgz")
        private val textTypeList = mutableListOf("txt")
        private val settingTypeList = mutableListOf("properties")
        private val imageTypeList = mutableListOf("jpeg","bmp,","jpg","png","tif","gif","pcx","tga","exif","fpx","svg","psd","cdr","pcd","dxf","ufo","eps","ai","raw","WMF","webp","avif","apng")

        fun getFileType(fileName:String,isDirectory:Boolean,fileInfo: FileInfo){
            if(isDirectory){
                fileInfo.fileType= FileType.DIRECTORY
                fileInfo.fileDrawable =R.drawable.directory
                return
            }
            val splitTmp = fileName.split(".")
            val  type = splitTmp[splitTmp.size-1]
            if(videoTypeList.contains(type.lowercase(Locale.ROOT))){
                fileInfo.fileType= FileType.VIDEO
                fileInfo.fileDrawable =R.drawable.video
            }else if(musicTypeList.contains(type.lowercase(Locale.ROOT))){
                fileInfo.fileType= FileType.MUSIC
                fileInfo.fileDrawable =R.drawable.music
            }else if(zipTypeList.contains(type.lowercase(Locale.ROOT))){
                fileInfo.fileType= FileType.ZIP
                fileInfo.fileDrawable =R.drawable.zip
            }else if(textTypeList.contains(type.lowercase(Locale.ROOT))){
                fileInfo.fileType= FileType.TEXT
                fileInfo.fileDrawable =R.drawable.text_file
            }else if(settingTypeList.contains(type.lowercase(Locale.ROOT))){
                fileInfo.fileType= FileType.PROPERTIES
                fileInfo.fileDrawable =R.drawable.setting
            }else if(imageTypeList.contains(type.lowercase(Locale.ROOT))){
                fileInfo.fileType= FileType.IMAGE
                fileInfo.fileDrawable =R.drawable.image
            }else{
                fileInfo.fileType= FileType.UNKNOWN
                fileInfo.fileDrawable =R.drawable.unknown_file
            }

        }
    }
}