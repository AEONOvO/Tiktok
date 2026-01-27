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
import com.example.tiktok.databinding.FragmentRecommendBinding
import com.example.tiktok.databinding.ItemGridVideoBinding
import com.example.tiktok.ui.activity.VideoPlayActivity
import com.example.tiktok.ui.adapter.GridVideoAdapter
import com.example.tiktok.viewmodel.RecommendViewModel
import com.example.tiktok.utils.Resource

class RecommendFragment : BaseBindingFragment<FragmentRecommendBinding>({FragmentRecommendBinding.inflate(it)}), IScrollToTop {

    //懒加载创建和获取 ViewModel 实例
    private val viewModel: RecommendViewModel by viewModels()
    //双列列表适配器
    private var adapter:GridVideoAdapter? = null
    //是否正在加载
    private var isLoading=false
    //是否是首次加载
    private var isFirstLoad = true
    // 标记是否还有更多数据
    private var hasMoreData = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadRecommendVideos(isRefresh = true) //首次加载视频
        initRecyclerView()      //设置 RecyclerView（双列瀑布流）
        setRefreshEvent()       //设置下拉刷新
        setupLoadMore()         //设置上拉加载更多
        observeViewModel()      //观察 ViewModel 数据变化

    }

    //设置双列瀑布流布局
    private fun initRecyclerView(){

        binding.recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        //初始化适配器并绑定数据
        adapter= GridVideoAdapter(
            context=requireContext(),
            onItemClick = { _, position, itemBinding->
                // 启动带共享元素的转场动画
                startVideoPlayWithTransition(position, itemBinding)
            },

        )

        binding.recyclerView.adapter=adapter
        binding.recyclerView.setHasFixedSize(true)      //RecyclerView宽高固定
    }

    // 启动带共享元素转场的视频播放页面
    private fun startVideoPlayWithTransition(position: Int, itemBinding: ItemGridVideoBinding) {
        // 获取完整视频列表
        val videoList = viewModel.getCurrentVideoList()

        // 创建共享元素配对
        val coverPair = Pair.create(itemBinding.ivCover as View, "video_cover_$position")

        // 创建转场动画选项
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            requireActivity(),
            coverPair
        )

        // 启动 Activity
        VideoPlayActivity.startWithTransition(
            requireContext(),
            videoList,           // 完整视频列表
            position,            // 当前点击的视频位置
            options.toBundle()   // 将动画配置传递给目标 Activity
        )
    }

    //下拉刷新
    private fun setRefreshEvent(){
        // 设置进度条颜色
        binding.refreshLayout.setColorSchemeResources(R.color.color_link)
        // 设置下拉刷新监听器
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

                if (dy <= 0 || isLoading || !hasMoreData) {
                    return
                }

                // 获取布局管理器，获取位置信息
                val layoutManager=recyclerView.layoutManager as StaggeredGridLayoutManager

                // 获取每一列最后可见的 item 位置
                val lastVisibleItems = IntArray(layoutManager.spanCount)
                layoutManager.findLastVisibleItemPositions(lastVisibleItems)

                // 取最大值，代表整个列表视觉上的最底部位置
                val lastVisibleItemPosition = lastVisibleItems.maxOrNull() ?: 0
                val totalItemCount = layoutManager.itemCount

                // 触发加载：倒数第 4 个时触发
                if (lastVisibleItemPosition >= totalItemCount - 4 && totalItemCount > 0) {
                    isLoading = true        // 立即上锁
                    viewModel.loadMore()
                }
            }
        })
    }

    //观察视频列表
    private fun observeViewModel() {

        //下拉刷新
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

                }
                is Resource.Success -> {
                    val newVideos = resource.data ?: emptyList()

                    if (newVideos.isEmpty()) {
                        // 数据耗尽
                        hasMoreData = false
                        Toast.makeText(context, "没有更多数据了", Toast.LENGTH_SHORT).show()
                    } else {
                        // 直接追加数据
                        adapter?.appendList(newVideos)
                    }
                    // 立即解锁
                    isLoading = false
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

    //滚动到顶部
    override fun scrollToTop() {
        // 添加生命周期检查
        if (!isAdded || isDetached) {
            return
        }

        binding.recyclerView.smoothScrollToPosition(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
    }
}
