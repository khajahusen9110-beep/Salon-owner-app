package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SupabaseUser(
    @Json(name = "id") val id: String,
    @Json(name = "email") val email: String? = null,
    @Json(name = "phone") val phone: String? = null
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "access_token") val accessToken: String? = null,
    @Json(name = "token_type") val tokenType: String? = null,
    @Json(name = "expires_in") val expiresIn: Long? = null,
    @Json(name = "refresh_token") val refreshToken: String? = null,
    @Json(name = "user") val user: SupabaseUser? = null
)

@JsonClass(generateAdapter = true)
data class SignUpRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class Profile(
    @Json(name = "id") val id: String,
    @Json(name = "full_name") val fullName: String? = null,
    @Json(name = "language") val language: String? = "en",
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "role") val role: String? = "salon_owner"
)

@JsonClass(generateAdapter = true)
data class Salon(
    @Json(name = "id") val id: String,
    @Json(name = "owner_id") val ownerId: String? = null,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "salon_type") val salonType: String = "unisex", // men, women, unisex
    @Json(name = "address") val address: String,
    @Json(name = "area") val area: String,
    @Json(name = "city") val city: String,
    @Json(name = "pincode") val pincode: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null,
    @Json(name = "gst_number") val gstNumber: String? = null,
    @Json(name = "verification_status") val verificationStatus: String = "draft", // draft, pending, approved, rejected
    @Json(name = "rejection_reason") val rejectionReason: String? = null,
    @Json(name = "photos") val photos: List<String> = emptyList(),
    @Json(name = "cover_photo_index") val coverPhotoIndex: Int = 0,
    @Json(name = "is_verified") val isVerified: Boolean = false,
    @Json(name = "rating_avg") val ratingAvg: Double = 4.8,
    @Json(name = "rating_count") val ratingCount: Int = 124,
    @Json(name = "slot_interval_minutes") val slotIntervalMinutes: Int = 30,
    @Json(name = "booking_window_days") val bookingWindowDays: Int = 7,
    @Json(name = "min_notice_minutes") val minNoticeMinutes: Int = 30,
    @Json(name = "late_threshold_minutes") val lateThresholdMinutes: Int = 15,
    @Json(name = "late_credit_amount") val lateCreditAmount: Double = 0.0,
    @Json(name = "is_active") val isActive: Boolean = true
)

@JsonClass(generateAdapter = true)
data class SalonDocument(
    @Json(name = "id") val id: String? = null,
    @Json(name = "salon_id") val salonId: String,
    @Json(name = "doc_type") val docType: String,
    @Json(name = "file_path") val filePath: String
)

@JsonClass(generateAdapter = true)
data class CreateMySalonParams(
    @Json(name = "p_owner_name") val ownerName: String,
    @Json(name = "p_name") val name: String,
    @Json(name = "p_salon_type") val salonType: String,
    @Json(name = "p_address") val address: String,
    @Json(name = "p_area") val area: String,
    @Json(name = "p_city") val city: String,
    @Json(name = "p_pincode") val pincode: String,
    @Json(name = "p_phone") val phone: String,
    @Json(name = "p_latitude") val latitude: Double? = null,
    @Json(name = "p_longitude") val longitude: Double? = null,
    @Json(name = "p_gst_number") val gstNumber: String? = null,
    @Json(name = "p_language") val language: String = "en"
)

@JsonClass(generateAdapter = true)
data class SubmitVerificationParams(
    @Json(name = "p_accept_terms") val acceptTerms: Boolean = true
)

// --- Part 2 & 3: Dashboard, Bookings, Customer History & Time Off Models ---

@JsonClass(generateAdapter = true)
data class OwnerDashboard(
    @Json(name = "today_bookings") val todayBookings: Int = 0,
    @Json(name = "today_completed") val todayCompleted: Int = 0,
    @Json(name = "today_upcoming") val todayUpcoming: Int = 0,
    @Json(name = "today_revenue") val todayRevenue: Double = 0.0,
    @Json(name = "today_no_shows") val todayNoShows: Int = 0,
    @Json(name = "pending_reviews_reply") val pendingReviewsReply: Int = 0,
    @Json(name = "salon_name") val salonName: String? = null,
    @Json(name = "verification_status") val verificationStatus: String? = "approved",
    @Json(name = "is_active") val isActive: Boolean = true
)

