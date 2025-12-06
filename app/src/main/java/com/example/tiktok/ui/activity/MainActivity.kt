package com.example.tiktok.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.example.tiktok.R
import com.example.tiktok.base.BaseBindingActivity
import com.example.tiktok.databinding.ActivityMainBinding
import com.example.tiktok.ui.fragment.MainFragment
import com.example.tiktok.utils.DataCreate

class MainActivity:BaseBindingActivity<ActivityMainBinding>({ActivityMainBinding.inflate(it)}) {
    private val mainFragment= MainFragment()
    private var lastTime:Long=0
    private val exitTime=2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化数据
        initializeData()
    }

    override fun init() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, mainFragment)
            .commit()

        setupBackPressed()
    }

     //初始化数据
    private fun initializeData() {
        try {
            DataCreate()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "数据初始化失败: ${e.message}", Toast.LENGTH_LONG).show()
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
}