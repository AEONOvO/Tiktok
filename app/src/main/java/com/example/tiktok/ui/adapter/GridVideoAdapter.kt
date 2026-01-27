package com.example.tiktok.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.tiktok.R
import com.example.tiktok.databinding.ItemGridVideoBinding
import com.example.tiktok.data.model.VideoBean
import com.example.tiktok.base.BaseAdapter
import com.example.tiktok.ui.adapter.GridVideoAdapter.GridVideoViewHolder
import java.util.Locale


class GridVideoAdapter(private val context: Context,
                       private val onItemClick: (VideoBean, Int,ItemGridVideoBinding) -> Unit) : BaseAdapter<GridVideoViewHolder, VideoBean>(VideoDiff()) {

    inner class GridVideoViewHolder(val binding: ItemGridVideoBinding) : RecyclerView.ViewHolder(binding.root)

    //创建新的 ViewHolder 实例
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridVideoViewHolder {
        return GridVideoViewHolder(
            ItemGridVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    //绑定数据到ViewHolder
    override fun onBindViewHolder(holder: GridVideoViewHolder, position: Int) {
        val video = mList.getOrNull(holder.bindingAdapterPosition) ?: return

        with(holder.binding){
            //加载视频封面
            loadVideoCover(video, holder)
            //加载作者头像
            loadAuthorAvatar(video, holder)

            //设置文字信息
            tvContent.text = video.content
            ivAuthorName.text = video.userBean?.nickName ?: "抖音用户xxx"
            ivLikeCount.text = formatLikeCount(video.likeCount)

            // 设置共享元素的 transitionName（每个封面唯一标识）
            ViewCompat.setTransitionName(ivCover, "video_cover_$position")

            //设置点击卡片事件监听
            root.setOnClickListener {
                val currentPosition = holder.bindingAdapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onItemClick(video, currentPosition,holder.binding)
                }
            }
        }
    }

    //加载视频封面（第一帧）
    private fun loadVideoCover(video: VideoBean, holder: GridVideoViewHolder){
        Glide.with(context)
            .asBitmap()                                 // 指定加载 Bitmap 以获取视频的帧图
            .load(video.videoRes)                       // 加载视频资源
            .apply(
                RequestOptions()
                    .frame(0)            // 获取视频的第一帧
                    .placeholder(R.drawable.loading)    // 加载时占位图
                    .error(R.drawable.default_error)    // 加载失败时的替代图
            )
            .into(holder.binding.ivCover) // 加载到指定的 ImageView
    }

    //加载作者头像
    private fun loadAuthorAvatar(video: VideoBean, holder: GridVideoViewHolder){
        val headId = video.userBean?.headId ?: R.mipmap.default_avatar

        Glide.with(context)
            .load(headId)
            .placeholder(R.mipmap.default_avatar)   // 加载中的占位图
            .error(R.mipmap.default_avatar)         // 加载错误时默认图
            .circleCrop()                           // 将加载的头像裁剪为圆形
            .into(holder.binding.ivAvatar)          // 显示到指定 ImageView 中
    }

    //格式化点赞数量
    private fun formatLikeCount(count:Int):String{
        return when {
            count < 1000 -> count.toString()
            count < 10000 -> String.format(Locale.US, "%.1fk", count / 1000.0)
            else -> String.format(Locale.US, "%.1fw", count / 10000.0)
        }
    }
}