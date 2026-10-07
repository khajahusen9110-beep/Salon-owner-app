package com.example.data.network

import com.example.data.model.AddTimeOffParams
import com.example.data.model.AuthResponse
import com.example.data.model.Booking
import com.example.data.model.Combo
import com.example.data.model.ComboDetailsParams
import com.example.data.model.ComboDetailsResponse
import com.example.data.model.ComboService
import com.example.data.model.CreateMySalonParams
import com.example.data.model.CreateServiceCategoryRequest
import com.example.data.model.CreateStaffRequest
import com.example.data.model.CreateWalkInParams
import com.example.data.model.CustomerSummary
import com.example.data.model.CustomerSummaryParams
import com.example.data.model.EarningsSummaryParams
import com.example.data.model.GetOwnerBookingsParams
import com.example.data.model.LoginRequest
import com.example.data.model.OwnerDashboard
import com.example.data.model.Profile
import com.example.data.model.RecurringBreak
import com.example.data.model.RescheduleBookingParams
import com.example.data.model.Salon
import com.example.data.model.SalonDocument
import com.example.data.model.SalonHours
import com.example.data.model.SalonNotification
import com.example.data.model.SalonPayoutDetails
import com.example.data.model.SalonReview
import com.example.data.model.SalonService
import com.example.data.model.ServiceCategory
import com.example.data.model.SignUpRequest
import com.example.data.model.Staff
import com.example.data.model.StaffEarningsSummary
import com.example.data.model.StaffHours
import com.example.data.model.StaffService
import com.example.data.model.StaffTimeOff
import com.example.data.model.SubmitVerificationParams
import com.example.data.model.TimeOffConflictParams
import com.example.data.model.UpdateBookingStatusRequest
import com.example.data.model.UpdateNotificationReadRequest
import com.example.data.model.UpdateProfileLanguageRequest
import com.example.data.model.UpdateReviewReplyRequest
import com.example.data.model.UpdateSalonProfileRequest
import com.example.data.model.UpdateSalonSettingsRequest
import com.example.data.model.UpdateSalonsActiveRequest
import com.example.data.model.UpdateServiceCategoryRequest
import com.example.data.model.UpdateStaffRequest
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SupabaseAuthApi {
    @Headers("Content-Type: application/json")
    @POST("auth/v1/signup")
    suspend fun signUp(
        @Header("apikey") apiKey: String,
        @Body request: SignUpRequest
    ): Response<AuthResponse>

    @Headers("Content-Type: application/json")
    @POST("auth/v1/token?grant_type=password")
    suspend fun signIn(
        @Header("apikey") apiKey: String,
        @Body request: LoginRequest
    ): Response<AuthResponse>
}

