package com.moroccotv

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class PlayerActivity : Activity() {

    private var player: ExoPlayer? = null
    private lateinit var names: ArrayList<String>
    private lateinit var urls: ArrayList<String>
    private var index = 0
    private lateinit var title: TextView
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        names = intent.getStringArrayListExtra("names") ?: arrayListOf()
        urls = intent.getStringArrayListExtra("urls") ?: arrayListOf()
        index = intent.getIntExtra("index", 0)

        val root = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        val view = PlayerView(this).apply { useController = true }
        root.addView(view)

        title = TextView(this).apply {
            textSize = 16f
            setTextColor(Color.parseColor("#D4AF37"))
            setBackgroundColor(Color.parseColor("#990B1020"))
            setPadding(40, 24, 40, 24)
        }
        root.addView(title, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.TOP or Gravity.START; topMargin = 60 })

        status = TextView(this).apply {
            textSize = 16f
            setTextColor(Color.WHITE)
            visibility = View.GONE
        }
        root.addView(status, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.CENTER })

        fun nav(text: String, g: Int, delta: Int) = TextView(this).apply {
            this.text = text
            textSize = 28f
            setTextColor(Color.parseColor("#D4AF37"))
            setBackgroundColor(Color.parseColor("#990B1020"))
            setPadding(40, 24, 40, 24)
            setOnClickListener { play((index + delta + urls.size) % urls.size) }
        }.also {
            root.addView(it, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply { gravity = g })
        }
        nav("‹", Gravity.CENTER_VERTICAL or Gravity.START, -1)
        nav("›", Gravity.CENTER_VERTICAL or Gravity.END, 1)

        setContentView(root)

        player = ExoPlayer.Builder(this).build().also { p ->
            view.player = p
            p.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_BUFFERING) {
                        status.text = "جاري التحميل..."
                        status.visibility = View.VISIBLE
                    } else status.visibility = View.GONE
                }
                override fun onPlayerError(error: PlaybackException) {
                    status.text = "القناة غير متاحة حاليا"
                    status.visibility = View.VISIBLE
                }
            })
        }
        if (urls.isNotEmpty()) play(index)
    }

    private fun play(i: Int) {
        index = i
        title.text = names[i]
        player?.apply {
            setMediaItem(MediaItem.fromUri(urls[i]))
            prepare()
            playWhenReady = true
        }
    }

    override fun onStop() { super.onStop(); player?.pause() }
    override fun onDestroy() { super.onDestroy(); player?.release(); player = null }
}
