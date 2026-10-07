package com.example.data.network

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object SupabaseConfig {
    const val DEFAULT_PROJECT_ID = "zmdjtcjbwimiiphjnvcd"
    const val DEFAULT_BASE_URL = "https://zmdjtcjbwimiiphjnvcd.supabase.co/"
    private const val PREFS_NAME = "supabase_config_prefs"
    private const val KEY_ANON_KEY = "supabase_anon_key"

    private var cachedAnonKey: String? = null

    fun getAnonKey(context: Context): String {
        cachedAnonKey?.let { if (it.isNotBlank()) return it }

        // 1. Try BuildConfig if injected from Secrets or .env
        try {
            val buildKey = BuildConfig::class.java.getField("SUPABASE_ANON_KEY").get(null) as? String
            if (!buildKey.isNullOrBlank() && !buildKey.contains("YOUR_SUPABASE", ignoreCase = true)) {
                cachedAnonKey = buildKey
                return buildKey
            }
        } catch (_: Exception) {}

        // 2. Try SharedPreferences
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_ANON_KEY, "") ?: ""
        if (saved.isNotBlank()) {
            cachedAnonKey = saved
            return saved
        }

        return ""
    }

    fun saveAnonKey(context: Context, key: String) {
        cachedAnonKey = key.trim()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ANON_KEY, key.trim()).apply()
    }

    fun isConfigured(context: Context): Boolean {
        return getAnonKey(context).isNotBlank()
    }
}

object SupabaseClient {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(SupabaseConfig.DEFAULT_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val authApi: SupabaseAuthApi = retrofit.create(SupabaseAuthApi::class.java)
    val restApi: SupabaseRestApi = retrofit.create(SupabaseRestApi::class.java)
}
