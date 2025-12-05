package com.example.tiktok.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.tiktok.data.repository.VideoRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tiktok.data.model.VideoBean
import com.example.tiktok.utils.Resource
import kotlinx.coroutines.launch
class WorkViewModel (private val repository: VideoRepository= VideoRepository()):ViewModel(){

    private val _videoList = MutableLiveData<Resource<List<VideoBean>>>()
    val videoList: LiveData<Resource<List<VideoBean>>> = _videoList

    private val _loadMoreResult = MutableLiveData<Resource<List<VideoBean>>>()
    val loadMoreResult: LiveData<Resource<List<VideoBean>>> = _loadMoreResult

    private val _likeResult = MutableLiveData<Pair<Int, Boolean>>() // <position, isLiked>
    val likeResult: LiveData<Pair<Int, Boolean>> = _likeResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage


    private var currentPage = 1
    private val pageSize = 20
    private val allVideos = mutableListOf<VideoBean>()

    //加载推荐视频（首次加载或刷新）
    fun loadRecommendVideos(isRefresh:Boolean=false){
        if(isRefresh){
            currentPage=1
            allVideos.clear()
        }

        viewModelScope.launch {
            _videoList.value=Resource.Loading()

            val result=repository.getRecommendVideos(currentPage,pageSize)
            if(result.isSuccess){
                val videos=result.getOrNull()?: emptyList()
                allVideos.addAll(videos)
                _videoList.value=Resource.Success(allVideos.toList())
                currentPage++

            }else{
                _videoList.value=Resource.Error(result.exceptionOrNull()?.message?:"加载失败")
            }
        }
    }

    //点赞/取消点赞
    fun toggleLike(video:VideoBean,position: Int){
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



    //获取当前视频列表（用于跳转播放页）
    fun getCurrentVideoList(): ArrayList<VideoBean> {
        return ArrayList(allVideos)
    }
}