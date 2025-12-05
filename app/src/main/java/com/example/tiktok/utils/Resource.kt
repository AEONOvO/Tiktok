package com.example.tiktok.utils

//网络请求的 3 种状态
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)                                   // 成功
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)   // 失效
    class Loading<T> : Resource<T>()                                                // 加载中
}