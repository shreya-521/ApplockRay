package com.applock
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.*

class LockActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        val pkg = intent.getStringExtra("p") ?: run { finish(); return }
        val label = try { packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)) } catch (e: Exception) { pkg }
        val l = LinearLayout(this); l.orientation = LinearLayout.VERTICAL; l.gravity = Gravity.CENTER
        try {
            val ic = ImageView(this); ic.setImageDrawable(packageManager.getApplicationIcon(pkg))
            l.addView(ic, LinearLayout.LayoutParams(dp(64), dp(64)).apply { bottomMargin = dp(12) })
        } catch (_: Exception) {}
        val a = AuthView(this, Store.type(this)) {
            if (Store.check(this, it)) { LockService.unlocked = pkg; finish(); true } else false
        }
        a.title("$label is locked")
        l.addView(a)
        setContentView(l)
    }
    override fun onNewIntent(i: Intent) { super.onNewIntent(i); setIntent(i); recreate() }
    @Deprecated("") override fun onBackPressed() {
        startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        finish()
    }
}
