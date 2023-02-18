package com.russell.smb


interface OnReadFileCallback{
    fun onSuccess(list:List<FileInfo>)
    fun onFail(message:String)
}

data class FileInfo (val FileName:String,val isDirectory:Boolean)

