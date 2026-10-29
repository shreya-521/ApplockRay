package com.applock
import android.accessibilityservice.AccessibilityService
import android.content.*
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager

// Event-driven (no polling, no timers): wakes only when the foreground window changes.
class LockService : AccessibilityService() {
    companion object {
        @Volatile var locked: Set<String> = emptySet()
        @Volatile var unlocked: String? = null
    }
    private var current: String? = null
    private var ime: Set<String> = emptySet()
    private val off = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) { unlocked = null; current = null }
    }

    override fun onServiceConnected() {
        locked = Store.locked(this)
        ime = (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).enabledInputMethodList.map { it.packageName }.toSet()
        registerReceiver(off, IntentFilter(Intent.ACTION_SCREEN_OFF)) // re-lock when screen turns off
    }

    override fun onAccessibilityEvent(e: AccessibilityEvent) {
        val pkg = e.packageName?.toString() ?: return
        if (pkg == packageName || pkg == "com.android.systemui" || pkg == "android" || pkg in ime) return
        if (pkg == current) return
        current = pkg
        if (pkg != unlocked) unlocked = null // user left the unlocked app -> lock it again
        if (pkg in locked && pkg != unlocked)
            startActivity(Intent(this, LockActivity::class.java).putExtra("p", pkg)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION))
    }

    override fun onInterrupt() {}
    override fun onDestroy() { try { unregisterReceiver(off) } catch (_: Exception) {}; super.onDestroy() }
}
