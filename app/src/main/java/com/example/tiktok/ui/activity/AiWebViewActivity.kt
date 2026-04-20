package com.example.tiktok.ui.activity

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.example.tiktok.base.BaseBindingActivity
import com.example.tiktok.databinding.ActivityAiWebviewBinding

class AiWebViewActivity : BaseBindingActivity<ActivityAiWebviewBinding>({ ActivityAiWebviewBinding.inflate(it) }) {

    private val deepseekUrl = "https://chat.deepseek.com/"

    override fun init() {
        setupUi()
        setupWebView()
    }

    private fun setupUi() {
        binding.tvBack.setOnClickListener {
            handleBack()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBack()
            }
        })
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        with(binding.webAi) {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            addJavascriptInterface(AiJsBridge(this@AiWebViewActivity, this), "AndroidBridge")
            loadUrl("file:///android_asset/ai/index.html")
        }
    }

    private fun handleBack() {
        if (binding.webAi.canGoBack()) {
            binding.webAi.goBack()
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        binding.webAi.apply {
            stopLoading()
            removeJavascriptInterface("AndroidBridge")
            destroy()
        }
        super.onDestroy()
    }

    private class AiJsBridge(
        private val activity: AiWebViewActivity,
        private val webView: WebView
    ) {
        @JavascriptInterface
        fun openNativeDialog(message: String?) {
            activity.runOnUiThread {
                val text = message?.trim().orEmpty().ifEmpty { "AI says hi" }
                Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
            }
        }

        @JavascriptInterface
        fun getAppInfo(): String {
            return "{\"app\":\"TikTok\",\"version\":\"1.0\"}"
        }

        @JavascriptInterface
        fun openDeepseek() {
            activity.runOnUiThread {
                webView.loadUrl(activity.deepseekUrl)
            }
        }

        @JavascriptInterface
        fun copyToClipboard(text: String?): Boolean {
            val content = text?.trim().orEmpty()
            if (content.isEmpty()) {
                return false
            }
            val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("ai", content))
            return true
        }

        @JavascriptInterface
        fun closeWebView() {
            activity.runOnUiThread {
                activity.finish()
            }
        }
    }
}