@JsonClass(generateAdapter = true)
data class Booking(
    @Json(name = "id") val id: String,
    @Json(name = "staff_id") val staffId: String? = null,
    @Json(name = "staff_name") val staffName: String? = null,
    @Json(name = "service_id") val serviceId: String? = null,
    @Json(name = "service_name") val serviceName: String? = null,
    @Json(name = "duration_minutes") val durationMinutes: Int? = 45,
    @Json(name = "customer_id") val customerId: String? = null,
    @Json(name = "customer_name") val customerName: String? = null,
    @Json(name = "customer_phone") val customerPhone: String? = null,
    @Json(name = "start_time") val startTime: String,
    @Json(name = "end_time") val endTime: String? = null,
    @Json(name = "status") val status: String = "confirmed", // confirmed, arrived, in_service, completed, cancelled, no_show
    @Json(name = "price") val price: Double? = 0.0,
    @Json(name = "source") val source: String? = "online",
    @Json(name = "notes") val notes: String? = null
)

@JsonClass(generateAdapter = true)
data class Staff(
    @Json(name = "id") val id: String,
    @Json(name = "salon_id") val salonId: String? = null,
    @Json(name = "name") val name: String,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "role") val role: String? = "Stylist",
    @Json(name = "photo_url") val photoUrl: String? = null,
    @Json(name = "commission_percent") val commissionPercent: Double = 20.0,
    @Json(name = "rating_avg") val ratingAvg: Double = 4.8,
    @Json(name = "rating_count") val ratingCount: Int = 36,
    @Json(name = "is_active") val isActive: Boolean = true
)

@JsonClass(generateAdapter = true)
data class SalonService(
    @Json(name = "id") val id: String,
    @Json(name = "salon_id") val salonId: String? = null,
    @Json(name = "category_id") val categoryId: String? = null,
    @Json(name = "name") val name: String,
    @Json(name = "category") val category: String? = "Hair",
    @Json(name = "price") val price: Double,
    @Json(name = "duration_mins") val durationMins: Int? = 30,
    @Json(name = "buffer_mins") val bufferMins: Int? = 0,
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "assigned_staff_ids") val assignedStaffIds: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CreateWalkInParams(
    @Json(name = "p_staff_id") val staffId: String,
    @Json(name = "p_service_id") val serviceId: String,
    @Json(name = "p_name") val name: String,
    @Json(name = "p_phone") val phone: String? = null
)

