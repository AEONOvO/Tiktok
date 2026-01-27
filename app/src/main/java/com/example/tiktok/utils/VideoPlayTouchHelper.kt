package com.example.tiktok.utils

import android.animation.ObjectAnimator
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class VideoPlayTouchHelper(
    private val viewPager: ViewPager2,
    private val onPullDown: (Float) -> Unit,        // 下拉距离回调
    private val onRefresh: () -> Unit,              // 触发刷新
    private val onLoadMore: () -> Unit,             // 触发加载更多
    private val refreshIcon: android.widget.ImageView? = null,
) {
    // 触摸起始位置
    private var startY = 0f
    private var startX = 0f

    private var isDragging = false
    private var currentDragDistance = 0f
    private var isPullingDown = false
    private var isPullingUp = false

    private val threshold = 200f
    private val minSwipeDistance = 50f
    private val maxDragDistance = 400f

    // 只保留刷新动画
    private var refreshIconAnimator: ObjectAnimator? = null

    // 记录上次触发加载的位置（防止重复触发）
    private var lastLoadMorePosition = -1

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            //手指刚触碰屏幕时，记录起始位置
            MotionEvent.ACTION_DOWN -> {
                startY = event.y
                startX = event.x
                isDragging = false
                isPullingDown = false
                isPullingUp = false
                currentDragDistance = 0f
            }

            //感知意图
            MotionEvent.ACTION_MOVE -> {
                val deltaY = event.y - startY
                val deltaX = event.x - startX

                // 只处理垂直滑动
                if (abs(deltaY) > abs(deltaX) && abs(deltaY) > minSwipeDistance) {
                    val currentPosition = viewPager.currentItem

                    // 下拉刷新（在第一个视频且向下拉）
                    if (currentPosition == 0 && deltaY > 0) {
                        isDragging = true
                        isPullingDown = true

                        // 计算拖拽距离
                        currentDragDistance = calculateDragDistance(deltaY)
                        val progress = (currentDragDistance / threshold).coerceIn(0f, 1.5f)

                        // 回调距离
                        onPullDown(currentDragDistance)

                        // 旋转刷新图标
                        animateRefreshIcon(progress)

                        return true
                    }

                    // 上拉加载更多（在最后一个视频且向上拉）
                    val adapter = viewPager.adapter
                    val itemCount = adapter?.itemCount ?: 0
                    if (itemCount > 0 && currentPosition == itemCount - 1 && deltaY < 0) {
                        isPullingUp = true
                        // 不拦截事件，让 ViewPager2 正常滑动
                    }
                }
            }

            //执行命令
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    val currentPosition = viewPager.currentItem

                    // 触发刷新
                    if (isPullingDown && currentPosition == 0 && currentDragDistance >= threshold) {
                        startRefreshIconAnimation()
                        onRefresh()
                    } else if (isPullingDown) {
                        // 未达到阈值，重置
                        resetPullDownState()
                    }

                    isDragging = false
                    isPullingDown = false
                    currentDragDistance = 0f
                    return true
                }

                // 触发加载更多
                if (isPullingUp) {
                    val currentPosition = viewPager.currentItem
                    val adapter = viewPager.adapter
                    val itemCount = adapter?.itemCount ?: 0

                    // 在最后一个视频且未重复触发
                    if (itemCount > 0 && currentPosition == itemCount - 1 && lastLoadMorePosition != currentPosition) {
                        lastLoadMorePosition = currentPosition
                        onLoadMore()
                    }

                    isPullingUp = false
                }
            }
        }

        return false
    }

    // 计算拖拽距离带有阻尼效果
    private fun calculateDragDistance(rawDistance: Float): Float {
        return if (rawDistance <= maxDragDistance) {
            rawDistance
        } else {
            maxDragDistance + (rawDistance - maxDragDistance) * 0.3f
        }.coerceAtMost(maxDragDistance * 1.5f)
    }

    // 刷新图标旋转动画
    private fun animateRefreshIcon(progress: Float) {
        refreshIcon?.let { icon ->
            icon.rotation = progress * 360f

            // 缩放效果
            val scale = 0.8f + (progress * 0.2f).coerceAtMost(0.2f)
            icon.scaleX = scale
            icon.scaleY = scale
        }
    }

    // 开始刷新图标持续旋转动画
    private fun startRefreshIconAnimation() {
        refreshIcon?.let { icon ->
            //清理异常残留
            refreshIconAnimator?.cancel()
            refreshIconAnimator = ObjectAnimator.ofFloat(
                icon,
                "rotation",
                icon.rotation,
                icon.rotation + 360f
            ).apply {
                duration = 1000                              //持续时间
                repeatCount = ObjectAnimator.INFINITE        //无限循环
                interpolator = LinearInterpolator()          //匀速
                start()
            }
        }
    }

    // 停止刷新动画并重置状态
    fun stopRefreshAnimation() {
        refreshIconAnimator?.cancel()
        refreshIcon?.apply {
            rotation = 0f
            scaleX = 1f
            scaleY = 1f
        }
        resetPullDownState()
    }

    // 重置下拉状态
    private fun resetPullDownState() {
        onPullDown(0f)
        refreshIcon?.apply {
            rotation = 0f
            scaleX = 1f
            scaleY = 1f
        }
    }

    // 重置加载更多状态（在数据加载成功后调用）
    fun resetLoadMoreState() {
        lastLoadMorePosition = -1
    }

    // 清理资源
    fun release() {
        refreshIconAnimator?.cancel()
        refreshIconAnimator = null
    }
}