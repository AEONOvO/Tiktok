package com.example.tiktok.ui.adapter

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.recyclerview.widget.RecyclerView
import com.example.tiktok.databinding.ItemVideoPlayBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.tiktok.R
import com.example.tiktok.data.model.VideoBean
import com.example.tiktok.ui.viewmodel.VideoPlayViewModel

class VideoPlayAdapter(
    private val videoList:List<VideoBean>,
    private val viewModel: VideoPlayViewModel,
    private val onCommentClick: ((VideoBean, Int) -> Unit)? = null
):RecyclerView.Adapter<VideoPlayAdapter.VideoViewHolder>() {

    private var currentPlayingPosition = -1
    private val videoHolders = mutableMapOf<Int, VideoViewHolder>()

    //缓存列表项布局中的所有控件，绑定控件的点击、状态更新等逻辑，将视频数据绑定到布局控件上实现列表项的渲染
    inner class VideoViewHolder(val binding: ItemVideoPlayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var exoPlayer: ExoPlayer? = null
        private var recordAnimator: ObjectAnimator? = null
        private lateinit var currentVideo: VideoBean
        private var isPlayerReady = false

        //将视频数据绑定到ViewHolder的视图上
        fun bind(video: VideoBean, position: Int) {
            android.util.Log.d("VideoPlayAdapter", "========== bind() 开始 ==========")
            android.util.Log.d("VideoPlayAdapter", "position=$position, videoId=${video.videoId}")

            currentVideo = video

            with(binding) {
                //加载封面
                android.util.Log.d("VideoPlayAdapter", "加载封面...")
                loadCover(video)

                //加载头像
                android.util.Log.d("VideoPlayAdapter", "加载头像...")
                loadAvatar(video)

                //加载唱片封面
                Glide.with(ivHeadAnim)
                    .load(video.userBean?.headId ?: R.drawable.default_avatar)
                    .apply(RequestOptions().circleCrop())
                    .into(ivHeadAnim)


                //设置文字信息
                val nickname = video.userBean?.nickName
                    ?: root.context.getString(R.string.default_user_name)
                tvNickname.text = root.context.getString(R.string.user_nickname_format, nickname)
                tvTitle.text = video.content ?: ""

                //设置统计数据
                updateUIState(video)

                //设置关注状态
                ivFollow.visibility =
                    if (video.userBean?.isFollowed == true) View.GONE else View.VISIBLE

                setupClickListeners(video, position)
                android.util.Log.d("VideoPlayAdapter", "准备初始化 ExoPlayer...")
                setupExoPlayer(video)
                android.util.Log.d("VideoPlayAdapter", "setupExoPlayer 调用完成")

                // 设置双击点赞动画监听
                setupLikeAnimationView(video, position)
            }

            //保存ViewHolder
            videoHolders[position] = this
            android.util.Log.d("VideoPlayAdapter", "========== bind() 完成 ==========")
        }

        //加载封面
        private fun loadCover(video: VideoBean) {
            // 封面初始可见
            binding.ivCover.visibility = View.VISIBLE

            if(video.coverRes!=0) {
                Glide.with(binding.ivCover)
                    .load(video.coverRes)
                    .into(binding.ivCover)
            }else
            {
                Glide.with(binding.ivCover)
                    .asBitmap()
                    .load(android.net.Uri.parse(video.videoRes))
                    .apply (RequestOptions().frame(0))
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.default_error)
                    .into(binding.ivCover)
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
                    Toast.makeText(it.context, "进入用户主页", Toast.LENGTH_SHORT).show()
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

        // 设置双击点赞动画视图的监听
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

        // 设置 ExoPlayer
        @OptIn(UnstableApi::class)  //使用了不稳定API
        private fun setupExoPlayer(video: VideoBean) {
            android.util.Log.d("VideoPlayAdapter", "========== setupExoPlayer 开始 ==========")

            // ✅ 检查 videoRes 是否为空
            if (video.videoRes.isNullOrEmpty()) {
                android.util.Log.e("VideoPlayAdapter", "❌ videoRes 为空或 null！")
                Toast.makeText(binding.root.context, "视频路径为空", Toast.LENGTH_SHORT).show()
                return
            }

            android.util.Log.d("VideoPlayAdapter", "videoRes: ${video.videoRes}")

            try {
                // 创建 ExoPlayer 实例
                android.util.Log.d("VideoPlayAdapter", "创建 ExoPlayer 实例...")
                exoPlayer = ExoPlayer.Builder(binding.root.context)
                    .build()
                    .apply {
                        repeatMode = Player.REPEAT_MODE_ONE  // 单曲循环
                        android.util.Log.d("VideoPlayAdapter", "ExoPlayer 实例创建成功")

                        // 绑定到 PlayerView
                        binding.playerView.player = this
                        android.util.Log.d("VideoPlayAdapter", "PlayerView 绑定成功")

                        // 设置播放监听
                        addListener(object : Player.Listener {
                            override fun onPlaybackStateChanged(playbackState: Int) {
                                when (playbackState) {
                                    Player.STATE_BUFFERING -> {
                                        // 缓冲中
                                        binding.progressBar.visibility = View.VISIBLE
                                        android.util.Log.d("VideoPlayAdapter", "缓冲中...")
                                    }
                                    Player.STATE_READY -> {
                                        // 视频准备好，隐藏封面
                                        binding.progressBar.visibility = View.GONE
                                        android.util.Log.d("VideoPlayAdapter", "视频准备完成")

                                        if (!isPlayerReady) {
                                            isPlayerReady = true
                                            hideCoverWithAnimation()
                                        }
                                    }
                                    Player.STATE_ENDED -> {
                                        android.util.Log.d("VideoPlayAdapter", "播放结束")
                                    }
                                    Player.STATE_IDLE -> {
                                        android.util.Log.d("VideoPlayAdapter", "播放器空闲")
                                    }
                                }
                            }


                            override fun onIsPlayingChanged(isPlaying: Boolean) {
                                android.util.Log.d("VideoPlayAdapter", "播放状态变化: $isPlaying")
                                if (isPlaying) {
                                    startRecordAnimation()
                                    hidePauseIcon()
                                } else {
                                    pauseRecordAnimation()
                                    showPauseIcon()
                                }
                            }

                            override fun onPlayerError(error: PlaybackException) {
                                android.util.Log.e("VideoPlayAdapter", "播放错误: ${error.message}")
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(
                                    binding.root.context,
                                    "视频加载失败: ${error.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                        android.util.Log.d("VideoPlayAdapter", "videoRes 值: ${video.videoRes}")
                        android.util.Log.d("VideoPlayAdapter", "videoRes 类型: ${video.videoRes::class.java.simpleName}")

                        // 设置视频源
                        android.util.Log.d("VideoPlayAdapter", "解析 URI...")
                        val uri = android.net.Uri.parse(video.videoRes)
                        android.util.Log.d("VideoPlayAdapter", "URI: $uri")
                        val mediaItem = MediaItem.fromUri(uri)
                        android.util.Log.d("VideoPlayAdapter", "MediaItem 创建成功")

                        val dataSourceFactory = DefaultDataSource.Factory(binding.root.context)
                        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(mediaItem)
                        android.util.Log.d("VideoPlayAdapter", "MediaSource 创建成功")
                        setMediaSource(mediaSource)
                        android.util.Log.d("VideoPlayAdapter", "MediaSource 设置成功")
                        prepare()
                        android.util.Log.d("VideoPlayAdapter", "prepare() 调用成功")
                        playWhenReady = false  // 默认不自动播放
                        android.util.Log.d("VideoPlayAdapter", "playWhenReady = false")
                    }
                android.util.Log.d("VideoPlayAdapter", "========== setupExoPlayer 完成 ==========")
            } catch (e: Exception) {
                android.util.Log.e("VideoPlayAdapter", "ExoPlayer 初始化失败: ${e.message}")
                e.printStackTrace()
                Toast.makeText(binding.root.context, "播放器初始化失败", Toast.LENGTH_SHORT).show()
            }
        }









        //切换播放/暂停
        private fun togglePlayPause() {
            exoPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                } else {
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

        // 隐藏封面（带动画）
        private fun hideCoverWithAnimation() {
            binding.ivCover.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    binding.ivCover.visibility = View.GONE
                    binding.ivCover.alpha = 1f
                }
                .start()
        }


        //播放
        fun play() {
            android.util.Log.d("VideoPlayAdapter", "========== play() 被调用 ==========")
            if (exoPlayer == null) {
                android.util.Log.e("VideoPlayAdapter", "❌ exoPlayer 为 null！")
                return
            }
            exoPlayer?.let {
                // 如果播放器处于 IDLE 或 ENDED 状态，重新准备
                if (it.playbackState == Player.STATE_IDLE || it.playbackState == Player.STATE_ENDED) {
                    android.util.Log.d("VideoPlayAdapter", "播放器状态异常，重新准备")
                    it.prepare()
                }

                // 如果视频还没准备好，显示封面和加载动画
                if (!isPlayerReady) {
                    android.util.Log.d("VideoPlayAdapter", "首次播放，显示封面和加载动画")
                    binding.ivCover.visibility = View.VISIBLE
                    binding.ivCover.alpha = 1f
                    binding.progressBar.visibility = View.VISIBLE
                } else {
                    // 如果视频已经准备好，不显示加载动画
                    android.util.Log.d("VideoPlayAdapter", "视频已准备好，直接播放")
                }

                it.play()
                android.util.Log.d("VideoPlayAdapter", "play() 调用完成")
            }
        }

        //暂停
        fun pause() {
            exoPlayer?.let {
                it.pause()
                android.util.Log.d("VideoPlayAdapter", "暂停播放")
            }
        }

        //视频资源释放，播放状态重置
        fun release() {
            android.util.Log.d("VideoPlayAdapter", "释放 ViewHolder 资源")
            // 先停止播放
            exoPlayer?.stop()
            exoPlayer?.clearMediaItems()
            exoPlayer?.release()
            exoPlayer = null

            stopRecordAnimation()
            binding.ivPause.visibility = View.GONE
            binding.ivCover.visibility = View.VISIBLE
            binding.ivCover.alpha = 1f
            binding.progressBar.visibility = View.GONE
            isPlayerReady = false
            android.util.Log.d("VideoPlayAdapter", "释放播放器")
        }

        //启动唱片旋转动画的方法
        private fun startRecordAnimation() {
            if (recordAnimator == null) {
                recordAnimator = ObjectAnimator.ofFloat(
                    binding.rlRecord,
                    "rotation",
                    0f,
                    360f
                ).apply {
                    duration = 10000
                    repeatCount = ObjectAnimator.INFINITE
                    interpolator = LinearInterpolator()
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



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoPlayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VideoViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videoList[position], position)
    }

    //更新列表中指定位置视频的点赞状态
    fun updateLikeStatus(position: Int, isLiked: Boolean) {
        videoHolders[position]?.updateLikeState(isLiked)
    }

    //更新列表中指定位置视频的收藏状态
    fun updateCollectStatus(position: Int, isCollected: Boolean) {
        videoHolders[position]?.updateCollectState(isCollected)
    }

    //更新列表中指定位置视频的关注状态
    fun updateFollowStatus(position: Int, isFollowed: Boolean) {
        videoHolders[position]?.updateFollowState(isFollowed)
    }

    //更新指定位置视频的评论数
    fun updateCommentCount(position: Int, newCount: Int) {
        if (position in videoList.indices) {
            videoList[position].commentCount = newCount
            videoHolders[position]?.updateCommentCount(newCount)
        }
    }

    fun onPageSelected(position: Int) {
        android.util.Log.d("VideoPlayAdapter", "========== onPageSelected ==========")
        android.util.Log.d("VideoPlayAdapter", "position: $position")
        android.util.Log.d("VideoPlayAdapter", "currentPlayingPosition: $currentPlayingPosition")
        android.util.Log.d("VideoPlayAdapter", "videoHolders.size: ${videoHolders.size}")
        android.util.Log.d("VideoPlayAdapter", "videoHolders.keys: ${videoHolders.keys}")

        if (currentPlayingPosition != -1 && currentPlayingPosition != position) {
            android.util.Log.d("VideoPlayAdapter", "暂停 position=$currentPlayingPosition")
            videoHolders[currentPlayingPosition]?.pause()
        }
        val holder = videoHolders[position]
        if (holder == null) {
            android.util.Log.e("VideoPlayAdapter", "❌ 找不到 position=$position 的 ViewHolder，延迟重试")
            // 延迟重试
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                videoHolders[position]?.play()
                android.util.Log.d("VideoPlayAdapter", "延迟播放 position=$position")
            }, 200)
        } else {
            android.util.Log.d("VideoPlayAdapter", "✅ 找到 ViewHolder，开始播放")
            holder.play()
        }
        currentPlayingPosition = position
        android.util.Log.d("VideoPlayAdapter", "========== onPageSelected 完成 ==========")
    }

    //恢复当前视频
    fun resumeCurrentVideo() {
        if (currentPlayingPosition != -1) {
            videoHolders[currentPlayingPosition]?.play()
        }
    }

    //暂停当前视频
    fun pauseCurrentVideo() {
        if (currentPlayingPosition != -1) {
            videoHolders[currentPlayingPosition]?.pause()
        }
    }

    //释放所有视频资源
    fun releaseAllVideos() {
        android.util.Log.d("VideoPlayAdapter", "释放所有视频资源，当前 holders: ${videoHolders.size}")
        videoHolders.values.forEach { it.release() }
        videoHolders.clear()
        currentPlayingPosition = -1  // 重置播放位置
        android.util.Log.d("VideoPlayAdapter", "资源释放完成")
    }
}
