package com.example.tiktok.utils

import android.content.Context
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class SwipeGestureHelper(
    context: Context,
    private val onSwipeLeft: () -> Unit,
    private val onSwipeRight: () -> Unit,
    swipeThresholdDp: Int = DEFAULT_SWIPE_THRESHOLD_DP,
    swipeVelocityThresholdDp: Int = DEFAULT_SWIPE_VELOCITY_THRESHOLD_DP
) {
    private val gestureDetector: GestureDetector

    private val swipeThreshold: Int = dpToPx(context, swipeThresholdDp)
    private val swipeVelocityThreshold: Int = dpToPx(context, swipeVelocityThresholdDp)

    // 添加变量追踪触摸状态
    private var initialX = 0f
    private var initialY = 0f
    private var isHorizontalSwipe = false

    companion object {
        const val DEFAULT_SWIPE_THRESHOLD_DP = 30
        const val DEFAULT_SWIPE_VELOCITY_THRESHOLD_DP = 30

        private fun dpToPx(context: Context, dp: Int): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                context.resources.displayMetrics
            ).toInt()
        }
    }

    init {
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false

                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y

                // 判断是否为水平滑动
                if (abs(diffX) > abs(diffY) * 1.2f) {
                    if (abs(diffX) > swipeThreshold || abs(velocityX) > swipeVelocityThreshold) {
                        if (diffX > 0) {
                            onSwipeRight()
                        } else {
                            onSwipeLeft()
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    fun attachToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {

            // 在 onInterceptTouchEvent 中判断滑动方向
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // 记录初始触摸点
                        initialX = e.x
                        initialY = e.y
                        isHorizontalSwipe = false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = abs(e.x - initialX)
                        val deltaY = abs(e.y - initialY)

                        // 判断是否为横向滑动
                        if (deltaX > deltaY && deltaX > 20) {  // 20像素的判断阈值
                            isHorizontalSwipe = true
                            // 请求父布局不要拦截触摸事件
                            rv.parent?.requestDisallowInterceptTouchEvent(true)
                            // 截事件，交给 GestureDetector 处理
                            gestureDetector.onTouchEvent(e)
                            return true  // 返回 true，拦截事件
                        }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        if (isHorizontalSwipe) {
                            gestureDetector.onTouchEvent(e)
                            rv.parent?.requestDisallowInterceptTouchEvent(false)
                            isHorizontalSwipe = false
                            return true
                        }
                    }
                }

                // 如果是横向滑动，继续传递给 GestureDetector
                if (isHorizontalSwipe) {
                    gestureDetector.onTouchEvent(e)
                    return true
                }

                // 非横向滑动，传递给 GestureDetector 但不拦截
                gestureDetector.onTouchEvent(e)
                return false
            }

            // 处理已拦截的触摸事件
            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                gestureDetector.onTouchEvent(e)
            }
        })
    }
}