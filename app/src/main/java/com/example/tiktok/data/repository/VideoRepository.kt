package com.example.tiktok.data.repository

import com.example.tiktok.data.model.VideoBean
import com.example.tiktok.data.source.local.VideoLocalDataSource
import com.example.tiktok.data.source.remote.VideoRemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VideoRepository(
    private val remoteDataSource: VideoRemoteDataSource = VideoRemoteDataSource(),
    private val localDataSource: VideoLocalDataSource = VideoLocalDataSource()
) {

    /**
     * 获取推荐视频列表
     */
    suspend fun getRecommendVideos(page: Int, pageSize: Int): Result<List<VideoBean>> {
        return withContext(Dispatchers.IO) {
            try {
                // 优先从网络获取
                val remoteResult = remoteDataSource.getRecommendVideos(page, pageSize)
                if (remoteResult.isSuccess && !remoteResult.getOrNull().isNullOrEmpty()) {
                    remoteResult
                } else {
                    // 网络失败或数据为空，从本地获取
                    Result.success(localDataSource.getRecommendVideos(page, pageSize))
                }
            } catch (e: Exception) {
                // 异常情况，返回本地数据
                Result.success(localDataSource.getRecommendVideos(page, pageSize))
            }
        }
    }

    /**
     * 点赞/取消点赞
     */
    suspend fun toggleLike(video: VideoBean): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val result = if (video.isLiked) {
                    remoteDataSource.unlikeVideo(video.videoId)
                } else {
                    remoteDataSource.likeVideo(video.videoId)
                }

                if (result.isSuccess) {
                    // 更新本地状态
                    localDataSource.updateLikeStatus(video.videoId, !video.isLiked)
                }
                result
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 收藏/取消收藏
     */
    suspend fun toggleCollect(video: VideoBean): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val result = if (video.isCollected) {
                    remoteDataSource.uncollectVideo(video.videoId)
                } else {
                    remoteDataSource.collectVideo(video.videoId)
                }

                if (result.isSuccess) {
                    localDataSource.updateCollectStatus(video.videoId, !video.isCollected)
                }
                result
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 关注用户
     */
    suspend fun followUser(userId: Int): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val result = remoteDataSource.followUser(userId)
                if (result.isSuccess) {
                    localDataSource.updateFollowStatus(userId, true)
                }
                result
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}