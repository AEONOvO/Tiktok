package com.example.tiktok.ui.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.tiktok.R
import com.example.tiktok.databinding.ItemWorkBinding
import com.example.tiktok.data.model.VideoBean
import com.example.tiktok.base.BaseAdapter
import com.example.tiktok.ui.adapter.WorkVideoAdapter.WorkVideoViewHolder
import java.util.Locale
class WorkVideoAdapter(private val context: Context,
                       private val onItemClick: (VideoBean, Int,ItemWorkBinding) -> Unit,
                       private val onLikeClick: (VideoBean, Int) -> Unit) : BaseAdapter<WorkVideoViewHolder, VideoBean>(VideoDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkVideoViewHolder {
        return WorkVideoViewHolder(ItemWorkBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    //绑定数据到ViewHolder
    override fun onBindViewHolder(holder: WorkVideoViewHolder, position: Int) {
        val video=mList.getOrNull(holder.bindingAdapterPosition)?:return

        with(holder.binding){
            //加载视频封面
            loadVideoCover(video,holder)
            ivLikeCount.text=formatLikeCount(video.likeCount)
            // 设置共享元素的 transitionName（每个封面唯一标识）
            ViewCompat.setTransitionName(ivCover, "video_cover_$position")

            root.setOnClickListener {
                val currentPosition = holder.bindingAdapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onItemClick(video, currentPosition,holder.binding)
                }
            }
            ivLike.setOnClickListener {
                onLikeClick(video, holder.bindingAdapterPosition)
            }
        }
    }

    //加载视频封面（第一帧）
    private fun loadVideoCover(video: VideoBean, holder: WorkVideoViewHolder){
        if (video.coverRes != 0) {

            Glide.with(context)
                .load(video.coverRes)
                .placeholder(R.drawable.loading)
                .error(R.drawable.default_error)
                .into(holder.binding.ivCover)
        } else {

            Glide.with(context)
                .asBitmap()
                .load(video.videoRes)
                .apply(
                    RequestOptions()
                        .frame(0)
                        .placeholder(R.drawable.loading)
                        .error(R.drawable.default_error)
                )
                .into(holder.binding.ivCover)
        }
    }

    //格式化点赞数量
    private fun formatLikeCount(count:Int):String{
        return when {
            count < 1000 -> count.toString()
            count < 10000 -> String.format(Locale.US, "%.1fk", count / 1000.0)
            else -> String.format(Locale.US, "%.1fw", count / 10000.0)
        }
    }

    //更新指定位置的点赞状态
    fun updateLikeStatus(position: Int, isLiked: Boolean) {
        mList.getOrNull(position)?.let { video ->
            video.isLiked = isLiked
            notifyItemChanged(position)
        }
    }


    inner class WorkVideoViewHolder(val binding: ItemWorkBinding) : RecyclerView.ViewHolder(binding.root)
}