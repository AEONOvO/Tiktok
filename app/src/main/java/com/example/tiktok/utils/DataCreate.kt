package com.example.tiktok.utils

import com.example.tiktok.R
import com.example.tiktok.data.model.UserBean
import com.example.tiktok.data.model.VideoBean
import java.util.ArrayList


class DataCreate {
    init {
        val videoBean1= VideoBean()
        videoBean1.content = "#街坊 #颜值打分 给自己颜值打100分的女生集合"
        videoBean1.videoId= R.raw.video1
        videoBean1.videoRes = "android.resource://com.example.tiktok/${R.raw.video1}"
        videoBean1.distance = 1f
        videoBean1.userBean?.isFollowed = false
        videoBean1.isLiked = false
        videoBean1.likeCount = 111
        videoBean1.commentCount = 111
        videoBean1.collectCount = 111
        videoBean1.shareCount = 111

        val userBean1 = UserBean()
        userBean1.userId = 1
        userBean1.headId = R.mipmap.head1
        userBean1.nickName = "南京街坊"
        userBean1.sign = "你们喜欢的话题，就是我们采访的内容"
        userBean1.subCount = 1000000
        userBean1.focusCount = 10
        userBean1.fansCount = 1000000
        userBean1.workCount = 50
        userBean1.dynamicCount = 50
        userBean1.likeCount = 10
        userBean1.isFollowed = false
        userList.add(userBean1)
        videoBean1.userBean = userBean1

        val videoBean2= VideoBean()
        videoBean2.content = "万万没想到... ..."
        videoBean2.videoId= R.raw.video2
        videoBean2.videoRes = "android.resource://com.example.tiktok/${R.raw.video2}"
        videoBean2.distance = 2f
        videoBean2.userBean?.isFollowed = false
        videoBean2.isLiked = false
        videoBean2.likeCount = 222
        videoBean2.commentCount = 222
        videoBean2.collectCount = 222
        videoBean2.shareCount = 222

        val userBean2 = UserBean()
        userBean2.userId = 2
        userBean2.headId = R.mipmap.head2
        userBean2.nickName = "王康"
        userBean2.sign = "视频仅供娱乐"
        userBean2.subCount = 1000000
        userBean2.focusCount = 10
        userBean2.fansCount = 1000000
        userBean2.workCount = 50
        userBean2.dynamicCount = 50
        userBean2.likeCount = 10
        userBean2.isFollowed = false
        userList.add(userBean2)
        videoBean2.userBean = userBean2

        val videoBean3= VideoBean()
        videoBean3.content = "谁懂周润发出场的这个运镜啊#周润发 #mama颁奖典礼"
        videoBean3.videoId= R.raw.video3
        videoBean3.videoRes = "android.resource://com.example.tiktok/${R.raw.video3}"
        videoBean3.distance = 3f
        videoBean3.userBean?.isFollowed = false
        videoBean3.isLiked = false
        videoBean3.likeCount = 333
        videoBean3.commentCount = 333
        videoBean3.collectCount = 333
        videoBean3.shareCount = 333

        val userBean3 = UserBean()
        userBean3.userId = 3
        userBean3.headId = R.mipmap.head3
        userBean3.nickName = "解忧Live"
        userBean3.sign = "把零碎的记忆定格永恒"
        userBean3.subCount = 1000000
        userBean3.focusCount = 10
        userBean3.fansCount = 1000000
        userBean3.workCount = 50
        userBean3.dynamicCount = 50
        userBean3.likeCount = 10
        userBean3.isFollowed = false
        userList.add(userBean3)
        videoBean3.userBean = userBean3

        val videoBean4= VideoBean()
        videoBean4.content = "朱雀三号发射成功，但回收失败"
        videoBean4.videoId= R.raw.video4
        videoBean4.videoRes = "android.resource://com.example.tiktok/${R.raw.video4}"
        videoBean4.distance = 4f
        videoBean4.userBean?.isFollowed = false
        videoBean4.isLiked = false
        videoBean4.likeCount = 444
        videoBean4.commentCount = 444
        videoBean4.collectCount = 444
        videoBean4.shareCount = 444

        val userBean4 = UserBean()
        userBean4.userId = 4
        userBean4.headId = R.mipmap.head4
        userBean4.nickName = "稻米"
        userBean4.sign = "年年有风·风吹年年·慢慢即漫漫"
        userBean4.subCount = 1000000
        userBean4.focusCount = 10
        userBean4.fansCount = 1000000
        userBean4.workCount = 50
        userBean4.dynamicCount = 50
        userBean4.likeCount = 10
        userBean4.isFollowed = false
        userList.add(userBean4)
        videoBean4.userBean = userBean4

        val videoBean5= VideoBean()
        videoBean5.content = "在你成功之前 没人想了解你的故事"
        videoBean5.videoId= R.raw.video5
        videoBean5.videoRes = "android.resource://com.example.tiktok/${R.raw.video5}"
        videoBean5.distance = 5f
        videoBean5.userBean?.isFollowed = false
        videoBean5.isLiked = false
        videoBean5.likeCount = 555
        videoBean5.commentCount = 555
        videoBean5.collectCount = 555
        videoBean5.shareCount = 555

        val userBean5 = UserBean()
        userBean5.userId = 5
        userBean5.headId = R.mipmap.head5
        userBean5.nickName = "Chips."
        userBean5.sign = "Live your life."
        userBean5.subCount = 1000000
        userBean5.focusCount = 10
        userBean5.fansCount = 1000000
        userBean5.workCount = 50
        userBean5.dynamicCount = 50
        userBean5.likeCount = 10
        userBean5.isFollowed = false
        userList.add(userBean5)
        videoBean5.userBean = userBean5

        val videoBean6= VideoBean()
        videoBean6.content = "在你成功之前 没人想了解你的故事"
        videoBean6.videoId= R.raw.video6
        videoBean6.videoRes = "android.resource://com.example.tiktok/${R.raw.video6}"
        videoBean6.distance = 6f
        videoBean6.userBean?.isFollowed = false
        videoBean6.isLiked = false
        videoBean6.likeCount = 6666
        videoBean6.commentCount = 6666
        videoBean6.collectCount = 6666
        videoBean6.shareCount = 6666

        val userBean6 = UserBean()
        userBean6.userId = 6
        userBean6.headId = R.mipmap.head6
        userBean6.nickName = "酷哥"
        userBean6.sign = "视频都是本人 谢谢你的关注。"
        userBean6.subCount = 1000000
        userBean6.focusCount = 10
        userBean6.fansCount = 1000000
        userBean6.workCount = 50
        userBean6.dynamicCount = 50
        userBean6.likeCount = 10
        userBean6.isFollowed = false
        userList.add(userBean6)
        videoBean6.userBean = userBean6

        datas.add(videoBean1)
        datas.add(videoBean2)
        datas.add(videoBean3)
        datas.add(videoBean4)
        datas.add(videoBean5)
        datas.add(videoBean6)

    }

    //无需实例化，直接静态访问
    companion object {
        @JvmField
        var datas = ArrayList<VideoBean>()
        @JvmField
        var userList = ArrayList<UserBean>()
    }
}