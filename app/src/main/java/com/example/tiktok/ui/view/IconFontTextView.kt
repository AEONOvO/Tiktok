package com.example.tiktok.ui.view

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 *图标字体文本视图
 */
class IconFontTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "IconFontTextView"
        private const val FONT_PATH = "iconfont.ttf"

        /**
         * 缓存的字体实例（所有 IconFontTextView 共享）
         */
        private var cachedTypeface: Typeface? = null

        /**
         * 获取图标字体
         */
        @Synchronized
        fun getIconTypeface(context: Context): Typeface? {
            if (cachedTypeface == null) {
                try {
                    cachedTypeface = Typeface.createFromAsset(context.assets, FONT_PATH)
                    android.util.Log.d(TAG, "字体加载成功: $FONT_PATH")
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "字体加载失败: $FONT_PATH", e)
                }
            }
            return cachedTypeface
        }
    }

    init {
        // 加载并设置图标字体
        getIconTypeface(context)?.let { typeface ->
            setTypeface(typeface)
        } ?: run {
            android.util.Log.w(TAG, "使用默认字体，因为图标字体加载失败")
        }
    }
}