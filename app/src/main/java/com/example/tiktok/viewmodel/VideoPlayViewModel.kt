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

class VideoPlayViewModel(application: Application): AndroidViewModel(application) {

    private val videoRepository = VideoRepository()
    private val commentRepository = CommentRepository(application)  // 添加评论仓库

    //点赞结果
    private val _likeResult = MutableLiveData<Pair<Int, Boolean>>() // <position, isLiked>
    val likeResult: LiveData<Pair<Int, Boolean>> = _likeResult

    //收藏结果
    private val _collectResult = MutableLiveData<Pair<Int, Boolean>>() // <position, isCollected>
    val collectResult: LiveData<Pair<Int, Boolean>> = _collectResult

    //关注结果
    private val _followResult = MutableLiveData<Pair<Int, Boolean>>() // <position, isFollowed>
    val followResult: LiveData<Pair<Int, Boolean>> = _followResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    // 刷新结果
    private val _refreshResult = MutableLiveData<Resource<List<VideoBean>>>()
    val refreshResult: LiveData<Resource<List<VideoBean>>> = _refreshResult

    // 加载更多结果
    private val _loadMoreResult = MutableLiveData<Resource<List<VideoBean>>>()
    val loadMoreResult: LiveData<Resource<List<VideoBean>>> = _loadMoreResult

    // 下拉刷新
    fun refreshVideos() {
        viewModelScope.launch {
            _refreshResult.value = Resource.Loading()

            val result = videoRepository.getRecommendVideos()

            if (result.isSuccess) {
                val videos = result.getOrNull() ?: emptyList()
                syncCommentCounts(videos)
                _refreshResult.value = Resource.Success(videos)

            } else {
                _refreshResult.value = Resource.Error(result.exceptionOrNull()?.message ?: "刷新失败")
            }
        }
    }

    // 上拉加载更多
    fun loadMoreVideos() {
        viewModelScope.launch {
            _loadMoreResult.value = Resource.Loading()

            val result = videoRepository.getRecommendVideos()

            if (result.isSuccess) {
                val videos = result.getOrNull() ?: emptyList()
                if (videos.isNotEmpty()) {
                    syncCommentCounts(videos)
                    _loadMoreResult.value = Resource.Success(videos)
                } else {
                    _loadMoreResult.value = Resource.Error("没有更多数据了")
                }
            } else {
                _loadMoreResult.value = Resource.Error(result.exceptionOrNull()?.message ?: "加载失败")
            }
        }
    }

    // 同步视频列表的评论数
    private suspend fun syncCommentCounts(videos: List<VideoBean>) {
        try {
            //把视频列表里的每个视频对象，转换成只有 ID 的列表
            val videoIds = videos.map { it.videoId }
            val commentCounts = commentRepository.getCommentCountsForVideos(videoIds)

            videos.forEach { video ->
                video.commentCount = commentCounts[video.videoId] ?: 0
            }
        } catch (e: Exception) {
            android.util.Log.e("VideoPlayViewModel", "同步评论数失败: ${e.message}")
        }
    }

    //点赞/取消点赞
    fun toggleLike(video: VideoBean, position: Int) {
        viewModelScope.launch {
            val result = videoRepository.toggleLike()
            if (result.isSuccess) {
                video.isLiked = !video.isLiked
                if (video.isLiked) {
                    video.likeCount++
                } else {
                    video.likeCount--
                }
                _likeResult.value = Pair(position, video.isLiked)
            } else {
                _errorMessage.value = "操作失败"
            }
        }
    }

    //收藏/取消收藏
    fun toggleCollect(video: VideoBean, position: Int) {
        viewModelScope.launch {
            val result = videoRepository.toggleCollect()

            if (result.isSuccess) {
                video.isCollected = !video.isCollected

                if (video.isCollected) {
                    video.collectCount++
                } else {
                    video.collectCount--
                }

                _collectResult.value = Pair(position, video.isCollected)
                _successMessage.value = if (video.isCollected) "已收藏" else "取消收藏"
            } else {
                _errorMessage.value = "操作失败"
            }
        }
    }

    //关注用户
    fun followUser(video: VideoBean, position: Int) {
        viewModelScope.launch {
            val result = videoRepository.followUser()
            if (result.isSuccess) {
                video.userBean?.isFollowed = true
                _followResult.value = Pair(position, true)
                _successMessage.value = "已关注 ${video.userBean?.nickName}"
            } else {
                _errorMessage.value = "关注失败"
            }
        }
    }

}