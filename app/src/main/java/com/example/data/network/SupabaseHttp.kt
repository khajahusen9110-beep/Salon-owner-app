package com.example.data.network

import android.content.Context
import android.content.SharedPreferences
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/** An error safe to show to the salon owner; [pgCode] is the Postgres SQLSTATE when there is one. */
class SupabaseException(message: String, val httpCode: Int = 0, val pgCode: String? = null) : Exception(message)

/**
 * Minimal Supabase REST/Auth/Storage client for the owner app.
 *
 * Holds the user session (access + refresh token) and refreshes the JWT before it expires.
 * Only the public anon key is used; every permission is enforced by RLS and the SECURITY DEFINER
 * RPCs on the server.
 */
object SupabaseHttp {
    private const val PREFS = "salon_auth_prefs"
    const val KEY_ACCESS_TOKEN = "access_token"
    const val KEY_REFRESH_TOKEN = "refresh_token"
    const val KEY_EXPIRES_AT = "expires_at"
    const val KEY_USER_ID = "user_id"
    const val KEY_USER_EMAIL = "user_email"

    private val JSON = "application/json; charset=utf-8".toMediaType()
    private val refreshLock = Any()
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private lateinit var appContext: Context

    fun init(context: Context) {
        if (!::appContext.isInitialized) appContext = context.applicationContext
    }

    private val prefs: SharedPreferences
        get() = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val baseUrl: String get() = SupabaseConfig.DEFAULT_BASE_URL.trimEnd('/')
    private val anonKey: String get() = SupabaseConfig.getAnonKey(appContext)

    // ---------------- Session ----------------

    val userId: String? get() = prefs.getString(KEY_USER_ID, null)
    val userEmail: String? get() = prefs.getString(KEY_USER_EMAIL, null)
    val accessToken: String? get() = prefs.getString(KEY_ACCESS_TOKEN, null)

    /** True only for a session that can be renewed (older builds stored no refresh token). */
    val hasSession: Boolean
        get() = !accessToken.isNullOrBlank() && !prefs.getString(KEY_REFRESH_TOKEN, null).isNullOrBlank()

