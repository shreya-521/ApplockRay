package com.applock
import android.content.Context
import android.graphics.*
import android.view.*
import android.widget.*

fun Context.dp(x: Int) = (x * resources.displayMetrics.density).toInt()

class PatternView(c: Context, private val done: (String) -> Unit) : View(c) {
    private val sel = ArrayList<Int>()
    private var fx = 0f; private var fy = 0f; private var down = false
    private val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xAAFFFFFF.toInt(); strokeWidth = 10f; strokeCap = Paint.Cap.ROUND }
    private fun cell() = width / 3f
    private fun x(i: Int) = (i % 3 + .5f) * cell()
    private fun y(i: Int) = (i / 3 + .5f) * cell()

    override fun onDraw(cv: Canvas) {
        for (k in 1 until sel.size) cv.drawLine(x(sel[k - 1]), y(sel[k - 1]), x(sel[k]), y(sel[k]), line)
        if (down && sel.isNotEmpty()) cv.drawLine(x(sel.last()), y(sel.last()), fx, fy, line)
        for (i in 0..8) cv.drawCircle(x(i), y(i), cell() * if (i in sel) .13f else .07f, dot)
    }
    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> { sel.clear(); down = true; hit(e) }
            MotionEvent.ACTION_MOVE -> hit(e)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                down = false; val s = sel.joinToString(""); sel.clear(); if (s.isNotEmpty()) done(s)
            }
        }
        fx = e.x; fy = e.y; invalidate(); return true
    }
    private fun hit(e: MotionEvent) {
        for (i in 0..8) if (i !in sel && Math.hypot((e.x - x(i)).toDouble(), (e.y - y(i)).toDouble()) < cell() * .3) { sel.add(i); break }
    }
}

/** PIN keypad (4 digits) or 9-dot pattern. cb returns true if the entry was accepted. */
class AuthView(c: Context, type: String, private val cb: (String) -> Boolean) : LinearLayout(c) {
    val msg = TextView(c)
    private val dots = TextView(c)
    private var base = ""; private var buf = ""; private var fails = 0; private var blocked = false

    init {
        orientation = VERTICAL; gravity = Gravity.CENTER_HORIZONTAL
        msg.setTextColor(Color.WHITE); msg.textSize = 18f; msg.gravity = Gravity.CENTER
        addView(msg, LayoutParams(-1, -2).apply { bottomMargin = c.dp(20) })
        if (type == "pin") pin(c) else pattern(c)
    }
    fun title(t: String) { base = t; msg.text = t }

    private fun pin(c: Context) {
        dots.setTextColor(Color.WHITE); dots.textSize = 32f; dots.gravity = Gravity.CENTER; upd()
        addView(dots, LayoutParams(-1, -2).apply { bottomMargin = c.dp(16) })
        val g = GridLayout(c); g.columnCount = 3
        for (k in listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "⌫")) {
            val t = TextView(c); t.text = k; t.textSize = 28f; t.setTextColor(Color.WHITE); t.gravity = Gravity.CENTER
            if (k.isNotEmpty()) t.setOnClickListener { press(k) }
            g.addView(t, GridLayout.LayoutParams().apply { width = c.dp(88); height = c.dp(68) })
        }
        addView(g)
    }
    private fun pattern(c: Context) {
        addView(PatternView(c) { s ->
            if (!blocked) { if (s.length < 4) msg.text = "Connect at least 4 dots" else if (!cb(s)) fail() }
        }, LayoutParams(c.dp(300), c.dp(300)))
    }
    private fun upd() { dots.text = (0..3).joinToString("  ") { if (it < buf.length) "●" else "○" } }
    private fun press(k: String) {
        if (blocked) return
        if (k == "⌫") buf = buf.dropLast(1) else if (buf.length < 4) buf += k
        upd()
        if (buf.length == 4) { val s = buf; buf = ""; upd(); if (!cb(s)) fail() }
    }
    private fun fail() {
        if (++fails >= 5) {
            blocked = true; msg.text = "Too many attempts. Wait 30 seconds"
            postDelayed({ blocked = false; fails = 0; msg.text = base }, 30000)
        } else msg.text = "Wrong, try again"
    }
}
