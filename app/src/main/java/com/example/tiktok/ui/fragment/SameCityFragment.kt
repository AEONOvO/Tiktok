package com.example.tiktok.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.tiktok.R
import com.example.tiktok.base.BaseBindingFragment
import com.example.tiktok.data.model.VideoBean
import com.example.tiktok.databinding.FragmentSameCityBinding
import com.example.tiktok.ui.adapter.SameCityVideoAdapter
import com.example.tiktok.utils.DataCreate
import kotlin.random.Random

class SameCityFragment : BaseBindingFragment<FragmentSameCityBinding>({ FragmentSameCityBinding.inflate(it)}), IScrollToTop {

    private var adapter: SameCityVideoAdapter? = null
    private var isLoading = false
    private var hasMoreData = true
    private var lastLoadTime = 0L
    private val loadInterval = 1000L

    companion object {
        private const val PAGE_SIZE = 20
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        setRefreshEvent()
        setupLoadMore()
        loadData()
    }

    private fun initRecyclerView() {
        binding.recyclerView.layoutManager = StaggeredGridLayoutManager(
            2,
            StaggeredGridLayoutManager.VERTICAL
        )

        adapter = SameCityVideoAdapter(
            context = requireContext(),
            onItemClick = { _, _ ->
            },
            onAvatarClick = { _, _ ->
            },
            onLikeClick = { video, position ->
                video.isLiked = !video.isLiked
                adapter?.updateLikeStatus(position, video.isLiked)
            }
        )

        binding.recyclerView.adapter = adapter
        binding.recyclerView.setHasFixedSize(true)
    }

    private fun setRefreshEvent() {
        binding.refreshLayout.setColorSchemeResources(R.color.color_link)
        binding.refreshLayout.setOnRefreshListener {
            hasMoreData = true
            loadData()
        }
    }

    private fun setupLoadMore() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy <= 0) return
                if (isLoading || !hasMoreData) return

                val currentTime = System.currentTimeMillis()
                if (currentTime - lastLoadTime < loadInterval) return

                val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
                val lastVisibleItems = IntArray(2)
                layoutManager.findLastVisibleItemPositions(lastVisibleItems)
                val lastVisibleItem = lastVisibleItems.maxOrNull() ?: 0
                val totalItemCount = layoutManager.itemCount

                if (lastVisibleItem >= totalItemCount - 4 && totalItemCount > 0) {
                    isLoading = true
                    lastLoadTime = currentTime
                    loadMoreData()
                }
            }
        })
    }

    private fun loadData() {
        binding.refreshLayout.isRefreshing = true
        isLoading = true

        binding.recyclerView.postDelayed({
            val mockData = createMockData()
            adapter?.clearList()
            adapter?.appendList(mockData)

            binding.refreshLayout.isRefreshing = false
            isLoading = false
        }, 500)
    }

    private fun loadMoreData() {
        binding.recyclerView.postDelayed({
            val mockData = createMockData()

            if (mockData.isEmpty()) {
                hasMoreData = false
                isLoading = false
                Toast.makeText(context, "没有更多数据了", Toast.LENGTH_SHORT).show()
                return@postDelayed
            }

            adapter?.appendList(mockData)
            isLoading = false
        }, 500)
    }

    // 基于 DataCreate 生成随机打乱的模拟数据
    private fun createMockData(): List<VideoBean> {
        val originalData = DataCreate.datas

        if (originalData.isEmpty()) {
            return emptyList()
        }

        // 完全随机生成索引序列
        val randomIndices = List(PAGE_SIZE) {
            Random.nextInt(originalData.size)
        }

        return randomIndices.mapIndexed { index, randomIndex ->
            val original = originalData[randomIndex]

            // 创建副本并随机化属性
            VideoBean(
                videoId = System.currentTimeMillis().toInt() + index + Random.nextInt(0, 10000),
                videoRes = original.videoRes,
                coverRes = original.coverRes,
                content = original.content,
                isLiked = false,
                isCollected = false,
                isPrivate = original.isPrivate,
                distance = randomFloat(),
                likeCount = original.likeCount + Random.nextInt(0, 5001),
                commentCount = original.commentCount + Random.nextInt(0, 1001),
                collectCount = original.collectCount + Random.nextInt(0, 501),
                shareCount = original.shareCount + Random.nextInt(0, 201),
                userBean = original.userBean
            )
        }
    }

    // 生成随机 Float
    private fun randomFloat(): Float {
        return Random.nextFloat() * (20f - 0.1f) + 0.1f
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
    }

    //滚动到顶部
    override fun scrollToTop() {
        if (!isAdded || isDetached) {
            return
        }

        binding.recyclerView.smoothScrollToPosition(0)
    }
}