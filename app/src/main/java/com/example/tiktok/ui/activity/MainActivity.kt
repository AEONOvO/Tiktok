package com.example.tiktok.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.tiktok.base.BaseBindingActivity
import com.example.tiktok.base.CommPagerAdapter
import com.example.tiktok.databinding.ActivityMainBinding
import com.example.tiktok.ui.fragment.HomeFragment
import com.example.tiktok.ui.fragment.IScrollToTop
import com.example.tiktok.ui.fragment.MessageFragment
import com.example.tiktok.ui.fragment.PersonalHomeFragment
import com.example.tiktok.ui.fragment.SimplePlaceholderFragment
import com.example.tiktok.utils.DataCreate
import com.google.android.material.tabs.TabLayout
import kotlin.collections.ArrayList

class MainActivity:BaseBindingActivity<ActivityMainBinding>({ActivityMainBinding.inflate(it)}) {
    private val mainFragments = ArrayList<Fragment>()
    private var pagerAdapter: CommPagerAdapter? = null

    private var lastTime:Long=0     //上次按返回键的时间戳
    private val exitTime=2000       //两次按键间隔时间

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化数据
        initializeData()
    }

    //UI
    override fun init() {
        setupMainPager()
        setupBottomMenu()
        setupBackPressed()
    }

    private fun setupMainPager() {
        mainFragments.clear()
        mainFragments.add(HomeFragment())
        mainFragments.add(SimplePlaceholderFragment.newInstance("朋友页面待实现"))
        mainFragments.add(SimplePlaceholderFragment.newInstance("拍摄功能待实现"))
        mainFragments.add(MessageFragment())
        mainFragments.add(PersonalHomeFragment())

        pagerAdapter = CommPagerAdapter(
            supportFragmentManager,
            lifecycle,
            mainFragments,
            arrayOf("首页", "朋友", "", "消息", "我")
        )
        binding.viewPagerMain.adapter = pagerAdapter
        binding.viewPagerMain.offscreenPageLimit = 5
        binding.viewPagerMain.isUserInputEnabled = false
        binding.viewPagerMain.setCurrentItem(0, false)
    }

    private fun setupBottomMenu() {
        with(binding.tabMainMenu) {
            addTab(newTab().setText("首页"))
            addTab(newTab().setText("朋友"))
            addTab(newTab().setText(""))
            addTab(newTab().setText("消息"))
            addTab(newTab().setText("我"))

            getTabAt(0)?.select()

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val position = tab?.position ?: 0
                    if (position == 2) {
                        Toast.makeText(this@MainActivity, "拍摄功能待实现", Toast.LENGTH_SHORT).show()
                    }
                    binding.viewPagerMain.setCurrentItem(position, false)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    val position = tab?.position ?: 0
                    binding.viewPagerMain.setCurrentItem(position, false)
                    (mainFragments.getOrNull(position) as? IScrollToTop)?.scrollToTop()
                }
            })
        }
    }

    //初始化数据
    private fun initializeData() {
        try {
            DataCreate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //双击退出
    private fun setupBackPressed(){
        onBackPressedDispatcher.addCallback(this,object :OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if(System.currentTimeMillis()-lastTime>exitTime){
                    Toast.makeText(applicationContext,"再按一次退出",Toast.LENGTH_SHORT).show()
                    lastTime=System.currentTimeMillis()
                }else{
                    finish()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        pagerAdapter = null
        mainFragments.clear()
    }
}
