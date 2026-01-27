package com.example.tiktok.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.tiktok.R
import com.example.tiktok.base.BaseBindingFragment
import com.example.tiktok.base.CommPagerAdapter
import com.example.tiktok.databinding.FragmentMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.collections.ArrayList
import com.google.android.material.tabs.TabLayout

class MainFragment: BaseBindingFragment<FragmentMainBinding>({FragmentMainBinding.inflate(it)}) {
    private var sameCityFragment:SameCityFragment? = null
    private var recommendFragment:RecommendFragment? = null

    private val fragments=ArrayList<Fragment>()
    private var pagerAdapter:CommPagerAdapter? = null
    private var tabLayoutMediator:TabLayoutMediator? = null

    //设置监听器、初始化 UI
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragment()
        setMainMenu()
    }

    //设置顶部Tab和ViewPager2实现左右滑动
    private fun setFragment(){

        sameCityFragment=SameCityFragment()
        recommendFragment=RecommendFragment()


        fragments.add(sameCityFragment!!)
        fragments.add(recommendFragment!!)

        //设置适配器
        pagerAdapter= CommPagerAdapter(
            childFragmentManager,
            viewLifecycleOwner.lifecycle,
            fragments,
            arrayOf("同城","推荐")
        )
        binding.viewPager.adapter=pagerAdapter

        //关联TabLayout与ViewPager2
        tabLayoutMediator= TabLayoutMediator(
            binding.tabTitle,
            binding.viewPager
        ){tab,position->
            tab.text=pagerAdapter?.getPageTitle(position)
        }

        //显示 Tab 标题
        tabLayoutMediator?.attach()

        // 设置默认选中推荐页
        binding.viewPager.post {
            binding.viewPager.setCurrentItem(1, false)
        }

        //监听Tab重复点击
        binding.tabTitle.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            // 已选中的 Tab 再次点击
            override fun onTabReselected(tab: TabLayout.Tab?) {
                scrollToTop(tab?.position?:0)
            }
        })
    }

    //滚动到顶部
    private fun scrollToTop(position:Int){
        val tag = "f$position"
        val fragment = childFragmentManager.findFragmentByTag(tag)

        when (position) {
            0 -> (fragment as? SameCityFragment)?.scrollToTop()
            1 -> (fragment as? RecommendFragment)?.scrollToTop()
        }
    }

    //下栏菜单
    private fun setMainMenu(){
        with(binding.tabMainMenu){
            addTab(newTab().setText("首页"))
            addTab(newTab().setText("朋友"))
            addTab(newTab().setText(""))
            addTab(newTab().setText("消息"))
            addTab(newTab().setText("我"))

            //默认选中第 1 个按钮
            getTabAt(0)?.select()

            //监听底部Tab切换
            addOnTabSelectedListener(object:TabLayout.OnTabSelectedListener{
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    when(tab?.position){

                        0 -> onHomeClick()      // 首页
                        1 -> onFriendClick()    // 朋友
                        2 -> onPlusClick()      // 中间加号
                        3 -> onMessageClick()   // 消息
                        4 -> onMineClick()      // 我
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
    }

    private fun onHomeClick(){
        //当前就在首页，无需处理
    }

    private fun onFriendClick(){
        Toast.makeText(context, "朋友页面待实现", Toast.LENGTH_SHORT).show()
        binding.tabMainMenu.getTabAt(0)?.select()
    }

    private fun onPlusClick() {
        Toast.makeText(context, "拍摄功能待实现", Toast.LENGTH_SHORT).show()
        binding.tabMainMenu.getTabAt(0)?.select()
    }

    private fun onMessageClick() {
        Toast.makeText(context, "消息页面待实现", Toast.LENGTH_SHORT).show()
        binding.tabMainMenu.getTabAt(0)?.select()
    }

    private fun onMineClick() {
        val personalHomeFragment = PersonalHomeFragment()

        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,    // 新页面从右侧到左侧滑入
                R.anim.slide_out_left,    // 旧页面向左侧到右侧滑出
                R.anim.slide_in_left,     // 返回时，旧页面从左边滑回来
                R.anim.slide_out_right    // 返回时，新页面向右边滑出去
            )
            .replace(android.R.id.content, personalHomeFragment)
            .addToBackStack(null)           // 加入回退栈
            .commit()

        val listener = object : FragmentManager.OnBackStackChangedListener {
            override fun onBackStackChanged() {
                // 当返回栈为空时
                if (parentFragmentManager.backStackEntryCount == 0) {
                    // 恢复底部导航栏到"首页"
                    binding.tabMainMenu.getTabAt(0)?.select()
                    // 移除监听器（避免重复触发）
                    parentFragmentManager.removeOnBackStackChangedListener(this)
                }
            }
        }

        parentFragmentManager.addOnBackStackChangedListener(listener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tabLayoutMediator?.detach()
        tabLayoutMediator=null
        pagerAdapter=null
        sameCityFragment=null
        recommendFragment=null
    }
}
