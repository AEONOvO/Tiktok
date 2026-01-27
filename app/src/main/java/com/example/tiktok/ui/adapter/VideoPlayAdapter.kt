package com.example.tiktok.ui.adapter

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.RecyclerView
import com.example.tiktok.databinding.ItemVideoPlayBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.tiktok.R
import com.example.tiktok.data.model.VideoBean
import com.example.tiktok.viewmodel.VideoPlayViewModel

class VideoPlayAdapter(
    private val videoList:List<VideoBean>,
    private val viewModel: VideoPlayViewModel,
    private val onCommentClick: ((VideoBean, Int) -> Unit)? = null
):RecyclerView.Adapter<VideoPlayAdapter.VideoViewHolder>() {

    // 保存 RecyclerView 的引用，用于获取 ViewHolder
    private var recyclerView: RecyclerView? = null
    //当前正在播放的位置
    private var currentPlayingPosition = -1

    //让 Adapter 持有一个 RecyclerView 的强引用
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    //解绑，防止内存泄漏
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    // 获取指定位置的 ViewHolder
    private fun getViewHolderAtPosition(position: Int): VideoViewHolder? {
        return recyclerView?.findViewHolderForAdapterPosition(position) as? VideoViewHolder
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoPlayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videoList[position], position)
    }

    //绑定控件的点击、状态更新等逻辑，将视频数据绑定到布局控件上实现列表项的渲染
    inner class VideoViewHolder(val binding: ItemVideoPlayBinding) : RecyclerView.ViewHolder(binding.root) {

        private var exoPlayer: ExoPlayer? = null
        private var recordAnimator: ObjectAnimator? = null
        private lateinit var currentVideo: VideoBean

        //将视频数据绑定到ViewHolder的视图上
        @SuppressLint("SetTextI18n")
        fun bind(video: VideoBean, position: Int) {

            currentVideo = video
            binding.ivCover.visibility = View.VISIBLE

            with(binding) {
                ivCover.transitionName = "video_cover_$position"

                //加载视频封面
                Glide.with(binding.ivCover)
                    .asBitmap()
                    .load(video.videoRes)
                    .apply(RequestOptions().frame(0))
                    .into(binding.ivCover)

                //加载头像
                loadAvatar(video)

                //加载唱片封面
                Glide.with(ivHeadAnim)
                    .load(video.userBean?.headId ?: R.drawable.default_avatar)
                    .apply(RequestOptions().circleCrop())
                    .into(ivHeadAnim)

                //设置文字信息
                val nickname = video.userBean?.nickName ?: root.context.getString(R.string.default_user_name)
                tvNickname.text = root.context.getString(R.string.user_nickname_format, nickname)
                tvTitle.text = video.content ?: ""
                tvMarquee.text = "@$nickname 创作的原声 - $nickname"       //动态设置走马灯文本

                updateUIState(video)                                     //设置统计数据

                //设置关注状态
                ivFollow.visibility =
                    if (video.userBean?.isFollowed == true) View.GONE else View.VISIBLE

                setupClickListeners(video, position)        //设置点击监听
                setupExoPlayer(video)                       // 设置 ExoPlayer视频播放器
                setupLikeAnimationView(video, position)     // 设置双击点赞动画/单机暂停监听
            }
        }

        // 设置 ExoPlayer
        private fun setupExoPlayer(video: VideoBean) {
            // 检查 videoRes 是否为空
            if (video.videoRes.isEmpty()) return

            try {
                // 创建 ExoPlayer 实例
                exoPlayer = ExoPlayer.Builder(binding.root.context)
                    .build()
                    .apply {
                        repeatMode = Player.REPEAT_MODE_ONE  // 单曲循环

                        // 绑定到 PlayerView
                        binding.playerView.player = this
                        startRecordAnimation()

                        // 设置播放监听
                        addListener(object : Player.Listener {
                            override fun onEvents(player: Player, events: Player.Events) {
                                //监听第一帧渲染
                                if (events.contains(Player.EVENT_RENDERED_FIRST_FRAME)) {
                                    binding.ivCover.visibility = View.GONE
                                }
                            }

                            override fun onPlayerError(error: PlaybackException) {
                                Toast.makeText(binding.root.context, "播放失败", Toast.LENGTH_SHORT).show()
                            }
                        })

                        // 设置视频源
                        val mediaItem = MediaItem.fromUri(android.net.Uri.parse(video.videoRes))
                        setMediaItem(mediaItem)
                        playWhenReady = false  // 默认不自动播放
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(binding.root.context, "播放器初始化失败", Toast.LENGTH_SHORT).show()
            }
        }

        //设置点击监听
        private fun setupClickListeners(video: VideoBean, position: Int) {
            with(binding) {
                // 点赞
                rlLike.setOnClickListener {
                    viewModel.toggleLike(video, position)
                }

                // 评论
                ivComment.setOnClickListener {
                    onCommentClick?.invoke(video, position)
                }

                // 收藏
                ivCollect.setOnClickListener {
                    viewModel.toggleCollect(video, position)
                }

                // 分享
                ivShare.setOnClickListener {
                    Toast.makeText(it.context, "分享功能待实现", Toast.LENGTH_SHORT).show()
                }

                // 头像
                ivAvatar.setOnClickListener {
                    Toast.makeText(it.context, "进入用户主页功能待实现", Toast.LENGTH_SHORT).show()
                }

                // 关注
                ivFollow.setOnClickListener {
                    viewModel.followUser(video, position)
                }

                // 唱片点击
                rlRecord.setOnClickListener {
                    togglePlayPause()
                }

            }
        }

        // 设置双击点赞动画视图/暂停的监听
        private fun setupLikeAnimationView(video: VideoBean, position: Int) {
            with(binding.likeAnimationView) {
                // 双击点赞
                setOnLikeListener {
                    if (!video.isLiked) {
                        viewModel.toggleLike(video, position)
                    }
                }

                // 单击播放/暂停
                setOnPlayPauseListener {
                    togglePlayPause()
                }
            }
        }

        //加载头像
        private fun loadAvatar(video: VideoBean) {
            val avatarRes = video.userBean?.headId ?: R.drawable.default_avatar
            Glide.with(binding.ivAvatar)
                .load(avatarRes)
                .apply(RequestOptions().circleCrop())
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(binding.ivAvatar)
        }

        //更新推荐单列页面状态
        private fun updateUIState(video: VideoBean) {
            with(binding) {
                // 点赞状态
                ivLike.text = ivLike.context.getString(R.string.icon_like_fill)
                ivLike.setTextColor(
                    if (video.isLiked) {
                        ivLike.context.getColor(R.color.red)      // 已点赞：红色
                    } else {
                        ivLike.context.getColor(R.color.white)    // 未点赞：白色
                    }
                )

                //收藏状态
                ivCollect.text = ivCollect.context.getString(R.string.icon_collect)
                ivCollect.setTextColor(
                    if (video.isCollected) {
                        ivCollect.context.getColor(R.color.yellow)  // 已收藏：黄色
                    } else {
                        ivCollect.context.getColor(R.color.white)   // 未收藏：白色
                    }
                )

                // 数字
                tvLikecount.text = formatCount(video.likeCount)
                tvCommentcount.text = formatCount(video.commentCount)
                tvCollectcount.text = formatCount(video.collectCount)
                tvSharecount.text = formatCount(video.shareCount)
            }
        }

        //切换播放/暂停
        private fun togglePlayPause() {
            exoPlayer?.let {
                if (it.isPlaying) {
                    showPauseIcon()
                    pauseRecordAnimation()
                    it.pause()
                } else {
                    hidePauseIcon()
                    startRecordAnimation()
                    it.play()
                }
            }
        }

        // 显示暂停图标
        private fun showPauseIcon() {
            binding.ivPause.visibility = View.VISIBLE
            //添加淡入动画
            binding.ivPause.alpha = 0f
            binding.ivPause.animate()
                .alpha(0.8f)
                .setDuration(200)
                .start()
        }

        // 隐藏暂停图标
        private fun hidePauseIcon() {
            //添加淡出动画
            binding.ivPause.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    binding.ivPause.visibility = View.GONE
                }
                .start()
        }

        //播放
        fun play() {
            if (exoPlayer == null) return

            hidePauseIcon()
            startRecordAnimation()

            exoPlayer?.let { player ->
                when (player.playbackState) {
                    Player.STATE_IDLE -> {
                        player.prepare()
                    }
                    Player.STATE_BUFFERING -> {
                    }
                    Player.STATE_READY -> {
                    }
                    Player.STATE_ENDED -> {
                        player.seekTo(0)
                    }
                }
                player.play()
            }
        }

        //暂停
        fun pause() {
            pauseRecordAnimation()
            exoPlayer?.pause()
        }

        //视频资源释放，播放状态重置
        fun release() {
            // 先暂停并停止解码
            exoPlayer?.stop()
            
            // 显式断开与 Surface 的连接
            exoPlayer?.clearVideoSurface()
            
            // 清空媒体项
            exoPlayer?.clearMediaItems()
            
            // 彻底释放播放器
            exoPlayer?.release()
            exoPlayer = null

            // 停止动画和重置 UI
            stopRecordAnimation()
            binding.ivPause.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
            binding.ivCover.visibility = View.VISIBLE
        }

        //启动唱片旋转动画的方法
        private fun startRecordAnimation() {
            if (recordAnimator == null) {
                recordAnimator = ObjectAnimator.ofFloat(
                    binding.rlRecord,                    // 目标 View：唱片容器
                    "rotation",             // 旋转
                    0f,                          // 起始角度：0°
                    360f                                 // 结束角度：360°（完整一圈）
                ).apply {
                    duration = 10000                       // 旋转一圈耗时：10 秒
                    repeatCount = ObjectAnimator.INFINITE  // 重复次数：无限循环
                    interpolator = LinearInterpolator()    // 插值器：匀速旋转
                }
            }
            recordAnimator?.start()
        }

        //暂停唱片旋转动画的方法
        private fun pauseRecordAnimation() {
            recordAnimator?.pause()
        }

        //停止唱片旋转动画，重置状态
        private fun stopRecordAnimation() {
            recordAnimator?.cancel()
            recordAnimator = null
            binding.rlRecord.rotation = 0f
        }

        //更新点赞状态，并同步UI与动画
        fun updateLikeState(isLiked: Boolean) {
            currentVideo.isLiked = isLiked
            updateUIState(currentVideo)
        }

        //更新收藏状态
        fun updateCollectState(isCollected: Boolean) {
            currentVideo.isCollected = isCollected
            updateUIState(currentVideo)
        }

        //更新关注状态
        fun updateFollowState(isFollowed: Boolean) {
            currentVideo.userBean?.isFollowed = isFollowed
            binding.ivFollow.visibility = if (isFollowed) View.GONE else View.VISIBLE
        }

        //更新评论数显示
        fun updateCommentCount(count: Int) {
            currentVideo.commentCount = count
            binding.tvCommentcount.text = formatCount(count)
            updateUIState(currentVideo)
        }

        //格式化数字
        private fun formatCount(count: Int): String {
            return when {
                count >= 10000 -> "${count / 10000}.${(count % 10000) / 1000}w"
                count >= 1000 -> "${count / 1000}.${(count % 1000) / 100}k"
                else -> count.toString()
            }
        }
    }

    // 页面切换时，暂停当前视频，播放新视频
    fun onPageSelected(position: Int) {
        // 暂停之前的视频
        if (currentPlayingPosition != -1 && currentPlayingPosition != position) {
            getViewHolderAtPosition(currentPlayingPosition)?.pause()
        }

        currentPlayingPosition = position

        // 播放当前视频
        getViewHolderAtPosition(position)?.play()
    }

    //更新列表中指定位置视频的点赞状态
    fun updateLikeStatus(position: Int, isLiked: Boolean) {
        getViewHolderAtPosition(position)?.updateLikeState(isLiked)
    }

    //更新列表中指定位置视频的收藏状态
    fun updateCollectStatus(position: Int, isCollected: Boolean) {
        getViewHolderAtPosition(position)?.updateCollectState(isCollected)
    }

    //更新列表中指定位置视频的关注状态
    fun updateFollowStatus(position: Int, isFollowed: Boolean) {
        getViewHolderAtPosition(position)?.updateFollowState(isFollowed)
    }

    //更新指定位置视频的评论数
    fun updateCommentCount(position: Int, newCount: Int) {
        if (position in videoList.indices) {
            videoList[position].commentCount = newCount
            getViewHolderAtPosition(position)?.updateCommentCount(newCount)
        }
    }

    //恢复当前视频
    fun resumeCurrentVideo() {
        if (currentPlayingPosition != -1) {
            getViewHolderAtPosition(currentPlayingPosition)?.play()
        }
    }

    //暂停当前视频
    fun pauseCurrentVideo() {
        if (currentPlayingPosition != -1) {
            getViewHolderAtPosition(currentPlayingPosition)?.pause()
        }
    }

    //释放所有视频资源
    fun releaseAllVideos() {
        // 释放当前正在播放的视频
        if (currentPlayingPosition != -1) {
            getViewHolderAtPosition(currentPlayingPosition)?.release()
        }
        
        // 释放前后预加载的 ViewHolder
        if (currentPlayingPosition > 0) {
            getViewHolderAtPosition(currentPlayingPosition - 1)?.release()
        }
        if (currentPlayingPosition < itemCount - 1) {
            getViewHolderAtPosition(currentPlayingPosition + 1)?.release()
        }
        
        currentPlayingPosition = -1
        recyclerView = null
    }

    // ViewHolder 被回收时释放资源
    override fun onViewRecycled(holder: VideoViewHolder) {
        super.onViewRecycled(holder)
        holder.release()
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

}
