package com.example.tiktok.utils

import android.content.Context
import android.view.MotionEvent
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class VideoPlayTouchHelper(
    private val context: Context,
    private val viewPager: ViewPager2,
    private val onPullDown: (Float) -> Unit,        // 下拉距离回调
    private val onPullUp: (Float) -> Unit,          // 上拉距离回调
    private val onRefresh: () -> Unit,              // 触发刷新
    private val onLoadMore: () -> Unit              // 触发加载更多
) {

    private var startY = 0f
    private var startX = 0f
    private var isDragging = false
    private var currentDragDistance = 0f

    private val threshold = 200f  // 触发刷新/加载的阈值（单位：px）
    private val minSwipeDistance = 50f  // 最小滑动距离，用于判断是否是有效滑动

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startY = event.y
                startX = event.x
                isDragging = false
                currentDragDistance = 0f
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaY = event.y - startY
                val deltaX = event.x - startX

                // 只有在垂直滑动明显大于水平滑动时才处理
                if (abs(deltaY) > abs(deltaX) && abs(deltaY) > minSwipeDistance) {
                    val currentPosition = viewPager.currentItem
                    val itemCount = viewPager.adapter?.itemCount ?: 0

                    // 在第一个视频且向下拉
                    if (currentPosition == 0 && deltaY > 0) {
                        isDragging = true
                        currentDragDistance = deltaY
                        onPullDown(deltaY)
                        return true  // 拦截事件
                    }

                    // 在最后一个视频且向上拉
                    if (currentPosition == itemCount - 1 && deltaY < 0) {
                        isDragging = true
                        currentDragDistance = abs(deltaY)
                        onPullUp(abs(deltaY))
                        return true  // 拦截事件
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    val deltaY = event.y - startY
                    val currentPosition = viewPager.currentItem
                    val itemCount = viewPager.adapter?.itemCount ?: 0

                    // 触发刷新
                    if (currentPosition == 0 && deltaY > threshold) {
                        onRefresh()
                    }

                    // 触发加载更多
                    if (currentPosition == itemCount - 1 && abs(deltaY) > threshold) {
                        onLoadMore()
                    }

                    // 重置
                    onPullDown(0f)
                    onPullUp(0f)
                    isDragging = false
                    currentDragDistance = 0f
                    return true
                }
            }
        }

        // 不拦截事件，让 ViewPager2 正常处理
        return false
    }
}