package com.example.tiktok.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.tiktok.ui.activity.VideoPlayActivity
import com.example.tiktok.databinding.FragmentPersonalLikeBinding
import com.example.tiktok.data.model.VideoBean
import com.example.tiktok.ui.adapter.LikeVideoGridAdapter
import com.example.tiktok.utils.DataCreate

class PersonalLikeFragment : Fragment() {

    private var _binding: FragmentPersonalLikeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: LikeVideoGridAdapter
    private val videoList = mutableListOf<VideoBean>()
    private var downY = 0f
    private var isStretching = false

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
        ) { _, position ->
            openVideoPlay(position)
        }

        binding.recyclerView.adapter = adapter

        // 防止瀑布流跳动
        binding.recyclerView.itemAnimator = null

        binding.recyclerView.setOnTouchListener { v, event ->
            val homeFragment = parentFragment as? PersonalHomeFragment
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downY = event.rawY
                    isStretching = false
                }

                MotionEvent.ACTION_MOVE -> {
                    val dy = event.rawY - downY
                    val reachTop = !v.canScrollVertically(-1)
                    if (reachTop && dy > 0f) {
                        isStretching = true
                        homeFragment?.applyBackgroundStretch(dy)
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isStretching) {
                        homeFragment?.resetBackgroundStretch()
                        isStretching = false
                    }
                }
            }
            false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadData() {
        val mockData = createMockData()
        videoList.clear()
        videoList.addAll(mockData)
        adapter.notifyDataSetChanged()

        // 显示/隐藏空状态
        binding.tvEmpty.visibility = if (videoList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun createMockData(): List<VideoBean> {
        val source = DataCreate.datas.filter { it.videoRes.isNotBlank() }
        if (source.isEmpty()) {
            return emptyList()
        }

        val type = arguments?.getInt(ARG_TYPE, TYPE_WORKS) ?: TYPE_WORKS
        val list = when (type) {
            TYPE_WORKS -> source.take(18)
            TYPE_RECOMMEND -> source.shuffled().take(18)
            TYPE_COLLECT -> source.filter { it.isCollected }.ifEmpty { source.shuffled().take(18) }
            TYPE_LIKE -> source.filter { it.isLiked }.ifEmpty { source.shuffled().take(18) }
            else -> source.take(18)
        }

        return list.mapIndexed { index, video ->
            video.copy(videoId = video.videoId + index + type * 1000)
        }
    }

    private fun openVideoPlay(position: Int) {
        if (position !in videoList.indices) {
            return
        }
        VideoPlayActivity.startWithTransition(
            requireContext(),
            ArrayList(videoList),
            position,
            null
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