interface SupabaseRestApi {
    @GET("rest/v1/profiles")
    suspend fun getProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("id") idFilter: String,
        @Query("select") select: String = "*"
    ): Response<List<Profile>>

    @GET("rest/v1/salons")
    suspend fun getSalons(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("select") select: String = "*"
    ): Response<List<Salon>>

    @Headers("Content-Type: application/json")
    @PATCH("rest/v1/salons")
    suspend fun updateSalonActive(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("id") idFilter: String,
        @Body body: UpdateSalonsActiveRequest
    ): Response<ResponseBody>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST("rest/v1/salon_documents")
    suspend fun insertSalonDocument(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body document: SalonDocument
    ): Response<List<SalonDocument>>

    @Headers("Content-Type: application/json")
    @POST("rest/v1/rpc/create_my_salon")
    suspend fun createMySalon(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body params: CreateMySalonParams
    ): Response<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("rest/v1/rpc/submit_salon_for_verification")
    suspend fun submitSalonForVerification(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body params: SubmitVerificationParams
    ): Response<ResponseBody>

    @POST("storage/v1/object/salon-documents/{filePath}")
    suspend fun uploadDocument(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Header("Content-Type") contentType: String,
        @Path("filePath", encoded = true) filePath: String,
        @Body fileData: RequestBody
    ): Response<ResponseBody>

    // --- Part 2: Dashboard & Bookings RPCs and Endpoints ---

    @Headers("Content-Type: application/json")
    @POST("rest/v1/rpc/get_owner_dashboard")
    suspend fun getOwnerDashboard(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body emptyBody: Map<String, String> = emptyMap()
    ): Response<OwnerDashboard>

    @Headers("Content-Type: application/json")
    @POST("rest/v1/rpc/get_owner_bookings")
    suspend fun getOwnerBookings(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body params: GetOwnerBookingsParams
    ): Response<List<Booking>>

    @Headers("Content-Type: application/json")
    @POST("rest/v1/rpc/create_walk_in_booking")
    suspend fun createWalkInBooking(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body params: CreateWalkInParams
    ): Response<ResponseBody>

    @Headers("Content-Type: application/json")
    @PATCH("rest/v1/bookings")
    suspend fun updateBookingStatus(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("id") idFilter: String,
        @Body body: UpdateBookingStatusRequest
    ): Response<ResponseBody>

    @GET("rest/v1/staff")
    suspend fun getStaff(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("salon_id") salonIdFilter: String? = null,
        @Query("is_active") isActive: String = "eq.true"
    ): Response<List<Staff>>

    @GET("rest/v1/services")
    suspend fun getServices(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("salon_id") salonIdFilter: String? = null,
        @Query("is_active") isActive: String = "eq.true"
    ): Response<List<SalonService>>

    // --- Part 3: Customer History, Reschedule & Staff Time Off ---

    @Headers("Content-Type: application/json")
    @POST("rest/v1/rpc/get_customer_summary")
    suspend fun getCustomerSummary(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body params: CustomerSummaryParams
    ): Response<CustomerSummary>

    @Headers("Content-Type: application/json")
    @POST("rest/v1/rpc/reschedule_booking")
    suspend fun rescheduleBooking(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body params: RescheduleBookingParams
    ): Response<ResponseBody>

    @GET("rest/v1/recurring_breaks")
    suspend fun getRecurringBreaks(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("salon_id") salonIdFilter: String? = null,
        @Query("select") select: String = "*"
    ): Response<List<RecurringBreak>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST("rest/v1/recurring_breaks")
    suspend fun insertRecurringBreak(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body recurringBreak: RecurringBreak
    ): Response<List<RecurringBreak>>

    @DELETE("rest/v1/recurring_breaks")
    suspend fun deleteRecurringBreak(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("id") idFilter: String
    ): Response<ResponseBody>

    @GET("rest/v1/staff_time_off")
    suspend fun getStaffTimeOff(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("salon_id") salonIdFilter: String? = null,
        @Query("select") select: String = "*"
    ): Response<List<StaffTimeOff>>

    @Headers("Content-Type: application/json")
    @POST("rest/v1/rpc/get_time_off_conflicts")
    suspend fun getTimeOffConflicts(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body params: TimeOffConflictParams
    ): Response<List<Booking>>

    @Headers("Content-Type: application/json")
    @POST("rest/v1/rpc/add_time_off")
    suspend fun addTimeOff(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body params: AddTimeOffParams
    ): Response<ResponseBody>

    @DELETE("rest/v1/staff_time_off")
    suspend fun deleteStaffTimeOff(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("id") idFilter: String
    ): Response<ResponseBody>

    // --- Part 4: Profile, Categories, Services, Combos, Staff, Hours, Settings, Payout ---

    @Headers("Content-Type: application/json")
    @PATCH("rest/v1/salons")
    suspend fun updateSalonProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("id") idFilter: String,
        @Body body: UpdateSalonProfileRequest
    ): Response<ResponseBody>

    @Headers("Content-Type: application/json")
    @PATCH("rest/v1/salons")
    suspend fun updateSalonSettings(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("id") idFilter: String,
        @Body body: UpdateSalonSettingsRequest
    ): Response<ResponseBody>

    @POST("storage/v1/object/salon-photos/{filePath}")
    suspend fun uploadSalonPhoto(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Header("Content-Type") contentType: String,
        @Path("filePath", encoded = true) filePath: String,
        @Body fileData: RequestBody
    ): Response<ResponseBody>

    @GET("rest/v1/service_categories")
    suspend fun getServiceCategories(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("salon_id") salonIdFilter: String? = null,
        @Query("order") order: String = "sort_order.asc"
    ): Response<List<ServiceCategory>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST("rest/v1/service_categories")
    suspend fun insertServiceCategory(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body category: CreateServiceCategoryRequest
    ): Response<List<ServiceCategory>>

    @Headers("Content-Type: application/json")
    @PATCH("rest/v1/service_categories")
    suspend fun updateServiceCategory(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("id") idFilter: String,
        @Body body: UpdateServiceCategoryRequest
    ): Response<ResponseBody>

    @DELETE("rest/v1/service_categories")
    suspend fun deleteServiceCategory(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("id") idFilter: String
    ): Response<ResponseBody>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST("rest/v1/services")
    suspend fun insertService(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body service: SalonService
    ): Response<List<SalonService>>

    @Headers("Content-Type: application/json")
    @PATCH("rest/v1/services")
    suspend fun updateService(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("id") idFilter: String,
        @Body service: SalonService
    ): Response<ResponseBody>

    @DELETE("rest/v1/services")
    suspend fun deleteService(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("id") idFilter: String
    ): Response<ResponseBody>

    @GET("rest/v1/staff_services")
    suspend fun getStaffServices(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("select") select: String = "*"
    ): Response<List<StaffService>>

    @Headers("Content-Type: application/json")
    @POST("rest/v1/staff_services")
    suspend fun insertStaffService(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body staffService: StaffService
    ): Response<ResponseBody>

    @DELETE("rest/v1/staff_services")
    suspend fun deleteStaffServicesForService(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("service_id") serviceIdFilter: String
    ): Response<ResponseBody>

    @DELETE("rest/v1/staff_services")
    suspend fun deleteStaffServiceSingle(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("staff_id") staffIdFilter: String,
        @Query("service_id") serviceIdFilter: String
    ): Response<ResponseBody>

    @GET("rest/v1/combos")
    suspend fun getCombos(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("salon_id") salonIdFilter: String? = null,
        @Query("select") select: String = "*"
    ): Response<List<Combo>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST("rest/v1/combos")
    suspend fun insertCombo(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body combo: Combo
    ): Response<List<Combo>>

    @Headers("Content-Type: application/json")
    @PATCH("rest/v1/combos")
    suspend fun updateCombo(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("id") idFilter: String,
        @Body combo: Combo
    ): Response<ResponseBody>

    @DELETE("rest/v1/combos")
    suspend fun deleteCombo(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("id") idFilter: String
    ): Response<ResponseBody>

    @GET("rest/v1/combo_services")
    suspend fun getComboServices(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("select") select: String = "*"
    ): Response<List<ComboService>>

    @Headers("Content-Type: application/json")
    @POST("rest/v1/combo_services")
    suspend fun insertComboService(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body item: ComboService
    ): Response<ResponseBody>

    @DELETE("rest/v1/combo_services")
    suspend fun deleteComboServicesForCombo(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("combo_id") comboIdFilter: String
    ): Response<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("rest/v1/rpc/get_combo_details")
    suspend fun getComboDetails(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body params: ComboDetailsParams
    ): Response<ComboDetailsResponse>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST("rest/v1/staff")
    suspend fun insertStaff(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body staff: CreateStaffRequest
    ): Response<List<Staff>>

    @Headers("Content-Type: application/json")
    @PATCH("rest/v1/staff")
    suspend fun updateStaff(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("id") idFilter: String,
        @Body body: UpdateStaffRequest
    ): Response<ResponseBody>

    @GET("rest/v1/staff_hours")
    suspend fun getStaffHours(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("staff_id") staffIdFilter: String? = null,
        @Query("order") order: String = "day_of_week.asc"
    ): Response<List<StaffHours>>

    @Headers("Content-Type: application/json")
    @POST("rest/v1/staff_hours")
    suspend fun insertStaffHours(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body hours: StaffHours
    ): Response<ResponseBody>

    @DELETE("rest/v1/staff_hours")
    suspend fun deleteStaffHours(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("staff_id") staffIdFilter: String
    ): Response<ResponseBody>

    @GET("rest/v1/salon_hours")
    suspend fun getSalonHours(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("salon_id") salonIdFilter: String? = null,
        @Query("order") order: String = "day_of_week.asc"
    ): Response<List<SalonHours>>

    @Headers("Content-Type: application/json")
    @POST("rest/v1/salon_hours")
    suspend fun insertSalonHours(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body hours: SalonHours
    ): Response<ResponseBody>

    @DELETE("rest/v1/salon_hours")
    suspend fun deleteSalonHours(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("salon_id") salonIdFilter: String
    ): Response<ResponseBody>

    @GET("rest/v1/salon_payout_details")
    suspend fun getSalonPayoutDetails(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("salon_id") salonIdFilter: String
    ): Response<List<SalonPayoutDetails>>

    @Headers("Content-Type: application/json", "Prefer: resolution=merge-duplicates")
    @POST("rest/v1/salon_payout_details")
    suspend fun upsertSalonPayoutDetails(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body details: SalonPayoutDetails
    ): Response<ResponseBody>

    // --- Part 5: Earnings Summary RPC, Reviews, Notifications, Profile Language ---

    @Headers("Content-Type: application/json")
    @POST("rest/v1/rpc/get_earnings_summary")
    suspend fun getEarningsSummary(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body params: EarningsSummaryParams
    ): Response<List<StaffEarningsSummary>>

    @GET("rest/v1/reviews")
    suspend fun getSalonReviews(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("salon_id") salonIdFilter: String,
        @Query("order") order: String = "created_at.desc"
    ): Response<List<SalonReview>>

    @Headers("Content-Type: application/json")
    @PATCH("rest/v1/reviews")
    suspend fun replyToReview(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("id") idFilter: String,
        @Body body: UpdateReviewReplyRequest
    ): Response<ResponseBody>

    @GET("rest/v1/notifications")
    suspend fun getNotifications(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("user_id") userIdFilter: String,
        @Query("order") order: String = "created_at.desc"
    ): Response<List<SalonNotification>>

    @Headers("Content-Type: application/json")
    @PATCH("rest/v1/notifications")
    suspend fun markNotificationRead(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("id") idFilter: String,
        @Body body: UpdateNotificationReadRequest
    ): Response<ResponseBody>

    @Headers("Content-Type: application/json")
    @PATCH("rest/v1/notifications")
    suspend fun markAllNotificationsRead(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("is_read") isReadFilter: String,
        @Body body: UpdateNotificationReadRequest
    ): Response<ResponseBody>


    @Headers("Content-Type: application/json")
    @PATCH("rest/v1/profiles")
    suspend fun updateProfileLanguage(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("id") idFilter: String,
        @Body body: UpdateProfileLanguageRequest
    ): Response<ResponseBody>
}


