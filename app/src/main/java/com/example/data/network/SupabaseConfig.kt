package com.example.data.network

import android.content.Context
import com.example.BuildConfig

object SupabaseConfig {
    const val DEFAULT_PROJECT_ID = "zmdjtcjbwimiiphjnvcd"
    const val DEFAULT_BASE_URL = "https://zmdjtcjbwimiiphjnvcd.supabase.co/"

    // Public anon key of the shared Salon Supabase project. It is designed to ship in clients; all
    // authorization is enforced by RLS and RPC checks. Never put the service_role key in this app.
    private const val PUBLIC_ANON_KEY =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InptZGp0Y2pid2ltaWlwaGpudmNkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODk4MjAzMzMsImV4cCI6MjEwNTM5NjMzM30.qeuC8iQgam09-c0UmWXnbSw1jN1E7SiUak_4xvNgRB4"

    private const val PREFS_NAME = "supabase_config_prefs"
    private const val KEY_ANON_KEY = "supabase_anon_key"

    private var cachedAnonKey: String? = null

    fun getAnonKey(context: Context): String {
        cachedAnonKey?.let { if (it.isNotBlank()) return it }

        // 1. Build-time key from .env / Secrets
        try {
            val buildKey = BuildConfig::class.java.getField("SUPABASE_ANON_KEY").get(null) as? String
            if (!buildKey.isNullOrBlank() && !buildKey.contains("YOUR_SUPABASE", ignoreCase = true)) {
                cachedAnonKey = buildKey
                return buildKey
            }
        } catch (_: Exception) {}

        // 2. Key entered in the in-app config dialog
        val saved = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_ANON_KEY, "") ?: ""
        cachedAnonKey = saved.ifBlank { PUBLIC_ANON_KEY }
        return cachedAnonKey!!
    }

    fun saveAnonKey(context: Context, key: String) {
        cachedAnonKey = key.trim().ifBlank { null }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(KEY_ANON_KEY, key.trim()).apply()
    }

    fun isConfigured(context: Context): Boolean = getAnonKey(context).isNotBlank()
}
