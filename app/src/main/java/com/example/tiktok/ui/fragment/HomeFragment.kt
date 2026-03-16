package com.example.tiktok.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.tiktok.base.BaseBindingFragment
import com.example.tiktok.base.CommPagerAdapter
import com.example.tiktok.databinding.FragmentHomeBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.collections.ArrayList

class HomeFragment : BaseBindingFragment<FragmentHomeBinding>({ FragmentHomeBinding.inflate(it) }), IScrollToTop {
    private var sameCityFragment: SameCityFragment? = null
    private var recommendFragment: RecommendFragment? = null

    private val fragments = ArrayList<Fragment>()
    private var pagerAdapter: CommPagerAdapter? = null
    private var tabLayoutMediator: TabLayoutMediator? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHomePager()
    }

    private fun setupHomePager() {
        sameCityFragment = SameCityFragment()
        recommendFragment = RecommendFragment()

        fragments.clear()
        fragments.add(sameCityFragment!!)
        fragments.add(recommendFragment!!)

        pagerAdapter = CommPagerAdapter(
            childFragmentManager,
            viewLifecycleOwner.lifecycle,
            fragments,
            arrayOf("同城", "推荐")
        )
        binding.viewPager.adapter = pagerAdapter

        tabLayoutMediator = TabLayoutMediator(
            binding.tabTitle,
            binding.viewPager
        ) { tab, position ->
            tab.text = pagerAdapter?.getPageTitle(position)
        }
        tabLayoutMediator?.attach()

        binding.viewPager.post {
            binding.viewPager.setCurrentItem(1, false)
        }

        binding.tabTitle.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {
                scrollInnerToTop(tab?.position ?: 0)
            }
        })
    }

    private fun scrollInnerToTop(position: Int) {
        val tag = "f$position"
        val fragment = childFragmentManager.findFragmentByTag(tag)

        when (position) {
            0 -> (fragment as? SameCityFragment)?.scrollToTop()
            1 -> (fragment as? RecommendFragment)?.scrollToTop()
        }
    }

    override fun scrollToTop() {
        scrollInnerToTop(binding.viewPager.currentItem)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
        pagerAdapter = null
        sameCityFragment = null
        recommendFragment = null
    }
}
