package com.example.tiktok.ui.view

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvCamera.setOnClickListener {
            listener?.onCamera()
            dismiss()
        }

        binding.tvGallery.setOnClickListener {
            listener?.onGallery()
            dismiss()
        }

        binding.tvCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}