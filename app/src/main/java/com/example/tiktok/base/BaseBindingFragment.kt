package com.example.tiktok.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseBindingFragment<VB : ViewBinding>(
    val block: (LayoutInflater) -> VB
) : Fragment() {


    //创建内部存储的 binding 实例（可空），用于在生命周期内管理 binding 的创建与销毁
    private var _binding:VB?=null

    // 添加检查，避免在 Fragment 销毁后访问 binding
    protected val binding: VB
        get() = _binding ?: throw IllegalArgumentException("The property of binding has been destroyed.")

    // 添加一个安全的 binding 访问方法
    protected fun getBindingSafely(): VB? = _binding

        //重写 Fragment 视图创建方法，初始化 ViewBinding 并返回根视图
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            _binding=block(inflater)
            return binding.root
        }

        // 视图销毁时释放 binding，防止内存泄漏
        override fun onDestroyView() {
            super.onDestroyView()
            _binding=null
        }
}
