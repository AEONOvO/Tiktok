package com.example.tiktok.data.repository

import android.content.Context
import com.example.tiktok.R
import com.example.tiktok.data.database.AppDatabase
import com.example.tiktok.data.database.CommentDao
import com.example.tiktok.data.database.CommentEntity
import com.example.tiktok.data.model.CommentBean
import com.example.tiktok.data.model.UserBean
import com.google.gson.Gson

class CommentRepository(context: Context) {

    private val commentDao: CommentDao = AppDatabase.getDatabase(context).commentDao()
    private val gson = Gson()

    // 获取评论列表（优先从数据库加载）
    suspend fun getCommentList(videoId: Int): Result<List<CommentBean>> {
        return try {
            // 从数据库读取
            val localComments = commentDao.getCommentsByVideoId(videoId)

            if (localComments.isNotEmpty()) {
                // 数据库有数据，直接返回
                val commentBeans = localComments.map { it.toCommentBean() }
                return Result.success(commentBeans)
            }

            // 数据库为空，为该视频生成专属模拟数据
            val mockComments = createMockCommentsForVideo(videoId)

            // 保存到数据库
            val entities = mockComments.map { it.toEntity() }
            commentDao.insertAll(entities)

            Result.success(mockComments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 发布评论（保存到数据库）
    suspend fun publishComment(videoId: Int, content: String): Result<CommentBean> {
        return try {
            // 创建评论对象
            val currentUser = UserBean(
                userId = 9999,
                nickName = "我",
                headId = R.mipmap.default_avatar,
                sign = "我好看"
            )

            val newComment = CommentBean(
                commentId = System.currentTimeMillis().toInt(),     //获取当前系统时间并强制转成 Int 类型
                videoId = videoId,
                userBean = currentUser,
                content = content,
                likeCount = 0,
                isLiked = false,
                createTime = System.currentTimeMillis()
            )

            // 保存到数据库
            commentDao.insert(newComment.toEntity())

            Result.success(newComment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 切换点赞状态（同步到数据库）
    suspend fun toggleCommentLike(comment: CommentBean): Result<Boolean> {
        return try {
            val newLikedState = !comment.isLiked
            val newLikeCount = if (newLikedState) {
                comment.likeCount + 1
            } else {
                comment.likeCount - 1
            }

            // 更新数据库
            commentDao.updateLikeStatus(
                commentId = comment.commentId,
                isLiked = newLikedState,
                likeCount = newLikeCount
            )

            Result.success(newLikedState)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 获取评论数量
    private suspend fun getCommentCount(videoId: Int): Int {
        return commentDao.getCommentCount(videoId)
    }

    // 批量获取多个视频的评论数
    suspend fun getCommentCountsForVideos(videoIds: List<Int>): Map<Int, Int> {
        val result = mutableMapOf<Int, Int>()

        videoIds.forEach { videoId ->
            val count = getCommentCount(videoId)
            result[videoId] = count
        }

        return result

    }

    // CommentBean → CommentEntity
    private fun CommentBean.toEntity(): CommentEntity {
        return CommentEntity(
            commentId = this.commentId,
            videoId = this.videoId,
            userJson = gson.toJson(this.userBean),
            content = this.content,
            likeCount = this.likeCount,
            isLiked = this.isLiked,
            createTime = this.createTime
        )
    }

    // CommentEntity → CommentBean
    private fun CommentEntity.toCommentBean(): CommentBean {
        return CommentBean(
            commentId = this.commentId,
            videoId = this.videoId,
            userBean = gson.fromJson(this.userJson, UserBean::class.java),
            content = this.content,
            likeCount = this.likeCount,
            isLiked = this.isLiked,
            createTime = this.createTime
        )
    }

    // 为每个视频生成专属的模拟评论
    private fun createMockCommentsForVideo(videoId: Int): List<CommentBean> {
        // 模拟用户池
        val userPool = listOf(
            UserBean(
                userId = 1001,
                nickName = "用户12138",
                headId = R.mipmap.head_one,
                sign = "这个人很懒，什么都没留下"
            ),
            UserBean(
                userId = 1002,
                nickName = "小红",
                headId = R.mipmap.head_two,
                sign = "热爱生活"
            ),
            UserBean(
                userId = 1003,
                nickName = "阿明",
                headId = R.mipmap.head_three
            ),
            UserBean(
                userId = 1004,
                nickName = "小美",
                headId = R.mipmap.head_four
            ),
            UserBean(
                userId = 1005,
                nickName = "老王",
                headId = R.mipmap.head_five
            ),
            UserBean(
                userId = 1006,
                nickName = "张三",
                headId = R.mipmap.head_one
            ),
            UserBean(
                userId = 1007,
                nickName = "李四",
                headId = R.mipmap.head_two
            )
        )

        // 评论内容池
        val commentPool = listOf(
            "这个也太好看了吧",
            "太有趣了哈哈哈",
            "学到了学到了",
            "已经循环看了10遍了",
            "这个bgm是什么",
            "笑死我了",
            "拍得真好",
            "这也太真实了",
            "厉害厉害",
            "有内味了",
            "666666",
            "太棒了",
            "好喜欢这个视频",
            "每天都来看一遍",
            "转发给朋友了",
            "好看爱看",
            "真是个天才！",
            "这个视频让我笑到肚子痛",
            "绝对是精品，值得一看！",
            "每次看到都想点赞！",
            "太有创意了！",
            "看一次就中毒了！",
            "超喜欢这个风格！",
            "这是什么神仙操作？",
            "对这种内容太上头了！",
            "来看看新番，果然不错！",
            "这背景音乐真好听！",
            "感觉被治愈了，感谢分享！",
            "这位小伙伴太有才了！",
            "忍不住想分享给更多人！",
            "看完真是心情大好！",
            "这画面也太美了吧！",
            "哪儿找的这么有趣的内容！",
            "又被圈粉了，爱了爱了！",
            "这一段好有意思！",
            "如果有无限循环就好了！"
        )

        // 根据 videoId 生成不同数量和内容的评论
        val random = java.util.Random(videoId.toLong())  // 使用 videoId 作为随机种子，确保相同视频总是生成相同评论
        val commentCount = random.nextInt(100) + 3  // 每个视频 3条以上评论

        return (1..commentCount).map { index ->
            val user = userPool[random.nextInt(userPool.size)]
            val content = commentPool[random.nextInt(commentPool.size)]
            val likeCount = random.nextInt(500)

            CommentBean(
                commentId = videoId * 1000 + index,  // 确保每个视频的评论 ID 不重复
                videoId = videoId,
                userBean = user.copy(),  // 复制用户对象，避免共享引用
                content = content,
                likeCount = likeCount,
                isLiked = false,
                createTime = System.currentTimeMillis() - (commentCount - index) * 3600000L  // 按时间倒序
            )
        }
    }
}