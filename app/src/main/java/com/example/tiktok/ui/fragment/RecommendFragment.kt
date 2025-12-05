package com.example.tiktok.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.util.Pair
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.tiktok.R
import com.example.tiktok.base.BaseBindingFragment
import com.example.tiktok.data.model.VideoBean
import com.example.tiktok.databinding.FragmentRecommendBinding
import com.example.tiktok.databinding.ItemGridvideoBinding
import com.example.tiktok.ui.activity.VideoPlayActivity
import com.example.tiktok.ui.adapter.GridVideoAdapter
import com.example.tiktok.ui.viewmodel.RecommendViewModel
import com.example.tiktok.utils.Resource
import com.example.tiktok.utils.SwipeGestureHelper

class RecommendFragment : BaseBindingFragment<FragmentRecommendBinding>({FragmentRecommendBinding.inflate(it)}), IScrollToTop {

    private val viewModel:RecommendViewModel by viewModels()            //by viewModels()确保屏幕旋转时数据不丢失，生命周期自动管理
    //双列列表适配器
    private var adapter:GridVideoAdapter? = null
    //是否正在加载
    private var isLoading=false
    //是否是首次加载
    private var isFirstLoad = true
    //手势检测器
    private var swipeGestureHelper: SwipeGestureHelper? = null

    // 标记是否还有更多数据
    private var hasMoreData = true
    // 上次触发加载的时间戳（防抖）
    private var lastLoadTime = 0L
    // 加载间隔（毫秒）
    private val LOAD_INTERVAL = 1000L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initRecyclerView()
        setRefreshEvent()
        setupLoadMore()
        observeViewModel()
        setupSwipeGesture()

