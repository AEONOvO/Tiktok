package com.example.tiktok.data.repository

import com.example.tiktok.R
import com.example.tiktok.data.model.CommentBean
import com.example.tiktok.data.model.UserBean
import kotlinx.coroutines.delay

class CommentRepository {

    suspend fun getCommentList(videoId: Int): Result<List<CommentBean>> {
    return try {
        delay(500)

        // 使用 UserBean 构造评论数据
        val comments = listOf(
            CommentBean(
                commentId = 1,
                videoId = videoId,
                userBean = UserBean(
                    userId = 1001,
                    nickName = "用户12138",
                    headId = R.mipmap.head_one,
                    sign = "这个人很懒，什么都没留下"
                ),
                content = "这个也太好看了吧",
                likeCount = 89,
                isLiked = false
            ),
            CommentBean(
                commentId = 2,
                videoId = videoId,
                userBean = UserBean(
                    userId = 1002,
                    nickName = "小红",
                    headId = R.mipmap.head_two,
                    sign = "热爱生活"
                ),
                content = "太有趣了哈哈哈",
                likeCount = 156,
                isLiked = false
            ),
            CommentBean(
                commentId = 3,
                videoId = videoId,
                userBean = UserBean(
                    userId = 1003,
                    nickName = "阿明",
                    headId = R.mipmap.head_three
                ),
                content = "学到了学到了",
                likeCount = 23,
                isLiked = false
            ),
            CommentBean(
                commentId = 4,
                videoId = videoId,
                userBean = UserBean(
                    userId = 1004,
                    nickName = "小美",
                    headId = R.mipmap.head_four
                ),
                content = "已经循环看了10遍了",
                likeCount = 345,
                isLiked = false
            ),
            CommentBean(
                commentId = 5,
                videoId = videoId,
                userBean = UserBean(
                    userId = 1005,
                    nickName = "老王",
                    headId = R.mipmap.head_five
                ),
                content = "这个bgm是什么",
                likeCount = 12,
                isLiked = false
            )
        )

        Result.success(comments)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

    // 模拟发布评论
    suspend fun publishComment(videoId: Int, content: String): Result<CommentBean> {
        return try {
            delay(800)

            // 模拟当前用户信息
            val currentUser = UserBean(
                userId = 9999,
                nickName = "我",
                headId = R.mipmap.default_avatar,
                sign = "我好看"
            )

            val newComment = CommentBean(
                commentId = System.currentTimeMillis().toInt(),  // 使用时间戳作为 ID
                videoId = videoId,
                userBean = currentUser,
                content = content,
                likeCount = 0,
                isLiked = false,
                createTime = System.currentTimeMillis()
            )

            Result.success(newComment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 模拟点赞评论
    suspend fun toggleCommentLike(comment: CommentBean): Result<Boolean> {
        return try {
            delay(200)
            Result.success(!comment.isLiked)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}