    fun saveSession(json: JSONObject, email: String? = null) {
        val token = json.optString("access_token")
        if (token.isEmpty()) return
        val expiresAt = json.optLong("expires_at", 0L).takeIf { it > 0 }
            ?: (System.currentTimeMillis() / 1000 + json.optLong("expires_in", 3600L))
        val user = json.optJSONObject("user")
        val editor = prefs.edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .putString(KEY_REFRESH_TOKEN, json.optString("refresh_token"))
            .putLong(KEY_EXPIRES_AT, expiresAt)
        user?.optString("id")?.takeIf { it.isNotEmpty() }?.let { editor.putString(KEY_USER_ID, it) }
        (email ?: user?.optString("email"))?.takeIf { it.isNotEmpty() }?.let { editor.putString(KEY_USER_EMAIL, it) }
        editor.apply()
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_EXPIRES_AT)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .apply()
    }

    private fun validAccessToken(): String? {
        synchronized(refreshLock) {
            val token = accessToken ?: return null
            val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
            if (expiresAt - 60 > System.currentTimeMillis() / 1000) return token
            val refresh = prefs.getString(KEY_REFRESH_TOKEN, null)
            if (refresh.isNullOrBlank()) return token
            return try {
                val request = Request.Builder()
                    .url("$baseUrl/auth/v1/token?grant_type=refresh_token")
                    .addHeader("apikey", anonKey)
                    .post(JSONObject().put("refresh_token", refresh).toString().toRequestBody(JSON))
                    .build()
                client.newCall(request).execute().use { response ->
                    val text = response.body?.string().orEmpty()
                    when {
                        response.isSuccessful -> {
                            saveSession(JSONObject(text))
                            accessToken
                        }
                        response.code in 400..401 -> {
                            clearSession()
                            null
                        }
                        else -> token
                    }
                }
            } catch (_: Exception) {
                token // offline or unexpected response: keep the current token, retry next call
            }
        }
    }

    // ---------------- Auth ----------------

    fun signIn(email: String, password: String): JSONObject {
        val body = JSONObject().put("email", email).put("password", password).toString()
        val json = JSONObject(execute("POST", "$baseUrl/auth/v1/token?grant_type=password", body, authenticated = false))
        saveSession(json, email)
        return json
    }

    fun signUp(email: String, password: String): JSONObject {
        val body = JSONObject().put("email", email).put("password", password).toString()
        val json = JSONObject(execute("POST", "$baseUrl/auth/v1/signup", body, authenticated = false))
        saveSession(json, email)
        return json
    }

    /** Clears the local session immediately and revokes the refresh token on the server in the background. */
    fun signOut() {
        val token = accessToken
        clearSession()
        if (token.isNullOrBlank()) return
        val key = anonKey
        Thread {
            try {
                val request = Request.Builder()
                    .url("$baseUrl/auth/v1/logout")
                    .addHeader("apikey", key)
                    .addHeader("Authorization", "Bearer $token")
                    .post("{}".toRequestBody(JSON))
                    .build()
                client.newCall(request).execute().close()
            } catch (_: IOException) {
                // Local sign-out already happened; the JWT expires on its own.
            }
        }.start()
    }

    // ---------------- REST / RPC / Storage ----------------

    fun select(pathAndQuery: String): JSONArray = JSONArray(execute("GET", "$baseUrl/rest/v1/$pathAndQuery", null))

    /** Insert/upsert rows and return the stored representation. */
    fun insert(table: String, body: Any, upsertOn: String? = null): JSONArray {
        val url = "$baseUrl/rest/v1/$table" + (upsertOn?.let { "?on_conflict=$it" } ?: "")
        val prefer = if (upsertOn != null) "return=representation,resolution=merge-duplicates" else "return=representation"
        return JSONArray(execute("POST", url, body.toString(), prefer = prefer))
    }

    /** PATCH rows matched by [filter]; returns the updated rows (empty if RLS hid them). */
    fun update(table: String, filter: String, body: JSONObject): JSONArray =
        JSONArray(execute("PATCH", "$baseUrl/rest/v1/$table?$filter", body.toString(), prefer = "return=representation"))

    fun delete(table: String, filter: String): JSONArray =
        JSONArray(execute("DELETE", "$baseUrl/rest/v1/$table?$filter", null, prefer = "return=representation"))

    /** Calls an RPC. With [single] the set-returning result is returned as one JSON object. */
    fun rpc(name: String, params: JSONObject = JSONObject(), single: Boolean = false): String =
        execute(
            "POST", "$baseUrl/rest/v1/rpc/$name", params.toString(),
            accept = if (single) "application/vnd.pgrst.object+json" else "application/json"
        )

    fun upload(bucket: String, path: String, bytes: ByteArray, mimeType: String) {
        val token = validAccessToken() ?: throw SupabaseException("Your session has expired. Please sign in again.", 401)
        val request = Request.Builder()
            .url("$baseUrl/storage/v1/object/$bucket/$path")
            .addHeader("apikey", anonKey)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("x-upsert", "true")
            .post(bytes.toRequestBody((mimeType.toMediaTypeOrNull() ?: "application/octet-stream".toMediaType())))
            .build()
        send(request)
    }

    /** Object names (full paths) directly under [folder] in [bucket]. */
    fun listObjects(bucket: String, folder: String): List<String> {
        val body = JSONObject().put("prefix", "$folder/").put("limit", 1000).toString()
        val rows = JSONArray(execute("POST", "$baseUrl/storage/v1/object/list/$bucket", body))
        return (0 until rows.length()).mapNotNull { i ->
            rows.getJSONObject(i).optString("name").takeIf { it.isNotBlank() }?.let { "$folder/$it" }
        }
    }

    fun removeObjects(bucket: String, paths: List<String>) {
        if (paths.isEmpty()) return
        execute("DELETE", "$baseUrl/storage/v1/object/$bucket", JSONObject().put("prefixes", JSONArray(paths)).toString())
    }

    fun publicUrl(bucket: String, path: String) = "$baseUrl/storage/v1/object/public/$bucket/$path"

    private fun execute(
        method: String,
        url: String,
        body: String?,
        authenticated: Boolean = true,
        prefer: String? = null,
        accept: String = "application/json"
    ): String {
        val token = if (authenticated) validAccessToken() else null
        if (authenticated && token == null) throw SupabaseException("Your session has expired. Please sign in again.", 401)
        val builder = Request.Builder()
            .url(url)
            .addHeader("apikey", anonKey)
            .addHeader("Authorization", "Bearer ${token ?: anonKey}")
            .addHeader("Accept", accept)
        if (prefer != null) builder.addHeader("Prefer", prefer)
        val requestBody = body?.toRequestBody(JSON)
        when (method) {
            "GET" -> builder.get()
            "DELETE" -> if (requestBody != null) builder.delete(requestBody) else builder.delete()
            else -> builder.method(method, requestBody ?: "".toRequestBody(JSON))
        }
        return send(builder.build())
    }

    private fun send(request: Request): String {
        try {
            client.newCall(request).execute().use { response ->
                val text = response.body?.string().orEmpty()
                if (response.isSuccessful) return text.ifBlank { "[]" }
                throw toException(response.code, text)
            }
        } catch (e: SupabaseException) {
            throw e
        } catch (e: IOException) {
            throw SupabaseException("No internet connection. Please check your network and try again.")
        }
    }

    private fun toException(code: Int, body: String): SupabaseException {
        var message = ""
        var pgCode: String? = null
        try {
            val json = JSONObject(body)
            message = listOf("message", "msg", "error_description", "error")
                .map { json.optString(it) }.firstOrNull { it.isNotBlank() }.orEmpty()
            pgCode = json.optString("code").takeIf { it.isNotBlank() }
        } catch (_: Exception) {
        }
        return SupabaseException(friendly(code, pgCode, message), code, pgCode)
    }

    /** Maps raw server errors to owner-friendly text; business messages raised by our RPCs pass through. */
    private fun friendly(code: Int, pgCode: String?, raw: String): String {
        val m = raw.lowercase()
        return when {
            m.contains("invalid login credentials") -> "Incorrect email or password."
            m.contains("email not confirmed") -> "Please confirm your email address, then sign in."
            m.contains("user already registered") -> "An account with this email already exists. Please sign in."
            m.contains("password should be") -> "Password must be at least 6 characters."
            m.contains("jwt") || code == 401 -> "Your session has expired. Please sign in again."
            pgCode == "23P01" -> "That stylist is already booked at this time."
            pgCode == "23505" -> "This already exists."
            pgCode == "23503" -> "This item is still in use and cannot be removed."
            pgCode == "23514" -> "Some values are not allowed. Please check the form."
            pgCode == "22P02" || pgCode == "22007" || pgCode == "22008" -> "Invalid value. Please check the form."
            pgCode == "42501" || m.contains("row-level security") || code == 403 -> "You are not allowed to do this."
            code == 413 || m.contains("exceeded the maximum") -> "File is too large (max 5 MB)."
            m.contains("mime type") -> "Only JPG, PNG, WEBP or PDF files are allowed."
            code == 429 || m.contains("rate limit") -> "Too many attempts. Please wait a moment and try again."
            code >= 500 -> "Server is busy. Please try again."
            pgCode == "P0001" && raw.isNotBlank() -> raw // raise exception '...' from our RPCs
            else -> "Something went wrong. Please try again."
        }
    }
}