@JsonClass(generateAdapter = true)
data class GetOwnerBookingsParams(
    @Json(name = "p_from") val fromDate: String,
    @Json(name = "p_to") val toDate: String,
    @Json(name = "p_staff_id") val staffId: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateBookingStatusRequest(
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class UpdateSalonsActiveRequest(
    @Json(name = "is_active") val isActive: Boolean
)

// --- Part 3 Models: Customer Summary, Reschedule, Breaks & Time Off ---

@JsonClass(generateAdapter = true)
data class CustomerSummary(
    @Json(name = "total_visits") val totalVisits: Int = 0,
    @Json(name = "no_show_count") val noShowCount: Int = 0,
    @Json(name = "total_spent") val totalSpent: Double = 0.0,
    @Json(name = "last_visit_date") val lastVisitDate: String? = null,
    @Json(name = "customer_name") val customerName: String? = null,
    @Json(name = "customer_phone") val customerPhone: String? = null
)

@JsonClass(generateAdapter = true)
data class CustomerSummaryParams(
    @Json(name = "p_booking_id") val bookingId: String
)

@JsonClass(generateAdapter = true)
data class RescheduleBookingParams(
    @Json(name = "p_booking_id") val bookingId: String,
    @Json(name = "p_new_start") val newStart: String
)

@JsonClass(generateAdapter = true)
data class RecurringBreak(
    @Json(name = "id") val id: String = "",
    @Json(name = "salon_id") val salonId: String? = null,
    @Json(name = "staff_id") val staffId: String? = null,
    @Json(name = "staff_name") val staffName: String? = null,
    @Json(name = "day_of_week") val dayOfWeek: Int? = null, // null for every day, 0=Sun, 1=Mon, ..., 6=Sat
    @Json(name = "start_time") val startTime: String, // e.g. "14:00"
    @Json(name = "end_time") val endTime: String,     // e.g. "15:00"
    @Json(name = "label") val label: String           // e.g. "Lunch"
)

@JsonClass(generateAdapter = true)
data class StaffTimeOff(
    @Json(name = "id") val id: String = "",
    @Json(name = "salon_id") val salonId: String? = null,
    @Json(name = "staff_id") val staffId: String? = null,
    @Json(name = "staff_name") val staffName: String? = null,
    @Json(name = "start_time") val startTime: String, // ISO timestamp
    @Json(name = "end_time") val endTime: String,     // ISO timestamp
    @Json(name = "reason") val reason: String? = null
)

@JsonClass(generateAdapter = true)
data class TimeOffConflictParams(
    @Json(name = "p_staff_id") val staffId: String? = null,
    @Json(name = "p_start") val start: String,
    @Json(name = "p_end") val end: String
)

@JsonClass(generateAdapter = true)
data class AddTimeOffParams(
    @Json(name = "p_staff_id") val staffId: String? = null,
    @Json(name = "p_start") val start: String,
    @Json(name = "p_end") val end: String,
    @Json(name = "p_reason") val reason: String? = null,
    @Json(name = "p_cancel_conflicts") val cancelConflicts: Boolean = false
)

// --- Part 5 Models: Earnings Summary, Reviews, Notifications, Profile Settings ---

@JsonClass(generateAdapter = true)
data class EarningsSummaryParams(
    @Json(name = "p_from") val fromDate: String,
    @Json(name = "p_to") val toDate: String
)

@JsonClass(generateAdapter = true)
data class StaffEarningsSummary(
    @Json(name = "staff_name") val staffName: String,
    @Json(name = "completed_count") val completedCount: Int = 0,
    @Json(name = "revenue") val revenue: Double = 0.0,
    @Json(name = "commission_percent") val commissionPercent: Double = 0.0,
    @Json(name = "commission_amount") val commissionAmount: Double = 0.0,
    @Json(name = "no_show_count") val noShowCount: Int = 0,
    @Json(name = "cancelled_count") val cancelledCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class SalonReview(
    @Json(name = "id") val id: String = "",
    @Json(name = "salon_id") val salonId: String,
    @Json(name = "booking_id") val bookingId: String? = null,
    @Json(name = "customer_id") val customerId: String? = null,
    @Json(name = "customer_name") val customerName: String? = "Valued Customer",
    @Json(name = "rating") val rating: Double = 5.0,
    @Json(name = "comment") val comment: String? = null,
    @Json(name = "staff_id") val staffId: String? = null,
    @Json(name = "staff_name") val staffName: String? = null,
    @Json(name = "owner_reply") val ownerReply: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateReviewReplyRequest(
    @Json(name = "owner_reply") val ownerReply: String
)

@JsonClass(generateAdapter = true)
data class SalonNotification(
    @Json(name = "id") val id: String = "",
    @Json(name = "user_id") val userId: String = "",
    @Json(name = "type") val type: String = "reminder",
    @Json(name = "title") val title: String = "",
    @Json(name = "body") val body: String = "",
    @Json(name = "booking_id") val bookingId: String? = null,
    @Json(name = "is_read") val isRead: Boolean = false,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateNotificationReadRequest(
    @Json(name = "is_read") val isRead: Boolean = true
)

@JsonClass(generateAdapter = true)
data class UpdateProfileLanguageRequest(
    @Json(name = "language") val language: String
)


@JsonClass(generateAdapter = true)
data class UpdateSalonProfileRequest(
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "salon_type") val salonType: String = "unisex",
    @Json(name = "address") val address: String,
    @Json(name = "area") val area: String,
    @Json(name = "city") val city: String,
    @Json(name = "pincode") val pincode: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "gst_number") val gstNumber: String? = null,
    @Json(name = "photos") val photos: List<String> = emptyList(),
    @Json(name = "cover_photo_index") val coverPhotoIndex: Int = 0,
    @Json(name = "is_active") val isActive: Boolean = true
)

@JsonClass(generateAdapter = true)
data class UpdateSalonSettingsRequest(
    @Json(name = "slot_interval_minutes") val slotIntervalMinutes: Int = 30,
    @Json(name = "booking_window_days") val bookingWindowDays: Int = 7,
    @Json(name = "min_notice_minutes") val minNoticeMinutes: Int = 30,
    @Json(name = "late_threshold_minutes") val lateThresholdMinutes: Int = 15,
    @Json(name = "late_credit_amount") val lateCreditAmount: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class ServiceCategory(
    @Json(name = "id") val id: String = "",
    @Json(name = "salon_id") val salonId: String? = null,
    @Json(name = "name") val name: String,
    @Json(name = "sort_order") val sortOrder: Int = 0
)

@JsonClass(generateAdapter = true)
data class CreateServiceCategoryRequest(
    @Json(name = "salon_id") val salonId: String,
    @Json(name = "name") val name: String,
    @Json(name = "sort_order") val sortOrder: Int = 0
)

@JsonClass(generateAdapter = true)
data class UpdateServiceCategoryRequest(
    @Json(name = "name") val name: String,
    @Json(name = "sort_order") val sortOrder: Int = 0
)

@JsonClass(generateAdapter = true)
data class StaffService(
    @Json(name = "staff_id") val staffId: String,
    @Json(name = "service_id") val serviceId: String
)

@JsonClass(generateAdapter = true)
data class Combo(
    @Json(name = "id") val id: String = "",
    @Json(name = "salon_id") val salonId: String? = null,
    @Json(name = "name") val name: String,
    @Json(name = "price") val price: Double,
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "service_ids") val serviceIds: List<String> = emptyList(),
    @Json(name = "total_duration_mins") val totalDurationMins: Int = 0
)

@JsonClass(generateAdapter = true)
data class ComboService(
    @Json(name = "combo_id") val comboId: String,
    @Json(name = "service_id") val serviceId: String
)

@JsonClass(generateAdapter = true)
data class ComboDetailsParams(
    @Json(name = "p_combo_id") val comboId: String
)

@JsonClass(generateAdapter = true)
data class ComboDetailsResponse(
    @Json(name = "id") val id: String? = null,
    @Json(name = "total_duration_mins") val totalDurationMins: Int = 0,
    @Json(name = "service_count") val serviceCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class CreateStaffRequest(
    @Json(name = "salon_id") val salonId: String,
    @Json(name = "name") val name: String,
    @Json(name = "role") val role: String = "Stylist",
    @Json(name = "photo_url") val photoUrl: String? = null,
    @Json(name = "commission_percent") val commissionPercent: Double = 20.0,
    @Json(name = "is_active") val isActive: Boolean = true
)

@JsonClass(generateAdapter = true)
data class UpdateStaffRequest(
    @Json(name = "name") val name: String? = null,
    @Json(name = "photo_url") val photoUrl: String? = null,
    @Json(name = "commission_percent") val commissionPercent: Double? = null,
    @Json(name = "is_active") val isActive: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class StaffHours(
    @Json(name = "id") val id: String = "",
    @Json(name = "staff_id") val staffId: String,
    @Json(name = "day_of_week") val dayOfWeek: Int, // 0=Sun, 1=Mon, ..., 6=Sat
    @Json(name = "is_working") val isWorking: Boolean = true,
    @Json(name = "start_time") val startTime: String = "10:00",
    @Json(name = "end_time") val endTime: String = "19:00"
)

@JsonClass(generateAdapter = true)
data class SalonHours(
    @Json(name = "id") val id: String = "",
    @Json(name = "salon_id") val salonId: String? = null,
    @Json(name = "day_of_week") val dayOfWeek: Int, // 0=Sun, 1=Mon, ..., 6=Sat
    @Json(name = "is_closed") val isClosed: Boolean = false,
    @Json(name = "open_time") val openTime: String = "09:00",
    @Json(name = "close_time") val closeTime: String = "21:00"
)

@JsonClass(generateAdapter = true)
data class SalonPayoutDetails(
    @Json(name = "id") val id: String? = null,
    @Json(name = "salon_id") val salonId: String? = null,
    @Json(name = "account_holder_name") val accountHolderName: String = "",
    @Json(name = "upi_id") val upiId: String = "",
    @Json(name = "bank_account_number") val bankAccountNumber: String = "",
    @Json(name = "bank_ifsc") val bankIfsc: String = ""
)


