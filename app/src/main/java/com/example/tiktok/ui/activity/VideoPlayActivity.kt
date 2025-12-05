package com.example.tiktok.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.tiktok.base.BaseBindingActivity
import com.example.tiktok.databinding.ActivityVideoPlayBinding
import com.example.tiktok.data.model.VideoBean
import com.example.tiktok.ui.adapter.VideoPlayAdapter
import com.example.tiktok.ui.view.CommentDialog
import com.example.tiktok.ui.viewmodel.VideoPlayViewModel
import com.example.tiktok.utils.FullScreenUtil
import com.example.tiktok.utils.Resource
import com.example.tiktok.utils.VideoPlayTouchHelper

class VideoPlayActivity:BaseBindingActivity<ActivityVideoPlayBinding>({ActivityVideoPlayBinding.inflate(it)}) {
    private val viewModel: VideoPlayViewModel by viewModels()
    private var videoPlayAdapter:VideoPlayAdapter?=null
    private var currentPosition:Int=0
    private val videoList = mutableListOf<VideoBean>()

    private var touchHelper: VideoPlayTouchHelper? = null
    private var isRefreshing = false
    private var isLoadingMore = false

    companion object{
        private const val KEY_VIDEO_LIST="video_list"
        private const val KEY_POSITION="position"

        // 带转场动画的启动方法
        fun startWithTransition(
            context: Context,
            videoList: ArrayList<VideoBean>,
            position: Int,
            options: Bundle?
        ) {
            val intent = Intent(context, VideoPlayActivity::class.java).apply {
                putParcelableArrayListExtra(KEY_VIDEO_LIST, videoList)
                putExtra(KEY_POSITION, position)
            }
            context.startActivity(intent, options)
        }

        // 原有的启动方法(不带转场）
        fun start(context: Context, videoList: ArrayList<VideoBean>, position: Int) {
            startWithTransition(context, videoList, position, null)
        }
    }

