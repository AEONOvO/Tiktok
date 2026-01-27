package com.example.tiktok.utils

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object ImageUtils {

    //创建临时图片文件
    fun createTempImageFile(context: Context): File {
        //获取当前时间戳
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        //创建文件名
        val imageFileName = "JPEG_${timeStamp}_"
        //选定存放地点
        val storageDir = context.cacheDir

        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }
}