package com.example.tiktok.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.tiktok.R
import com.example.tiktok.databinding.ItemMessageBinding

data class MessageItem(
    val userId: Int,
    val avatarRes: Int,
    val nickname: String,
    val lastMessage: String,
    val timeText: String,
    val unreadCount: Int
)

class MessageAdapter(
    private val messages: List<MessageItem>
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    private fun MessageViewHolder.bind(item: MessageItem) {
        with(binding) {
            Glide.with(ivAvatar)
                .load(item.avatarRes)
                .apply(RequestOptions().circleCrop())
                .placeholder(R.mipmap.default_avatar)
                .error(R.mipmap.default_avatar)
                .into(ivAvatar)

            tvNickname.text = item.nickname
            tvMessage.text = item.lastMessage
            tvTime.text = item.timeText

            if (item.unreadCount > 0) {
                tvUnread.visibility = View.VISIBLE
                tvUnread.text = if (item.unreadCount > 99) "99+" else item.unreadCount.toString()
            } else {
                tvUnread.visibility = View.GONE
            }

            root.setOnClickListener {
                Toast.makeText(
                    root.context,
                    root.context.getString(R.string.chat_feature_pending, item.nickname),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
