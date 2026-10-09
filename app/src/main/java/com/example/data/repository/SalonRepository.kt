package com.example.data.repository

import android.content.Context
import com.example.data.model.Amenity
import com.example.data.model.Booking
import com.example.data.model.Combo
import com.example.data.model.CustomerSummary
import com.example.data.model.OwnerDashboard
import com.example.data.model.RecurringBreak
import com.example.data.model.Salon
import com.example.data.model.SalonHours
import com.example.data.model.SalonNotification
import com.example.data.model.SalonPayoutDetails
import com.example.data.model.SalonReview
import com.example.data.model.SalonService
import com.example.data.model.ServiceCategory
import com.example.data.model.Staff
import com.example.data.model.StaffEarningsSummary
import com.example.data.model.StaffHours
import com.example.data.model.StaffTimeOff
import com.example.data.model.SalonSetupStatus
import com.example.data.model.SalonWallet
import com.example.data.model.WithdrawalRequest
import com.example.data.model.UpdateSalonProfileRequest
import com.example.data.model.UpdateSalonSettingsRequest
import com.example.data.network.IstTime
import com.example.data.network.SupabaseException
import com.example.data.network.SupabaseHttp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

sealed class SalonResult<out T> {
    data class Success<out T>(val data: T) : SalonResult<T>()
    data class Error(val message: String) : SalonResult<Nothing>()
}

/**
 * Owner-side data access against the shared Supabase backend.
 *
 * Every call goes to the server; RLS restricts the owner to their own salon. Failures are returned
 * as [SalonResult.Error] with a user-facing message - nothing is faked locally.
 */