        viewModel.loadRecommendVideos(isRefresh = true)

    }

    //设置双列瀑布流布局
    private fun initRecyclerView(){

        binding.recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        //初始化适配器并绑定数据
        adapter= GridVideoAdapter(
            context=requireContext(),
            onItemClick = { video, position, itemBinding->
                // 启动带共享元素的转场动画
                startVideoPlayWithTransition(video, position, itemBinding)
            },
            onAvatarClick = { video, _ ->
                // 点击头像跳转到作者页面
                Toast.makeText(context, "进入 ${video.userBean?.nickName} 的主页", Toast.LENGTH_SHORT).show()

            },
            onLikeClick = { video, position ->
                // 点赞
                viewModel.toggleLike(video, position)
            }
        )

        //性能优化
        binding.recyclerView.adapter=adapter
        binding.recyclerView.setHasFixedSize(true)
    }

    // 启动带共享元素转场的视频播放页面
    private fun startVideoPlayWithTransition(
        video: VideoBean,
        position: Int,
        itemBinding: ItemGridvideoBinding
    ) {
        val videoList = viewModel.getCurrentVideoList()

        // 创建共享元素配对
        val coverPair = Pair.create(
            itemBinding.ivCover as View,
            "video_cover_$position"
        )

        // 创建转场动画选项
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            requireActivity(),
            coverPair
        )

        // 启动 Activity
        VideoPlayActivity.startWithTransition(
            requireContext(),
            videoList,
            position,
            options.toBundle()
        )
    }


    //设置滑动手势
    private fun setupSwipeGesture() {
        swipeGestureHelper = SwipeGestureHelper(
            context = requireContext(),
            onSwipeLeft = {
                // 推荐页已是最后一页
                Toast.makeText(context, "已经是最后一页了", Toast.LENGTH_SHORT).show()
            },
            onSwipeRight = {
                // 向右滑动，切换到同城页
                (parentFragment as? MainFragment)?.switchTab(0)
            }
        )
        swipeGestureHelper?.attachToRecyclerView(binding.recyclerView)
    }


    //下拉刷新
    private fun setRefreshEvent(){
        binding.refreshLayout.setColorSchemeResources(R.color.color_link)
        binding.refreshLayout.setOnRefreshListener {
            isFirstLoad = false
            hasMoreData = true
            viewModel.loadRecommendVideos(isRefresh = true)
        }
    }

    //上拉加载更多
    private fun setupLoadMore(){
        binding.recyclerView.addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // 只处理向上滚动
                if (dy <= 0) return

                // 防止重复触发
                if (isLoading || !hasMoreData) {
                    return
                }

                // 距离上次加载不足 1 秒则不触发
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastLoadTime < LOAD_INTERVAL) {
                    return
                }

                val layoutManager=recyclerView.layoutManager as StaggeredGridLayoutManager

                //最后一个可见项的位置
                val lastVisibleItems=IntArray(2)
                layoutManager.findLastVisibleItemPositions(lastVisibleItems)
                val lastVisibleItem=lastVisibleItems.maxOrNull()?:0
                val totalItemCount=layoutManager.itemCount

                //滚动到倒数第三行加载更多
                if(lastVisibleItem>=totalItemCount-4&&totalItemCount>0){
                    android.util.Log.d("RecommendFragment", "触发加载更多: lastVisibleItem=$lastVisibleItem, total=$totalItemCount")
                    isLoading = true
                    lastLoadTime = currentTime
                    viewModel.loadMore()
                }
            }
        })
    }

    //观察视频列表
    private fun observeViewModel() {

        viewModel.videoList.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // 首次加载（不是下拉刷新触发的）
                }
                is Resource.Success -> {
                    binding.refreshLayout.isRefreshing = false
                    isLoading = false

                    resource.data?.let { videos ->
                        adapter?.clearList()                //  刷新时清空
                        adapter?.appendList(videos)         //  添加全部数据

                        if (!isFirstLoad) {
                            Toast.makeText(context, "刷新成功", Toast.LENGTH_SHORT).show()
                        }
                        isFirstLoad = false
                    }
                }
                is Resource.Error -> {
                    binding.refreshLayout.isRefreshing = false
                    isLoading = false
                    Toast.makeText(context, resource.message ?: "加载失败", Toast.LENGTH_SHORT).show()
                }
            }
        }


        // 加载更多结果
        viewModel.loadMoreResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    android.util.Log.d("RecommendFragment", "正在加载更多...")
                }
                is Resource.Success -> {
                    resource.data?.let { newVideos ->
                        if (newVideos.isEmpty()) {
                            // 没有更多数据了
                            hasMoreData = false
                            Toast.makeText(context, "没有更多数据了", Toast.LENGTH_SHORT).show()
                        } else {
                            // 记录当前滚动位置
                            val layoutManager = binding.recyclerView.layoutManager as StaggeredGridLayoutManager

                            val firstVisibleItems = IntArray(2)
                            layoutManager.findFirstVisibleItemPositions(firstVisibleItems)
                            val firstVisiblePosition = firstVisibleItems.minOrNull() ?: 0

                            val firstView = layoutManager.findViewByPosition(firstVisiblePosition)
                            val topOffset = firstView?.top ?: 0

                            android.util.Log.d("RecommendFragment", "添加数据前: position=$firstVisiblePosition, offset=$topOffset")

                            // 添加新数据
                            adapter?.appendList(newVideos)

                            // 恢复滚动位置
                            binding.recyclerView.post {
                                layoutManager.scrollToPositionWithOffset(firstVisiblePosition, topOffset)
                                android.util.Log.d("RecommendFragment", "恢复滚动位置完成")
                            }
                            Toast.makeText(context, "加载了 ${newVideos.size} 条数据", Toast.LENGTH_SHORT).show()
                        }
                    }

                    // 延迟重置 isLoading，确保数据渲染完成
                    binding.recyclerView.postDelayed({
                        isLoading = false
                        android.util.Log.d("RecommendFragment", "加载更多完成，重置 isLoading")
                    }, 300)
                }

                is Resource.Error -> {
                    isLoading = false  //  加载失败，恢复状态
                    Toast.makeText(context, resource.message ?: "加载失败", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 观察点赞结果
        viewModel.likeResult.observe(viewLifecycleOwner) { (position, isLiked) ->
            adapter?.updateLikeStatus(position, isLiked)
            Toast.makeText(
                context,
                if (isLiked) "已点赞" else "取消点赞",
                Toast.LENGTH_SHORT
            ).show()
        }

        // 观察关注结果
        viewModel.followResult.observe(viewLifecycleOwner) { (_, isFollowed) ->
            if (isFollowed) {
                Toast.makeText(context, "已关注", Toast.LENGTH_SHORT).show()
            }
        }

        // 观察错误信息
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun scrollToTop() {
        // 添加生命周期检查
        if (!isAdded || isDetached) {
            return
        }

        // 使用 try-catch 防止崩溃
        try {
            binding.recyclerView.smoothScrollToPosition(0)
        } catch (e: IllegalArgumentException) {
            // binding 已被销毁，忽略
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        swipeGestureHelper = null
        adapter = null
    }
}
