package com.example.tiktok.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.tiktok.R
import com.example.tiktok.databinding.FragmentPersonalLikeBinding
import com.example.tiktok.data.model.VideoBean
import com.example.tiktok.ui.adapter.LikeVideoGridAdapter

class PersonalLikeFragment : Fragment() {

    private var _binding: FragmentPersonalLikeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: LikeVideoGridAdapter
    private val videoList = mutableListOf<VideoBean>()

    companion object {
        private const val ARG_TYPE = "type"

        @Suppress("unused")
        private const val TYPE_WORKS = 0      // 作品
        @Suppress("unused")
        private const val TYPE_RECOMMEND = 1  // 推荐
        @Suppress("unused")
        private const val TYPE_COLLECT = 2    // 收藏
        @Suppress("unused")
        private const val TYPE_LIKE = 3       // 喜欢

        fun newInstance(type: Int): PersonalLikeFragment {
            return PersonalLikeFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TYPE, type)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalLikeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadData()
    }

    private fun setupRecyclerView() {
        // 设置三列瀑布流布局
        val layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        binding.recyclerView.layoutManager = layoutManager

        // 初始化适配器
        adapter = LikeVideoGridAdapter(
            videoList,
        ) { video, _ ->
            // 点击视频项
            openVideoPlay(video)
        }

        binding.recyclerView.adapter = adapter

        // 防止瀑布流跳动
        binding.recyclerView.itemAnimator = null
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadData() {
        // 这里用模拟数据
        val mockData = createMockData()
        videoList.clear()
        videoList.addAll(mockData)
        adapter.notifyDataSetChanged()

        // 显示/隐藏空状态
        binding.tvEmpty.visibility = if (videoList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun createMockData(): List<VideoBean> {
        // 模拟数据（不同高度模拟瀑布流效果）
        return List(20) { index ->
            VideoBean(
                videoId = index,
                coverRes = R.drawable.default_cover,
                videoRes = "android.resource://${requireContext().packageName}/raw/video_${index % 3}",
                likeCount = (1000..100000).random(),
                commentCount = (100..10000).random(),
                shareCount = (50..5000).random(),
                collectCount = (100..10000).random(),
                content = "视频标题 $index",
                isLiked = false,
                isCollected = false
            )
        }
    }

    private fun openVideoPlay(video: VideoBean) {
        android.util.Log.d("VideoListFragment", "点击视频: ${video.videoId}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}