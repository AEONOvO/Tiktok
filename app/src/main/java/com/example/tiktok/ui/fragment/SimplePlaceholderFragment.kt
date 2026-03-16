package com.example.tiktok.ui.fragment

import android.os.Bundle
import android.view.View
import com.example.tiktok.base.BaseBindingFragment
import com.example.tiktok.databinding.FragmentSimplePlaceholderBinding

class SimplePlaceholderFragment : BaseBindingFragment<FragmentSimplePlaceholderBinding>({ FragmentSimplePlaceholderBinding.inflate(it) }) {

    companion object {
        private const val KEY_TITLE = "key_title"

        fun newInstance(title: String): SimplePlaceholderFragment {
            return SimplePlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_TITLE, title)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvTitle.text = arguments?.getString(KEY_TITLE) ?: ""
    }
}
