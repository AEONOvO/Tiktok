package com.example.tiktok.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.tiktok.ui.fragment.PersonalLikeFragment

class PersonalHomePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val titles = arrayOf("作品", "推荐", "收藏", "喜欢")

    override fun getItemCount(): Int = titles.size

    override fun createFragment(position: Int): Fragment {
        return PersonalLikeFragment.newInstance(position)
    }

    fun getPageTitle(position: Int): String {
        return titles[position]
    }
}