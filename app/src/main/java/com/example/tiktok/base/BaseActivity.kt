package com.example.tiktok.base

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar

abstract class BaseActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }
    protected  abstract fun init()

    //状态栏颜色
    protected fun setSystemBarColor(color:Int){
        ImmersionBar.with(this).statusBarColor(color).init()
    }

    //去除状态栏
    protected fun hideStatusBar(){
        ImmersionBar.with(this).hideBar(BarHide.FLAG_HIDE_STATUS_BAR).init()
    }

    //保持屏幕常亮
    protected fun keepScreenOn() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    //设置退出动画
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    protected fun setExitAnimation(animId: Int) {
        overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE,0,animId)
    }

    //设置全屏
    protected fun setFullScreen() {
        ImmersionBar.with(this).init()
    }
}
