package com.example.tiktok.base

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding

abstract class BaseBindingActivity<VB : ViewBinding>(
    val block: (LayoutInflater) -> VB
) : BaseActivity() {

    //私有可空的 ViewBinding 实例
    private var _binding: VB? = null

    //对外暴露的非空 ViewBinding 访问器
    protected val binding: VB
        get() = requireNotNull(_binding) { "The property of binding has been destroyed." }

    //重写 Activity 的 onCreate 方法，初始化 ViewBinding 并设置布局
    override fun onCreate(savedInstanceState: Bundle?) {
        _binding = block(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }

    //重写 Activity 的 onDestroy 方法，清理 ViewBinding 实例，防止内存泄漏
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    //兼容新旧 API 的 getParcelableExtra
    protected inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(key: String): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(key) as? T
        }
    }

    //容新旧 API 的 getParcelableArrayListExtra
    protected inline fun <reified T : Parcelable> Intent.getParcelableArrayListExtraCompat(key: String): ArrayList<T>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableArrayListExtra(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableArrayListExtra(key)
        }
    }

    //兼容新旧 API 的 getSerializableExtra
    protected inline fun <reified T : java.io.Serializable> Intent.getSerializableExtraCompat(key: String): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSerializableExtra(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            getSerializableExtra(key) as? T
        }
    }
}