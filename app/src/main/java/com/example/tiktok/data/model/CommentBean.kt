package com.example.tiktok.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CommentBean(
    val commentId: Int,                  // 评论 ID
    val videoId: Int,                    // 视频 ID
    val userBean: UserBean,              // 用户信息
    val content: String,                 // 评论内容
    var likeCount: Int = 0,              // 点赞数
    var isLiked: Boolean = false,        // 是否已点赞
    val createTime: Long = System.currentTimeMillis()  // 创建时间
) : Parcelable