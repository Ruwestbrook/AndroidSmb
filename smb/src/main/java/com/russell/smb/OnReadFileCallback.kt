package com.russell.smb


interface OnReadFileCallback{
    fun onSuccess(list:List<FileInfo>)
    fun onFail(message:String)
}

data class FileInfo (val FileName:String,val isDirectory:Boolean,var fileType:FileType,var fileDrawable:Int)

enum class FileType{
    TEXT,//文本文件
    VIDEO,//视频
    MUSIC,//音乐
    IMAGE,//图片
    DIRECTORY,//文件夹
    ZIP, //压缩文件
    PROPERTIES, //properties配置文件
    UNKNOWN,//未知文件
}


