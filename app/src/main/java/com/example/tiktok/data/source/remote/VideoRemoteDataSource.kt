package com.example.tiktok.data.source.remote

import com.example.tiktok.data.model.VideoBean
import kotlinx.coroutines.delay

class VideoRemoteDataSource {
    //从服务器获取推荐视频
    suspend fun getRecommendVideos(page: Int, pageSize: Int): Result<List<VideoBean>> {
        return try {
            // TODO: 调用实际 API
            // val response = apiService.getRecommendVideos(page, pageSize)
            // Result.success(response.data)

            // 模拟网络请求
            delay(1000)
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    //点赞视频
    suspend fun likeVideo(videoId: Int): Result<Boolean> {
        return try {
            delay(300)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //取消点赞
    suspend fun unlikeVideo(videoId: Int): Result<Boolean> {
        return try {
            delay(300)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //收藏视频
    suspend fun collectVideo(videoId: Int): Result<Boolean> {
        return try {
            delay(300)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //取消收藏
    suspend fun uncollectVideo(videoId: Int): Result<Boolean> {
        return try {
            delay(300)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //关注用户
    suspend fun followUser(userId: Int): Result<Boolean> {
        return try {
            delay(300)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}