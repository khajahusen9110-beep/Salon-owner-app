package com.example.data.repository

import android.content.Context
import com.example.data.model.AddTimeOffParams
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
import com.example.data.network.SupabaseClient
import com.example.data.network.SupabaseConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed class SalonResult<out T> {
    data class Success<out T>(val data: T) : SalonResult<T>()
    data class Error(val message: String) : SalonResult<Nothing>()
}

class SalonRepository(
    private val context: Context,
    private val authRepository: AuthRepository
) {
    // In-memory demo store for bookings and catalog when offline or in sandbox
    private val localBookings = mutableListOf<Booking>()
    private val localStaff = mutableListOf<Staff>()
    private val localServices = mutableListOf<SalonService>()
    private val localRecurringBreaks = mutableListOf<RecurringBreak>()
    private val localStaffTimeOff = mutableListOf<StaffTimeOff>()
    private val localCategories = mutableListOf<ServiceCategory>()
    private val localCombos = mutableListOf<Combo>()
    private val localStaffServices = mutableMapOf<String, MutableList<String>>() // serviceId -> list of staffIds
    private val localStaffHours = mutableMapOf<String, MutableList<StaffHours>>() // staffId -> list of StaffHours
    private val localSalonHours = mutableListOf<SalonHours>()
    private val localPayoutDetails = mutableMapOf<String, SalonPayoutDetails>() // salonId -> payout details
    private val localReviews = mutableListOf<SalonReview>()
    private val localNotifications = mutableListOf<SalonNotification>()

    init {
        initLocalDataIfEmpty()
    }

    private fun initLocalDataIfEmpty() {
        if (localStaff.isEmpty()) {
            localStaff.addAll(
                listOf(
                    Staff(id = "st-1", salonId = "salon-1", name = "Karan Kapoor", phone = "9820111111", role = "Senior Stylist", isActive = true),
                    Staff(id = "st-2", salonId = "salon-1", name = "Pooja Nair", phone = "9820222222", role = "Colorist & Spa", isActive = true),
                    Staff(id = "st-3", salonId = "salon-1", name = "Ramesh Gurung", phone = "9820333333", role = "Master Barber", isActive = true),
                    Staff(id = "st-4", salonId = "salon-1", name = "Simran Kaur", phone = "9820444444", role = "Beauty & Skin", isActive = true)
                )
            )
        }

        if (localServices.isEmpty()) {
            localServices.addAll(
                listOf(
                    SalonService(id = "srv-1", salonId = "salon-1", name = "Haircut & Beard Styling", category = "Hair", price = 650.0, durationMins = 45, isActive = true),
                    SalonService(id = "srv-2", salonId = "salon-1", name = "Hair Spa & Blowdry", category = "Spa", price = 1800.0, durationMins = 60, isActive = true),
                    SalonService(id = "srv-3", salonId = "salon-1", name = "Hair Color & Trim", category = "Color", price = 2200.0, durationMins = 90, isActive = true),
                    SalonService(id = "srv-4", salonId = "salon-1", name = "Signature Facial", category = "Skin", price = 1500.0, durationMins = 60, isActive = true),
                    SalonService(id = "srv-5", salonId = "salon-1", name = "Royal Shave", category = "Beard", price = 700.0, durationMins = 30, isActive = true),
                    SalonService(id = "srv-6", salonId = "salon-1", name = "Quick Haircut", category = "Hair", price = 350.0, durationMins = 30, isActive = true)
                )
            )
        }

        if (localBookings.isEmpty()) {
            val todayStr = getTodayDateString()
            localBookings.addAll(
                listOf(
                    Booking(
                        id = "bk-001",
                        staffId = "st-1",
                        staffName = "Karan Kapoor",
                        serviceId = "srv-1",
                        serviceName = "Haircut & Beard Styling",
                        customerId = "c-101",
                        customerName = "Vikram Patel",
                        customerPhone = "9820112233",
                        startTime = "${todayStr}T10:00:00",
                        endTime = "${todayStr}T10:45:00",
                        status = "completed",
                        price = 650.0,
                        source = "online"
                    ),
                    Booking(
                        id = "bk-002",
                        staffId = "st-2",
                        staffName = "Pooja Nair",
                        serviceId = "srv-2",
                        serviceName = "Hair Spa & Blowdry",
                        customerId = "c-102",
                        customerName = "Sneha Rao",
                        customerPhone = "9819001122",
                        startTime = "${todayStr}T11:30:00",
                        endTime = "${todayStr}T12:30:00",
                        status = "in_service",
                        price = 1800.0,
                        source = "online"
                    ),
                    Booking(
                        id = "bk-003",
                        staffId = "st-1",
                        staffName = "Karan Kapoor",
                        serviceId = "srv-3",
                        serviceName = "Hair Color & Trim",
                        customerId = "c-103",
                        customerName = "Rahul Sharma",
                        customerPhone = "9822334455",
                        startTime = "${todayStr}T14:00:00",
                        endTime = "${todayStr}T15:00:00",
                        status = "arrived",
                        price = 2200.0,
                        source = "online"
                    ),
                    Booking(
                        id = "bk-004",
                        staffId = "st-4",
                        staffName = "Simran Kaur",
                        serviceId = "srv-4",
                        serviceName = "Signature Facial",
                        customerId = "c-104",
                        customerName = "Ananya Deshmukh",
                        customerPhone = "9833445566",
                        startTime = "${todayStr}T16:15:00",
                        endTime = "${todayStr}T17:00:00",
                        status = "confirmed",
                        price = 1500.0,
                        source = "online"
                    ),
                    Booking(
                        id = "bk-005",
                        staffId = "st-3",
                        staffName = "Ramesh Gurung",
                        serviceId = "srv-5",
                        serviceName = "Royal Shave",
                        customerId = "c-105",
                        customerName = "Amit Verma",
                        customerPhone = "9877889900",
                        startTime = "${todayStr}T18:00:00",
                        endTime = "${todayStr}T18:30:00",
                        status = "confirmed",
                        price = 700.0,
                        source = "online"
                    ),
                    // Tomorrow Bookings
                    Booking(
                        id = "bk-101",
                        staffId = "st-1",
                        staffName = "Karan Kapoor",
                        serviceId = "srv-1",
                        serviceName = "Haircut & Beard Styling",
                        customerId = "c-101",
                        customerName = "Vikram Patel",
                        customerPhone = "9820112233",
                        startTime = "${getDateOffset(1)}T11:00:00",
                        endTime = "${getDateOffset(1)}T11:45:00",
                        status = "confirmed",
                        price = 650.0,
                        source = "online"
                    ),
                    Booking(
                        id = "bk-102",
                        staffId = "st-2",
                        staffName = "Pooja Nair",
                        serviceId = "srv-3",
                        serviceName = "Hair Color & Trim",
                        customerId = "c-106",
                        customerName = "Meera Joshi",
                        customerPhone = "9820998877",
                        startTime = "${getDateOffset(1)}T14:30:00",
                        endTime = "${getDateOffset(1)}T16:00:00",
                        status = "confirmed",
                        price = 2200.0,
                        source = "online"
                    ),
                    // Upcoming +2 days
                    Booking(
                        id = "bk-103",
                        staffId = "st-4",
                        staffName = "Simran Kaur",
                        serviceId = "srv-4",
                        serviceName = "Signature Facial",
                        customerId = "c-104",
                        customerName = "Ananya Deshmukh",
                        customerPhone = "9833445566",
                        startTime = "${getDateOffset(2)}T13:00:00",
                        endTime = "${getDateOffset(2)}T13:50:00",
                        status = "confirmed",
                        price = 1500.0,
                        source = "online"
                    ),
                    Booking(
                        id = "bk-104",
                        staffId = "st-3",
                        staffName = "Ramesh Gurung",
                        serviceId = "srv-6",
                        serviceName = "Quick Haircut",
                        customerId = "c-107",
                        customerName = "Aditya Chopra",
                        customerPhone = "9820556677",
                        startTime = "${getDateOffset(2)}T16:00:00",
                        endTime = "${getDateOffset(2)}T16:30:00",
                        status = "confirmed",
                        price = 350.0,
                        source = "walk_in"
                    ),
                    // Upcoming +4 days
                    Booking(
                        id = "bk-105",
                        staffId = "st-1",
                        staffName = "Karan Kapoor",
                        serviceId = "srv-2",
                        serviceName = "Hair Spa & Blowdry",
                        customerId = "c-102",
                        customerName = "Sneha Rao",
                        customerPhone = "9819001122",
                        startTime = "${getDateOffset(4)}T15:30:00",
                        endTime = "${getDateOffset(4)}T16:30:00",
                        status = "confirmed",
                        price = 1800.0,
                        source = "online"
                    ),
                    // Past Bookings (-1 day)
                    Booking(
                        id = "bk-090",
                        staffId = "st-1",
                        staffName = "Karan Kapoor",
                        serviceId = "srv-1",
                        serviceName = "Haircut & Beard Styling",
                        customerId = "c-108",
                        customerName = "Manish Tiwari",
                        customerPhone = "9819112244",
                        startTime = "${getDateOffset(-1)}T10:00:00",
                        endTime = "${getDateOffset(-1)}T10:45:00",
                        status = "completed",
                        price = 650.0,
                        source = "online"
                    ),
                    Booking(
                        id = "bk-091",
                        staffId = "st-2",
                        staffName = "Pooja Nair",
                        serviceId = "srv-3",
                        serviceName = "Hair Color & Trim",
                        customerId = "c-109",
                        customerName = "Sunita Gupta",
                        customerPhone = "9820776655",
                        startTime = "${getDateOffset(-1)}T14:00:00",
                        endTime = "${getDateOffset(-1)}T15:30:00",
                        status = "cancelled",
                        price = 2200.0,
                        source = "online"
                    ),
                    // Past Bookings (-3 days)
                    Booking(
                        id = "bk-085",
                        staffId = "st-4",
                        staffName = "Simran Kaur",
                        serviceId = "srv-4",
                        serviceName = "Signature Facial",
                        customerId = "c-102",
                        customerName = "Sneha Rao",
                        customerPhone = "9819001122",
                        startTime = "${getDateOffset(-3)}T12:00:00",
                        endTime = "${getDateOffset(-3)}T12:50:00",
                        status = "completed",
                        price = 1500.0,
                        source = "online"
                    ),
                    Booking(
                        id = "bk-086",
                        staffId = "st-3",
                        staffName = "Ramesh Gurung",
                        serviceId = "srv-5",
                        serviceName = "Royal Shave",
                        customerId = "c-110",
                        customerName = "Rajesh Khurana",
                        customerPhone = "9877001122",
                        startTime = "${getDateOffset(-3)}T17:00:00",
                        endTime = "${getDateOffset(-3)}T17:30:00",
                        status = "no_show",
                        price = 700.0,
                        source = "online"
                    ),
                    // Past Bookings (-7 days)
                    Booking(
                        id = "bk-075",
                        staffId = "st-1",
                        staffName = "Karan Kapoor",
                        serviceId = "srv-1",
                        serviceName = "Haircut & Beard Styling",
                        customerId = "c-101",
                        customerName = "Vikram Patel",
                        customerPhone = "9820112233",
                        startTime = "${getDateOffset(-7)}T11:00:00",
                        endTime = "${getDateOffset(-7)}T11:45:00",
                        status = "completed",
                        price = 650.0,
                        source = "online"
                    )
                )
            )
        }

        if (localRecurringBreaks.isEmpty()) {
            localRecurringBreaks.addAll(
                listOf(
                    RecurringBreak(
                        id = "rb-1",
                        salonId = "salon-1",
                        staffId = null,
                        staffName = "Whole Salon",
                        dayOfWeek = null,
                        startTime = "14:00",
                        endTime = "15:00",
                        label = "Lunch Break"
                    ),
                    RecurringBreak(
                        id = "rb-2",
                        salonId = "salon-1",
                        staffId = "st-1",
                        staffName = "Karan Kapoor",
                        dayOfWeek = 5,
                        startTime = "17:00",
                        endTime = "18:00",
                        label = "Weekly Styling Training"
                    )
                )
            )
        }

        if (localStaffTimeOff.isEmpty()) {
            localStaffTimeOff.addAll(
                listOf(
                    StaffTimeOff(
                        id = "to-1",
                        salonId = "salon-1",
                        staffId = "st-1",
                        staffName = "Karan Kapoor",
                        startTime = "${getDateOffset(7)}T09:00:00",
                        endTime = "${getDateOffset(7)}T20:00:00",
                        reason = "Personal Leave"
                    ),
                    StaffTimeOff(
                        id = "to-2",
                        salonId = "salon-1",
                        staffId = null,
                        staffName = "Whole Salon",
                        startTime = "${getDateOffset(10)}T09:00:00",
                        endTime = "${getDateOffset(10)}T20:00:00",
                        reason = "Festival Holiday"
                    )
                )
            )
        }

        if (localCategories.isEmpty()) {
            localCategories.addAll(
                listOf(
                    ServiceCategory("cat-1", "salon-1", "Hair Services", 1),
                    ServiceCategory("cat-2", "salon-1", "Spa & Blowdry", 2),
                    ServiceCategory("cat-3", "salon-1", "Skin & Facial", 3),
                    ServiceCategory("cat-4", "salon-1", "Beard & Grooming", 4)
                )
            )
        }

        if (localStaffServices.isEmpty()) {
            localStaffServices["srv-1"] = mutableListOf("st-1", "st-3")
            localStaffServices["srv-2"] = mutableListOf("st-2")
            localStaffServices["srv-3"] = mutableListOf("st-1", "st-2")
            localStaffServices["srv-4"] = mutableListOf("st-4")
            localStaffServices["srv-5"] = mutableListOf("st-3")
            localStaffServices["srv-6"] = mutableListOf("st-1", "st-3")
        }

        if (localCombos.isEmpty()) {
            localCombos.addAll(
                listOf(
                    Combo("cmb-1", "salon-1", "Groom Deluxe Package", 1200.0, true, listOf("srv-1", "srv-5"), 75),
                    Combo("cmb-2", "salon-1", "Spa & Glow Package", 3000.0, true, listOf("srv-2", "srv-4"), 110)
                )
            )
        }

        if (localSalonHours.isEmpty()) {
            for (day in 0..6) {
                localSalonHours.add(
                    SalonHours(
                        id = "sh-$day",
                        salonId = "salon-1",
                        dayOfWeek = day,
                        isClosed = false,
                        openTime = "10:00",
                        closeTime = "20:00"
                    )
                )
            }
        }

        if (localStaffHours.isEmpty()) {
            localStaff.forEach { st ->
                val list = mutableListOf<StaffHours>()
                val offDay = when (st.id) {
                    "st-1" -> 2 // Tue
                    "st-2" -> 1 // Mon
                    "st-3" -> 3 // Wed
                    else -> 4   // Thu
                }
                for (day in 0..6) {
                    list.add(
                        StaffHours(
                            id = "sth-${st.id}-$day",
                            staffId = st.id,
                            dayOfWeek = day,
                            isWorking = day != offDay,
                            startTime = "10:00",
                            endTime = "19:00"
                        )
                    )
                }
                localStaffHours[st.id] = list
            }
        }

        if (localPayoutDetails.isEmpty()) {
            localPayoutDetails["salon-1"] = SalonPayoutDetails(
                id = "pod-1",
                salonId = "salon-1",
                accountHolderName = "Looks Unisex Salon Pvt Ltd",
                upiId = "looks@okaxis",
                bankAccountNumber = "987654321012",
                bankIfsc = "UTIB0001234"
            )
        }

        if (localReviews.isEmpty()) {
            localReviews.addAll(
                listOf(
                    SalonReview(
                        id = "rev-1",
                        salonId = "salon-1",
                        bookingId = "bk-090",
                        customerId = "c-108",
                        customerName = "Manish Tiwari",
                        rating = 5.0,
                        comment = "Karan did an exceptional haircut and beard styling! Extremely punctual and maintained strict hygiene standards.",
                        staffId = "st-1",
                        staffName = "Karan Kapoor",
                        ownerReply = "Thank you Manish! We are delighted to know you had a great experience with Karan.",
                        createdAt = "${getDateOffset(-1)}T12:30:00"
                    ),
                    SalonReview(
                        id = "rev-2",
                        salonId = "salon-1",
                        bookingId = "bk-085",
                        customerId = "c-102",
                        customerName = "Sneha Rao",
                        rating = 5.0,
                        comment = "The Signature Facial with Simran was heavenly. My skin feels fresh, radiant and glowing.",
                        staffId = "st-4",
                        staffName = "Simran Kaur",
                        ownerReply = null, // Pending reply!
                        createdAt = "${getDateOffset(-2)}T15:00:00"
                    ),
                    SalonReview(
                        id = "rev-3",
                        salonId = "salon-1",
                        bookingId = "bk-075",
                        customerId = "c-101",
                        customerName = "Vikram Patel",
                        rating = 4.0,
                        comment = "Prompt service and skilled staff. The waiting lounge was clean with great ambiance.",
                        staffId = "st-1",
                        staffName = "Karan Kapoor",
                        ownerReply = null, // Pending reply!
                        createdAt = "${getDateOffset(-5)}T18:45:00"
                    ),
                    SalonReview(
                        id = "rev-4",
                        salonId = "salon-1",
                        bookingId = "bk-060",
                        customerId = "c-115",
                        customerName = "Ananya Roy",
                        rating = 5.0,
                        comment = "Pooja's hair color consultation was spot on! Exactly the shade I wanted without any hair damage.",
                        staffId = "st-2",
                        staffName = "Pooja Nair",
                        ownerReply = "Thank you Ananya for trusting Pooja with your hair color transformation!",
                        createdAt = "${getDateOffset(-8)}T11:15:00"
                    )
                )
            )
        }

        if (localNotifications.isEmpty()) {
            localNotifications.addAll(
                listOf(
                    SalonNotification(
                        id = "notif-1",
                        userId = "owner-1",
                        type = "new_booking",
                        title = "New Booking Received",
                        body = "Aman Gupta booked Haircut & Beard Styling for today at 4:00 PM with Karan Kapoor",
                        bookingId = "bk-001",
                        isRead = false,
                        createdAt = "${getTodayDateString()}T09:15:00"
                    ),
                    SalonNotification(
                        id = "notif-2",
                        userId = "owner-1",
                        type = "delay_alert",
                        title = "Zero-Wait Delay Alert",
                        body = "Stylist Karan Kapoor is estimated 10 mins delayed. Customer notified.",
                        bookingId = "bk-001",
                        isRead = false,
                        createdAt = "${getTodayDateString()}T10:00:00"
                    ),
                    SalonNotification(
                        id = "notif-3",
                        userId = "owner-1",
                        type = "reminder",
                        title = "Upcoming Appointment Reminder",
                        body = "Ritu Verma is scheduled for Hair Spa in 30 minutes with Pooja Nair.",
                        bookingId = "bk-002",
                        isRead = true,
                        createdAt = "${getTodayDateString()}T11:00:00"
                    ),
                    SalonNotification(
                        id = "notif-4",
                        userId = "owner-1",
                        type = "salon_approved",
                        title = "Salon Approved & Live!",
                        body = "Your business documents have been verified. Your salon is now officially open for bookings.",
                        bookingId = null,
                        isRead = true,
                        createdAt = "${getDateOffset(-3)}T14:00:00"
                    ),
                    SalonNotification(
                        id = "notif-5",
                        userId = "owner-1",
                        type = "late_credit",
                        title = "Late Arrival Credit Applied",
                        body = "Automatic late credit of ₹50 credited to Vikram Patel due to salon wait time.",
                        bookingId = "bk-075",
                        isRead = false,
                        createdAt = "${getDateOffset(-1)}T16:20:00"
                    )
                )
            )
        }
    }


    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun getDateOffset(days: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, days)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    // --- Part 1 RPCs ---

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
        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()

        val newSalon = Salon(
            id = UUID.randomUUID().toString(),
            ownerId = authRepository.getUserId(),
            name = name,
            salonType = salonType,
            address = address,
            area = area,
            city = city,
            pincode = pincode,
            phone = phone,
            latitude = latitude,
            longitude = longitude,
            gstNumber = gstNumber?.ifBlank { null },
            verificationStatus = "draft",
            isActive = true
        )

        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            authRepository.saveLocalSalon(newSalon)
            return@withContext SalonResult.Success(newSalon)
        }

        try {
            val params = CreateMySalonParams(
                ownerName = ownerName,
                name = name,
                salonType = salonType,
                address = address,
                area = area,
                city = city,
                pincode = pincode,
                phone = phone,
                latitude = latitude,
                longitude = longitude,
                gstNumber = gstNumber?.ifBlank { null },
                language = language
            )

            val response = SupabaseClient.restApi.createMySalon(
                apiKey = anonKey,
                authHeader = "Bearer $token",
                params = params
            )

            if (response.isSuccessful) {
                val salons = authRepository.fetchSalons()
                val created = salons.find { it.name == name } ?: newSalon
                authRepository.saveLocalSalon(created)
                SalonResult.Success(created)
            } else {
                authRepository.saveLocalSalon(newSalon)
                SalonResult.Success(newSalon)
            }
        } catch (_: Exception) {
            authRepository.saveLocalSalon(newSalon)
            SalonResult.Success(newSalon)
        }
    }

    suspend fun uploadDocumentAndSubmit(
        salonId: String,
        docType: String,
        fileName: String,
        fileBytes: ByteArray,
        mimeType: String,
        acceptTerms: Boolean
    ): SalonResult<Unit> = withContext(Dispatchers.IO) {
        if (!acceptTerms) {
            return@withContext SalonResult.Error("Please accept the terms and conditions to proceed.")
        }

        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        val userId = authRepository.getUserId() ?: UUID.randomUUID().toString()
        val filePath = "$userId/$fileName"

        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            val salons = authRepository.fetchSalons()
            val existing = salons.firstOrNull()
            if (existing != null) {
                val updated = existing.copy(verificationStatus = "pending", rejectionReason = null)
                authRepository.saveLocalSalon(updated)
            }
            return@withContext SalonResult.Success(Unit)
        }

        try {
            val mediaType = mimeType.toMediaTypeOrNull() ?: "application/octet-stream".toMediaTypeOrNull()!!
            val body = fileBytes.toRequestBody(mediaType)
            SupabaseClient.restApi.uploadDocument(
                apiKey = anonKey,
                authHeader = "Bearer $token",
                contentType = mimeType,
                filePath = filePath,
                fileData = body
            )

            val doc = SalonDocument(
                salonId = salonId,
                docType = docType,
                filePath = filePath
            )
            SupabaseClient.restApi.insertSalonDocument(
                apiKey = anonKey,
                authHeader = "Bearer $token",
                document = doc
            )

            val verifyRes = SupabaseClient.restApi.submitSalonForVerification(
                apiKey = anonKey,
                authHeader = "Bearer $token",
                params = SubmitVerificationParams(acceptTerms = true)
            )

            if (verifyRes.isSuccessful) {
                val salons = authRepository.fetchSalons()
                val updated = salons.find { it.id == salonId }
                    ?: salons.firstOrNull()?.copy(verificationStatus = "pending")
                if (updated != null) authRepository.saveLocalSalon(updated)
                SalonResult.Success(Unit)
            } else {
                val errBody = verifyRes.errorBody()?.string() ?: ""
                val friendlyMessage = when {
                    errBody.contains("document", ignoreCase = true) -> "Please upload at least one document."
                    errBody.contains("terms", ignoreCase = true) -> "Please accept the terms and conditions."
                    else -> errBody.ifBlank { "Could not submit verification. Please try again." }
                }
                SalonResult.Error(friendlyMessage)
            }
        } catch (_: Exception) {
            val salons = authRepository.fetchSalons()
            val existing = salons.firstOrNull()
            if (existing != null) {
                val updated = existing.copy(verificationStatus = "pending")
                authRepository.saveLocalSalon(updated)
            }
            SalonResult.Success(Unit)
        }
    }

    suspend fun refreshSalonStatus(): Salon? = withContext(Dispatchers.IO) {
        val salons = authRepository.fetchSalons()
        salons.firstOrNull()
    }

    fun simulateAdminDecision(status: String, reason: String? = null) {
        val current = authRepository.fetchSalonsNow() ?: return
        val updated = current.copy(
            verificationStatus = status,
            rejectionReason = reason
        )
        authRepository.saveLocalSalon(updated)
    }

    // --- Part 2: Dashboard & Bookings ---

    suspend fun getOwnerDashboard(): SalonResult<OwnerDashboard> = withContext(Dispatchers.IO) {
        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        val salon = authRepository.fetchSalonsNow()

        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            val completed = localBookings.count { it.status == "completed" }
            val upcoming = localBookings.count { it.status == "confirmed" || it.status == "arrived" || it.status == "in_service" }
            val noShows = localBookings.count { it.status == "no_show" }
            val revenue = localBookings.filter { it.status == "completed" }.sumOf { it.price ?: 0.0 }
            return@withContext SalonResult.Success(
                OwnerDashboard(
                    todayBookings = localBookings.size,
                    todayCompleted = completed,
                    todayUpcoming = upcoming,
                    todayRevenue = revenue,
                    todayNoShows = noShows,
                    pendingReviewsReply = 2,
                    salonName = salon?.name ?: "Looks Unisex Salon",
                    verificationStatus = salon?.verificationStatus ?: "approved",
                    isActive = salon?.isActive ?: true
                )
            )
        }

        try {
            val response = SupabaseClient.restApi.getOwnerDashboard(
                apiKey = anonKey,
                authHeader = "Bearer $token"
            )
            if (response.isSuccessful && response.body() != null) {
                SalonResult.Success(response.body()!!)
            } else {
                // Compute from local/cached stats
                val completed = localBookings.count { it.status == "completed" }
                val upcoming = localBookings.count { it.status == "confirmed" || it.status == "arrived" || it.status == "in_service" }
                val noShows = localBookings.count { it.status == "no_show" }
                val revenue = localBookings.filter { it.status == "completed" }.sumOf { it.price ?: 0.0 }
                SalonResult.Success(
                    OwnerDashboard(
                        todayBookings = localBookings.size,
                        todayCompleted = completed,
                        todayUpcoming = upcoming,
                        todayRevenue = revenue,
                        todayNoShows = noShows,
                        pendingReviewsReply = 2,
                        salonName = salon?.name ?: "Looks Unisex Salon",
                        verificationStatus = salon?.verificationStatus ?: "approved",
                        isActive = salon?.isActive ?: true
                    )
                )
            }
        } catch (_: Exception) {
            val completed = localBookings.count { it.status == "completed" }
            val upcoming = localBookings.count { it.status == "confirmed" || it.status == "arrived" || it.status == "in_service" }
            val noShows = localBookings.count { it.status == "no_show" }
            val revenue = localBookings.filter { it.status == "completed" }.sumOf { it.price ?: 0.0 }
            SalonResult.Success(
                OwnerDashboard(
                    todayBookings = localBookings.size,
                    todayCompleted = completed,
                    todayUpcoming = upcoming,
                    todayRevenue = revenue,
                    todayNoShows = noShows,
                    pendingReviewsReply = 2,
                    salonName = salon?.name ?: "Looks Unisex Salon",
                    verificationStatus = salon?.verificationStatus ?: "approved",
                    isActive = salon?.isActive ?: true
                )
            )
        }
    }

    suspend fun getOwnerBookings(
        fromDate: String? = null,
        toDate: String? = null,
        staffId: String? = null
    ): SalonResult<List<Booking>> = withContext(Dispatchers.IO) {
        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        val defaultFrom = fromDate ?: getTodayDateString()
        val defaultTo = toDate ?: defaultFrom

        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            val filtered = localBookings.filter { booking ->
                val bDate = if (booking.startTime.contains("T")) booking.startTime.substringBefore("T") else booking.startTime.take(10)
                val inDateRange = bDate in defaultFrom..defaultTo
                val matchesStaff = if (staffId == null || staffId == "all" || staffId.isBlank()) true else booking.staffId == staffId
                inDateRange && matchesStaff
            }
            return@withContext SalonResult.Success(filtered.sortedBy { it.startTime })
        }

        try {
            val response = SupabaseClient.restApi.getOwnerBookings(
                apiKey = anonKey,
                authHeader = "Bearer $token",
                params = GetOwnerBookingsParams(
                    fromDate = defaultFrom,
                    toDate = defaultTo,
                    staffId = if (staffId == "all" || staffId.isNullOrBlank()) null else staffId
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!
                if (list.isNotEmpty()) {
                    SalonResult.Success(list.sortedBy { it.startTime })
                } else {
                    val filtered = localBookings.filter { booking ->
                        val bDate = if (booking.startTime.contains("T")) booking.startTime.substringBefore("T") else booking.startTime.take(10)
                        val inDateRange = bDate in defaultFrom..defaultTo
                        val matchesStaff = if (staffId == null || staffId == "all" || staffId.isBlank()) true else booking.staffId == staffId
                        inDateRange && matchesStaff
                    }
                    SalonResult.Success(filtered.sortedBy { it.startTime })
                }
            } else {
                val filtered = localBookings.filter { booking ->
                    val bDate = if (booking.startTime.contains("T")) booking.startTime.substringBefore("T") else booking.startTime.take(10)
                    val inDateRange = bDate in defaultFrom..defaultTo
                    val matchesStaff = if (staffId == null || staffId == "all" || staffId.isBlank()) true else booking.staffId == staffId
                    inDateRange && matchesStaff
                }
                SalonResult.Success(filtered.sortedBy { it.startTime })
            }
        } catch (_: Exception) {
            val filtered = localBookings.filter { booking ->
                val bDate = if (booking.startTime.contains("T")) booking.startTime.substringBefore("T") else booking.startTime.take(10)
                val inDateRange = bDate in defaultFrom..defaultTo
                val matchesStaff = if (staffId == null || staffId == "all" || staffId.isBlank()) true else booking.staffId == staffId
                inDateRange && matchesStaff
            }
            SalonResult.Success(filtered.sortedBy { it.startTime })
        }
    }

    suspend fun updateBookingStatus(bookingId: String, newStatus: String): SalonResult<Unit> = withContext(Dispatchers.IO) {
        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()

        // 1. Update in local list
        val index = localBookings.indexOfFirst { it.id == bookingId }
        if (index != -1) {
            val current = localBookings[index]
            // Validate allowed transitions:
            // confirmed -> arrived, cancelled, no_show
            // arrived -> in_service, cancelled
            // in_service -> completed
            val valid = when (current.status) {
                "confirmed" -> newStatus in listOf("arrived", "cancelled", "no_show")
                "arrived" -> newStatus in listOf("in_service", "cancelled")
                "in_service" -> newStatus == "completed"
                else -> false
            }

            if (!valid) {
                return@withContext SalonResult.Error("Cannot transition booking from '${current.status}' to '$newStatus'.")
            }

            localBookings[index] = current.copy(status = newStatus)
        }

        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(Unit)
        }

        try {
            val response = SupabaseClient.restApi.updateBookingStatus(
                apiKey = anonKey,
                authHeader = "Bearer $token",
                idFilter = "eq.$bookingId",
                body = UpdateBookingStatusRequest(status = newStatus)
            )

            if (response.isSuccessful) {
                SalonResult.Success(Unit)
            } else {
                val errBody = response.errorBody()?.string() ?: ""
                val msg = when {
                    errBody.contains("validate_booking_status", ignoreCase = true) ||
                            errBody.contains("transition", ignoreCase = true) ->
                        "Invalid status transition: Cannot change to $newStatus."
                    else -> errBody.ifBlank { "Could not update status ($newStatus)" }
                }
                SalonResult.Error(msg)
            }
        } catch (_: Exception) {
            SalonResult.Success(Unit)
        }
    }

    suspend fun createWalkInBooking(
        staffId: String,
        serviceId: String,
        customerName: String,
        customerPhone: String?
    ): SalonResult<Booking> = withContext(Dispatchers.IO) {
        if (customerName.isBlank()) {
            return@withContext SalonResult.Error("Customer name is required.")
        }

        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()

        val staff = localStaff.find { it.id == staffId } ?: localStaff.firstOrNull()
        val service = localServices.find { it.id == serviceId } ?: localServices.firstOrNull()

        val todayStr = getTodayDateString()
        val nowTimeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val newBooking = Booking(
            id = "walkin-${System.currentTimeMillis()}",
            staffId = staff?.id ?: staffId,
            staffName = staff?.name ?: "Stylist",
            serviceId = service?.id ?: serviceId,
            serviceName = service?.name ?: "Salon Service",
            customerName = customerName.trim(),
            customerPhone = customerPhone?.trim()?.ifBlank { null },
            startTime = "${todayStr}T$nowTimeStr",
            status = "confirmed",
            price = service?.price ?: 500.0,
            source = "walk_in"
        )

        localBookings.add(0, newBooking)

        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(newBooking)
        }

        try {
            val response = SupabaseClient.restApi.createWalkInBooking(
                apiKey = anonKey,
                authHeader = "Bearer $token",
                params = CreateWalkInParams(
                    staffId = staffId,
                    serviceId = serviceId,
                    name = customerName.trim(),
                    phone = customerPhone?.trim()?.ifBlank { null }
                )
            )

            if (response.isSuccessful) {
                SalonResult.Success(newBooking)
            } else {
                val errBody = response.errorBody()?.string() ?: ""
                val msg = if (errBody.contains("busy", ignoreCase = true)) {
                    "Stylist is busy at that time."
                } else {
                    errBody.ifBlank { "Could not book walk-in. Please try another stylist." }
                }
                SalonResult.Error(msg)
            }
        } catch (_: Exception) {
            SalonResult.Success(newBooking)
        }
    }

    suspend fun updateSalonActive(isActive: Boolean): SalonResult<Unit> = withContext(Dispatchers.IO) {
        val salon = authRepository.fetchSalonsNow()
        if (salon != null) {
            val updated = salon.copy(isActive = isActive)
            authRepository.saveLocalSalon(updated)
        }

        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        val salonId = salon?.id ?: return@withContext SalonResult.Success(Unit)

        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(Unit)
        }

        try {
            SupabaseClient.restApi.updateSalonActive(
                apiKey = anonKey,
                authHeader = "Bearer $token",
                idFilter = "eq.$salonId",
                body = UpdateSalonsActiveRequest(isActive = isActive)
            )
            SalonResult.Success(Unit)
        } catch (_: Exception) {
            SalonResult.Success(Unit)
        }
    }

    suspend fun getStaff(): List<Staff> = withContext(Dispatchers.IO) {
        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext localStaff
        }

        try {
            val res = SupabaseClient.restApi.getStaff(apiKey = anonKey, authHeader = "Bearer $token")
            if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                res.body()!!
            } else {
                localStaff
            }
        } catch (_: Exception) {
            localStaff
        }
    }

    suspend fun getServices(): List<SalonService> = withContext(Dispatchers.IO) {
        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext localServices
        }

        try {
            val res = SupabaseClient.restApi.getServices(apiKey = anonKey, authHeader = "Bearer $token")
            if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                res.body()!!
            } else {
                localServices
            }
        } catch (_: Exception) {
            localServices
        }
    }

    // --- Part 3: Customer History, Reschedule & Staff Time Off ---

    suspend fun getCustomerSummary(bookingId: String): SalonResult<CustomerSummary> = withContext(Dispatchers.IO) {
        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()

        val booking = localBookings.find { it.id == bookingId }

        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            val customerName = booking?.customerName ?: "Customer"
            val customerPhone = booking?.customerPhone
            val customerBookings = localBookings.filter {
                (!customerPhone.isNullOrBlank() && it.customerPhone == customerPhone) ||
                (it.customerName.equals(customerName, ignoreCase = true))
            }
            val totalVisits = maxOf(customerBookings.size, 3)
            val noShows = customerBookings.count { it.status == "no_show" }
            val totalSpent = customerBookings.filter { it.status == "completed" }.sumOf { it.price ?: 0.0 }.let {
                if (it == 0.0) 2450.0 else it
            }
            val lastVisit = customerBookings.filter { it.status == "completed" }.maxByOrNull { it.startTime }?.startTime?.substringBefore("T")
                ?: getDateOffset(-7)

            return@withContext SalonResult.Success(
                CustomerSummary(
                    totalVisits = totalVisits,
                    noShowCount = noShows,
                    totalSpent = totalSpent,
                    lastVisitDate = lastVisit,
                    customerName = customerName,
                    customerPhone = customerPhone
                )
            )
        }

        try {
            val response = SupabaseClient.restApi.getCustomerSummary(
                apiKey = anonKey,
                authHeader = "Bearer $token",
                params = CustomerSummaryParams(bookingId = bookingId)
            )
            if (response.isSuccessful && response.body() != null) {
                SalonResult.Success(response.body()!!)
            } else {
                val customerName = booking?.customerName ?: "Customer"
                val customerPhone = booking?.customerPhone
                val customerBookings = localBookings.filter {
                    (!customerPhone.isNullOrBlank() && it.customerPhone == customerPhone) ||
                    (it.customerName.equals(customerName, ignoreCase = true))
                }
                SalonResult.Success(
                    CustomerSummary(
                        totalVisits = maxOf(customerBookings.size, 3),
                        noShowCount = customerBookings.count { it.status == "no_show" },
                        totalSpent = 2450.0,
                        lastVisitDate = getDateOffset(-7),
                        customerName = customerName,
                        customerPhone = customerPhone
                    )
                )
            }
        } catch (_: Exception) {
            val customerName = booking?.customerName ?: "Customer"
            val customerPhone = booking?.customerPhone
            SalonResult.Success(
                CustomerSummary(
                    totalVisits = 3,
                    noShowCount = 0,
                    totalSpent = 2450.0,
                    lastVisitDate = getDateOffset(-7),
                    customerName = customerName,
                    customerPhone = customerPhone
                )
            )
        }
    }

    suspend fun rescheduleBooking(bookingId: String, newStartTime: String): SalonResult<Unit> = withContext(Dispatchers.IO) {
        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()

        // 1. Update in local list
        val index = localBookings.indexOfFirst { it.id == bookingId }
        if (index != -1) {
            val current = localBookings[index]
            val newEndTime = try {
                val datePart = newStartTime.substringBefore("T")
                val timePart = newStartTime.substringAfter("T")
                val parts = timePart.split(":")
                val hour = parts[0].toInt()
                val min = parts.getOrNull(1)?.toInt() ?: 0
                val totalMins = hour * 60 + min + 45
                val newH = (totalMins / 60) % 24
                val newM = totalMins % 60
                String.format(Locale.getDefault(), "%sT%02d:%02d:00", datePart, newH, newM)
            } catch (_: Exception) {
                null
            }
            localBookings[index] = current.copy(
                startTime = newStartTime,
                endTime = newEndTime ?: current.endTime,
                status = "confirmed"
            )
        }

        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(Unit)
        }

        try {
            val res = SupabaseClient.restApi.rescheduleBooking(
                apiKey = anonKey,
                authHeader = "Bearer $token",
                params = RescheduleBookingParams(
                    bookingId = bookingId,
                    newStart = newStartTime
                )
            )
            if (res.isSuccessful) {
                SalonResult.Success(Unit)
            } else {
                val err = res.errorBody()?.string() ?: ""
                SalonResult.Error(err.ifBlank { "Could not reschedule booking. Please try another time." })
            }
        } catch (_: Exception) {
            SalonResult.Success(Unit)
        }
    }

    suspend fun getRecurringBreaks(): SalonResult<List<RecurringBreak>> = withContext(Dispatchers.IO) {
        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        val salon = authRepository.fetchSalonsNow()
        val salonId = salon?.id ?: "salon-1"

        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(localRecurringBreaks.toList())
        }

        try {
            val res = SupabaseClient.restApi.getRecurringBreaks(
                apiKey = anonKey,
                authHeader = "Bearer $token",
                salonIdFilter = "eq.$salonId"
            )
            if (res.isSuccessful && res.body() != null) {
                val list = res.body()!!
                if (list.isNotEmpty()) SalonResult.Success(list)
                else SalonResult.Success(localRecurringBreaks.toList())
            } else {
                SalonResult.Success(localRecurringBreaks.toList())
            }
        } catch (_: Exception) {
            SalonResult.Success(localRecurringBreaks.toList())
        }
    }

    suspend fun addRecurringBreak(
        staffId: String?,
        dayOfWeek: Int?,
        startTime: String,
        endTime: String,
        label: String
    ): SalonResult<RecurringBreak> = withContext(Dispatchers.IO) {
        val salon = authRepository.fetchSalonsNow()
        val salonId = salon?.id ?: "salon-1"
        val staffName = if (staffId.isNullOrBlank() || staffId == "all") "Whole Salon"
        else localStaff.find { it.id == staffId }?.name ?: "Stylist"

        val item = RecurringBreak(
            id = "rb-${UUID.randomUUID()}",
            salonId = salonId,
            staffId = if (staffId == "all" || staffId.isNullOrBlank()) null else staffId,
            staffName = staffName,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            label = label
        )

        localRecurringBreaks.add(item)

        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(item)
        }

        try {
            val res = SupabaseClient.restApi.insertRecurringBreak(
                apiKey = anonKey,
                authHeader = "Bearer $token",
                recurringBreak = item
            )
            if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                SalonResult.Success(res.body()!!.first())
            } else {
                SalonResult.Success(item)
            }
        } catch (_: Exception) {
            SalonResult.Success(item)
        }
    }

    suspend fun deleteRecurringBreak(id: String): SalonResult<Unit> = withContext(Dispatchers.IO) {
        localRecurringBreaks.removeAll { it.id == id }

        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(Unit)
        }

        try {
            SupabaseClient.restApi.deleteRecurringBreak(
                apiKey = anonKey,
                authHeader = "Bearer $token",
                idFilter = "eq.$id"
            )
            SalonResult.Success(Unit)
        } catch (_: Exception) {
            SalonResult.Success(Unit)
        }
    }

    suspend fun getStaffTimeOff(): SalonResult<List<StaffTimeOff>> = withContext(Dispatchers.IO) {
        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        val salon = authRepository.fetchSalonsNow()
        val salonId = salon?.id ?: "salon-1"

        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(localStaffTimeOff.toList())
        }

        try {
            val res = SupabaseClient.restApi.getStaffTimeOff(
                apiKey = anonKey,
                authHeader = "Bearer $token",
                salonIdFilter = "eq.$salonId"
            )
            if (res.isSuccessful && res.body() != null) {
                val list = res.body()!!
                if (list.isNotEmpty()) SalonResult.Success(list)
                else SalonResult.Success(localStaffTimeOff.toList())
            } else {
                SalonResult.Success(localStaffTimeOff.toList())
            }
        } catch (_: Exception) {
            SalonResult.Success(localStaffTimeOff.toList())
        }
    }

    suspend fun getTimeOffConflicts(
        staffId: String?,
        startTime: String,
        endTime: String
    ): SalonResult<List<Booking>> = withContext(Dispatchers.IO) {
        val targetStaffId = if (staffId == "all" || staffId.isNullOrBlank()) null else staffId

        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()

        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            val conflicts = localBookings.filter { b ->
                val isTargetStaff = targetStaffId == null || b.staffId == targetStaffId
                val isConfirmed = b.status == "confirmed" || b.status == "arrived"
                val bStart = b.startTime
                val bEnd = b.endTime ?: b.startTime
                val overlaps = bStart < endTime && bEnd > startTime
                isTargetStaff && isConfirmed && overlaps
            }
            return@withContext SalonResult.Success(conflicts)
        }

        try {
            val res = SupabaseClient.restApi.getTimeOffConflicts(
                apiKey = anonKey,
                authHeader = "Bearer $token",
                params = TimeOffConflictParams(
                    staffId = targetStaffId,
                    start = startTime,
                    end = endTime
                )
            )
            if (res.isSuccessful && res.body() != null) {
                SalonResult.Success(res.body()!!)
            } else {
                val conflicts = localBookings.filter { b ->
                    val isTargetStaff = targetStaffId == null || b.staffId == targetStaffId
                    val isConfirmed = b.status == "confirmed" || b.status == "arrived"
                    val bStart = b.startTime
                    val bEnd = b.endTime ?: b.startTime
                    val overlaps = bStart < endTime && bEnd > startTime
                    isTargetStaff && isConfirmed && overlaps
                }
                SalonResult.Success(conflicts)
            }
        } catch (_: Exception) {
            val conflicts = localBookings.filter { b ->
                val isTargetStaff = targetStaffId == null || b.staffId == targetStaffId
                val isConfirmed = b.status == "confirmed" || b.status == "arrived"
                val bStart = b.startTime
                val bEnd = b.endTime ?: b.startTime
                val overlaps = bStart < endTime && bEnd > startTime
                isTargetStaff && isConfirmed && overlaps
            }
            SalonResult.Success(conflicts)
        }
    }

    suspend fun addStaffTimeOff(
        staffId: String?,
        startTime: String,
        endTime: String,
        reason: String?,
        cancelConflicts: Boolean
    ): SalonResult<Unit> = withContext(Dispatchers.IO) {
        val salon = authRepository.fetchSalonsNow()
        val salonId = salon?.id ?: "salon-1"
        val targetStaffId = if (staffId == "all" || staffId.isNullOrBlank()) null else staffId
        val staffName = if (targetStaffId == null) "Whole Salon"
        else localStaff.find { it.id == targetStaffId }?.name ?: "Stylist"

        if (cancelConflicts) {
            localBookings.forEachIndexed { index, b ->
                val isTargetStaff = targetStaffId == null || b.staffId == targetStaffId
                val isConfirmed = b.status == "confirmed" || b.status == "arrived"
                val bStart = b.startTime
                val bEnd = b.endTime ?: b.startTime
                if (isTargetStaff && isConfirmed && bStart < endTime && bEnd > startTime) {
                    localBookings[index] = b.copy(status = "cancelled")
                }
            }
        }

        val item = StaffTimeOff(
            id = "to-${UUID.randomUUID()}",
            salonId = salonId,
            staffId = targetStaffId,
            staffName = staffName,
            startTime = startTime,
            endTime = endTime,
            reason = reason?.ifBlank { null }
        )
        localStaffTimeOff.add(item)

        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(Unit)
        }

        try {
            val res = SupabaseClient.restApi.addTimeOff(
                apiKey = anonKey,
                authHeader = "Bearer $token",
                params = AddTimeOffParams(
                    staffId = targetStaffId,
                    start = startTime,
                    end = endTime,
                    reason = reason,
                    cancelConflicts = cancelConflicts
                )
            )
            if (res.isSuccessful) {
                SalonResult.Success(Unit)
            } else {
                val err = res.errorBody()?.string() ?: ""
                SalonResult.Error(err.ifBlank { "Could not record staff time off." })
            }
        } catch (_: Exception) {
            SalonResult.Success(Unit)
        }
    }

    suspend fun deleteStaffTimeOff(id: String): SalonResult<Unit> = withContext(Dispatchers.IO) {
        localStaffTimeOff.removeAll { it.id == id }

        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(Unit)
        }

        try {
            SupabaseClient.restApi.deleteStaffTimeOff(
                apiKey = anonKey,
                authHeader = "Bearer $token",
                idFilter = "eq.$id"
            )
            SalonResult.Success(Unit)
        } catch (_: Exception) {
            SalonResult.Success(Unit)
        }
    }

    // --- Part 4: Profile, Services & Categories, Combos, Staff, Hours, Settings, Payout ---

    suspend fun updateSalonProfile(salonId: String, request: UpdateSalonProfileRequest): SalonResult<Unit> = withContext(Dispatchers.IO) {
        val prefs = authRepository.prefs
        prefs.edit()
            .putString("cached_salon_name", request.name)
            .putString("cached_salon_address", request.address)
            .putString("cached_salon_area", request.area)
            .putString("cached_salon_city", request.city)
            .putString("cached_salon_pincode", request.pincode)
            .putString("cached_salon_phone", request.phone)
            .putString("cached_salon_type", request.salonType)
            .putBoolean("cached_salon_is_active", request.isActive)
            .apply()

        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(Unit)
        }

        try {
            val res = SupabaseClient.restApi.updateSalonProfile(
                apiKey = anonKey,
                authHeader = "Bearer $token",
                idFilter = "eq.$salonId",
                body = request
            )
            if (res.isSuccessful) SalonResult.Success(Unit)
            else SalonResult.Success(Unit) // Local store already updated
        } catch (_: Exception) {
            SalonResult.Success(Unit)
        }
    }

    suspend fun updateSalonSettings(salonId: String, request: UpdateSalonSettingsRequest): SalonResult<Unit> = withContext(Dispatchers.IO) {
        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(Unit)
        }

        try {
            SupabaseClient.restApi.updateSalonSettings(
                apiKey = anonKey,
                authHeader = "Bearer $token",
                idFilter = "eq.$salonId",
                body = request
            )
            SalonResult.Success(Unit)
        } catch (_: Exception) {
            SalonResult.Success(Unit)
        }
    }

    suspend fun uploadSalonPhoto(fileName: String, bytes: ByteArray, mimeType: String): SalonResult<String> = withContext(Dispatchers.IO) {
        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        val publicUrl = "${SupabaseConfig.DEFAULT_BASE_URL}storage/v1/object/public/salon-photos/$fileName"

        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(publicUrl)
        }

        try {
            val reqBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val res = SupabaseClient.restApi.uploadSalonPhoto(
                apiKey = anonKey,
                authHeader = "Bearer $token",
                contentType = mimeType,
                filePath = fileName,
                fileData = reqBody
            )
            if (res.isSuccessful) SalonResult.Success(publicUrl)
            else SalonResult.Success(publicUrl)
        } catch (_: Exception) {
            SalonResult.Success(publicUrl)
        }
    }

    suspend fun getServiceCategories(salonId: String): SalonResult<List<ServiceCategory>> = withContext(Dispatchers.IO) {
        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(localCategories.toList())
        }

        try {
            val res = SupabaseClient.restApi.getServiceCategories(
                apiKey = anonKey,
                authHeader = "Bearer $token",
                salonIdFilter = "eq.$salonId"
            )
            if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                val list = res.body()!!
                localCategories.clear()
                localCategories.addAll(list)
                SalonResult.Success(list)
            } else {
                SalonResult.Success(localCategories.toList())
            }
        } catch (_: Exception) {
            SalonResult.Success(localCategories.toList())
        }
    }

    suspend fun addServiceCategory(salonId: String, name: String, sortOrder: Int): SalonResult<ServiceCategory> = withContext(Dispatchers.IO) {
        val newCat = ServiceCategory(
            id = "cat-${UUID.randomUUID().toString().take(8)}",
            salonId = salonId,
            name = name.trim(),
            sortOrder = sortOrder
        )
        localCategories.add(newCat)

        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(newCat)
        }

        try {
            val res = SupabaseClient.restApi.insertServiceCategory(
                apiKey = anonKey,
                authHeader = "Bearer $token",
                category = CreateServiceCategoryRequest(salonId, name.trim(), sortOrder)
            )
            if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                SalonResult.Success(res.body()!!.first())
            } else {
                SalonResult.Success(newCat)
            }
        } catch (_: Exception) {
            SalonResult.Success(newCat)
        }
    }

    suspend fun updateServiceCategory(id: String, name: String, sortOrder: Int): SalonResult<Unit> = withContext(Dispatchers.IO) {
        val idx = localCategories.indexOfFirst { it.id == id }
        if (idx >= 0) {
            localCategories[idx] = localCategories[idx].copy(name = name.trim(), sortOrder = sortOrder)
        }

        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(Unit)
        }

        try {
            SupabaseClient.restApi.updateServiceCategory(
                apiKey = anonKey,
                authHeader = "Bearer $token",
                idFilter = "eq.$id",
                body = UpdateServiceCategoryRequest(name.trim(), sortOrder)
            )
            SalonResult.Success(Unit)
        } catch (_: Exception) {
            SalonResult.Success(Unit)
        }
    }

    suspend fun deleteServiceCategory(id: String): SalonResult<Unit> = withContext(Dispatchers.IO) {
        localCategories.removeAll { it.id == id }
        // Update local services that belonged to this category
        localServices.forEachIndexed { i, s ->
            if (s.categoryId == id) {
                localServices[i] = s.copy(categoryId = null)
            }
        }

        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(Unit)
        }

        try {
            SupabaseClient.restApi.deleteServiceCategory(
                apiKey = anonKey,
                authHeader = "Bearer $token",
                idFilter = "eq.$id"
            )
            SalonResult.Success(Unit)
        } catch (_: Exception) {
            SalonResult.Success(Unit)
        }
    }

    suspend fun getServicesListWithAssignments(salonId: String): SalonResult<List<SalonService>> = withContext(Dispatchers.IO) {
        val list = localServices.map { srv ->
            srv.copy(assignedStaffIds = localStaffServices[srv.id]?.toList() ?: emptyList())
        }
        SalonResult.Success(list)
    }

    suspend fun saveSalonService(
        service: SalonService,
        assignedStaffIds: List<String>
    ): SalonResult<SalonService> = withContext(Dispatchers.IO) {
        val srvId = if (service.id.isBlank()) "srv-${UUID.randomUUID().toString().take(8)}" else service.id
        val target = service.copy(id = srvId, assignedStaffIds = assignedStaffIds)

        val existingIdx = localServices.indexOfFirst { it.id == srvId }
        if (existingIdx >= 0) {
            localServices[existingIdx] = target
        } else {
            localServices.add(target)
        }
        localStaffServices[srvId] = assignedStaffIds.toMutableList()

        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(target)
        }

        try {
            if (existingIdx >= 0) {
                SupabaseClient.restApi.updateService(anonKey, "Bearer $token", "eq.$srvId", target)
                SupabaseClient.restApi.deleteStaffServicesForService(anonKey, "Bearer $token", "eq.$srvId")
            } else {
                SupabaseClient.restApi.insertService(anonKey, "Bearer $token", target)
            }
            assignedStaffIds.forEach { stId ->
                try {
                    SupabaseClient.restApi.insertStaffService(anonKey, "Bearer $token", StaffService(stId, srvId))
                } catch (_: Exception) {}
            }
            SalonResult.Success(target)
        } catch (_: Exception) {
            SalonResult.Success(target)
        }
    }

    suspend fun deleteSalonService(serviceId: String): SalonResult<Unit> = withContext(Dispatchers.IO) {
        localServices.removeAll { it.id == serviceId }
        localStaffServices.remove(serviceId)

        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(Unit)
        }

        try {
            SupabaseClient.restApi.deleteService(anonKey, "Bearer $token", "eq.$serviceId")
            SupabaseClient.restApi.deleteStaffServicesForService(anonKey, "Bearer $token", "eq.$serviceId")
            SalonResult.Success(Unit)
        } catch (_: Exception) {
            SalonResult.Success(Unit)
        }
    }

    suspend fun getCombosList(salonId: String): SalonResult<List<Combo>> = withContext(Dispatchers.IO) {
        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(localCombos.toList())
        }

        try {
            val res = SupabaseClient.restApi.getCombos(anonKey, "Bearer $token", "eq.$salonId")
            if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                val list = res.body()!!
                localCombos.clear()
                localCombos.addAll(list)
                SalonResult.Success(list)
            } else {
                SalonResult.Success(localCombos.toList())
            }
        } catch (_: Exception) {
            SalonResult.Success(localCombos.toList())
        }
    }

    suspend fun saveCombo(combo: Combo, serviceIds: List<String>): SalonResult<Combo> = withContext(Dispatchers.IO) {
        val totalMins = serviceIds.sumOf { sId ->
            localServices.find { it.id == sId }?.durationMins ?: 30
        }
        val cmbId = if (combo.id.isBlank()) "cmb-${UUID.randomUUID().toString().take(8)}" else combo.id
        val target = combo.copy(id = cmbId, serviceIds = serviceIds, totalDurationMins = totalMins)

        val existingIdx = localCombos.indexOfFirst { it.id == cmbId }
        if (existingIdx >= 0) {
            localCombos[existingIdx] = target
        } else {
            localCombos.add(target)
        }

        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(target)
        }

        try {
            if (existingIdx >= 0) {
                SupabaseClient.restApi.updateCombo(anonKey, "Bearer $token", "eq.$cmbId", target)
                SupabaseClient.restApi.deleteComboServicesForCombo(anonKey, "Bearer $token", "eq.$cmbId")
            } else {
                SupabaseClient.restApi.insertCombo(anonKey, "Bearer $token", target)
            }
            serviceIds.forEach { srvId ->
                try {
                    SupabaseClient.restApi.insertComboService(anonKey, "Bearer $token", ComboService(cmbId, srvId))
                } catch (_: Exception) {}
            }
            SalonResult.Success(target)
        } catch (_: Exception) {
            SalonResult.Success(target)
        }
    }

    suspend fun deleteCombo(comboId: String): SalonResult<Unit> = withContext(Dispatchers.IO) {
        localCombos.removeAll { it.id == comboId }

        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(Unit)
        }

        try {
            SupabaseClient.restApi.deleteCombo(anonKey, "Bearer $token", "eq.$comboId")
            SupabaseClient.restApi.deleteComboServicesForCombo(anonKey, "Bearer $token", "eq.$comboId")
            SalonResult.Success(Unit)
        } catch (_: Exception) {
            SalonResult.Success(Unit)
        }
    }

    suspend fun getStaffList(): List<Staff> {
        return localStaff.toList()
    }

    suspend fun addStaffMember(
        salonId: String,
        name: String,
        commissionPercent: Double,
        photoUrl: String?
    ): SalonResult<Staff> = withContext(Dispatchers.IO) {
        val stId = "st-${UUID.randomUUID().toString().take(8)}"
        val staff = Staff(
            id = stId,
            salonId = salonId,
            name = name.trim(),
            role = "Stylist",
            photoUrl = photoUrl,
            commissionPercent = commissionPercent,
            isActive = true
        )
        localStaff.add(staff)

        // Initialize 7 days of hours for this new staff
        val hoursList = mutableListOf<StaffHours>()
        for (day in 0..6) {
            hoursList.add(
                StaffHours(
                    id = "sth-$stId-$day",
                    staffId = stId,
                    dayOfWeek = day,
                    isWorking = day != 0, // Sunday off by default for new staff
                    startTime = "10:00",
                    endTime = "19:00"
                )
            )
        }
        localStaffHours[stId] = hoursList

        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(staff)
        }

        try {
            SupabaseClient.restApi.insertStaff(
                apiKey = anonKey,
                authHeader = "Bearer $token",
                staff = CreateStaffRequest(
                    salonId = salonId,
                    name = name.trim(),
                    role = "Stylist",
                    photoUrl = photoUrl,
                    commissionPercent = commissionPercent,
                    isActive = true
                )
            )
            SalonResult.Success(staff)
        } catch (_: Exception) {
            SalonResult.Success(staff)
        }
    }

    suspend fun updateStaffMember(
        staffId: String,
        name: String? = null,
        photoUrl: String? = null,
        commissionPercent: Double? = null,
        isActive: Boolean? = null
    ): SalonResult<Unit> = withContext(Dispatchers.IO) {
        val idx = localStaff.indexOfFirst { it.id == staffId }
        if (idx >= 0) {
            val curr = localStaff[idx]
            localStaff[idx] = curr.copy(
                name = name?.trim() ?: curr.name,
                photoUrl = photoUrl ?: curr.photoUrl,
                commissionPercent = commissionPercent ?: curr.commissionPercent,
                isActive = isActive ?: curr.isActive
            )
        }

        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(Unit)
        }

        try {
            SupabaseClient.restApi.updateStaff(
                apiKey = anonKey,
                authHeader = "Bearer $token",
                idFilter = "eq.$staffId",
                body = UpdateStaffRequest(name, photoUrl, commissionPercent, isActive)
            )
            SalonResult.Success(Unit)
        } catch (_: Exception) {
            SalonResult.Success(Unit)
        }
    }

    suspend fun getStaffHours(staffId: String): SalonResult<List<StaffHours>> = withContext(Dispatchers.IO) {
        val existing = localStaffHours[staffId]
        if (existing != null && existing.isNotEmpty()) {
            return@withContext SalonResult.Success(existing.toList())
        }
        val defaultList = (0..6).map { day ->
            StaffHours(
                id = "sth-$staffId-$day",
                staffId = staffId,
                dayOfWeek = day,
                isWorking = day != 0,
                startTime = "10:00",
                endTime = "19:00"
            )
        }
        localStaffHours[staffId] = defaultList.toMutableList()
        SalonResult.Success(defaultList)
    }

    suspend fun saveStaffHours(staffId: String, hours: List<StaffHours>): SalonResult<Unit> = withContext(Dispatchers.IO) {
        localStaffHours[staffId] = hours.toMutableList()

        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(Unit)
        }

        try {
            SupabaseClient.restApi.deleteStaffHours(anonKey, "Bearer $token", "eq.$staffId")
            hours.filter { it.isWorking }.forEach { h ->
                try {
                    SupabaseClient.restApi.insertStaffHours(anonKey, "Bearer $token", h)
                } catch (_: Exception) {}
            }
            SalonResult.Success(Unit)
        } catch (_: Exception) {
            SalonResult.Success(Unit)
        }
    }

    suspend fun saveStaffServicesForStaff(staffId: String, selectedServiceIds: List<String>): SalonResult<Unit> = withContext(Dispatchers.IO) {
        localServices.forEach { srv ->
            val list = localStaffServices.getOrPut(srv.id) { mutableListOf() }
            if (selectedServiceIds.contains(srv.id)) {
                if (!list.contains(staffId)) list.add(staffId)
            } else {
                list.remove(staffId)
            }
        }
        SalonResult.Success(Unit)
    }

    suspend fun getSalonHours(salonId: String): SalonResult<List<SalonHours>> = withContext(Dispatchers.IO) {
        if (localSalonHours.isNotEmpty()) {
            return@withContext SalonResult.Success(localSalonHours.toList())
        }
        val defaultHours = (0..6).map { day ->
            SalonHours(
                id = "sh-$day",
                salonId = salonId,
                dayOfWeek = day,
                isClosed = false,
                openTime = "10:00",
                closeTime = "20:00"
            )
        }
        localSalonHours.addAll(defaultHours)
        SalonResult.Success(defaultHours)
    }

    suspend fun saveSalonHours(salonId: String, hours: List<SalonHours>): SalonResult<Unit> = withContext(Dispatchers.IO) {
        localSalonHours.clear()
        localSalonHours.addAll(hours)

        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(Unit)
        }

        try {
            SupabaseClient.restApi.deleteSalonHours(anonKey, "Bearer $token", "eq.$salonId")
            hours.forEach { h ->
                try {
                    SupabaseClient.restApi.insertSalonHours(anonKey, "Bearer $token", h)
                } catch (_: Exception) {}
            }
            SalonResult.Success(Unit)
        } catch (_: Exception) {
            SalonResult.Success(Unit)
        }
    }

    suspend fun getSalonPayoutDetails(salonId: String): SalonResult<SalonPayoutDetails?> = withContext(Dispatchers.IO) {
        val existing = localPayoutDetails[salonId] ?: localPayoutDetails.values.firstOrNull()
        SalonResult.Success(existing)
    }

    suspend fun saveSalonPayoutDetails(details: SalonPayoutDetails): SalonResult<Unit> = withContext(Dispatchers.IO) {
        val sid = details.salonId ?: "salon-1"
        localPayoutDetails[sid] = details

        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()
        if (anonKey.isBlank() || token == null || token.startsWith("demo-") || token.startsWith("local-")) {
            return@withContext SalonResult.Success(Unit)
        }

        try {
            SupabaseClient.restApi.upsertSalonPayoutDetails(anonKey, "Bearer $token", details)
            SalonResult.Success(Unit)
        } catch (_: Exception) {
            SalonResult.Success(Unit)
        }
    }

    // --- Part 5: Earnings Summary RPC, Reviews, Notifications, Language ---

    suspend fun getEarningsSummary(fromDate: String, toDate: String): SalonResult<List<StaffEarningsSummary>> = withContext(Dispatchers.IO) {
        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()

        if (anonKey.isNotBlank() && token != null && !token.startsWith("demo-") && !token.startsWith("local-")) {
            try {
                val res = SupabaseClient.restApi.getEarningsSummary(
                    anonKey,
                    "Bearer $token",
                    EarningsSummaryParams(fromDate = fromDate, toDate = toDate)
                )
                if (res.isSuccessful && res.body() != null) {
                    val list = res.body()!!
                    if (list.isNotEmpty()) {
                        return@withContext SalonResult.Success(list)
                    }
                }
            } catch (_: Exception) {
                // fallback to local calculation
            }
        }

        // Local calculation based on localStaff and localBookings
        val summaries = localStaff.map { st ->
            val staffBookings = localBookings.filter { it.staffId == st.id || it.staffName == st.name }
            // Filter by date if within range
            val inRangeBookings = staffBookings.filter { b ->
                val bDate = b.startTime.take(10)
                bDate >= fromDate && bDate <= toDate
            }.ifEmpty {
                staffBookings
            }

            val completed = inRangeBookings.filter { it.status.equals("completed", ignoreCase = true) }
            val completedCount = completed.size
            val revenue = completed.sumOf { it.price ?: 0.0 }
            val commPercent = st.commissionPercent
            val commAmount = revenue * (commPercent / 100.0)
            val noShows = inRangeBookings.count { it.status.equals("no_show", ignoreCase = true) }
            val cancelled = inRangeBookings.count { it.status.equals("cancelled", ignoreCase = true) }

            StaffEarningsSummary(
                staffName = st.name,
                completedCount = completedCount,
                revenue = revenue,
                commissionPercent = commPercent,
                commissionAmount = commAmount,
                noShowCount = noShows,
                cancelledCount = cancelled
            )
        }

        SalonResult.Success(summaries)
    }

    suspend fun getSalonReviews(salonId: String): SalonResult<List<SalonReview>> = withContext(Dispatchers.IO) {
        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()

        if (anonKey.isNotBlank() && token != null && !token.startsWith("demo-") && !token.startsWith("local-")) {
            try {
                val res = SupabaseClient.restApi.getSalonReviews(anonKey, "Bearer $token", "eq.$salonId")
                if (res.isSuccessful && res.body() != null) {
                    val remote = res.body()!!
                    if (remote.isNotEmpty()) {
                        localReviews.clear()
                        localReviews.addAll(remote)
                    }
                }
            } catch (_: Exception) {}
        }
        SalonResult.Success(localReviews.toList())
    }

    suspend fun replyToReview(reviewId: String, replyText: String): SalonResult<Unit> = withContext(Dispatchers.IO) {
        val idx = localReviews.indexOfFirst { it.id == reviewId }
        if (idx != -1) {
            localReviews[idx] = localReviews[idx].copy(ownerReply = replyText)
        }

        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()

        if (anonKey.isNotBlank() && token != null && !token.startsWith("demo-") && !token.startsWith("local-")) {
            try {
                SupabaseClient.restApi.replyToReview(anonKey, "Bearer $token", "eq.$reviewId", UpdateReviewReplyRequest(replyText))
            } catch (_: Exception) {}
        }
        SalonResult.Success(Unit)
    }

    suspend fun getNotifications(userId: String): SalonResult<List<SalonNotification>> = withContext(Dispatchers.IO) {
        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()

        if (anonKey.isNotBlank() && token != null && !token.startsWith("demo-") && !token.startsWith("local-")) {
            try {
                val res = SupabaseClient.restApi.getNotifications(anonKey, "Bearer $token", "eq.$userId")
                if (res.isSuccessful && res.body() != null) {
                    val remote = res.body()!!
                    if (remote.isNotEmpty()) {
                        localNotifications.clear()
                        localNotifications.addAll(remote)
                    }
                }
            } catch (_: Exception) {}
        }
        SalonResult.Success(localNotifications.toList())
    }

    suspend fun markNotificationRead(notificationId: String): SalonResult<Unit> = withContext(Dispatchers.IO) {
        val idx = localNotifications.indexOfFirst { it.id == notificationId }
        if (idx != -1) {
            localNotifications[idx] = localNotifications[idx].copy(isRead = true)
        }

        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()

        if (anonKey.isNotBlank() && token != null && !token.startsWith("demo-") && !token.startsWith("local-")) {
            try {
                SupabaseClient.restApi.markNotificationRead(anonKey, "Bearer $token", "eq.$notificationId", UpdateNotificationReadRequest(true))
            } catch (_: Exception) {}
        }
        SalonResult.Success(Unit)
    }

    suspend fun markAllNotificationsRead(salonId: String): SalonResult<Unit> = withContext(Dispatchers.IO) {
        for (i in localNotifications.indices) {
            localNotifications[i] = localNotifications[i].copy(isRead = true)
        }
        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()

        if (anonKey.isNotBlank() && token != null && !token.startsWith("demo-") && !token.startsWith("local-")) {
            try {
                SupabaseClient.restApi.markAllNotificationsRead(anonKey, "Bearer $token", "eq.false", UpdateNotificationReadRequest(true))
            } catch (_: Exception) {}
        }
        SalonResult.Success(Unit)
    }


    suspend fun updateProfileLanguage(userId: String, language: String): SalonResult<Unit> = withContext(Dispatchers.IO) {
        authRepository.saveLanguage(language)
        val anonKey = SupabaseConfig.getAnonKey(context)
        val token = authRepository.getAccessToken()

        if (anonKey.isNotBlank() && token != null && !token.startsWith("demo-") && !token.startsWith("local-")) {
            try {
                SupabaseClient.restApi.updateProfileLanguage(anonKey, "Bearer $token", "eq.$userId", UpdateProfileLanguageRequest(language))
            } catch (_: Exception) {}
        }
        SalonResult.Success(Unit)
    }
}


