package com.moroccotv

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private val channelsUrl =
        "https://raw.githubusercontent.com/czaki4694-hub/Morocco-TV/main/channels.json"

    private lateinit var grid: GridLayout
    private lateinit var info: TextView

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun shape(color: String, radius: Int, stroke: String? = null) =
        GradientDrawable().apply {
            setColor(Color.parseColor(color))
            cornerRadius = dp(radius).toFloat()
            if (stroke != null) setStroke(dp(1), Color.parseColor(stroke))
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0B1020"))
            setPadding(dp(16), dp(44), dp(16), dp(8))
        }

        root.addView(TextView(this).apply {
            text = "MOROCCO TV"
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#D4AF37"))
            letterSpacing = 0.15f
        })
        root.addView(TextView(this).apply {
            text = "القنوات المغربية"
            textSize = 14f
            setTextColor(Color.parseColor("#8E96B0"))
            setPadding(0, dp(2), 0, dp(16))
        })

        info = TextView(this).apply {
            textSize = 14f
            setTextColor(Color.parseColor("#8E96B0"))
            gravity = Gravity.CENTER
            setPadding(0, dp(24), 0, dp(24))
        }
        root.addView(info)

        grid = GridLayout(this).apply { columnCount = 2 }
        root.addView(
            ScrollView(this).apply { addView(grid) },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        )

        setContentView(root)
        load()
    }

    private fun load() {
        info.text = "جاري تحميل القنوات..."
        thread {
            try {
                val c = URL(channelsUrl).openConnection() as HttpURLConnection
                c.connectTimeout = 8000
                c.readTimeout = 8000
                val arr = JSONArray(c.inputStream.bufferedReader().readText())
                val names = ArrayList<String>()
                val urls = ArrayList<String>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    names.add(o.getString("name"))
                    urls.add(o.getString("url"))
                }
                runOnUiThread {
                    info.visibility = View.GONE
                    render(names, urls)
                }
            } catch (e: Exception) {
                runOnUiThread { info.text = "تعذر تحميل القنوات، تأكد من الأنترنت" }
            }
        }
    }

    private fun render(names: ArrayList<String>, urls: ArrayList<String>) {
        grid.removeAllViews()
        val w = (resources.displayMetrics.widthPixels - dp(32)) / 2
        for (i in names.indices) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                background = shape("#151B33", 18, "#2A3358")
                setPadding(dp(12), dp(28), dp(12), dp(28))
                setOnClickListener {
                    startActivity(
                        Intent(this@MainActivity, PlayerActivity::class.java)
                            .putStringArrayListExtra("names", names)
                            .putStringArrayListExtra("urls", urls)
                            .putExtra("index", i)
                    )
                }
            }
            card.addView(TextView(this).apply {
                text = names[i].take(1).uppercase()
                textSize = 26f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#0B1020"))
                background = shape("#D4AF37", 30)
            }, LinearLayout.LayoutParams(dp(60), dp(60)))
            card.addView(TextView(this).apply {
                text = names[i]
                textSize = 15f
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                setPadding(0, dp(12), 0, 0)
            })
            val lp = GridLayout.LayoutParams().apply {
                width = w - dp(8)
                height = GridLayout.LayoutParams.WRAP_CONTENT
                setMargins(dp(4), dp(4), dp(4), dp(4))
            }
            grid.addView(card, lp)
        }
    }
}
