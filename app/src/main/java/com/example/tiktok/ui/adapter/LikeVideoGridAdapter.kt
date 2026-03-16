package com.example.tiktok.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.example.tiktok.R
import com.example.tiktok.databinding.ItemGridVideoLikeBinding
import com.example.tiktok.data.model.VideoBean
import java.util.Locale

class LikeVideoGridAdapter(
    private val videoList: List<VideoBean>,
    private val onItemClick: (VideoBean, Int) -> Unit
) : RecyclerView.Adapter<LikeVideoGridAdapter.VideoGridViewHolder>() {

    inner class VideoGridViewHolder(val binding: ItemGridVideoLikeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(video: VideoBean, position: Int) {
            if (video.coverRes != 0) {
                Glide.with(binding.ivCover)
                    .load(video.coverRes)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.default_error)
                    .into(binding.ivCover)
            } else {
                Glide.with(binding.ivCover)
                    .asBitmap()
                    .load(video.videoRes)
                    .apply(
                        RequestOptions()
                            .frame(0)
                            .placeholder(R.drawable.loading)
                            .error(R.drawable.default_error)
                    )
                    .into(binding.ivCover)
            }

            // 显示点赞数（修改为使用 tv_like_count）
            binding.tvLikeCount.text = formatCount(video.likeCount)

            // 点击事件
            binding.root.setOnClickListener {
                val currentPosition = bindingAdapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onItemClick(videoList[currentPosition], currentPosition)
                }
            }
        }

        private fun formatCount(count: Int): String {
            return when {
                count < 1000 -> count.toString()
                count < 10000 -> String.format(Locale.US, "%.1fk", count / 1000.0)
                else -> String.format(Locale.US, "%.1fw", count / 10000.0)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoGridViewHolder {
        val binding = ItemGridVideoLikeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VideoGridViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoGridViewHolder, position: Int) {
        holder.bind(videoList[position], position)
    }

    override fun getItemCount(): Int = videoList.size
}
