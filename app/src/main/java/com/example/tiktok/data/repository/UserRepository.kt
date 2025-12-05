package com.example.tiktok.data.repository

import android.content.Context
import android.net.Uri
import com.example.tiktok.data.model.UserInfo
import com.example.tiktok.utils.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class UserRepository(private val context: Context) {

    /**
     * 获取用户信息
     */
    suspend fun getUserInfo(userId: String): Result<UserInfo> {
        return withContext(Dispatchers.IO) {
            try {
                // TODO: 替换为实际的网络请求
                delay(500) // 模拟网络延迟

                // 模拟数据
                val userInfo = UserInfo(
                    userId = "123456",
                    nickname = "AEON",
                    douyinId = "AEONOVO",
                    avatarUrl = "",             // 实际应该是服务器返回的 URL
                    backgroundUrl = "",
                    signature = "CQUPT!!!!加油",
                    age = 24,
                    location = "重庆",
                    likesCount = 125,
                    followingCount = 66,
                    fansCount = 66,
                    isFollowing = false
                )

                Result.success(userInfo)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 上传头像
     */
    suspend fun uploadAvatar(uri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // 压缩图片
                val compressedFile = ImageUtils.compressImage(context, uri, maxSize = 512)
                    ?: return@withContext Result.failure(Exception("图片压缩失败"))

                // TODO: 使用 Retrofit 上传到服务器
                delay(1000) // 模拟上传延迟

                // 模拟返回的头像 URL
                val avatarUrl = uri.toString() // 实际应该是服务器返回的 URL

                Result.success(avatarUrl)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 更新用户信息
     */
    suspend fun updateUserInfo(userInfo: UserInfo): Result<UserInfo> {
        return withContext(Dispatchers.IO) {
            try {
                // TODO: 发送到服务器
                delay(500)

                Result.success(userInfo)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}