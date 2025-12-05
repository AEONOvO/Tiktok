package com.example.tiktok.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tiktok.data.model.UserInfo
import com.example.tiktok.data.repository.UserRepository
import com.example.tiktok.utils.Resource
import kotlinx.coroutines.launch

class PersonalHomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository(application)

    // 用户信息
    private val _userInfo = MutableLiveData<Resource<UserInfo>>()
    val userInfo: LiveData<Resource<UserInfo>> = _userInfo

    // 头像上传状态
    private val _avatarUploadStatus = MutableLiveData<Resource<String>>()
    val avatarUploadStatus: LiveData<Resource<String>> = _avatarUploadStatus

    // Toast 消息
    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage

    //加载用户信息
    fun loadUserInfo(userId: String = "123456") {
        viewModelScope.launch {
            _userInfo.value = Resource.Loading()

            val result = repository.getUserInfo(userId)

            if (result.isSuccess) {
                _userInfo.value = Resource.Success(result.getOrNull()!!)
            } else {
                _userInfo.value = Resource.Error(
                    result.exceptionOrNull()?.message ?: "加载用户信息失败"
                )
            }
        }
    }

    //上传头像
    fun uploadAvatar(uri: Uri) {
        viewModelScope.launch {
            _avatarUploadStatus.value = Resource.Loading()

            val result = repository.uploadAvatar(uri)

            if (result.isSuccess) {
                val avatarUrl = result.getOrNull()!!
                _avatarUploadStatus.value = Resource.Success(avatarUrl)

                // 更新本地用户信息
                _userInfo.value?.data?.let { currentUser ->
                    _userInfo.value = Resource.Success(
                        currentUser.copy(avatarUrl = avatarUrl)
                    )
                }

                _toastMessage.value = "头像更新成功"
            } else {
                _avatarUploadStatus.value = Resource.Error(
                    result.exceptionOrNull()?.message ?: "头像上传失败"
                )
                _toastMessage.value = "头像上传失败"
            }
        }
    }


     //格式化数字
    fun formatCount(count: Int): String {
        return when {
            count >= 10000 -> String.format("%.1fw", count / 10000.0)
            count >= 1000 -> String.format("%.1fk", count / 1000.0)
            else -> count.toString()
        }
    }
}