package com.example.tiktok.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiktok.data.model.VideoBean
import com.example.tiktok.data.repository.VideoRepository
import com.example.tiktok.utils.Resource
import kotlinx.coroutines.launch

class VideoPlayViewModel(private val repository: VideoRepository=VideoRepository()):ViewModel() {


    private val _likeResult = MutableLiveData<Pair<Int, Boolean>>() // <position, isLiked>
    val likeResult: LiveData<Pair<Int, Boolean>> = _likeResult

    private val _collectResult = MutableLiveData<Pair<Int, Boolean>>() // <position, isCollected>
    val collectResult: LiveData<Pair<Int, Boolean>> = _collectResult

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

    private var currentPage = 1
    private val pageSize = 20

    // 下拉刷新
    fun refreshVideos() {
        viewModelScope.launch {
            _refreshResult.value = Resource.Loading()

            currentPage = 1
            val result = repository.getRecommendVideos(currentPage, pageSize)

            if (result.isSuccess) {
                val videos = result.getOrNull() ?: emptyList()
                _refreshResult.value = Resource.Success(videos)
                currentPage++
            } else {
                _refreshResult.value = Resource.Error(result.exceptionOrNull()?.message ?: "刷新失败")
            }
        }
    }

    // 上拉加载更多
    fun loadMoreVideos() {
        viewModelScope.launch {
            _loadMoreResult.value = Resource.Loading()

            val result = repository.getRecommendVideos(currentPage, pageSize)

            if (result.isSuccess) {
                val videos = result.getOrNull() ?: emptyList()
                if (videos.isNotEmpty()) {
                    _loadMoreResult.value = Resource.Success(videos)
                    currentPage++
                } else {
                    _loadMoreResult.value = Resource.Error("没有更多数据了")
                }
            } else {
                _loadMoreResult.value = Resource.Error(result.exceptionOrNull()?.message ?: "加载失败")
            }
        }
    }

    //点赞/取消点赞
    fun toggleLike(video: VideoBean, position: Int) {
        viewModelScope.launch {
            val result = repository.toggleLike(video)
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
            val result = repository.toggleCollect(video)

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
            val userId = video.userBean?.userId ?: return@launch
            val result = repository.followUser(userId)
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