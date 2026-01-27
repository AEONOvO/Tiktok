package com.example.tiktok.data.repository

import com.example.tiktok.data.model.VideoBean
import com.example.tiktok.data.source.local.VideoLocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VideoRepository(
    private val localDataSource: VideoLocalDataSource = VideoLocalDataSource()
) {


     //获取推荐视频列表
    suspend fun getRecommendVideos(): Result<List<VideoBean>> {
        return withContext(Dispatchers.IO) {
          Result.success(localDataSource.getRecommendVideos())
        }
    }


    //点赞/取消点赞
    suspend fun toggleLike(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                // 更新本地状态
                localDataSource.updateLikeStatus()
                Result.success(true) // 返回成功状态
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    //收藏/取消收藏
    suspend fun toggleCollect(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                // 更新本地状态
                localDataSource.updateCollectStatus()
                Result.success(true) // 返回成功状态
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    //关注
    suspend fun followUser(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                // 更新本地状态
                localDataSource.updateFollowStatus()
                Result.success(true) // 返回成功状态
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