/** Salon times are Indian Standard Time; the server stores/returns UTC timestamps. */
object IstTime {
    private val ist = TimeZone.getTimeZone("Asia/Kolkata")

    private fun fmt(pattern: String) = SimpleDateFormat(pattern, Locale.US).apply { timeZone = ist }

    fun today(): String = fmt("yyyy-MM-dd").format(Date())

    /** Current instant as "2026-10-07T10:00:00+05:30". */
    fun now(): String = fmt("yyyy-MM-dd'T'HH:mm:ssXXX").format(Date())

    /** "2026-10-07T04:30:00.12+00:00" -> "2026-10-07T10:00:00" (IST wall clock, the format the UI renders). */
    fun toLocal(iso: String?): String? {
        if (iso.isNullOrBlank()) return iso
        return try {
            val clean = iso.replace(Regex("\\.\\d+"), "").replace(" ", "T")
            val parsed = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).parse(clean) ?: return iso
            fmt("yyyy-MM-dd'T'HH:mm:ss").format(parsed)
        } catch (_: Exception) {
            iso
        }
    }

    /** "2026-10-07T10:00:00" (IST wall clock from the UI) -> "2026-10-07T10:00:00+05:30". */
    fun toOffset(local: String): String {
        val t = local.trim()
        return if (Regex("(Z|[+-]\\d{2}:?\\d{2})$").containsMatchIn(t)) t else "$t+05:30"
    }

    /** "10:00:00" -> "10:00" */
    fun hhmm(time: String?): String = time?.take(5).orEmpty()
}
