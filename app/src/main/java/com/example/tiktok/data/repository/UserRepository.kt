package com.example.tiktok.data.repository

import android.content.Context
import android.net.Uri
import com.example.tiktok.R
import com.example.tiktok.data.model.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class UserRepository(private val context: Context) {

    //获取用户信息
    suspend fun getUserInfo(userId: String): Result<UserInfo> {
        return withContext(Dispatchers.IO) {
            try {
                // 模拟数据
                val userInfo = UserInfo(
                    userId = "123456",
                    nickname = "AEON",
                    douyinId = "1934335658",
                    avatarUrl = "android.resource://com.example.tiktok/${R.drawable.user_info_avatar}",
                    backgroundUrl = "",
                    signature = "看我干嘛",
                    age = 24,
                    location = "重庆",
                    likesCount = 520,
                    followingCount = 13,
                    fansCount = 14,
                    isFollowing = false
                )

                Result.success(userInfo)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    //上传头像
    suspend fun uploadAvatar(uri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // 模拟返回的头像 URL
                val avatarUrl = uri.toString()

                Result.success(avatarUrl)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}