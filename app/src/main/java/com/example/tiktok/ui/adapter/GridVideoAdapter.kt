package com.example.tiktok.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.tiktok.R
import com.example.tiktok.databinding.ItemGridvideoBinding
import com.example.tiktok.data.model.VideoBean
import com.example.tiktok.base.BaseAdapter
import com.example.tiktok.ui.adapter.GridVideoAdapter.GridVideoViewHolder
import java.util.Locale


class GridVideoAdapter(private val context: Context,
                       private val onItemClick: (VideoBean, Int,ItemGridvideoBinding) -> Unit,
                       private val onAvatarClick: (VideoBean, Int) -> Unit,
                       private val onLikeClick: (VideoBean, Int) -> Unit) : BaseAdapter<GridVideoViewHolder, VideoBean>(VideoDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridVideoViewHolder {
        return GridVideoViewHolder(ItemGridvideoBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    //绑定数据到ViewHolder
    override fun onBindViewHolder(holder: GridVideoViewHolder, position: Int) {
        val video=mList.getOrNull(holder.bindingAdapterPosition)?:return

        with(holder.binding){
            //加载视频封面
            loadVideoCover(video,holder)

            //加载作者头像
            loadAuthorAvatar(video,holder)

            //设置文字信息
            tvContent.text=video.content
            ivAuthorName.text=video.userBean?.nickName?:"抖音用户xxx"
            ivLikeCount.text=formatLikeCount(video.likeCount)

            // 设置共享元素的 transitionName（每个封面唯一标识）
            ViewCompat.setTransitionName(ivCover, "video_cover_$position")

            //点击整个卡片跳转
            root.setOnClickListener {
                val currentPosition = holder.bindingAdapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onItemClick(video, currentPosition,holder.binding)
                }
            }

            //点击头像跳转
            ivAvatar.setOnClickListener {
                onAvatarClick(video, holder.bindingAdapterPosition)
            }

            //点击点赞区域
            ivLike.setOnClickListener {
                onLikeClick(video, holder.bindingAdapterPosition)
            }
        }
    }
    //加载视频封面（第一帧）
    private fun loadVideoCover(video: VideoBean, holder: GridVideoViewHolder){
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

    //加载作者头像
    private fun loadAuthorAvatar(video: VideoBean, holder: GridVideoViewHolder){
        val headId = video.userBean?.headId ?: 0

        Glide.with(context)
            //优先显示视频所属用户的自定义头像，若用户信息不存在、头像为空或无效，则显示默认头像
            .load(if (headId != 0) headId else R.mipmap.default_avatar)
            .placeholder(R.mipmap.default_avatar)
            .error(R.mipmap.default_avatar)
            .circleCrop()                   //将加载的头像裁剪为圆形
            .into(holder.binding.ivAvatar)  //将加载的头像显示到ImageView中
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


    inner class GridVideoViewHolder(val binding: ItemGridvideoBinding) : RecyclerView.ViewHolder(binding.root)
}