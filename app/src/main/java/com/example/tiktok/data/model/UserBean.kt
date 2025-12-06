package com.example.tiktok.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserBean(
    var userId: Int = 0,
    var nickName: String? = null,       // 昵称
    var headId: Int = 0,                // 头像ID
    var sign: String? = null,           // 个性签名
    var isFollowed: Boolean = false,    // 是否关注
    var subCount: Int = 0,              // 获赞数量
    var focusCount: Int = 0,            // 关注数量
    var fansCount: Int = 0,             // 粉丝数
    var workCount: Int = 0,             // 作品数
    var dynamicCount: Int = 0,          // 动态数
    var likeCount: Int = 0              // 点赞数
) : Parcelable