class SalonRepository(
    @Suppress("unused") private val context: Context,
    private val authRepository: AuthRepository
) {

    private suspend fun <T> io(block: suspend () -> T): SalonResult<T> = withContext(Dispatchers.IO) {
        try {
            SalonResult.Success(block())
        } catch (e: SupabaseException) {
            SalonResult.Error(e.message ?: "Something went wrong. Please try again.")
        } catch (e: org.json.JSONException) {
            SalonResult.Error("Unexpected response from server. Please try again.")
        }
    }

    /** The owner's salon id, loading it from the server when not cached yet. */
    private suspend fun salonId(): String =
        (authRepository.fetchSalonsNow() ?: authRepository.fetchSalons().firstOrNull())?.id
            ?: throw SupabaseException("No salon found for this account.")

    private fun salonIdBlocking(): String =
        authRepository.fetchSalonsNow()?.id ?: throw SupabaseException("No salon found for this account.")

    private fun <T> SalonResult<T>.orNull(): T? = (this as? SalonResult.Success<T>)?.data

    private fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }

    private fun JSONObject.str(key: String): String? = optStringOrNull(key)

    private fun nested(o: JSONObject, rel: String, key: String): String? = o.optJSONObject(rel)?.str(key)

    // ======================= Registration & verification =======================

    suspend fun createMySalon(
        ownerName: String,
        name: String,
        salonType: String,
        address: String,
        area: String,
        city: String,
        pincode: String,
        phone: String,
        latitude: Double?,
        longitude: Double?,
        gstNumber: String?,
        language: String
    ): SalonResult<Salon> = withContext(Dispatchers.IO) {
        val result = io {
            val params = JSONObject()
                .put("p_owner_name", ownerName.trim())
                .put("p_name", name.trim())
                .put("p_salon_type", salonType.lowercase())
                .put("p_address", address.trim())
                .put("p_area", area.trim())
                .put("p_city", city.trim())
                .put("p_pincode", pincode.trim())
                .put("p_phone", phone.trim())
                .put("p_latitude", latitude ?: JSONObject.NULL)
                .put("p_longitude", longitude ?: JSONObject.NULL)
                .put("p_gst_number", gstNumber?.trim()?.ifBlank { null } ?: JSONObject.NULL)
                .put("p_language", if (language == "hi") "hi" else "en")
            SupabaseHttp.rpc("create_my_salon", params)
        }
        when (result) {
            is SalonResult.Error -> SalonResult.Error(result.message)
            is SalonResult.Success -> authRepository.fetchSalons().firstOrNull()?.let { SalonResult.Success(it) }
                ?: SalonResult.Error("Salon was created but could not be loaded. Please refresh.")
        }
    }

    suspend fun uploadDocumentAndSubmit(
        salonId: String,
        docType: String,
        fileName: String,
        fileBytes: ByteArray,
        mimeType: String,
        acceptTerms: Boolean
    ): SalonResult<Unit> {
        if (!acceptTerms) return SalonResult.Error("Please accept the terms and conditions to proceed.")
        if (fileBytes.isEmpty()) return SalonResult.Error("Please choose a document to upload.")
        val realSalonId = runCatching { salonId() }.getOrNull() ?: salonId
        return io {
            val uid = authRepository.getUserId() ?: throw SupabaseException("Your session has expired. Please sign in again.", 401)
            val safeName = fileName.replace(Regex("[^A-Za-z0-9._-]"), "_").takeLast(80)
            val path = "$uid/${System.currentTimeMillis()}_$safeName"
            SupabaseHttp.upload("salon-documents", path, fileBytes, mimeType)
            SupabaseHttp.insert(
                "salon_documents",
                JSONObject().put("salon_id", realSalonId).put("doc_type", docType).put("file_path", path)
            )
            SupabaseHttp.rpc("submit_salon_for_verification", JSONObject().put("p_accept_terms", true))
            Unit
        }
    }

    suspend fun refreshSalonStatus(): Salon? = authRepository.fetchSalons().firstOrNull()

    // ======================= Dashboard & bookings =======================

    suspend fun getOwnerDashboard(): SalonResult<OwnerDashboard> = io {
        val o = JSONObject(SupabaseHttp.rpc("get_owner_dashboard", single = true))
        OwnerDashboard(
            todayBookings = o.optInt("today_bookings"),
            todayCompleted = o.optInt("today_completed"),
            todayUpcoming = o.optInt("today_upcoming"),
            todayRevenue = o.optDouble("today_revenue", 0.0),
            todayNoShows = o.optInt("today_no_shows"),
            pendingReviewsReply = o.optInt("pending_reviews_reply"),
            salonName = o.str("salon_name"),
            verificationStatus = o.str("verification_status"),
            isActive = o.optBoolean("is_active", true)
        )
    }

    private fun parseBooking(o: JSONObject, idKey: String = "id"): Booking {
        val start = IstTime.toLocal(o.str("start_time")).orEmpty()
        val end = IstTime.toLocal(o.str("end_time"))
        return Booking(
            id = o.getString(idKey),
            staffId = o.str("staff_id"),
            staffName = o.str("staff_name"),
            serviceId = o.str("service_id"),
            serviceName = o.str("service_name"),
            durationMinutes = null,
            customerId = o.str("customer_id"),
            customerName = o.str("customer_name"),
            customerPhone = o.str("customer_phone"),
            startTime = start,
            endTime = end,
            status = o.optString("status", "confirmed"),
            price = o.optDouble("price", 0.0),
            source = o.str("source"),
            notes = o.str("notes")
        )
    }

    suspend fun getOwnerBookings(
        fromDate: String? = null,
        toDate: String? = null,
        staffId: String? = null
    ): SalonResult<List<Booking>> = io {
        val from = fromDate ?: IstTime.today()
        val params = JSONObject()
            .put("p_from", from)
            .put("p_to", toDate ?: from)
            .put("p_staff_id", staffId?.takeIf { it.isNotBlank() && it != "all" } ?: JSONObject.NULL)
        withPayments(JSONArray(SupabaseHttp.rpc("get_owner_bookings", params)).objects().map { parseBooking(it) })
    }

    /** Adds what each customer already paid online (the owner collects only the rest). */
    private fun withPayments(bookings: List<Booking>): List<Booking> {
        val ids = bookings.filter { it.source != "walk_in" }.map { it.id }
        if (ids.isEmpty()) return bookings
        val pay = ids.chunked(80).flatMap { chunk ->
            SupabaseHttp.select("bookings?select=id,payment_status,amount_paid&id=in.(${chunk.joinToString(",")})").objects()
        }.associateBy { it.getString("id") }
        return bookings.map { b ->
            val p = pay[b.id] ?: return@map b
            b.copy(paymentStatus = p.str("payment_status"), amountPaid = p.optDouble("amount_paid", 0.0))
        }
    }

    suspend fun updateBookingStatus(bookingId: String, newStatus: String): SalonResult<Unit> = io {
        val rows = SupabaseHttp.update("bookings", "id=eq.$bookingId", JSONObject().put("status", newStatus))
        if (rows.length() == 0) throw SupabaseException("Booking not found.")
    }

    suspend fun createWalkInBooking(
        staffId: String,
        serviceId: String,
        customerName: String,
        customerPhone: String?
    ): SalonResult<Booking> = io {
        if (customerName.isBlank()) throw SupabaseException("Please enter the customer's name.")
        val params = JSONObject()
            .put("p_staff_id", staffId)
            .put("p_service_id", serviceId)
            .put("p_name", customerName.trim())
            .put("p_phone", customerPhone?.trim()?.ifBlank { null } ?: JSONObject.NULL)
        val id = SupabaseHttp.rpc("create_walk_in_booking", params).trim().trim('"')
        val today = IstTime.today()
        val todays = JSONArray(
            SupabaseHttp.rpc("get_owner_bookings", JSONObject().put("p_from", today).put("p_to", today).put("p_staff_id", JSONObject.NULL))
        ).objects().map { parseBooking(it) }
        todays.firstOrNull { it.id == id }
            ?: Booking(id = id, staffId = staffId, serviceId = serviceId, customerName = customerName, customerPhone = customerPhone,
                startTime = IstTime.toLocal(java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.US).format(java.util.Date())).orEmpty(),
                status = "arrived", source = "walk_in")
    }

    suspend fun updateSalonActive(isActive: Boolean): SalonResult<Unit> = io {
        val id = salonIdBlocking()
        val rows = SupabaseHttp.update("salons", "id=eq.$id", JSONObject().put("is_active", isActive))
        rows.objects().firstOrNull()?.let { authRepository.saveLocalSalon(AuthRepository.parseSalon(it)) }
        Unit
    }

    private fun parseStaff(o: JSONObject) = Staff(
        id = o.getString("id"),
        salonId = o.str("salon_id"),
        name = o.optString("name"),
        photoUrl = o.str("photo_url"),
        commissionPercent = o.optDouble("commission_percent", 0.0),
        ratingAvg = o.optDouble("rating_avg", 0.0),
        ratingCount = o.optInt("rating_count", 0),
        isActive = o.optBoolean("is_active", true),
        doesAllServices = o.optBoolean("does_all_services", false)
    )

    private fun parseService(o: JSONObject): SalonService {
        val links = o.optJSONArray("staff_services") ?: JSONArray()
        return SalonService(
            id = o.getString("id"),
            salonId = o.str("salon_id"),
            categoryId = o.str("category_id"),
            name = o.optString("name"),
            category = nested(o, "service_categories", "name"),
            price = o.optDouble("price", 0.0),
            durationMins = o.optInt("duration_minutes", 30),
            bufferMins = o.optInt("buffer_minutes", 0),
            isActive = o.optBoolean("is_active", true),
            assignedStaffIds = (0 until links.length()).map { links.getJSONObject(it).getString("staff_id") },
            imageUrl = o.str("image_url"),
            weddingType = o.str("wedding_type")?.takeIf { it == "bridal" || it == "groom" }
        )
    }

    /** Active staff of the owner's salon. */
    suspend fun getStaff(): List<Staff> =
        io { SupabaseHttp.select("staff?salon_id=eq.${salonId()}&is_active=eq.true&select=*&order=name").objects().map(::parseStaff) }
            .orNull() ?: emptyList()

    /** Active services of the owner's salon, with stylist assignments and category name. */
    suspend fun getServices(): List<SalonService> =
        io {
            SupabaseHttp.select(
                "services?salon_id=eq.${salonId()}&is_active=eq.true" +
                    "&select=*,staff_services(staff_id),service_categories(name)&order=name"
            ).objects().map(::parseService)
        }.orNull() ?: emptyList()

    suspend fun getCustomerSummary(bookingId: String): SalonResult<CustomerSummary> = io {
        val o = JSONObject(SupabaseHttp.rpc("get_customer_summary", JSONObject().put("p_booking_id", bookingId), single = true))
        CustomerSummary(
            totalVisits = o.optInt("total_visits"),
            noShowCount = o.optInt("no_show_count"),
            totalSpent = o.optDouble("total_spent", 0.0),
            lastVisitDate = IstTime.toLocal(o.str("last_visit"))?.take(10),
            customerName = o.str("customer_name"),
            customerPhone = o.str("customer_phone")
        )
    }

    suspend fun rescheduleBooking(bookingId: String, newStartTime: String): SalonResult<Unit> = io {
        SupabaseHttp.rpc(
            "reschedule_booking",
            JSONObject().put("p_booking_id", bookingId).put("p_new_start", IstTime.toOffset(newStartTime))
        )
        Unit
    }

    // ======================= Breaks & time off =======================

    suspend fun getRecurringBreaks(): SalonResult<List<RecurringBreak>> = io {
        SupabaseHttp.select("recurring_breaks?salon_id=eq.${salonId()}&select=*,staff(name)&order=day_of_week.asc.nullsfirst,start_time.asc")
            .objects().map { o ->
                RecurringBreak(
                    id = o.getString("id"),
                    salonId = o.str("salon_id"),
                    staffId = o.str("staff_id"),
                    staffName = nested(o, "staff", "name") ?: "Whole Salon",
                    dayOfWeek = if (o.isNull("day_of_week")) null else o.optInt("day_of_week"),
                    startTime = IstTime.hhmm(o.str("start_time")),
                    endTime = IstTime.hhmm(o.str("end_time")),
                    label = o.optString("label", "Break")
                )
            }
    }

    suspend fun addRecurringBreak(
        staffId: String?,
        dayOfWeek: Int?,
        startTime: String,
        endTime: String,
        label: String
    ): SalonResult<RecurringBreak> = io {
        if (endTime <= startTime) throw SupabaseException("End time must be after start time.")
        val body = JSONObject()
            .put("salon_id", salonIdBlocking())
            .put("staff_id", staffId?.takeIf { it.isNotBlank() && it != "all" } ?: JSONObject.NULL)
            .put("day_of_week", dayOfWeek ?: JSONObject.NULL)
            .put("start_time", startTime)
            .put("end_time", endTime)
            .put("label", label.trim().ifBlank { "Break" })
        val o = SupabaseHttp.insert("recurring_breaks", body).getJSONObject(0)
        RecurringBreak(
            id = o.getString("id"),
            salonId = o.str("salon_id"),
            staffId = o.str("staff_id"),
            dayOfWeek = if (o.isNull("day_of_week")) null else o.optInt("day_of_week"),
            startTime = IstTime.hhmm(o.str("start_time")),
            endTime = IstTime.hhmm(o.str("end_time")),
            label = o.optString("label", "Break")
        )
    }

    suspend fun deleteRecurringBreak(id: String): SalonResult<Unit> = io {
        SupabaseHttp.delete("recurring_breaks", "id=eq.$id")
        Unit
    }

    suspend fun getStaffTimeOff(): SalonResult<List<StaffTimeOff>> = io {
        // Past time off is irrelevant for scheduling; keep the list short.
        val since = IstTime.toOffset("${IstTime.today()}T00:00:00")
        SupabaseHttp.select(
            "staff_time_off?salon_id=eq.${salonId()}&end_time=gte.${java.net.URLEncoder.encode(since, "UTF-8")}" +
                "&select=*,staff(name)&order=start_time&limit=200"
        ).objects().map { o ->
            StaffTimeOff(
                id = o.getString("id"),
                salonId = o.str("salon_id"),
                staffId = o.str("staff_id"),
                staffName = nested(o, "staff", "name") ?: "Whole Salon",
                startTime = IstTime.toLocal(o.str("start_time")).orEmpty(),
                endTime = IstTime.toLocal(o.str("end_time")).orEmpty(),
                reason = o.str("reason")
            )
        }
    }

    suspend fun getTimeOffConflicts(
        staffId: String?,
        startTime: String,
        endTime: String
    ): SalonResult<List<Booking>> = io {
        val params = JSONObject()
            .put("p_staff_id", staffId?.takeIf { it.isNotBlank() && it != "all" } ?: JSONObject.NULL)
            .put("p_start", IstTime.toOffset(startTime))
            .put("p_end", IstTime.toOffset(endTime))
        JSONArray(SupabaseHttp.rpc("get_time_off_conflicts", params)).objects().map { parseBooking(it, idKey = "booking_id") }
    }

    suspend fun addStaffTimeOff(
        staffId: String?,
        startTime: String,
        endTime: String,
        reason: String?,
        cancelConflicts: Boolean
    ): SalonResult<Unit> = io {
        val params = JSONObject()
            .put("p_staff_id", staffId?.takeIf { it.isNotBlank() && it != "all" } ?: JSONObject.NULL)
            .put("p_start", IstTime.toOffset(startTime))
            .put("p_end", IstTime.toOffset(endTime))
            .put("p_reason", reason?.trim()?.ifBlank { null } ?: JSONObject.NULL)
            .put("p_cancel_conflicts", cancelConflicts)
        SupabaseHttp.rpc("add_time_off", params)
        Unit
    }

    suspend fun deleteStaffTimeOff(id: String): SalonResult<Unit> = io {
        SupabaseHttp.delete("staff_time_off", "id=eq.$id")
        Unit
    }

    // ======================= Salon profile & settings =======================

    suspend fun updateSalonProfile(salonId: String, request: UpdateSalonProfileRequest): SalonResult<Unit> = io {
        val body = JSONObject()
            .put("name", request.name.trim())
            .put("description", request.description?.trim()?.ifBlank { null } ?: JSONObject.NULL)
            .put("salon_type", request.salonType.lowercase())
            .put("address", request.address.trim())
            .put("area", request.area.trim())
            .put("city", request.city.trim())
            .put("pincode", request.pincode.trim())
            .put("phone", request.phone.trim())
            .put("gst_number", request.gstNumber?.trim()?.uppercase()?.ifBlank { null } ?: JSONObject.NULL)
            .put("photos", JSONArray(request.photos))
            .put("cover_photo_index", request.coverPhotoIndex)
            .put("is_active", request.isActive)
        val rows = SupabaseHttp.update("salons", "id=eq.$salonId", body)
        if (rows.length() == 0) throw SupabaseException("Salon not found.")
        authRepository.saveLocalSalon(AuthRepository.parseSalon(rows.getJSONObject(0)))
    }

    /** Pins the salon on the map (customers' "nearby" search uses this point). */
    suspend fun updateSalonLocation(salonId: String, latitude: Double, longitude: Double): SalonResult<Unit> = io {
        val body = JSONObject().put("latitude", latitude).put("longitude", longitude)
        val rows = SupabaseHttp.update("salons", "id=eq.$salonId", body)
        if (rows.length() == 0) throw SupabaseException("Salon not found.")
        authRepository.saveLocalSalon(AuthRepository.parseSalon(rows.getJSONObject(0)))
    }

    suspend fun updateSalonSettings(salonId: String, request: UpdateSalonSettingsRequest): SalonResult<Unit> = io {
        val body = JSONObject()
            .put("slot_interval_minutes", request.slotIntervalMinutes)
            .put("booking_window_days", request.bookingWindowDays)
            .put("min_notice_minutes", request.minNoticeMinutes)
            .put("late_threshold_minutes", request.lateThresholdMinutes)
            .put("late_credit_amount", request.lateCreditAmount)
        val rows = SupabaseHttp.update("salons", "id=eq.$salonId", body)
        if (rows.length() == 0) throw SupabaseException("Salon not found.")
        authRepository.saveLocalSalon(AuthRepository.parseSalon(rows.getJSONObject(0)))
    }

    /**
     * Uploads an already-compressed JPEG into the owner's own folder (required by storage RLS) and
     * returns its public URL. [bucket] is "salon-photos" (banner, stylists) or "service-images".
     */
    suspend fun uploadImage(bucket: String, folder: String, jpeg: ByteArray): SalonResult<String> = io {
        val uid = authRepository.getUserId() ?: throw SupabaseException("Your session has expired. Please sign in again.", 401)
        val path = "$uid/$folder/${java.util.UUID.randomUUID()}.jpg"
        SupabaseHttp.upload(bucket, path, jpeg, "image/jpeg")
        SupabaseHttp.publicUrl(bucket, path)
    }

    /** Saves the salon's photo list and which one is the banner (shown first to customers). */
    suspend fun updateSalonPhotos(salonId: String, photos: List<String>, bannerIndex: Int): SalonResult<Unit> = io {
        val body = JSONObject().put("photos", JSONArray(photos)).put("cover_photo_index", bannerIndex.coerceIn(0, maxOf(photos.size - 1, 0)))
        val rows = SupabaseHttp.update("salons", "id=eq.$salonId", body)
        if (rows.length() == 0) throw SupabaseException("Salon not found.")
        authRepository.saveLocalSalon(AuthRepository.parseSalon(rows.getJSONObject(0)))
    }

    // ======================= Catalog: categories, services, combos =======================

    suspend fun getServiceCategories(salonId: String): SalonResult<List<ServiceCategory>> = io {
        SupabaseHttp.select("service_categories?salon_id=eq.$salonId&select=id,salon_id,name,sort_order,image_url&order=sort_order,name")
            .objects().map { ServiceCategory(it.getString("id"), it.str("salon_id"), it.optString("name"), it.optInt("sort_order"), it.str("image_url")) }
    }

    suspend fun addServiceCategory(salonId: String, name: String, sortOrder: Int, imageUrl: String): SalonResult<ServiceCategory> = io {
        if (name.isBlank()) throw SupabaseException("Please enter a category name.")
        if (imageUrl.isBlank()) throw SupabaseException("Please add a photo for this category.")
        val o = SupabaseHttp.insert(
            "service_categories",
            JSONObject().put("salon_id", salonId).put("name", name.trim()).put("sort_order", sortOrder).put("image_url", imageUrl)
        ).getJSONObject(0)
        ServiceCategory(o.getString("id"), o.str("salon_id"), o.optString("name"), o.optInt("sort_order"), o.str("image_url"))
    }

    suspend fun updateServiceCategory(id: String, name: String, sortOrder: Int, imageUrl: String): SalonResult<Unit> = io {
        if (name.isBlank()) throw SupabaseException("Please enter a category name.")
        if (imageUrl.isBlank()) throw SupabaseException("Please add a photo for this category.")
        SupabaseHttp.update(
            "service_categories", "id=eq.$id",
            JSONObject().put("name", name.trim()).put("sort_order", sortOrder).put("image_url", imageUrl)
        )
        Unit
    }

    /** Services in the category become uncategorized (FK is ON DELETE SET NULL). */
    suspend fun deleteServiceCategory(id: String): SalonResult<Unit> = io {
        SupabaseHttp.delete("service_categories", "id=eq.$id")
        Unit
    }

    suspend fun getServicesListWithAssignments(salonId: String): SalonResult<List<SalonService>> = io {
        SupabaseHttp.select("services?salon_id=eq.$salonId&select=*,staff_services(staff_id),service_categories(name)&order=name")
            .objects().map(::parseService)
    }

    private fun isUuid(id: String) = Regex("^[0-9a-fA-F-]{36}$").matches(id)

    suspend fun saveSalonService(
        service: SalonService,
        assignedStaffIds: List<String>
    ): SalonResult<SalonService> = io {
        if (service.name.isBlank()) throw SupabaseException("Please enter a service name.")
        val body = JSONObject()
            .put("name", service.name.trim())
            .put("category_id", service.categoryId?.takeIf { isUuid(it) } ?: JSONObject.NULL)
            .put("price", service.price)
            .put("duration_minutes", service.durationMins ?: 30)
            .put("buffer_minutes", service.bufferMins ?: 0)
            .put("is_active", service.isActive)
            .put("image_url", service.imageUrl?.ifBlank { null } ?: JSONObject.NULL)
            .put("wedding_type", service.weddingType ?: JSONObject.NULL)
        val saved = if (isUuid(service.id)) {
            SupabaseHttp.update("services", "id=eq.${service.id}", body).objects().firstOrNull()
                ?: throw SupabaseException("Service not found.")
        } else {
            SupabaseHttp.insert("services", body.put("salon_id", service.salonId?.takeIf { isUuid(it) } ?: salonIdBlocking()))
                .getJSONObject(0)
        }
        val serviceId = saved.getString("id")
        // All-rounders always do every service, whatever was ticked.
        val allRounders = SupabaseHttp.select("staff?salon_id=eq.${saved.getString("salon_id")}&does_all_services=eq.true&select=id")
            .objects().map { it.getString("id") }
        SupabaseHttp.delete("staff_services", "service_id=eq.$serviceId")
        val links = JSONArray()
        (assignedStaffIds + allRounders).distinct().filter { isUuid(it) }.forEach { links.put(JSONObject().put("staff_id", it).put("service_id", serviceId)) }
        if (links.length() > 0) SupabaseHttp.insert("staff_services", links)
        parseService(saved).copy(assignedStaffIds = (assignedStaffIds + allRounders).distinct())
    }

    /**
     * Deletes a service. Services with booking history cannot be deleted (bookings keep a reference),
     * so those are deactivated instead and disappear from the menu.
     */
    suspend fun deleteSalonService(serviceId: String): SalonResult<Unit> = io {
        try {
            SupabaseHttp.delete("services", "id=eq.$serviceId")
        } catch (e: SupabaseException) {
            if (e.pgCode != "23503") throw e
            SupabaseHttp.update("services", "id=eq.$serviceId", JSONObject().put("is_active", false))
        }
        Unit
    }

    suspend fun getCombosList(salonId: String): SalonResult<List<Combo>> = io {
        SupabaseHttp.select("combos?salon_id=eq.$salonId&select=*,combo_services(service_id,services(duration_minutes))&order=name")
            .objects().map { o ->
                val items = (o.optJSONArray("combo_services") ?: JSONArray()).objects()
                Combo(
                    id = o.getString("id"),
                    salonId = o.str("salon_id"),
                    name = o.optString("name"),
                    price = o.optDouble("price", 0.0),
                    isActive = o.optBoolean("is_active", true),
                    serviceIds = items.map { it.getString("service_id") },
                    totalDurationMins = items.sumOf { it.optJSONObject("services")?.optInt("duration_minutes") ?: 0 }
                )
            }
    }

    suspend fun saveCombo(combo: Combo, serviceIds: List<String>): SalonResult<Combo> = io {
        if (combo.name.isBlank()) throw SupabaseException("Please enter a package name.")
        if (serviceIds.size < 2) throw SupabaseException("A package needs at least two services.")
        if (serviceIds.distinct().size > 6) throw SupabaseException("A package can have at most 6 services.")
        // One stylist does the whole package, so at least one stylist must have all of its services ticked.
        val ids = JSONArray().apply { serviceIds.distinct().forEach { put(it) } }
        if (JSONArray(SupabaseHttp.rpc("get_staff_for_services", JSONObject().put("p_service_ids", ids))).length() == 0) {
            throw SupabaseException("No stylist does all of these services. Tick them for one stylist first (Staff section), or mark a stylist as all-rounder.")
        }
        val body = JSONObject().put("name", combo.name.trim()).put("price", combo.price).put("is_active", combo.isActive)
        val saved = if (isUuid(combo.id)) {
            SupabaseHttp.update("combos", "id=eq.${combo.id}", body).objects().firstOrNull()
                ?: throw SupabaseException("Package not found.")
        } else {
            SupabaseHttp.insert("combos", body.put("salon_id", combo.salonId?.takeIf { isUuid(it) } ?: salonIdBlocking())).getJSONObject(0)
        }
        val comboId = saved.getString("id")
        SupabaseHttp.delete("combo_services", "combo_id=eq.$comboId")
        val links = JSONArray()
        serviceIds.distinct().forEach { links.put(JSONObject().put("combo_id", comboId).put("service_id", it)) }
        SupabaseHttp.insert("combo_services", links)
        combo.copy(id = comboId, salonId = saved.str("salon_id"), serviceIds = serviceIds.distinct())
    }

    suspend fun deleteCombo(comboId: String): SalonResult<Unit> = io {
        SupabaseHttp.delete("combo_services", "combo_id=eq.$comboId")
        SupabaseHttp.delete("combos", "id=eq.$comboId")
        Unit
    }

    // ======================= Staff =======================

    /** All staff (including inactive) of the owner's salon. */
    suspend fun getStaffList(): List<Staff> =
        io { SupabaseHttp.select("staff?salon_id=eq.${salonId()}&select=*&order=name").objects().map(::parseStaff) }
            .orNull() ?: emptyList()

    /** Adds a stylist; their working hours start as the salon's opening hours so they are bookable. */
    suspend fun addStaffMember(
        salonId: String,
        name: String,
        commissionPercent: Double,
        photoUrl: String?,
        doesAllServices: Boolean = false,
        serviceIds: List<String> = emptyList()
    ): SalonResult<Staff> = io {
        if (name.isBlank()) throw SupabaseException("Please enter the stylist's name.")
        val body = JSONObject()
            .put("salon_id", salonId)
            .put("name", name.trim())
            .put("commission_percent", commissionPercent)
            .put("photo_url", photoUrl?.ifBlank { null } ?: JSONObject.NULL)
            .put("does_all_services", doesAllServices)
        val staff = parseStaff(SupabaseHttp.insert("staff", body).getJSONObject(0))
        // An all-rounder is linked to every service by the database; otherwise link the ticked services.
        if (!doesAllServices && serviceIds.isNotEmpty()) {
            val links = JSONArray()
            serviceIds.distinct().filter { isUuid(it) }.forEach { links.put(JSONObject().put("staff_id", staff.id).put("service_id", it)) }
            if (links.length() > 0) SupabaseHttp.insert("staff_services", links)
        }
        val hours = JSONArray()
        SupabaseHttp.select("salon_hours?salon_id=eq.$salonId&is_closed=eq.false&select=day_of_week,open_time,close_time")
            .objects().forEach { h ->
                hours.put(
                    JSONObject().put("staff_id", staff.id).put("day_of_week", h.optInt("day_of_week"))
                        .put("start_time", h.optString("open_time")).put("end_time", h.optString("close_time"))
                )
            }
        if (hours.length() > 0) SupabaseHttp.insert("staff_hours", hours)
        staff
    }

    suspend fun updateStaffMember(
        staffId: String,
        name: String? = null,
        photoUrl: String? = null,
        commissionPercent: Double? = null,
        isActive: Boolean? = null
    ): SalonResult<Unit> = io {
        val body = JSONObject()
        name?.let { if (it.isBlank()) throw SupabaseException("Please enter the stylist's name."); body.put("name", it.trim()) }
        photoUrl?.let { body.put("photo_url", it.ifBlank { null } ?: JSONObject.NULL) }
        commissionPercent?.let { body.put("commission_percent", it) }
        isActive?.let { body.put("is_active", it) }
        if (body.length() > 0) {
            val rows = SupabaseHttp.update("staff", "id=eq.$staffId", body)
            if (rows.length() == 0) throw SupabaseException("Stylist not found.")
        }
    }

    suspend fun getStaffHours(staffId: String): SalonResult<List<StaffHours>> = io {
        val rows = SupabaseHttp.select("staff_hours?staff_id=eq.$staffId&select=*").objects().associateBy { it.optInt("day_of_week") }
        (0..6).map { day ->
            val r = rows[day]
            StaffHours(
                id = r?.str("id").orEmpty(),
                staffId = staffId,
                dayOfWeek = day,
                isWorking = r != null,
                startTime = r?.let { IstTime.hhmm(it.str("start_time")) } ?: "10:00",
                endTime = r?.let { IstTime.hhmm(it.str("end_time")) } ?: "19:00"
            )
        }
    }

    suspend fun saveStaffHours(staffId: String, hours: List<StaffHours>): SalonResult<Unit> = io {
        val working = hours.filter { it.isWorking }
        working.forEach { if (it.endTime <= it.startTime) throw SupabaseException("End time must be after start time.") }
        SupabaseHttp.delete("staff_hours", "staff_id=eq.$staffId")
        val rows = JSONArray()
        working.forEach {
            rows.put(JSONObject().put("staff_id", staffId).put("day_of_week", it.dayOfWeek).put("start_time", it.startTime).put("end_time", it.endTime))
        }
        if (rows.length() > 0) SupabaseHttp.insert("staff_hours", rows)
        Unit
    }

    /**
     * Saves what a stylist can do. An all-rounder gets every service of the salon (and, through the
     * database, every service added later); otherwise exactly the ticked services.
     */
    suspend fun saveStaffServicesForStaff(staffId: String, selectedServiceIds: List<String>, doesAllServices: Boolean): SalonResult<Unit> = io {
        SupabaseHttp.update("staff", "id=eq.$staffId", JSONObject().put("does_all_services", doesAllServices))
        val ids = if (doesAllServices) {
            SupabaseHttp.select("services?salon_id=eq.${salonIdBlocking()}&select=id").objects().map { it.getString("id") }
        } else selectedServiceIds
        SupabaseHttp.delete("staff_services", "staff_id=eq.$staffId")
        val rows = JSONArray()
        ids.distinct().forEach { rows.put(JSONObject().put("staff_id", staffId).put("service_id", it)) }
        if (rows.length() > 0) SupabaseHttp.insert("staff_services", rows)
        Unit
    }

    /** Upcoming bookings that would lose their stylist's skill if these services are removed from them. */
    suspend fun countFutureBookingsFor(staffId: String, serviceIds: List<String>): SalonResult<Int> = io {
        if (serviceIds.isEmpty()) return@io 0
        SupabaseHttp.rpc(
            "count_future_bookings_for",
            JSONObject().put("p_staff_id", staffId).put("p_service_ids", JSONArray(serviceIds))
        ).trim().toIntOrNull() ?: 0
    }

    /** Setup checklist for the Go Live screen. */
    suspend fun getSetupStatus(): SalonResult<SalonSetupStatus> = io {
        val o = JSONObject(SupabaseHttp.rpc("get_salon_setup_status", JSONObject()))
        fun list(key: String): List<String> = o.optJSONArray(key)?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList()
        SalonSetupStatus(
            isVerified = o.optBoolean("is_verified"),
            isActive = o.optBoolean("is_active"),
            isLive = o.optBoolean("is_live"),
            ready = o.optBoolean("ready"),
            missing = list("missing"),
            hoursSet = o.optBoolean("hours_set"),
            serviceCount = o.optInt("service_count"),
            staffCount = o.optInt("staff_count"),
            locationSet = o.optBoolean("location_set"),
            servicesWithoutStaff = list("services_without_staff"),
            staffWithoutServices = list("staff_without_services"),
            staffWithoutHours = list("staff_without_hours")
        )
    }

    // ======================= Hours & payout =======================

    suspend fun getSalonHours(salonId: String): SalonResult<List<SalonHours>> = io {
        val rows = SupabaseHttp.select("salon_hours?salon_id=eq.$salonId&select=*").objects().associateBy { it.optInt("day_of_week") }
        (0..6).map { day ->
            val r = rows[day]
            SalonHours(
                id = r?.str("id").orEmpty(),
                salonId = salonId,
                dayOfWeek = day,
                isClosed = r?.optBoolean("is_closed") ?: true,
                openTime = r?.str("open_time")?.let(IstTime::hhmm) ?: "10:00",
                closeTime = r?.str("close_time")?.let(IstTime::hhmm) ?: "20:00"
            )
        }
    }

    suspend fun saveSalonHours(salonId: String, hours: List<SalonHours>): SalonResult<Unit> = io {
        val rows = JSONArray()
        hours.forEach { h ->
            if (!h.isClosed && h.closeTime <= h.openTime) throw SupabaseException("Closing time must be after opening time.")
            rows.put(
                JSONObject().put("salon_id", salonId).put("day_of_week", h.dayOfWeek).put("is_closed", h.isClosed)
                    .put("open_time", h.openTime).put("close_time", h.closeTime)
            )
        }
        SupabaseHttp.insert("salon_hours", rows, upsertOn = "salon_id,day_of_week")
        Unit
    }

    suspend fun getSalonPayoutDetails(salonId: String): SalonResult<SalonPayoutDetails?> = io {
        SupabaseHttp.select("salon_payout_details?salon_id=eq.$salonId&select=*").objects().firstOrNull()?.let { o ->
            SalonPayoutDetails(
                salonId = o.str("salon_id"),
                accountHolderName = o.str("account_holder_name").orEmpty(),
                upiId = o.str("upi_id").orEmpty(),
                bankAccountNumber = o.str("bank_account_number").orEmpty(),
                bankIfsc = o.str("bank_ifsc").orEmpty()
            )
        }
    }

    suspend fun saveSalonPayoutDetails(details: SalonPayoutDetails): SalonResult<Unit> = io {
        val body = JSONObject()
            .put("salon_id", details.salonId?.takeIf { isUuid(it) } ?: salonIdBlocking())
            .put("account_holder_name", details.accountHolderName.trim().ifBlank { null } ?: JSONObject.NULL)
            .put("upi_id", details.upiId.trim().ifBlank { null } ?: JSONObject.NULL)
            .put("bank_account_number", details.bankAccountNumber.trim().ifBlank { null } ?: JSONObject.NULL)
            .put("bank_ifsc", details.bankIfsc.trim().uppercase().ifBlank { null } ?: JSONObject.NULL)
        SupabaseHttp.insert("salon_payout_details", body, upsertOn = "salon_id")
        Unit
    }

    // ======================= Facilities =======================

    suspend fun getAmenities(): SalonResult<List<Amenity>> = io {
        SupabaseHttp.select("amenities?is_active=eq.true&select=id,name,icon,group_name,exclusive_group,highlight,sort_order&order=sort_order,name")
            .objects().map {
                Amenity(
                    id = it.getString("id"), name = it.optString("name"), icon = it.optString("icon"),
                    groupName = it.optString("group_name"), exclusiveGroup = it.str("exclusive_group"),
                    highlight = it.optBoolean("highlight"), sortOrder = it.optInt("sort_order", 100)
                )
            }
    }

    /** Replaces the salon's facilities (the server checks the list and the one-of rules). */
    suspend fun saveMyAmenities(ids: List<String>): SalonResult<Unit> = io {
        SupabaseHttp.rpc("set_my_amenities", JSONObject().put("p_amenity_ids", JSONArray(ids)))
        Unit
    }

    // ======================= Wallet & withdrawals =======================

    suspend fun getMyWallet(): SalonResult<SalonWallet> = io {
        val o = JSONObject(SupabaseHttp.rpc("get_my_wallet", JSONObject()))
        val list = o.optJSONArray("withdrawals") ?: JSONArray()
        SalonWallet(
            earned = o.optDouble("earned", 0.0),
            commission = o.optDouble("commission", 0.0),
            commissionRate = o.optDouble("commission_rate", 0.0),
            held = o.optDouble("held", 0.0),
            withdrawn = o.optDouble("withdrawn", 0.0),
            pending = o.optDouble("pending", 0.0),
            available = o.optDouble("available", 0.0),
            owed = o.optDouble("owed", 0.0),
            minWithdrawal = o.optDouble("min_withdrawal", 100.0),
            hasUpi = o.optBoolean("has_upi"),
            hasBank = o.optBoolean("has_bank"),
            withdrawals = list.objects().map { w ->
                WithdrawalRequest(
                    id = w.getString("id"),
                    amount = w.optDouble("amount", 0.0),
                    method = w.optString("method"),
                    status = w.optString("status"),
                    upiId = w.str("upi_id"),
                    bankAccountLast4 = w.str("bank_account_last4"),
                    payoutReference = w.str("payout_reference"),
                    adminNote = w.str("admin_note"),
                    createdAt = IstTime.toLocal(w.str("created_at")).orEmpty(),
                    processedAt = IstTime.toLocal(w.str("processed_at"))
                )
            }
        )
    }

    /** Asks the platform to pay [amount] rupees to the saved UPI ID or bank account. */
    suspend fun requestWithdrawal(amount: Double, method: String): SalonResult<Unit> = io {
        SupabaseHttp.rpc("request_withdrawal", JSONObject().put("p_amount", amount).put("p_method", method))
        Unit
    }

    suspend fun cancelWithdrawal(id: String): SalonResult<Unit> = io {
        SupabaseHttp.rpc("cancel_my_withdrawal", JSONObject().put("p_id", id))
        Unit
    }

    // ======================= Earnings, reviews, notifications, profile =======================

    suspend fun getEarningsSummary(fromDate: String, toDate: String): SalonResult<List<StaffEarningsSummary>> = io {
        JSONArray(SupabaseHttp.rpc("get_earnings_summary", JSONObject().put("p_from", fromDate).put("p_to", toDate)))
            .objects().map { o ->
                StaffEarningsSummary(
                    staffName = o.optString("staff_name"),
                    completedCount = o.optInt("completed_count"),
                    revenue = o.optDouble("revenue", 0.0),
                    commissionPercent = o.optDouble("commission_percent", 0.0),
                    commissionAmount = o.optDouble("commission_amount", 0.0),
                    noShowCount = o.optInt("no_show_count"),
                    cancelledCount = o.optInt("cancelled_count")
                )
            }
    }

    suspend fun getSalonReviews(salonId: String): SalonResult<List<SalonReview>> = io {
        SupabaseHttp.select("reviews?salon_id=eq.$salonId&select=*,staff(name)&order=created_at.desc&limit=100")
            .objects().map { o ->
                SalonReview(
                    id = o.getString("id"),
                    salonId = o.optString("salon_id"),
                    bookingId = o.str("booking_id"),
                    customerId = o.str("customer_id"),
                    customerName = o.str("customer_name") ?: "Customer",
                    rating = o.optDouble("rating", 0.0),
                    comment = o.str("comment"),
                    staffId = o.str("staff_id"),
                    staffName = nested(o, "staff", "name"),
                    ownerReply = o.str("owner_reply"),
                    createdAt = IstTime.toLocal(o.str("created_at"))
                )
            }
    }

    suspend fun replyToReview(reviewId: String, replyText: String): SalonResult<Unit> = io {
        if (replyText.isBlank()) throw SupabaseException("Please write a reply.")
        val rows = SupabaseHttp.update("reviews", "id=eq.$reviewId", JSONObject().put("owner_reply", replyText.trim()))
        if (rows.length() == 0) throw SupabaseException("Review not found.")
    }

    suspend fun getNotifications(userId: String): SalonResult<List<SalonNotification>> = io {
        SupabaseHttp.select("notifications?user_id=eq.$userId&select=*&order=created_at.desc&limit=100")
            .objects().map { o ->
                SalonNotification(
                    id = o.getString("id"),
                    userId = o.optString("user_id"),
                    type = o.optString("type"),
                    title = o.optString("title"),
                    body = o.str("body").orEmpty(),
                    bookingId = o.str("booking_id"),
                    isRead = o.optBoolean("is_read"),
                    createdAt = IstTime.toLocal(o.str("created_at"))
                )
            }
    }

    suspend fun markNotificationRead(notificationId: String): SalonResult<Unit> = io {
        SupabaseHttp.update("notifications", "id=eq.$notificationId", JSONObject().put("is_read", true))
        Unit
    }

    @Suppress("UNUSED_PARAMETER")
    suspend fun markAllNotificationsRead(salonId: String): SalonResult<Unit> = io {
        val uid = authRepository.getUserId() ?: throw SupabaseException("Your session has expired. Please sign in again.", 401)
        SupabaseHttp.update("notifications", "user_id=eq.$uid&is_read=eq.false", JSONObject().put("is_read", true))
        Unit
    }

    /**
     * Permanently deletes the owner's account (Play Store requirement): refuses while customers have
     * upcoming bookings, then removes the owner's uploaded files and finally the account itself.
     */
    suspend fun deleteMyAccount(): SalonResult<Unit> = io {
        val uid = authRepository.getUserId() ?: throw SupabaseException("Your session has expired. Please sign in again.", 401)
        val salon = authRepository.fetchSalonsNow() ?: authRepository.fetchSalons().firstOrNull()
        if (salon != null) {
            val upcoming = SupabaseHttp.select(
                "bookings?salon_id=eq.${salon.id}&status=in.(confirmed,arrived,in_service)" +
                    "&end_time=gt.${java.net.URLEncoder.encode(IstTime.now(), "UTF-8")}" +
                    "&select=id&limit=1"
            )
            if (upcoming.length() > 0) {
                throw SupabaseException("Your salon has upcoming bookings. Cancel or complete them before deleting your account.")
            }
        }
        listOf("salon-documents", "salon-photos", "service-images").forEach { bucket ->
            SupabaseHttp.removeObjects(bucket, SupabaseHttp.listObjects(bucket, uid))
        }
        SupabaseHttp.rpc("delete_my_account")
        authRepository.clearSession()
    }

    suspend fun updateProfileLanguage(userId: String, language: String): SalonResult<Unit> {
        authRepository.saveLanguage(language)
        return io {
            SupabaseHttp.update("profiles", "id=eq.$userId", JSONObject().put("language", if (language == "hi") "hi" else "en"))
            Unit
        }
    }
}
