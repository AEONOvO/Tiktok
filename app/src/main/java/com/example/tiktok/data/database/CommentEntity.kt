package com.example.tiktok.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters


@Entity(tableName = "comments")
@TypeConverters(UserBeanConverter::class)  // 用于转换 UserBean
data class CommentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                        // 本地数据库 ID（自增）
    val commentId: Int,                     // 评论 ID（业务 ID）
    val videoId: Int,                       // 视频 ID
    val userJson: String,                   // 用户信息（JSON 字符串）
    val content: String,                    // 评论内容
    val likeCount: Int = 0,                 // 点赞数
    val isLiked: Boolean = false,           // 是否已点赞
    val createTime: Long = System.currentTimeMillis()  // 创建时间
)