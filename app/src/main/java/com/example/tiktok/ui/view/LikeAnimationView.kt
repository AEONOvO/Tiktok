package com.example.tiktok.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.widget.ImageView
import android.widget.RelativeLayout
import com.example.tiktok.R
import com.example.tiktok.utils.AnimUtils
import kotlin.random.Random

/**
 * 点赞动画视图
 * 支持双击显示爱心动画和单击播放/暂停
 */
class LikeAnimationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private var gestureDetector: GestureDetector? = null

    /** 爱心图片大小（dp 转 px） */
    private val likeViewSize = (100 * context.resources.displayMetrics.density).toInt()

    /** 随机旋转角度 */
    private val angles = intArrayOf(-30, 0, 30)

    private var onPlayPauseListener: OnPlayPauseListener? = null
    private var onLikeListener: OnLikeListener? = null


    init {
        initGestureDetector()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initGestureDetector() {
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            // 双击事件
            override fun onDoubleTap(e: MotionEvent): Boolean {
                addLikeView(e)
                onLikeListener?.onLike()
                return true
            }

            // 单击事件
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                onPlayPauseListener?.onPlayOrPause()
                return true
            }
        })

        setOnTouchListener { _, event ->
            gestureDetector?.onTouchEvent(event)
            true
        }
    }

    /**
     * 添加爱心动画
     */
    private fun addLikeView(e: MotionEvent) {
        val imageView = ImageView(context)
        imageView.setImageResource(R.mipmap.ic_like)
        addView(imageView)

        val layoutParams = LayoutParams(likeViewSize, likeViewSize)
        layoutParams.leftMargin = e.x.toInt() - likeViewSize / 2
        layoutParams.topMargin = e.y.toInt() - likeViewSize
        imageView.layoutParams = layoutParams

        playAnim(imageView)
    }

    /**
     * 播放爱心动画
     */
    private fun playAnim(view: View) {
        val animationSet = AnimationSet(true)
        val degrees = angles[Random.nextInt(3)]

        // 添加动画效果
        animationSet.addAnimation(AnimUtils.rotateAnim(0, 0, degrees.toFloat()))
        animationSet.addAnimation(AnimUtils.scaleAnim(100, 2f, 1f, 0))
        animationSet.addAnimation(AnimUtils.alphaAnim(0f, 1f, 100, 0))
        animationSet.addAnimation(AnimUtils.scaleAnim(500, 1f, 1.8f, 300))
        animationSet.addAnimation(AnimUtils.alphaAnim(1f, 0f, 500, 300))
        animationSet.addAnimation(AnimUtils.translationAnim(500, 0f, 0f, 0f, -400f, 300))

        animationSet.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                Handler(Looper.getMainLooper()).post {
                    removeView(view)
                }
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

        view.startAnimation(animationSet)
    }

    /**
     * 播放/暂停回调接口
     */
    fun interface OnPlayPauseListener {
        fun onPlayOrPause()
    }

    /**
     * 点赞回调接口
     */
    fun interface OnLikeListener {
        fun onLike()
    }

    /**
     * 设置单击播放暂停事件
     */
    fun setOnPlayPauseListener(listener: OnPlayPauseListener?) {
        this.onPlayPauseListener = listener
    }

    /**
     * 设置双击点赞事件
     */
    fun setOnLikeListener(listener: OnLikeListener?) {
        this.onLikeListener = listener
    }
}