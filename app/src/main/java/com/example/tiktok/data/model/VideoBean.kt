package com.example.tiktok.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoBean(
    var videoId: Int = 0,
    var videoRes: String = "",          //视频播放资源
    var coverRes: Int = 0,              //封面播放资源
    var content: String? = null,        // 视频描述

    var isLiked: Boolean = false,       // 是否点赞
    var isCollected:Boolean=false,      // 是否收藏
    var distance: Float = 0f,           // 距离
    var likeCount: Int = 0,
    var commentCount: Int = 0,
    var collectCount: Int =0,
    var shareCount: Int = 0,

    var userBean: UserBean? = null
) : Parcelable


