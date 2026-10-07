package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AuthResponse
import com.example.data.model.Profile
import com.example.data.model.Salon
import com.example.data.model.SupabaseUser
import com.example.data.network.SupabaseException
import com.example.data.network.SupabaseHttp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val code: Int? = null) : AuthResult<Nothing>()
}

class AuthRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("salon_auth_prefs", Context.MODE_PRIVATE)

    /** The owner's salon as last loaded from the server (owners have at most one salon). */
    @Volatile
    private var currentSalon: Salon? = null

    init {
        SupabaseHttp.init(context)
    }

    companion object {
        private const val KEY_LANGUAGE = "user_language"
        private const val SALON_COLUMNS =
            "id,owner_id,name,description,salon_type,address,area,city,pincode,phone,latitude,longitude," +
                "gst_number,verification_status,rejection_reason,photos,cover_photo_index,is_verified," +
                "rating_avg,rating_count,slot_interval_minutes,booking_window_days,min_notice_minutes," +
                "late_threshold_minutes,late_credit_amount,is_active"

        fun parseSalon(o: JSONObject): Salon {
            val photos = o.optJSONArray("photos") ?: JSONArray()
            return Salon(
                id = o.getString("id"),
                ownerId = o.optStringOrNull("owner_id"),
                name = o.optString("name"),
                description = o.optStringOrNull("description"),
                salonType = o.optString("salon_type", "unisex"),
                address = o.optStringOrNull("address").orEmpty(),
                area = o.optString("area"),
                city = o.optString("city"),
                pincode = o.optStringOrNull("pincode").orEmpty(),
                phone = o.optStringOrNull("phone").orEmpty(),
                latitude = if (o.isNull("latitude")) null else o.optDouble("latitude"),
                longitude = if (o.isNull("longitude")) null else o.optDouble("longitude"),
                gstNumber = o.optStringOrNull("gst_number"),
                verificationStatus = o.optString("verification_status", "draft"),
                rejectionReason = o.optStringOrNull("rejection_reason"),
                photos = (0 until photos.length()).map { photos.getString(it) },
                coverPhotoIndex = o.optInt("cover_photo_index", 0),
                isVerified = o.optBoolean("is_verified", false),
                ratingAvg = o.optDouble("rating_avg", 0.0),
                ratingCount = o.optInt("rating_count", 0),
                slotIntervalMinutes = o.optInt("slot_interval_minutes", 30),
                bookingWindowDays = o.optInt("booking_window_days", 7),
                minNoticeMinutes = o.optInt("min_notice_minutes", 30),
                lateThresholdMinutes = o.optInt("late_threshold_minutes", 15),
                lateCreditAmount = o.optDouble("late_credit_amount", 0.0),
                isActive = o.optBoolean("is_active", true)
            )
        }
    }

    fun getAccessToken(): String? = if (SupabaseHttp.hasSession) SupabaseHttp.accessToken else null
    fun getUserId(): String? = if (SupabaseHttp.hasSession) SupabaseHttp.userId else null
    fun getUserEmail(): String? = if (SupabaseHttp.hasSession) SupabaseHttp.userEmail else null
    fun getSavedLanguage(): String = prefs.getString(KEY_LANGUAGE, "en") ?: "en"

    fun saveLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
    }

    fun clearSession() {
        currentSalon = null
        SupabaseHttp.signOut()
    }

    suspend fun signUp(email: String, pass: String): AuthResult<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val json = SupabaseHttp.signUp(email.trim(), pass)
            if (json.optString("access_token").isEmpty()) {
                // Email confirmation is enabled: the account exists but there is no session yet.
                return@withContext AuthResult.Error("Account created. Please confirm your email, then sign in.")
            }
            AuthResult.Success(json.toAuthResponse(email.trim()))
        } catch (e: SupabaseException) {
            AuthResult.Error(e.message ?: "Sign up failed", e.httpCode)
        }
    }

    suspend fun signIn(email: String, pass: String): AuthResult<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            currentSalon = null
            AuthResult.Success(SupabaseHttp.signIn(email.trim(), pass).toAuthResponse(email.trim()))
        } catch (e: SupabaseException) {
            AuthResult.Error(e.message ?: "Login failed", e.httpCode)
        }
    }

    suspend fun fetchProfile(userId: String): Profile? = withContext(Dispatchers.IO) {
        try {
            val rows = SupabaseHttp.select("profiles?id=eq.$userId&select=id,full_name,language,phone,role")
            if (rows.length() == 0) return@withContext null
            val o = rows.getJSONObject(0)
            Profile(
                id = o.getString("id"),
                fullName = o.optStringOrNull("full_name"),
                language = o.optStringOrNull("language") ?: getSavedLanguage(),
                phone = o.optStringOrNull("phone"),
                role = o.optStringOrNull("role")
            )
        } catch (_: SupabaseException) {
            null
        }
    }

    /** Loads the signed-in owner's salon (the salons policy also exposes live salons, so filter by owner). */
    suspend fun fetchSalons(): List<Salon> = withContext(Dispatchers.IO) {
        val uid = getUserId() ?: return@withContext emptyList()
        try {
            val rows = SupabaseHttp.select("salons?owner_id=eq.$uid&select=$SALON_COLUMNS")
            val list = (0 until rows.length()).map { parseSalon(rows.getJSONObject(it)) }
            currentSalon = list.firstOrNull()
            list
        } catch (_: SupabaseException) {
            listOfNotNull(currentSalon)
        }
    }

    /** Last loaded salon without a network call. */
    fun fetchSalonsNow(): Salon? = currentSalon

    fun saveLocalSalon(salon: Salon) {
        currentSalon = salon
    }

    private fun JSONObject.toAuthResponse(email: String): AuthResponse {
        val user = optJSONObject("user")
        return AuthResponse(
            accessToken = optStringOrNull("access_token"),
            tokenType = optStringOrNull("token_type"),
            expiresIn = optLong("expires_in"),
            refreshToken = optStringOrNull("refresh_token"),
            user = user?.let {
                SupabaseUser(id = it.optString("id"), email = it.optStringOrNull("email") ?: email, phone = it.optStringOrNull("phone"))
            }
        )
    }
}

internal fun JSONObject.optStringOrNull(key: String): String? =
    if (!has(key) || isNull(key)) null else optString(key).takeIf { it.isNotEmpty() }
