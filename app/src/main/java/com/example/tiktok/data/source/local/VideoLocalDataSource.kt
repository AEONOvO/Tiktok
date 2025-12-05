package com.example.tiktok.data.source.local

import com.example.tiktok.data.model.VideoBean
import com.example.tiktok.utils.DataCreate

class VideoLocalDataSource {

    //获取本地推荐视频
    fun getRecommendVideos(page:Int,pageSize:Int):List<VideoBean>{
        return DataCreate.datas.take(pageSize)
    }

    //更新点赞状态
    fun updateLikeStatus(videoId: Int, isLiked: Boolean): Boolean {
        return true
    }

    //更新收藏状态
    fun updateCollectStatus(videoId: Int, isCollected: Boolean): Boolean {
        return true
    }

    //更新关注状态
    fun updateFollowStatus(userId: Int, isFollowed: Boolean): Boolean {
        return true
    }
}