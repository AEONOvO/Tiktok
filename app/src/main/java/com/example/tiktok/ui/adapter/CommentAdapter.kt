package com.example.tiktok.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.tiktok.R
import android.util.Log
import com.example.tiktok.data.model.CommentBean
import com.example.tiktok.databinding.ItemCommentBinding

class CommentAdapter(
    private val onLikeClick: (CommentBean, Int) -> Unit                        // 点赞回调函数
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private val commentList = mutableListOf<CommentBean>()

    // 创建评论 Item 的视图
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        Log.d("CommentAdapter", "onCreateViewHolder 调用")

        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommentViewHolder(binding)
    }

    //将评论数据绑定到 ViewHolder
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        Log.d("CommentAdapter", "onBindViewHolder 调用，position: $position")
        holder.bind(commentList[position], position)
    }

    override fun getItemCount(): Int {
        Log.d("CommentAdapter", "getItemCount: ${commentList.size}")
        return commentList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<CommentBean>?) {
        Log.d("CommentAdapter", "submitList 调用，数量: ${newList?.size ?: 0}")

        commentList.clear()
        if (newList != null) {
            commentList.addAll(newList)
        }

        Log.d("CommentAdapter", "当前列表数量: ${commentList.size}")

        notifyDataSetChanged()

        Log.d("CommentAdapter", "notifyDataSetChanged 已调用")
    }


    inner class CommentViewHolder(val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: CommentBean, position: Int) {
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
                val likeIconView = binding.root.findViewById<com.example.tiktok.ui.view.IconFontTextView>(
                    R.id.ll_like  // item_comment.xml 中的 ID
                )

                likeIconView?.setTextColor(
                    if (comment.isLiked) {
                        root.context.getColor(R.color.red)
                    } else {
                        root.context.getColor(R.color.gray)
                    }
                )

                // 点击整个评论 Item
                root.setOnClickListener {
                    val currentPosition = bindingAdapterPosition
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        onLikeClick(comment, currentPosition)
                    }
                }
                Log.d("CommentAdapter", "绑定完成 [$position]")
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
}