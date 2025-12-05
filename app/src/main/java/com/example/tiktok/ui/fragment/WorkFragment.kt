package com.example.tiktok.ui.fragment

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.GridLayoutManager // 引入网格布局
import com.example.tiktok.base.BaseBindingFragment
import com.example.tiktok.data.model.VideoBean
import com.example.tiktok.databinding.FragmentWorkBinding
import com.example.tiktok.databinding.ItemGridvideoBinding
import com.example.tiktok.ui.activity.VideoPlayActivity
import com.example.tiktok.ui.adapter.WorkVideoAdapter
import com.example.tiktok.ui.viewmodel.WorkViewModel
import com.example.tiktok.utils.Resource
import com.example.tiktok.utils.SwipeGestureHelper
class WorkFragment: BaseBindingFragment<FragmentWorkBinding>({FragmentWorkBinding.inflate(it)}), IScrollToTop {
    private val viewModel: WorkViewModel by viewModels()
    //列表适配器
    private var adapter:WorkVideoAdapter? = null
    //是否正在加载
    private var isLoading=false
    //是否是首次加载
    private var isFirstLoad = true
    //手势检测器
    private var swipeGestureHelper: SwipeGestureHelper? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        observeViewModel()
        setupSwipeGesture()
        viewModel.loadRecommendVideos(isRefresh = true)
    }

    // 设置三列对齐网格布局
    private fun initRecyclerView(){
        // 修改为 GridLayoutManager，跨度为 3
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        //初始化适配器并绑定数据
        adapter= WorkVideoAdapter(
            context=requireContext(),
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

    //观察视频列表
    private fun observeViewModel() {
        viewModel.videoList.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // 首次加载
                }
                is Resource.Success -> {
                    // 移除了 refreshLayout.isRefreshing 的设置
                    isLoading = false

                    resource.data?.let { videos ->
                        adapter?.clearList()
                        adapter?.appendList(videos)

                        if (!isFirstLoad) {
                            // Toast.makeText(context, "加载成功", Toast.LENGTH_SHORT).show()
                        }
                        isFirstLoad = false
                    }
                }
                is Resource.Error -> {
                    // 移除了 refreshLayout.isRefreshing 的设置
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
                            Toast.makeText(context, "没有更多数据了", Toast.LENGTH_SHORT).show()
                        } else {
                            // 记录当前滚动位置
                            // 修改类型转换为 GridLayoutManager
                            val layoutManager = binding.recyclerView.layoutManager as GridLayoutManager

                            // GridLayoutManager 直接获取第一个可见位置
                            val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()

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