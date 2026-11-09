package com.moroccotv

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast

class WebActivity : Activity() {
    private lateinit var web: WebView
    private lateinit var root: FrameLayout
    private lateinit var found: TextView
    private var full: View? = null
    private var cb: WebChromeClient.CustomViewCallback? = null
    private var streamUrl: String? = null
    private val mp = ViewGroup.LayoutParams.MATCH_PARENT

    private fun btn(t: String) = TextView(this).apply {
        text = t
        textSize = 18f
        setTextColor(Color.parseColor("#D4AF37"))
        setBackgroundColor(Color.parseColor("#CC0B1020"))
        setPadding(36, 22, 36, 22)
    }

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
            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView, request: WebResourceRequest
                ): WebResourceResponse? {
                    val u = request.url.toString()
                    if (u.contains(".m3u8")) {
                        runOnUiThread {
                            streamUrl = u
                            found.visibility = View.VISIBLE
                            val cm = getSystemService(Context.CLIPBOARD_SERVICE)
                                as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("stream", u))
                        }
                    }
                    return null
                }
            }
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

        val rotate = btn("⟳").apply {
            setOnClickListener {
                val land = resources.configuration.orientation ==
                    Configuration.ORIENTATION_LANDSCAPE
                requestedOrientation = if (land)
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                else ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
        }
        root.addView(rotate, FrameLayout.LayoutParams(-2, -2).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            bottomMargin = 80; marginEnd = 16
        })

        found = btn("▶ مشغل").apply {
            visibility = View.GONE
            setOnClickListener {
                val u = streamUrl ?: return@setOnClickListener
                Toast.makeText(context, "تنسخ الرابط", Toast.LENGTH_SHORT).show()
                startActivity(
                    Intent(context, PlayerActivity::class.java)
                        .putStringArrayListExtra("names", arrayListOf("Live"))
                        .putStringArrayListExtra("urls", arrayListOf(u))
                        .putExtra("index", 0)
                )
            }
        }
        root.addView(found, FrameLayout.LayoutParams(-2, -2).apply {
            gravity = Gravity.BOTTOM or Gravity.START
            bottomMargin = 80; marginStart = 16
        })

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
