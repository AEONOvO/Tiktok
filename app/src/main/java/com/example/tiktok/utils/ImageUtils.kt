package com.example.tiktok.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ImageUtils {

    /**
     * 创建临时图片文件
     */
    fun createTempImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.cacheDir
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    /**
     * 压缩图片
     * @param maxSize 最大文件大小（KB）
     */
    fun compressImage(context: Context, uri: Uri, maxSize: Int = 1024): File? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            val compressedFile = createTempImageFile(context)
            var quality = 100

            do {
                val outputStream = FileOutputStream(compressedFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                outputStream.close()
                quality -= 10
            } while (compressedFile.length() / 1024 > maxSize && quality > 10)

            bitmap.recycle()
            return compressedFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}