    //初始化
    override fun init() {
        //设置全屏
        FullScreenUtil.setFullScreen(this)

        //获取传递的数据
        val receivedList = intent.getParcelableArrayListExtraCompat<VideoBean>(KEY_VIDEO_LIST)
        currentPosition=intent.getIntExtra(KEY_POSITION,0)

        receivedList?.let {
            videoList.clear()
            videoList.addAll(it)
            android.util.Log.d("VideoPlayActivity", "✅ 接收到 ${videoList.size} 条视频数据")
        }

        if (videoList.isEmpty()) {
            android.util.Log.e("VideoPlayActivity", "❌ 视频列表为空！")
            Toast.makeText(this, "没有视频数据", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViewPager()
        setupClickListeners()
        setupTouchHelper()
        observeViewModel()

        // 延迟转场动画，等待 View 准备好
        supportPostponeEnterTransition()
    }

    //设置页面
    private fun setupViewPager(){
        android.util.Log.d("VideoPlayActivity", "========== 设置 ViewPager2 ==========")
        android.util.Log.d("VideoPlayActivity", "视频列表大小: ${videoList.size}")
        android.util.Log.d("VideoPlayActivity", "当前位置: $currentPosition")

        if (videoList.isEmpty()) {
            android.util.Log.e("VideoPlayActivity", "❌ 视频列表为空！")
            Toast.makeText(this, "没有视频数据", Toast.LENGTH_SHORT).show()
            return
        }

        videoList?.let{list->
            videoPlayAdapter= VideoPlayAdapter(
                list,
                viewModel,
                onCommentClick = { video, position ->
                    showCommentDialog(video, position)
                }
            )


            binding.viewPager.adapter=videoPlayAdapter
            binding.viewPager.orientation=ViewPager2.ORIENTATION_VERTICAL
            binding.viewPager.offscreenPageLimit=1
            //设置当前位置
            binding.viewPager.setCurrentItem(currentPosition, false)

            // 监听 RecyclerView 布局完成
            val recyclerView = binding.viewPager.getChildAt(0) as? androidx.recyclerview.widget.RecyclerView
            recyclerView?.viewTreeObserver?.addOnGlobalLayoutListener(object : android.view.ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    // 移除监听器，防止多次调用
                    recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    android.util.Log.d("VideoPlayActivity", "RecyclerView 布局完成")

                    // 再延迟一帧，确保 ViewHolder 绑定完成
                    recyclerView.post {
                        android.util.Log.d("VideoPlayActivity", "开始播放视频")
                        videoPlayAdapter?.onPageSelected(currentPosition)

                        // 设置共享元素的 transitionName（与列表页保持一致）
                        val firstItemView = getViewPagerItemAt(currentPosition)
                        firstItemView?.let { view ->
                            ViewCompat.setTransitionName(view, "video_cover_$currentPosition")
                        }

                        // 启动转场动画
                        supportStartPostponedEnterTransition()
                    }
                }
            })

            //监听页面切换
            binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    currentPosition=position

                    //暂停上一个视频，播放当前视频
                    videoPlayAdapter?.onPageSelected(position)
                }
            })
        }
    }

    //设置点击监听
    private fun setupClickListeners(){
        //返回按钮
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    // 设置触摸监听
    private fun setupTouchHelper() {
        touchHelper = VideoPlayTouchHelper(
            context = this,
            viewPager = binding.viewPager,
            onPullDown = { distance ->
                // 下拉时更新提示文字
                val alpha = (distance / 300f).coerceIn(0f, 1f)
                binding.tvRefreshHint.alpha = alpha
                binding.tvRefreshHint.text = if (distance > 200f) "释放刷新" else "下拉刷新"
            },
            onPullUp = { distance ->
                // 上拉时更新提示文字
                val alpha = (distance / 300f).coerceIn(0f, 1f)
                binding.tvLoadMoreHint.alpha = alpha
                binding.tvLoadMoreHint.text = if (distance > 200f) "释放加载" else "上拉加载更多"
            },
            onRefresh = {
                // 触发刷新
                if (!isRefreshing) {
                    isRefreshing = true
                    binding.tvRefreshHint.text = "正在刷新..."
                    viewModel.refreshVideos()
                }
            },
            onLoadMore = {
                // 触发加载更多
                if (!isLoadingMore) {
                    isLoadingMore = true
                    binding.tvLoadMoreHint.text = "正在加载..."
                    viewModel.loadMoreVideos()
                }
            }
        )
    }

    // 拦截触摸事件
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // 先让 touchHelper 尝试处理
        val handled = touchHelper?.onTouchEvent(ev) ?: false

        // 如果 touchHelper 拦截了事件，返回 true
        if (handled) {
            return true
        }

        // 否则交给父类处理（让 ViewPager2 正常工作）
        return super.dispatchTouchEvent(ev)
    }


    // 获取 ViewPager2 中指定位置的 View
    private fun getViewPagerItemAt(position: Int): View? {
        val recyclerView = binding.viewPager.getChildAt(0) as? androidx.recyclerview.widget.RecyclerView
        val viewHolder = recyclerView?.findViewHolderForAdapterPosition(position)
        return viewHolder?.itemView?.findViewById(com.example.tiktok.R.id.iv_cover)
    }


    //观察事件
    private fun observeViewModel() {
        // 观察刷新结果
        viewModel.refreshResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    android.util.Log.d("VideoPlayActivity", "正在刷新...")
                }
                is Resource.Success -> {
                    isRefreshing = false
                    binding.tvRefreshHint.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .start()

                    resource.data?.let { newVideos ->
                        android.util.Log.d("VideoPlayActivity", "刷新成功，获取到 ${newVideos.size} 条视频")

                        val currentPos = currentPosition

                        videoList.clear()
                        videoList.addAll(0, newVideos)  // 插入到顶部

                        videoPlayAdapter?.releaseAllVideos()
                        videoPlayAdapter?.notifyDataSetChanged()

                        val safePosition = if (currentPos < videoList.size) currentPos else 0
                        currentPosition = safePosition

                        binding.viewPager.postDelayed({
                            if (binding.viewPager.currentItem != safePosition) {
                                binding.viewPager.setCurrentItem(safePosition, false)
                            }

                            videoPlayAdapter?.onPageSelected(safePosition)

                            android.util.Log.d("VideoPlayActivity", "刷新后恢复播放，position=$safePosition")
                        }, 300)
                        Toast.makeText(this, "刷新成功，加载了 ${newVideos.size} 条视频", Toast.LENGTH_SHORT).show()

                    }
                }
                is Resource.Error -> {
                    isRefreshing = false
                    binding.tvRefreshHint.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .start()
                    Toast.makeText(this, resource.message ?: "刷新失败", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 观察加载更多结果
        viewModel.loadMoreResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // 加载中
                }
                is Resource.Success -> {
                    isLoadingMore = false
                    binding.tvLoadMoreHint.alpha = 0f

                    resource.data?.let { newVideos ->
                        val startPosition = videoList.size
                        videoList.addAll(newVideos)  // 追加到末尾
                        videoPlayAdapter?.notifyItemRangeInserted(startPosition, newVideos.size)
                        Toast.makeText(this, "加载了 ${newVideos.size} 条视频", Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Error -> {
                    isLoadingMore = false
                    binding.tvLoadMoreHint.alpha = 0f
                    Toast.makeText(this, resource.message ?: "加载失败", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 观察点赞结果
        viewModel.likeResult.observe(this) { (position, isLiked) ->
            videoPlayAdapter?.updateLikeStatus(position, isLiked)
        }

        // 观察收藏结果
        viewModel.collectResult.observe(this) { (position, isCollected) ->
            videoPlayAdapter?.updateCollectStatus(position, isCollected)
        }

        // 观察关注结果
        viewModel.followResult.observe(this) { (position, isFollowed) ->
            videoPlayAdapter?.updateFollowStatus(position, isFollowed)
        }

        // 观察错误信息
        viewModel.errorMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        // 观察成功信息
        viewModel.successMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    //恢复
    override fun onResume() {
        super.onResume()

        //恢复视频播放
        videoPlayAdapter?.resumeCurrentVideo()
    }

    //暂停
    override fun onPause() {
        super.onPause()

        //暂停当前视频播放
        videoPlayAdapter?.pauseCurrentVideo()
    }

    // 显示评论弹窗
    private fun showCommentDialog(video: VideoBean, position: Int) {
        // 暂停当前视频
        videoPlayAdapter?.pauseCurrentVideo()

        val commentDialog = CommentDialog(
            context = this,
            videoId = video.videoId,
            viewModelStoreOwner = this,

            // 传入评论数变化的回调
            onCommentCountChanged = { newCount ->

                // 更新 VideoBean 中的评论数
                videoList?.getOrNull(position)?.let {
                    it.commentCount = newCount
                }

                // 更新 Adapter 中的显示
                videoPlayAdapter?.updateCommentCount(position, newCount)
            }
        )

        // 弹窗关闭时恢复视频播放
        commentDialog.setOnDismissListener {
            videoPlayAdapter?.resumeCurrentVideo()
        }
        commentDialog.show()
    }

    //释放
    override fun onDestroy() {
        super.onDestroy()

        //释放资源
        videoPlayAdapter?.releaseAllVideos()
    }
}