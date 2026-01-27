package com.example.tiktok.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tiktok.data.model.CommentBean
import com.example.tiktok.data.repository.CommentRepository
import com.example.tiktok.utils.Resource
import kotlinx.coroutines.launch

class CommentViewModel(
    application: Application
) : AndroidViewModel(application) {

    // 传入 Application Context，Repository 负责数据获取
    private val repository: CommentRepository = CommentRepository(application)

    //评论列表数据
    private val _commentList = MutableLiveData<Resource<List<CommentBean>>>()
    val commentList: LiveData<Resource<List<CommentBean>>> = _commentList

    //发布评论结果
    private val _publishResult = MutableLiveData<Resource<CommentBean>>()
    val publishResult: LiveData<Resource<CommentBean>> = _publishResult

    //评论总数
    private val _commentCount = MutableLiveData<Int>()
    val commentCount: LiveData<Int> = _commentCount

    //错误消息
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // 评论数变化
    private val _commentCountUpdate = MutableLiveData<Pair<Int, Int>>()
    val commentCountUpdate: LiveData<Pair<Int, Int>> = _commentCountUpdate

    private var currentVideoId: Int = 0

    // 加载评论（从数据库）
    fun loadComments(videoId: Int) {
        currentVideoId = videoId

        viewModelScope.launch {
            _commentList.value = Resource.Loading()

            // 从数据库加载评论
            val result = repository.getCommentList(videoId)

            if (result.isSuccess) {
                val comments = result.getOrNull() ?: emptyList()
                _commentList.value = Resource.Success(comments)

                // 通知评论数变化
                _commentCountUpdate.value = Pair(videoId, comments.size)
            } else {
                _commentList.value = Resource.Error("加载评论失败")
                _errorMessage.value = "加载评论失败"
            }
        }
    }

    // 发表评论（保存到数据库）
    fun publishComment(content: String) {
        if (content.trim().isEmpty()) {
            _errorMessage.value = "评论内容不能为空"
            return
        }

        viewModelScope.launch {
            _publishResult.value = Resource.Loading()

            // 发布评论到数据库
            val result = repository.publishComment(currentVideoId, content.trim())

            if (result.isSuccess) {
                val newComment = result.getOrNull()
                if (newComment != null) {
                    val currentList = _commentList.value?.data?.toMutableList() ?: mutableListOf()

                    // 将新评论插入到列表头部
                    currentList.add(0, newComment)

                    // 更新评论列表
                    _commentList.value = Resource.Success(currentList)
                    _commentCountUpdate.value = Pair(currentVideoId, currentList.size)
                    _publishResult.value = Resource.Success(newComment)
                }
            } else {
                _publishResult.value = Resource.Error("发布失败")
                _errorMessage.value = "发布评论失败"
            }
        }
    }

    // 给评论点赞（同步到数据库）
    fun toggleCommentLike(comment: CommentBean, position: Int) {
        viewModelScope.launch {
            // 切换点赞状态
            val result = repository.toggleCommentLike(comment)

            if (result.isSuccess) {
                comment.isLiked = result.getOrNull() ?: false

                // 点赞数同步更新
                if (comment.isLiked) {
                    comment.likeCount++
                } else {
                    comment.likeCount--
                }

                val currentList = _commentList.value?.data?.toMutableList() ?: return@launch
                currentList[position] = comment
                _commentList.value = Resource.Success(currentList)
            } else {
                _errorMessage.value = "操作失败"
            }
        }
    }
}