package com.example.tiktok.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tiktok.data.model.VideoBean
import com.example.tiktok.data.repository.CommentRepository
import com.example.tiktok.data.repository.VideoRepository
import com.example.tiktok.utils.Resource
import kotlinx.coroutines.launch

class RecommendViewModel(application: Application): AndroidViewModel(application) {

    private val videoRepository = VideoRepository()
    private val commentRepository = CommentRepository(application)  // 添加评论仓库

    //刷新结果
    private val _videoList = MutableLiveData<Resource<List<VideoBean>>>()
    val videoList: LiveData<Resource<List<VideoBean>>> = _videoList

    //加载更多结果
    private val _loadMoreResult = MutableLiveData<Resource<List<VideoBean>>>()
    val loadMoreResult: LiveData<Resource<List<VideoBean>>> = _loadMoreResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    //视频缓存
    private val allVideos = mutableListOf<VideoBean>()

    // 加载推荐视频（首次加载或刷新）
    fun loadRecommendVideos(isRefresh: Boolean = false) {
        if (isRefresh) {
            allVideos.clear()
        }

        viewModelScope.launch {
            _videoList.value = Resource.Loading()

            val result = videoRepository.getRecommendVideos()  // 从数据源加载视频
            if (result.isSuccess) {
                val videos = result.getOrNull() ?: emptyList()

                // 同步评论数
                syncCommentCounts(videos)

                // 累加到缓存
                allVideos.addAll(videos)

                _videoList.value =
                    Resource.Success(allVideos.toList())         //toList()->数据安全与防御性拷贝

            } else {
                _videoList.value = Resource.Error(result.exceptionOrNull()?.message ?: "加载失败")
            }
        }
    }

    //加载更多
    fun loadMore() {
        viewModelScope.launch {
            _loadMoreResult.value = Resource.Loading()

            val result = videoRepository.getRecommendVideos()
            if (result.isSuccess) {
                val newVideos = result.getOrNull() ?: emptyList()

                if (newVideos.isNotEmpty()) {
                    // 同步评论数
                    syncCommentCounts(newVideos)

                    // 累加到缓存
                    allVideos.addAll(newVideos)

                    _loadMoreResult.value = Resource.Success(newVideos)
                } else {
                    _loadMoreResult.value = Resource.Error("没有更多数据了")
                }
            } else {
                _errorMessage.value = "加载失败"
            }
        }
    }

    // 同步视频列表的评论数
    private suspend fun syncCommentCounts(videos: List<VideoBean>) {
        // 收集所有视频的 ID
        val videoIdList = ArrayList<Int>()

        for (video in videos) {
            videoIdList.add(video.videoId)
        }

        // 批量查询评论数
        val countMap = commentRepository.getCommentCountsForVideos(videoIdList)

        // 遍历视频列表，把查到的数字填进去
        for (video in videos) {
            // 从 Map 中取出这个视频对应的数量
            val count = countMap[video.videoId]

            video.commentCount = count ?: 0
        }
    }

    //获取当前视频列表（用于跳转播放页）
    fun getCurrentVideoList(): ArrayList<VideoBean> {
        return ArrayList(allVideos)
    }
}
