package com.example.tiktok.ui.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.widget.ViewPager2
import com.example.tiktok.base.BaseBindingFragment
import com.example.tiktok.base.CommPagerAdapter
import com.example.tiktok.databinding.FragmentMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.collections.ArrayList
import com.google.android.material.tabs.TabLayout

class MainFragment: BaseBindingFragment<FragmentMainBinding>({FragmentMainBinding.inflate(it)}) {
    val videoPlayStateLiveData = MutableLiveData<Boolean>()

    private var sameCityFragment:SameCityFragment? = null
    private var recommendFragment:RecommendFragment? = null

    private val fragments=ArrayList<Fragment>()
    private var pagerAdapter:CommPagerAdapter? = null
    private var tabLayoutMediator:TabLayoutMediator? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragment()
        setMainMenu()
        setupSearchClick()
    }

    //设置顶部Tab和ViewPager2
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

        //预加载所有页面
        binding.viewPager.offscreenPageLimit=1

        // 启用 ViewPager2 的滑动
        binding.viewPager.isUserInputEnabled = true


        //关联TabLayout与ViewPager2
        tabLayoutMediator= TabLayoutMediator(
            binding.tabTitle,
            binding.viewPager
        ){tab,position->
            tab.text=pagerAdapter?.getPageTitle(position)
        }
        tabLayoutMediator?.attach()

        //动态设置文字居中
        binding.viewPager.post {
            for (i in 0 until binding.tabTitle.tabCount) {
                val tab = binding.tabTitle.getTabAt(i)
                val tabView = (tab?.view as? ViewGroup)?.getChildAt(1) as? TextView
                tabView?.gravity = Gravity.CENTER
            }
            binding.viewPager.setCurrentItem(1, false)

            //默认选中推荐页
            videoPlayStateLiveData.value = true
        }


        //监听页面切换
        binding.viewPager.registerOnPageChangeCallback(object :ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                curPage=position

                when (position) {
                    0 -> {
                        // 同城页：暂停视频
                        videoPlayStateLiveData.value = false
                    }
                    1 -> {
                        // 推荐页：播放视频
                        videoPlayStateLiveData.value = true
                    }
                }
            }
        })

        //监听Tab重复点击
        binding.tabTitle.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {
                scrollToTop(tab?.position?:0)
            }
        })
    }

    //切换到指定 Tab
    fun switchTab(position: Int) {
        if (position in 0 until fragments.size) {
            binding.viewPager.setCurrentItem(position, true)  // true = 平滑滚动
        }
    }

    //滚动到顶部
    private fun scrollToTop(position:Int){
        val tag = "f$position"
        val fragment = childFragmentManager.findFragmentByTag(tag)

        // 检查 Fragment 是否存在且已添加
        if (fragment is IScrollToTop && fragment.isAdded && !fragment.isDetached) {
            fragment.scrollToTop()
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
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right,
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
            .replace(android.R.id.content, personalHomeFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setupSearchClick(){
        binding.ivSearch.setOnClickListener{
            Toast.makeText(context, "搜索功能待实现", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tabLayoutMediator?.detach()
        tabLayoutMediator=null
        pagerAdapter=null
        sameCityFragment=null
        recommendFragment=null
    }

    companion object {
        // 当前页码（默认推荐页，索引为1）
        var curPage = 1
    }
}
