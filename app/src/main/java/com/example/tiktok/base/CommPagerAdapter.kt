package com.example.tiktok.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.lifecycle.Lifecycle


class CommPagerAdapter(
    fragmentManager: FragmentManager,                // Fragment 管理器
    lifecycle: Lifecycle,                            // 生命周期
    private val items: ArrayList<out Fragment>,      // 页面列表
    private val titles: Array<String>                // 标题列表
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun createFragment(position: Int): Fragment {
        return items[position]
    }

    //获取页面标题
    fun getPageTitle(position: Int):CharSequence?{
        return if (position<titles.size) titles[position] else null
    }
}