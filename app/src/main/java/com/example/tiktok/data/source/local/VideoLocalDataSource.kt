package com.example.tiktok.data.source.local

import com.example.tiktok.data.model.VideoBean
import com.example.tiktok.utils.DataCreate

class VideoLocalDataSource {

    //获取本地推荐视频
    fun getRecommendVideos():List<VideoBean>{
        return DataCreate.datas
    }

    //更新点赞状态
    fun updateLikeStatus(): Boolean {
        return true
    }

    //更新收藏状态
    fun updateCollectStatus(): Boolean {
        return true
    }

    //更新关注状态
    fun updateFollowStatus(): Boolean {
        return true
    }
}