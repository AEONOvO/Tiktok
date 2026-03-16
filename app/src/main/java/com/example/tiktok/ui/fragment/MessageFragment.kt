package com.example.tiktok.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tiktok.base.BaseBindingFragment
import com.example.tiktok.databinding.FragmentMessageBinding
import com.example.tiktok.ui.adapter.MessageAdapter
import com.example.tiktok.ui.adapter.MessageItem
import com.example.tiktok.utils.DataCreate
import java.util.LinkedHashMap

class MessageFragment : BaseBindingFragment<FragmentMessageBinding>({ FragmentMessageBinding.inflate(it) }) {

    private var messageAdapter: MessageAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val messages = buildMessageList()
        messageAdapter = MessageAdapter(messages)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = messageAdapter
        binding.recyclerView.setHasFixedSize(true)
    }

    private fun buildMessageList(): List<MessageItem> {
        val timePool = listOf("刚刚", "3分钟前", "8分钟前", "12分钟前", "1小时前", "昨天")
        val messageMap = LinkedHashMap<Int, MessageItem>()

        DataCreate.datas.forEachIndexed { index, video ->
            val user = video.userBean ?: return@forEachIndexed
            if (messageMap.containsKey(user.userId)) {
                return@forEachIndexed
            }

            val nickname = user.nickName ?: "抖音用户xxx"
            val preview = video.content?.take(22) ?: "给你发来一条新消息"
            val unreadCount = when {
                index % 5 == 0 -> 0
                index % 4 == 0 -> 3
                else -> 1
            }

            messageMap[user.userId] = MessageItem(
                userId = user.userId,
                avatarRes = user.headId,
                nickname = nickname,
                lastMessage = preview,
                timeText = timePool[index % timePool.size],
                unreadCount = unreadCount
            )
        }

        return messageMap.values.toList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        messageAdapter = null
    }
}
