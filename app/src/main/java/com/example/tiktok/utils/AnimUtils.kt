package com.example.tiktok.utils

import android.view.animation.*

/**
 * 动画工具类
 */
object AnimUtils {
    /**
     * 以中心缩放动画
     */
    fun scaleAnim(time: Long, from: Float, to: Float, offsetTime: Long): ScaleAnimation {
        val scaleAnimation = ScaleAnimation(
            from, to, from, to,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        scaleAnimation.startOffset = offsetTime
        scaleAnimation.interpolator = DecelerateInterpolator()
        scaleAnimation.duration = time
        return scaleAnimation
    }

    /**
     * 旋转动画
     */
    fun rotateAnim(time: Long, fromDegrees: Int, toDegrees: Float): RotateAnimation {
        val rotateAnimation = RotateAnimation(
            fromDegrees.toFloat(), toDegrees,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        rotateAnimation.duration = time
        return rotateAnimation
    }

    /**
     * 移动动画
     */
    fun translationAnim(
        time: Long,
        fromX: Float,
        toX: Float,
        fromY: Float,
        toY: Float,
        offsetTime: Long
    ): TranslateAnimation {
        val anim = TranslateAnimation(fromX, toX, fromY, toY)
        anim.duration = time
        anim.interpolator = DecelerateInterpolator()
        anim.startOffset = offsetTime
        return anim
    }

    /**
     * 透明度动画
     */
    fun alphaAnim(fromAlpha: Float, toAlpha: Float, duration: Long, offsetTime: Long): AlphaAnimation {
        val anim = AlphaAnimation(fromAlpha, toAlpha)
        anim.duration = duration
        anim.startOffset = offsetTime
        return anim
    }
}