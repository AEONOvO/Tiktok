package com.example.tiktok.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.tiktok.R
import com.example.tiktok.data.model.CommentBean
import com.example.tiktok.databinding.ItemCommentBinding

class CommentAdapter(
    private val onLikeClick: (CommentBean, Int) -> Unit                        // 点赞回调函数
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private val commentList = mutableListOf<CommentBean>()

    // 创建评论 Item 的视图
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommentViewHolder(binding)
    }

    //将评论数据绑定到 ViewHolder
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(commentList[position])
    }

    //缓存视图控件、绑定评论数据
    inner class CommentViewHolder(val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: CommentBean) {
            with(binding) {
                // 加载头像
                Glide.with(ivHead.context)
                    .load(comment.userBean.headId)
                    .apply(RequestOptions().circleCrop())
                    .placeholder(R.mipmap.default_avatar)
                    .error(R.mipmap.default_avatar)
                    .into(ivHead)

                // 设置文字
                tvNickname.text = comment.userBean.nickName ?: "抖音用户xxx"
                tvContent.text = comment.content
                tvLikecount.text = formatCount(comment.likeCount)

                // 设置点赞图标颜色
                llLike.setTextColor(
                    if (comment.isLiked) {
                        root.context.getColor(R.color.red)
                    } else {
                        root.context.getColor(R.color.gray)
                    }
                )

                // 点击点赞图标
                llLike.setOnClickListener {
                    val currentPosition = bindingAdapterPosition
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        // 触发回调
                        onLikeClick(comment, currentPosition)
                    }
                }
            }
        }

        //格式化点赞数
        private fun formatCount(count: Int): String {
            return when {
                count >= 10000 -> "${count / 10000}.${(count % 10000) / 1000}w"
                count >= 1000 -> "${count / 1000}.${(count % 1000) / 100}k"
                else -> count.toString()
            }
        }
    }

    //返回评论列表的大小
    override fun getItemCount(): Int {
        return commentList.size
    }

    //更新评论列表
    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<CommentBean>?) {
        commentList.clear()
        if (newList != null) {
            commentList.addAll(newList)
        }

        // 通知 RecyclerView 数据已更新
        notifyDataSetChanged()
    }
}