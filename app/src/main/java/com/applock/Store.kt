package com.applock
import android.content.Context
import java.security.MessageDigest
import java.util.UUID

object Store {
    private fun p(c: Context) = c.getSharedPreferences("s", 0)
    private fun hash(salt: String, s: String) =
        MessageDigest.getInstance("SHA-256").digest((salt + s).toByteArray()).joinToString("") { "%02x".format(it) }
    fun isSet(c: Context) = p(c).contains("h")
    fun type(c: Context) = p(c).getString("t", "pin")!!
    fun save(c: Context, type: String, secret: String) {
        val salt = UUID.randomUUID().toString()
        p(c).edit().putString("t", type).putString("salt", salt).putString("h", hash(salt, secret)).apply()
    }
    fun check(c: Context, secret: String) = p(c).let { hash(it.getString("salt", "")!!, secret) == it.getString("h", "") }
    fun locked(c: Context): MutableSet<String> = HashSet(p(c).getStringSet("l", emptySet())!!)
    fun setLocked(c: Context, s: Set<String>) = p(c).edit().putStringSet("l", s).apply()
}