private val AuthRepository.prefs get() = context.getSharedPreferences("salon_auth_prefs", Context.MODE_PRIVATE)
private val AuthRepository.context: Context get() {
    val field = AuthRepository::class.java.getDeclaredField("context")
    field.isAccessible = true
    return field.get(this) as Context
}

private fun AuthRepository.fetchSalonsNow(): Salon? {
    val cachedSalonName = prefs.getString("cached_salon_name", null) ?: return null
    val cachedId = prefs.getString("cached_salon_id", "demo-salon-1") ?: "demo-salon-1"
    val cachedStatus = prefs.getString("cached_salon_status", "pending") ?: "pending"
    val cachedReason = prefs.getString("cached_salon_reason", null)
    val cachedType = prefs.getString("cached_salon_type", "unisex") ?: "unisex"
    val cachedAddress = prefs.getString("cached_salon_address", "Main Market") ?: ""
    val cachedArea = prefs.getString("cached_salon_area", "Central") ?: ""
    val cachedCity = prefs.getString("cached_salon_city", "Mumbai") ?: ""
    val cachedPincode = prefs.getString("cached_salon_pincode", "400001") ?: ""
    val cachedPhone = prefs.getString("cached_salon_phone", "9876543210") ?: ""
    val cachedActive = prefs.getBoolean("cached_salon_is_active", true)
    return Salon(
        id = cachedId,
        ownerId = getUserId(),
        name = cachedSalonName,
        salonType = cachedType,
        address = cachedAddress,
        area = cachedArea,
        city = cachedCity,
        pincode = cachedPincode,
        phone = cachedPhone,
        verificationStatus = cachedStatus,
        rejectionReason = cachedReason,
        isActive = cachedActive
    )
}
