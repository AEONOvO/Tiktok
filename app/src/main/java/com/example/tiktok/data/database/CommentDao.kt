package com.example.tiktok.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {

    // 插入单条评论
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: CommentEntity): Long

    // 插入多条评论
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(comments: List<CommentEntity>)

    // 查询指定视频的所有评论（按时间倒序）
    @Query("SELECT * FROM comments WHERE videoId = :videoId ORDER BY createTime DESC")
    suspend fun getCommentsByVideoId(videoId: Int): List<CommentEntity>

    // 查询指定视频的评论数量
    @Query("SELECT COUNT(*) FROM comments WHERE videoId = :videoId")
    suspend fun getCommentCount(videoId: Int): Int

    // 更新评论点赞状态
    @Query("UPDATE comments SET isLiked = :isLiked, likeCount = :likeCount WHERE commentId = :commentId")
    suspend fun updateLikeStatus(commentId: Int, isLiked: Boolean, likeCount: Int)

    // 删除指定评论
    @Delete
    suspend fun delete(comment: CommentEntity)

    // 删除指定视频的所有评论
    @Query("DELETE FROM comments WHERE videoId = :videoId")
    suspend fun deleteByVideoId(videoId: Int)

    // 清空所有评论
    @Query("DELETE FROM comments")
    suspend fun deleteAll()

    // 实时监听评论变化（Flow）
    @Query("SELECT * FROM comments WHERE videoId = :videoId ORDER BY createTime DESC")
    fun observeComments(videoId: Int): Flow<List<CommentEntity>>
}