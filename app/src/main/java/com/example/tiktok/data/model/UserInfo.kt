package com.example.tiktok.data.model

data class UserInfo(
    val userId: String = "",
    val nickname: String = "",
    val douyinId: String = "",
    val avatarUrl: String = "",
    val backgroundUrl: String = "",
    val signature: String = "",
    val age: Int = 0,
    val location: String = "",
    val likesCount: Int = 0,
    val followingCount: Int = 0,
    val fansCount: Int = 0,
    val isFollowing: Boolean = false
)