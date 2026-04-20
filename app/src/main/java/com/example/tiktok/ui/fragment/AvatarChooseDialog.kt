package com.example.tiktok.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tiktok.databinding.DialogAvatarChooseBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AvatarChooseDialog : BottomSheetDialogFragment() {

    private var _binding: DialogAvatarChooseBinding? = null
    private val binding get() = _binding!!
    private var listener: OnChooseListener? = null

    interface OnChooseListener {
        fun onCamera()
        fun onGallery()
    }

    fun setOnChooseListener(listener: OnChooseListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAvatarChooseBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 视图创建，绑定按钮点击事件
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 拍照按钮
        binding.tvCamera.setOnClickListener {
            listener?.onCamera()
            //关掉弹窗
            dismiss()
        }

        // 相册按钮
        binding.tvGallery.setOnClickListener {
            listener?.onGallery()
            dismiss()
        }

        // 取消按钮
        binding.tvCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}