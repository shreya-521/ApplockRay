package com.applock
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.provider.Settings
import android.view.*
import android.widget.*

class MainActivity : Activity() {
    private var authed = false

    override fun onStart() { super.onStart(); if (!Store.isSet(this)) choose() else unlock() }
    override fun onStop() { super.onStop(); authed = false }

    private fun screen(center: Boolean): LinearLayout {
        val l = LinearLayout(this); l.orientation = LinearLayout.VERTICAL
        l.gravity = if (center) Gravity.CENTER else Gravity.CENTER_HORIZONTAL
        l.setPadding(dp(20), dp(48), dp(20), dp(20)); setContentView(l); return l
    }
    private fun tv(t: String, s: Float) = TextView(this).apply { text = t; textSize = s; setTextColor(Color.WHITE); gravity = Gravity.CENTER }
    private fun btn(t: String, f: () -> Unit) = Button(this).apply {
        text = t; setTextColor(Color.WHITE); isAllCaps = false
        background = GradientDrawable().apply { setColor(0x33FFFFFF); cornerRadius = dp(24).toFloat() }
        setOnClickListener { f() }
    }
    private fun lp() = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(10) }

    private fun choose() {
        val l = screen(true)
        l.addView(tv("Choose your lock", 26f))
        l.addView(btn("4-digit PIN") { create("pin", null) }, lp())
        l.addView(btn("Pattern (9 dots)") { create("pattern", null) }, lp())
    }
    private fun create(type: String, first: String?) {
        val l = screen(true)
        val a = AuthView(this, type) { s ->
            when {
                first == null -> { create(type, s); true }
                s == first -> { Store.save(this, type, s); authed = true; main(); true }
                else -> false
            }
        }
        a.title(if (first == null) (if (type == "pin") "Enter a new 4-digit PIN" else "Draw a new pattern") else "Repeat to confirm")
        l.addView(a)
        if (first != null) l.addView(btn("Start over") { choose() }, lp())
    }
    private fun unlock() {
        val l = screen(true)
        val a = AuthView(this, Store.type(this)) { if (Store.check(this, it)) { authed = true; main(); true } else false }
        a.title("Unlock AppLock")
        l.addView(a)
    }
    private fun enabled() = (Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: "")
        .contains("$packageName/${LockService::class.java.name}")

    private fun main() {
        val l = screen(false)
        l.addView(tv("AppLock", 30f))
        val on = enabled()
        l.addView(tv(if (on) "Protection is ON" else "Protection is OFF", 14f))
        if (!on) l.addView(btn("Enable protection") { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }, lp())
        l.addView(btn("Change lock") { choose() }, lp())
        l.addView(tv("Tap apps to lock them", 16f), lp())

        val pm = packageManager
        val apps = pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
            .map { it.activityInfo.packageName to it.loadLabel(pm).toString() }
            .filter { it.first != packageName }.distinctBy { it.first }.sortedBy { it.second.lowercase() }
        val set = Store.locked(this)
        val lv = ListView(this); lv.divider = null
        lv.adapter = object : BaseAdapter() {
            override fun getCount() = apps.size
            override fun getItem(i: Int) = apps[i]
            override fun getItemId(i: Int) = i.toLong()
            override fun getView(i: Int, v: View?, p: ViewGroup?): View {
                val ctx = this@MainActivity
                val row = (v as? LinearLayout) ?: LinearLayout(ctx).apply {
                    gravity = Gravity.CENTER_VERTICAL; setPadding(0, dp(8), 0, dp(8))
                    addView(ImageView(ctx), LinearLayout.LayoutParams(dp(40), dp(40)))
                    addView(TextView(ctx).apply { setTextColor(Color.WHITE); textSize = 16f; setPadding(dp(12), 0, 0, 0) }, LinearLayout.LayoutParams(0, -2, 1f))
                    addView(Switch(ctx).apply { isClickable = false; isFocusable = false })
                }
                val (pkg, label) = apps[i]
                try { (row.getChildAt(0) as ImageView).setImageDrawable(pm.getApplicationIcon(pkg)) } catch (_: Exception) {}
                (row.getChildAt(1) as TextView).text = label
                (row.getChildAt(2) as Switch).isChecked = pkg in set
                return row
            }
        }
        lv.setOnItemClickListener { _, v, i, _ ->
            val pkg = apps[i].first
            if (!set.remove(pkg)) set.add(pkg)
            Store.setLocked(this, set); LockService.locked = HashSet(set)
            ((v as LinearLayout).getChildAt(2) as Switch).isChecked = pkg in set
        }
        l.addView(lv, LinearLayout.LayoutParams(-1, 0, 1f))
    }
}
