package com.example.tiktok.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.tiktok.R
import com.example.tiktok.data.model.VideoBean
import com.example.tiktok.databinding.ItemGridVideoSameCityBinding
import java.util.Locale

class SameCityVideoAdapter(
    private val context: Context,
    private val onItemClick: ((VideoBean, Int, ItemGridVideoSameCityBinding) -> Unit)? = null,
    private val onAvatarClick: ((VideoBean, Int) -> Unit)? = null,
    private val onLikeClick: ((VideoBean, Int) -> Unit)? = null
) : RecyclerView.Adapter<SameCityVideoAdapter.ViewHolder>() {

    private val videoList = mutableListOf<VideoBean>()

    inner class ViewHolder(val binding: ItemGridVideoSameCityBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGridVideoSameCityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val video = videoList[position]
        val binding = holder.binding

        // 加载视频封面
        loadVideoCover(video, holder)

        // 设置距离
        binding.tvDistance.text = formatDistance(video.distance)

        // 设置内容
        binding.tvContent.text = video.content?.ifEmpty { "精彩视频内容" } ?: "精彩视频内容"

        // 加载作者头像
        loadAuthorAvatar(video, holder)

        // 设置用户名
        binding.tvAuthorName.text = video.userBean?.nickName ?: "抖音用户"

        // 设置点赞数
        binding.tvLikeCount.text = formatLikeCount(video.likeCount)
        ViewCompat.setTransitionName(binding.ivCover, "video_cover_$position")

        // 点击事件
        binding.root.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                onItemClick?.invoke(video, currentPosition, holder.binding)
            }
        }

        binding.llAuthor.setOnClickListener {
            onAvatarClick?.invoke(video, position)
        }

        binding.llLike.setOnClickListener {
            onLikeClick?.invoke(video, position)
        }
    }

    override fun getItemCount(): Int = videoList.size

    // 加载视频封面
    private fun loadVideoCover(video: VideoBean, holder: ViewHolder) {
        if (video.coverRes != 0) {
            // 有封面图，直接加载
            Glide.with(context)
                .load(video.coverRes)
                .placeholder(R.drawable.loading)
                .error(R.drawable.default_error)
                .centerCrop()
                .into(holder.binding.ivCover)
        } else {
            // 没有封面图，从视频中提取第一帧
            Glide.with(context)
                .asBitmap()
                .load(video.videoRes)
                .apply(
                    RequestOptions()
                        .frame(0)  // 提取第一帧
                        .placeholder(R.drawable.loading)
                        .error(R.drawable.default_error)
                )
                .centerCrop()
                .into(holder.binding.ivCover)
        }
    }

    // 加载作者头像
    private fun loadAuthorAvatar(video: VideoBean, holder: ViewHolder) {
        val headId = video.userBean?.headId ?: 0

        Glide.with(context)
            .load(if (headId != 0) headId else R.mipmap.default_avatar)
            .placeholder(R.mipmap.default_avatar)
            .error(R.mipmap.default_avatar)
            .circleCrop()
            .into(holder.binding.ivAvatar)
    }

    // 格式化距离
    private fun formatDistance(distance: Float): String {
        return when {
            distance < 0.1f -> "${(distance * 1000).toInt()}m"
            distance < 1f -> String.format(Locale.US, "%.1fm", distance * 1000)
            distance < 10f -> String.format(Locale.US, "%.1fkm", distance)
            else -> "${distance.toInt()}km"
        }
    }

    // 格式化点赞数
    private fun formatLikeCount(count: Int): String {
        return when {
            count < 1000 -> count.toString()
            count < 10000 -> String.format(Locale.US, "%.1fk", count / 1000.0)
            else -> String.format(Locale.US, "%.1fw", count / 10000.0)
        }
    }

    // 添加数据
    fun appendList(newVideos: List<VideoBean>) {
        val startPosition = videoList.size
        videoList.addAll(newVideos)
        notifyItemRangeInserted(startPosition, newVideos.size)
    }

    // 清空数据
    fun clearList() {
        val size = videoList.size
        videoList.clear()
        notifyItemRangeRemoved(0, size)
    }

    // 更新点赞状态
    fun updateLikeStatus(position: Int, isLiked: Boolean) {
        videoList.getOrNull(position)?.let { video ->
            video.isLiked = isLiked
            // 更新点赞数
            video.likeCount += if (isLiked) 1 else -1
            notifyItemChanged(position)
        }
    }
}
