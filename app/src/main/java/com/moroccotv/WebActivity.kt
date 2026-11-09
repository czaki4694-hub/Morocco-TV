package com.moroccotv

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.TextView

class WebActivity : Activity() {
    private lateinit var web: WebView
    private lateinit var root: FrameLayout
    private var full: View? = null
    private var cb: WebChromeClient.CustomViewCallback? = null
    private val mp = ViewGroup.LayoutParams.MATCH_PARENT

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        root = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }

        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            webViewClient = WebViewClient()
            webChromeClient = object : WebChromeClient() {
                override fun onShowCustomView(v: View, c: CustomViewCallback) {
                    full = v; cb = c
                    web.visibility = View.GONE
                    root.addView(v, ViewGroup.LayoutParams(mp, mp))
                }
                override fun onHideCustomView() { exitFull() }
            }
        }
        root.addView(web, ViewGroup.LayoutParams(mp, mp))

        val rotate = TextView(this).apply {
            text = "⟳"
            textSize = 22f
            setTextColor(Color.parseColor("#D4AF37"))
            setBackgroundColor(Color.parseColor("#990B1020"))
            setPadding(36, 22, 36, 22)
            setOnClickListener {
                val land = resources.configuration.orientation ==
                    Configuration.ORIENTATION_LANDSCAPE
                requestedOrientation = if (land)
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                else ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
        }
        root.addView(rotate, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.BOTTOM or Gravity.END; bottomMargin = 80; marginEnd = 16 })

        setContentView(root)
        web.loadUrl(intent.getStringExtra("url") ?: "about:blank")
    }

    private fun exitFull() {
        full?.let { root.removeView(it) }
        full = null
        cb?.onCustomViewHidden()
        cb = null
        web.visibility = View.VISIBLE
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when {
            full != null -> exitFull()
            web.canGoBack() -> web.goBack()
            else -> super.onBackPressed()
        }
    }

    override fun onPause() { super.onPause(); web.onPause() }
    override fun onResume() { super.onResume(); web.onResume() }
    override fun onDestroy() { super.onDestroy(); root.removeAllViews(); web.destroy() }
}
