package com.example.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Booking
import com.example.data.model.Combo
import com.example.data.model.CustomerSummary
import com.example.data.model.OwnerDashboard
import com.example.data.model.Profile
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
import com.example.data.model.SupabaseUser
import com.example.data.model.SalonSetupStatus
import com.example.data.model.UpdateSalonProfileRequest
import com.example.data.model.UpdateSalonSettingsRequest
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthResult
import com.example.data.repository.SalonRepository
import com.example.data.repository.SalonResult
import com.example.util.SalonStrings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: SupabaseUser? = null,
    val profile: Profile? = null,
    val salon: Salon? = null,
    val language: String = "en",
    val destinationRoute: String = "login",
    val errorMessage: String? = null,
    val infoMessage: String? = null,

    // Registration 3-Step State
    val currentStep: Int = 1,
    // Step 1
    val ownerName: String = "",
    // Mobile OTP login
    val otpSent: Boolean = false,
    val otpMobile: String = "",
    val regLanguage: String = "en",
    // Step 2
    val salonName: String = "",
    val salonType: String = "unisex", // men, women, unisex
    val address: String = "",
    val area: String = "",
    val city: String = "",
    val pincode: String = "",
    val phone: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val gstNumber: String = "",
    // Step 3
    val docType: String = "gst",
    val documentName: String? = null,
    val documentBytes: ByteArray? = null,
    val documentMimeType: String = "image/jpeg",
    val acceptTerms: Boolean = false,

    // In-app Supabase Anon Key Config state
    val showConfigDialog: Boolean = false,

    // Part 2: Main App Shell & Tabs State
    val selectedTab: String = "today", // "today", "bookings", "salon", "earnings"
    val dashboard: OwnerDashboard? = null,
    val isLoadingDashboard: Boolean = false,
    val todayBookings: List<Booking> = emptyList(),
    val isLoadingBookings: Boolean = false,
    val selectedStaffFilter: String = "all",
    val staffList: List<Staff> = emptyList(),
    val servicesList: List<SalonService> = emptyList(),
    val isWalkInModalOpen: Boolean = false,
    val walkInStaffId: String = "",
    val walkInServiceId: String = "",
    val walkInCustomerName: String = "",
    val walkInCustomerPhone: String = "",
    val walkInError: String? = null,
    val isSubmittingWalkIn: Boolean = false,
    val lastSyncTime: String = "",

    // Part 3: Bookings List Subtabs & Details
    val bookingsSubTab: String = "upcoming", // "upcoming" or "past"
    val upcomingBookings: List<Booking> = emptyList(),
    val pastBookings: List<Booking> = emptyList(),
    val isLoadingUpcoming: Boolean = false,
    val isLoadingPast: Boolean = false,
    val showBookingDetailSheet: Boolean = false,
    val selectedBookingForDetail: Booking? = null,
    val customerSummary: com.example.data.model.CustomerSummary? = null,
    val isLoadingCustomerSummary: Boolean = false,
    val showCustomerSummaryDialog: Boolean = false,
    val showRescheduleDialog: Boolean = false,
    val rescheduleSelectedDate: String = "",
    val rescheduleSelectedTime: String = "",
    val isSubmittingReschedule: Boolean = false,
    val showCancelBookingDialog: Boolean = false,

    // Part 3: Staff Time Off & Breaks
    val recurringBreaks: List<RecurringBreak> = emptyList(),
    val staffTimeOffList: List<StaffTimeOff> = emptyList(),
    val isLoadingTimeOff: Boolean = false,
    val showAddRecurringBreakDialog: Boolean = false,
    val showAddTimeOffDialog: Boolean = false,
    val conflictingBookings: List<Booking> = emptyList(),
    val showConflictsDialog: Boolean = false,
    val pendingTimeOffStaffId: String? = null,
    val pendingTimeOffStart: String = "",
    val pendingTimeOffEnd: String = "",
    val pendingTimeOffReason: String? = null,

    // Part 4: Salon Management
    val salonActiveSection: String = "menu", // "menu", "profile", "services", "combos", "staff", "working_hours", "time_off", "booking_settings", "payout_details"

    // Salon Profile Edit
    val editSalonName: String = "",
    val editSalonDescription: String = "",
    val editSalonAddress: String = "",
    val editSalonArea: String = "",
    val editSalonCity: String = "",
    val editSalonPincode: String = "",
    val editSalonPhone: String = "",
    val editSalonGst: String = "",
    val editSalonType: String = "unisex",
    val editSalonPhotos: List<String> = emptyList(),
    val editCoverPhotoIndex: Int = 0,
    val editSalonIsActive: Boolean = true,
    val isSavingSalonProfile: Boolean = false,

    // Services & Categories
    val categoriesList: List<ServiceCategory> = emptyList(),
    val showAddCategoryDialog: Boolean = false,
    val categoryBeingEdited: ServiceCategory? = null,
    val categoryFormName: String = "",
    val categoryFormSortOrder: String = "1",
    val showAddEditServiceDialog: Boolean = false,
    val serviceBeingEdited: SalonService? = null,
    val serviceFormName: String = "",
    val serviceFormCategoryId: String? = null,
    val serviceFormPrice: String = "",
    val serviceFormDurationMins: Int = 30, // 30, 60, 90, 120, 150, 180, 210, 240
    val serviceFormBufferMins: Int = 0, // 0, 5, 10, 15, 20, 30
    val serviceFormStaffIds: Set<String> = emptySet(),
    val isSavingService: Boolean = false,

    // Combos / Packages
    val combosList: List<Combo> = emptyList(),
    val showAddEditComboDialog: Boolean = false,
    val comboBeingEdited: Combo? = null,
    val comboFormName: String = "",
    val comboFormPrice: String = "",
    val comboFormServiceIds: Set<String> = emptySet(),
    val isSavingCombo: Boolean = false,

    // Staff Management
    val showAddStaffDialog: Boolean = false,
    val staffFormName: String = "",
    val staffFormCommission: String = "20",
    val staffFormPhotoUrl: String? = null,
    val isSavingStaff: Boolean = false,
    val selectedStaffForDetail: Staff? = null,
    val staffHoursList: List<StaffHours> = emptyList(),
    val staffSelectedServiceIds: Set<String> = emptySet(),
    val isSavingStaffDetails: Boolean = false,
    val staffFormAllServices: Boolean = false,
    val staffFormServiceIds: Set<String> = emptySet(),
    val staffDetailAllServices: Boolean = false,
    // Upcoming bookings affected by removing services from a stylist (asks the owner to confirm).
    val staffServiceRemovalWarning: Int? = null,

    // Setup checklist / Go Live
    val setupStatus: SalonSetupStatus? = null,
    val isGoingLive: Boolean = false,

    // Salon Working Hours
    val salonHoursList: List<SalonHours> = emptyList(),
    val isSavingSalonHours: Boolean = false,

    // Booking Settings
    val settingSlotInterval: Int = 30,
    val settingBookingWindowDays: Int = 7,
    val settingMinNoticeMinutes: Int = 30,
    val settingLateThresholdMinutes: Int = 15,
    val settingLateCreditAmount: Double = 0.0,
    val isSavingSettings: Boolean = false,

    // Payout Details
    val payoutDetails: SalonPayoutDetails? = null,
    val payoutHolderName: String = "",
    val payoutUpiId: String = "",
    val payoutAccountNumber: String = "",
    val payoutIfsc: String = "",
    val isSavingPayout: Boolean = false,

    // Part 5: Earnings (Kamai)
    val earningsDatePreset: String = "today", // "today", "week", "month", "custom"
    val earningsCustomFrom: String = "",
    val earningsCustomTo: String = "",
    val staffEarnings: List<StaffEarningsSummary> = emptyList(),
    val isLoadingEarnings: Boolean = false,
    val earningsError: String? = null,

    // Part 5: Reviews
    val salonReviews: List<SalonReview> = emptyList(),
    val isLoadingReviews: Boolean = false,
    val reviewsError: String? = null,
    val replyingReviewId: String? = null,
    val replyInputText: String = "",
    val isSubmittingReply: Boolean = false,
    val replyError: String? = null,

    // Part 5: Notifications
    val notificationsList: List<SalonNotification> = emptyList(),
    val isLoadingNotifications: Boolean = false,
    val showNotificationsSheet: Boolean = false,

    // Part 5: Settings / Profile Modal
    val showProfileModal: Boolean = false
) {
    val unreadNotificationCount: Int get() = notificationsList.count { !it.isRead }
    val totalEarningsRevenue: Double get() = staffEarnings.sumOf { it.revenue }
    val totalEarningsCompleted: Int get() = staffEarnings.sumOf { it.completedCount }
    val totalEarningsNoShows: Int get() = staffEarnings.sumOf { it.noShowCount }
    val totalEarningsCommission: Double get() = staffEarnings.sumOf { it.commissionAmount }
}


class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepo = AuthRepository(application)
    private val salonRepo = SalonRepository(application, authRepo)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var realtimeSyncJob: Job? = null

    init {
        checkInitialSession()
    }

    private fun checkInitialSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val savedUserId = authRepo.getUserId()
            val savedEmail = authRepo.getUserEmail()
            val savedLang = authRepo.getSavedLanguage()

            if (savedUserId != null) {
                val user = SupabaseUser(id = savedUserId, email = savedEmail)
                val profile = authRepo.fetchProfile(savedUserId)
                val effectiveLang = profile?.language ?: savedLang
                val salons = authRepo.fetchSalons()
                val currentSalon = salons.firstOrNull()

                val targetRoute = determineRoute(user, currentSalon)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = user,
                        profile = profile,
                        salon = currentSalon,
                        language = effectiveLang,
                        regLanguage = effectiveLang,
                        destinationRoute = targetRoute,
                        currentStep = if (currentSalon?.verificationStatus == "draft") 3 else 1
                    )
                }

                if (targetRoute == "main") {
                    startRealtimeSync()
                    loadDashboard()
                    loadTodayBookings()
                    loadStaffAndServices()
                    loadEarningsSummary()
                    loadSalonReviews()
                    loadNotifications()
                }

            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        destinationRoute = "login",
                        language = savedLang,
                        regLanguage = savedLang
                    )
                }
            }
        }
    }

    private fun determineRoute(user: SupabaseUser?, salon: Salon?): String {
        if (user == null) return "login"
        if (salon == null) return "register"
        return when (salon.verificationStatus.lowercase()) {
            "pending" -> "pending"
            "rejected", "suspended" -> "rejected"
            "approved" -> "main"
            "draft" -> "register"
            else -> "register"
        }
    }

    fun setLanguage(lang: String) {
        authRepo.saveLanguage(lang)
        _uiState.update { it.copy(language = lang, regLanguage = lang) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    fun toggleConfigDialog(show: Boolean) {
        _uiState.update { it.copy(showConfigDialog = show) }
    }

    // --- Auth Actions ---

    /** After any successful sign-in: load profile + salon and open the right screen. */
    private suspend fun onSignedIn(user: SupabaseUser) {
        val profile = authRepo.fetchProfile(user.id)
        val lang = profile?.language ?: _uiState.value.language
        val salon = authRepo.fetchSalons().firstOrNull()
        val route = determineRoute(user, salon)
        _uiState.update {
            it.copy(
                isLoading = false,
                user = user,
                profile = profile,
                salon = salon,
                language = lang,
                ownerName = it.ownerName.ifBlank { profile?.fullName.orEmpty() },
                // Pre-fill the salon contact number with the verified mobile (owner can change it).
                phone = it.phone.ifBlank { user.phone.orEmpty() },
                destinationRoute = route,
                currentStep = if (salon?.verificationStatus == "draft") 3 else 1,
                otpSent = false
            )
        }
        if (route == "main") {
            startRealtimeSync()
            loadDashboard()
            loadTodayBookings()
            loadStaffAndServices()
            loadEarningsSummary()
            loadSalonReviews()
            loadNotifications()
        }
    }

    /** Mobile login step 1: send the OTP. New numbers are registered automatically after verification. */
    fun sendLoginOtp(mobile: String) {
        val m = mobile.filter { it.isDigit() }
        if (!Regex("^[6-9]\\d{9}$").matches(m)) {
            _uiState.update { it.copy(errorMessage = "Enter a valid 10-digit mobile number") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val res = authRepo.sendOtp(m)) {
                is AuthResult.Success -> _uiState.update { it.copy(isLoading = false, otpSent = true, otpMobile = m) }
                is AuthResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
            }
        }
    }

    /** Mobile login step 2: verify the OTP and continue to registration or the dashboard. */
    fun verifyLoginOtp(code: String) {
        val mobile = _uiState.value.otpMobile
        if (code.length != 6) {
            _uiState.update { it.copy(errorMessage = "Enter the 6-digit OTP") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val res = authRepo.verifyOtp(mobile, code)) {
                is AuthResult.Success -> {
                    val uid = res.data.user?.id ?: authRepo.getUserId() ?: ""
                    onSignedIn(SupabaseUser(id = uid, email = null, phone = mobile))
                }
                is AuthResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
            }
        }
    }

    fun changeOtpNumber() {
        _uiState.update { it.copy(otpSent = false, errorMessage = null) }
    }

    /** Email + password sign-in, kept for owners who registered with email before mobile login existed. */
    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.update { it.copy(errorMessage = SalonStrings.get("err_fill_all", it.language)) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val res = authRepo.signIn(email, pass)) {
                is AuthResult.Success -> {
                    val uid = res.data.user?.id ?: authRepo.getUserId() ?: ""
                    onSignedIn(SupabaseUser(id = uid, email = email))
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
            }
        }
    }

    fun navigateToEmailLogin() {
        _uiState.update { it.copy(destinationRoute = "email_login", errorMessage = null) }
    }

    fun navigateToLogin() {
        _uiState.update { it.copy(destinationRoute = "login", errorMessage = null, otpSent = false) }
    }

    /** Deletes the account; [onResult] gets null on success or an error message. */
    fun deleteAccount(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            when (val res = salonRepo.deleteMyAccount()) {
                is SalonResult.Success -> {
                    realtimeSyncJob?.cancel()
                    _uiState.update {
                        it.copy(user = null, profile = null, salon = null, destinationRoute = "login", currentStep = 1,
                            errorMessage = null, selectedTab = "today", infoMessage = "Your account has been deleted.")
                    }
                    onResult(null)
                }
                is SalonResult.Error -> onResult(res.message)
            }
        }
    }

    fun signOut() {
        realtimeSyncJob?.cancel()
        authRepo.clearSession()
        _uiState.update {
            it.copy(
                user = null,
                profile = null,
                salon = null,
                destinationRoute = "login",
                currentStep = 1,
                errorMessage = null,
                selectedTab = "today"
            )
        }
    }

    // --- 3-Step Registration Form Flow ---

    fun updateOwnerName(name: String) {
        _uiState.update { it.copy(ownerName = name) }
    }

    fun updateRegLanguage(lang: String) {
        _uiState.update { it.copy(regLanguage = lang, language = lang) }
        authRepo.saveLanguage(lang)
    }

    fun updateSalonDetails(
        name: String? = null,
        type: String? = null,
        address: String? = null,
        area: String? = null,
        city: String? = null,
        pincode: String? = null,
        phone: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        gstNumber: String? = null
    ) {
        _uiState.update {
            it.copy(
                salonName = name ?: it.salonName,
                salonType = type ?: it.salonType,
                address = address ?: it.address,
                area = area ?: it.area,
                city = city ?: it.city,
                pincode = pincode ?: it.pincode,
                phone = phone ?: it.phone,
                latitude = if (latitude != null) latitude else it.latitude,
                longitude = if (longitude != null) longitude else it.longitude,
                gstNumber = if (gstNumber != null) gstNumber else it.gstNumber
            )
        }
    }

    fun updateDocType(docType: String) {
        _uiState.update { it.copy(docType = docType) }
    }

    fun selectDocument(name: String, bytes: ByteArray, mimeType: String) {
        _uiState.update {
            it.copy(
                documentName = name,
                documentBytes = bytes,
                documentMimeType = mimeType,
                errorMessage = null
            )
        }
    }

    fun updateAcceptTerms(accept: Boolean) {
        _uiState.update { it.copy(acceptTerms = accept) }
    }

    fun goToNextStep() {
        val s = _uiState.value
        val lang = s.language
        if (s.currentStep == 1) {
            if (s.ownerName.trim().isBlank()) {
                _uiState.update { it.copy(errorMessage = SalonStrings.get("err_fill_all", lang)) }
                return
            }
            _uiState.update { it.copy(currentStep = 2, errorMessage = null) }
        } else if (s.currentStep == 2) {
            if (s.salonName.trim().isBlank() || s.address.trim().isBlank() ||
                s.area.trim().isBlank() || s.city.trim().isBlank() ||
                s.pincode.trim().isBlank() || s.phone.trim().isBlank()
            ) {
                _uiState.update { it.copy(errorMessage = SalonStrings.get("err_fill_all", lang)) }
                return
            }
            if (!Regex("^[1-9][0-9]{5}$").matches(s.pincode.trim())) {
                _uiState.update { it.copy(errorMessage = SalonStrings.get("err_pincode", lang)) }
                return
            }
            if (s.phone.trim().length < 10) {
                _uiState.update { it.copy(errorMessage = SalonStrings.get("err_phone", lang)) }
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                val res = salonRepo.createMySalon(
                    ownerName = s.ownerName.trim(),
                    name = s.salonName.trim(),
                    salonType = s.salonType,
                    address = s.address.trim(),
                    area = s.area.trim(),
                    city = s.city.trim(),
                    pincode = s.pincode.trim(),
                    phone = s.phone.trim(),
                    latitude = s.latitude,
                    longitude = s.longitude,
                    gstNumber = s.gstNumber.trim().ifBlank { null },
                    language = s.regLanguage
                )
                when (res) {
                    is SalonResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                salon = res.data,
                                currentStep = 3,
                                errorMessage = null
                            )
                        }
                    }
                    is SalonResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                    }
                }
            }
        }
    }

    fun goToPreviousStep() {
        _uiState.update {
            if (it.currentStep > 1) it.copy(currentStep = it.currentStep - 1, errorMessage = null)
            else it
        }
    }

    fun submitVerification() {
        val s = _uiState.value
        val lang = s.language
        if (!s.acceptTerms) {
            _uiState.update { it.copy(errorMessage = SalonStrings.get("err_terms", lang)) }
            return
        }

        val salonId = s.salon?.id ?: ""
        val fileBytes = s.documentBytes
        if (fileBytes == null || fileBytes.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please choose a document to upload.") }
            return
        }
        val fileName = s.documentName ?: "doc_${System.currentTimeMillis()}.jpg"

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val res = salonRepo.uploadDocumentAndSubmit(
                salonId = salonId,
                docType = s.docType,
                fileName = fileName,
                fileBytes = fileBytes,
                mimeType = s.documentMimeType,
                acceptTerms = true
            )

            when (res) {
                is SalonResult.Success -> {
                    val updatedSalon = salonRepo.refreshSalonStatus()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            salon = updatedSalon ?: it.salon?.copy(verificationStatus = "pending"),
                            destinationRoute = "pending",
                            errorMessage = null
                        )
                    }
                }
                is SalonResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
            }
        }
    }

    fun refreshStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val salon = salonRepo.refreshSalonStatus()
            val targetRoute = determineRoute(_uiState.value.user, salon)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    salon = salon,
                    destinationRoute = targetRoute,
                    infoMessage = if (salon?.verificationStatus == "pending") "Status: Under review" else null
                )
            }
            if (targetRoute == "main") {
                startRealtimeSync()
                loadDashboard()
                loadTodayBookings()
                loadStaffAndServices()
            }
        }
    }

    fun editAndResubmit() {
        _uiState.update {
            it.copy(
                destinationRoute = "register",
                currentStep = 3,
                acceptTerms = false,
                errorMessage = null
            )
        }
    }

    // ==========================================
    // PART 2: App Shell, Dashboard, and Today's Schedule
    // ==========================================

    fun selectTab(tab: String) {
        _uiState.update { it.copy(selectedTab = tab, errorMessage = null) }
        when (tab) {
            "today" -> {
                loadDashboard()
                loadTodayBookings()
            }
            "bookings" -> {
                loadUpcomingBookings()
                loadPastBookings()
            }
            "salon" -> {
                loadStaffAndServices()
                loadBreaksAndTimeOff()
            }
            "earnings" -> {
                loadEarningsSummary()
            }
        }

    }

    fun loadSetupStatus() {
        viewModelScope.launch {
            val res = salonRepo.getSetupStatus()
            if (res is SalonResult.Success) _uiState.update { it.copy(setupStatus = res.data) }
        }
    }

    /** Makes the salon visible to customers. The server refuses while setup is incomplete. */
    fun goLive() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGoingLive = true) }
            when (val res = salonRepo.updateSalonActive(true)) {
                is SalonResult.Success -> _uiState.update {
                    it.copy(
                        isGoingLive = false,
                        salon = it.salon?.copy(isActive = true),
                        dashboard = it.dashboard?.copy(isActive = true),
                        infoMessage = "Your salon is LIVE. Customers can now book."
                    )
                }
                is SalonResult.Error -> _uiState.update { it.copy(isGoingLive = false, errorMessage = res.message) }
            }
            loadSetupStatus()
        }
    }

    fun loadDashboard() {
        loadSetupStatus()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDashboard = true) }
            when (val res = salonRepo.getOwnerDashboard()) {
                is SalonResult.Success -> {
                    val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                    _uiState.update {
                        it.copy(
                            isLoadingDashboard = false,
                            dashboard = res.data,
                            lastSyncTime = timeStr,
                            salon = it.salon?.copy(isActive = res.data.isActive) ?: it.salon
                        )
                    }
                }
                is SalonResult.Error -> {
                    _uiState.update { it.copy(isLoadingDashboard = false) }
                }
            }
        }
    }

    fun loadTodayBookings() {
        val staffFilter = _uiState.value.selectedStaffFilter
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingBookings = true) }
            val today = getTodayDateString()
            when (val res = salonRepo.getOwnerBookings(fromDate = today, toDate = today, staffId = staffFilter)) {
                is SalonResult.Success -> {
                    val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                    _uiState.update {
                        it.copy(
                            isLoadingBookings = false,
                            todayBookings = res.data,
                            lastSyncTime = timeStr
                        )
                    }
                }
                is SalonResult.Error -> {
                    _uiState.update { it.copy(isLoadingBookings = false) }
                }
            }
        }
    }

    fun refreshAllTodayData() {
        loadDashboard()
        loadTodayBookings()
        loadUpcomingBookings()
        loadPastBookings()
        loadStaffAndServices()
    }

    fun selectStaffFilter(staffId: String) {
        _uiState.update { it.copy(selectedStaffFilter = staffId) }
        loadTodayBookings()
        loadUpcomingBookings()
        loadPastBookings()
    }

    private fun loadStaffAndServices() {
        loadSetupStatus()
        viewModelScope.launch {
            val staff = salonRepo.getStaff()
            val services = salonRepo.getServices()
            _uiState.update {
                it.copy(
                    staffList = staff,
                    servicesList = services,
                    walkInStaffId = if (it.walkInStaffId.isBlank()) staff.firstOrNull()?.id ?: "" else it.walkInStaffId,
                    walkInServiceId = if (it.walkInServiceId.isBlank()) services.firstOrNull()?.id ?: "" else it.walkInServiceId
                )
            }
        }
    }

    fun toggleSalonActive(isActive: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val res = salonRepo.updateSalonActive(isActive)) {
                is SalonResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            salon = it.salon?.copy(isActive = isActive),
                            dashboard = it.dashboard?.copy(isActive = isActive),
                            infoMessage = if (isActive) "Salon is now ON for customers" else "Salon is now OFF for customers"
                        )
                    }
                }
                is SalonResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
            }
            loadSetupStatus()
        }
    }

    fun updateBookingStatus(bookingId: String, newStatus: String) {
        viewModelScope.launch {
            when (val res = salonRepo.updateBookingStatus(bookingId, newStatus)) {
                is SalonResult.Success -> {
                    _uiState.update {
                        it.copy(
                            infoMessage = "Booking status updated to $newStatus",
                            errorMessage = null
                        )
                    }
                    loadTodayBookings()
                    loadDashboard()
                }
                is SalonResult.Error -> {
                    _uiState.update { it.copy(errorMessage = res.message) }
                }
            }
        }
    }

    fun openWalkInModal() {
        loadStaffAndServices()
        _uiState.update {
            it.copy(
                isWalkInModalOpen = true,
                walkInCustomerName = "",
                walkInCustomerPhone = "",
                walkInError = null
            )
        }
    }

    fun closeWalkInModal() {
        _uiState.update {
            it.copy(
                isWalkInModalOpen = false,
                walkInError = null
            )
        }
    }

    fun updateWalkInForm(
        staffId: String? = null,
        serviceId: String? = null,
        name: String? = null,
        phone: String? = null
    ) {
        _uiState.update {
            it.copy(
                walkInStaffId = staffId ?: it.walkInStaffId,
                walkInServiceId = serviceId ?: it.walkInServiceId,
                walkInCustomerName = name ?: it.walkInCustomerName,
                walkInCustomerPhone = phone ?: it.walkInCustomerPhone,
                walkInError = null
            )
        }
    }

    fun submitWalkIn() {
        val s = _uiState.value
        val name = s.walkInCustomerName.trim()
        val staffId = s.walkInStaffId.ifBlank { s.staffList.firstOrNull()?.id ?: "" }
        val serviceId = s.walkInServiceId.ifBlank { s.servicesList.firstOrNull()?.id ?: "" }
        val phone = s.walkInCustomerPhone.trim().ifBlank { null }

        if (name.isBlank()) {
            _uiState.update { it.copy(walkInError = "Customer name is required.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingWalkIn = true, walkInError = null) }
            val res = salonRepo.createWalkInBooking(
                staffId = staffId,
                serviceId = serviceId,
                customerName = name,
                customerPhone = phone
            )

            when (res) {
                is SalonResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingWalkIn = false,
                            isWalkInModalOpen = false,
                            infoMessage = "Walk-in booking created successfully for $name",
                            walkInCustomerName = "",
                            walkInCustomerPhone = "",
                            walkInError = null
                        )
                    }
                    loadTodayBookings()
                    loadDashboard()
                }
                is SalonResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingWalkIn = false,
                            walkInError = res.message
                        )
                    }
                }
            }
        }
    }

    // Realtime update background subscription / sync loop
    private fun startRealtimeSync() {
        realtimeSyncJob?.cancel()
        realtimeSyncJob = viewModelScope.launch {
            while (isActive) {
                delay(12000) // sync every 12 seconds
                if (_uiState.value.destinationRoute == "main") {
                    val staffFilter = _uiState.value.selectedStaffFilter
                    val today = getTodayDateString()
                    when (val res = salonRepo.getOwnerBookings(fromDate = today, toDate = today, staffId = staffFilter)) {
                        is SalonResult.Success -> {
                            val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                            _uiState.update {
                                it.copy(
                                    todayBookings = res.data,
                                    lastSyncTime = timeStr
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    // ==========================================
    // PART 3: Bookings List, Customer History, Reschedule & Time Off
    // ==========================================

    private fun getDateOffset(days: Int): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, days)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    fun setBookingsSubTab(subTab: String) {
        _uiState.update { it.copy(bookingsSubTab = subTab) }
        if (subTab == "upcoming") {
            loadUpcomingBookings()
        } else {
            loadPastBookings()
        }
    }

    fun loadUpcomingBookings() {
        val staffFilter = _uiState.value.selectedStaffFilter
        val fromDate = getTodayDateString()
        val toDate = getDateOffset(30)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingUpcoming = true) }
            when (val res = salonRepo.getOwnerBookings(fromDate = fromDate, toDate = toDate, staffId = staffFilter)) {
                is SalonResult.Success -> {
                    // Filter client-side to status in (confirmed, arrived, in_service)
                    val filtered = res.data.filter {
                        it.status == "confirmed" || it.status == "arrived" || it.status == "in_service"
                    }.sortedBy { it.startTime }
                    _uiState.update {
                        it.copy(
                            isLoadingUpcoming = false,
                            upcomingBookings = filtered
                        )
                    }
                }
                is SalonResult.Error -> {
                    _uiState.update { it.copy(isLoadingUpcoming = false) }
                }
            }
        }
    }

    fun loadPastBookings() {
        val staffFilter = _uiState.value.selectedStaffFilter
        val fromDate = getDateOffset(-30)
        val toDate = getTodayDateString()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPast = true) }
            when (val res = salonRepo.getOwnerBookings(fromDate = fromDate, toDate = toDate, staffId = staffFilter)) {
                is SalonResult.Success -> {
                    // Filter client-side to (completed, cancelled, no_show)
                    val filtered = res.data.filter {
                        it.status == "completed" || it.status == "cancelled" || it.status == "no_show"
                    }.sortedByDescending { it.startTime }
                    _uiState.update {
                        it.copy(
                            isLoadingPast = false,
                            pastBookings = filtered
                        )
                    }
                }
                is SalonResult.Error -> {
                    _uiState.update { it.copy(isLoadingPast = false) }
                }
            }
        }
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun openBookingDetail(booking: Booking) {
        _uiState.update {
            it.copy(
                selectedBookingForDetail = booking,
                showBookingDetailSheet = true,
                customerSummary = null,
                showCustomerSummaryDialog = false,
                showRescheduleDialog = false,
                showCancelBookingDialog = false
            )
        }
    }

    fun closeBookingDetail() {
        _uiState.update {
            it.copy(
                selectedBookingForDetail = null,
                showBookingDetailSheet = false,
                showCustomerSummaryDialog = false,
                showRescheduleDialog = false,
                showCancelBookingDialog = false
            )
        }
    }

    fun viewCustomerHistory(bookingId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCustomerSummary = true, showCustomerSummaryDialog = true) }
            when (val res = salonRepo.getCustomerSummary(bookingId)) {
                is SalonResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingCustomerSummary = false,
                            customerSummary = res.data
                        )
                    }
                }
                is SalonResult.Error -> {
                    _uiState.update { it.copy(isLoadingCustomerSummary = false) }
                }
            }
        }
    }

    fun closeCustomerSummaryDialog() {
        _uiState.update { it.copy(showCustomerSummaryDialog = false) }
    }

    fun closeCustomerHistory() {
        closeCustomerSummaryDialog()
    }

    fun openRescheduleDialog(booking: Booking? = null) {
        val target = booking ?: _uiState.value.selectedBookingForDetail ?: return
        val currentDate = if (target.startTime.contains("T")) target.startTime.substringBefore("T") else getTodayDateString()
        _uiState.update {
            it.copy(
                selectedBookingForDetail = target,
                showRescheduleDialog = true,
                rescheduleSelectedDate = currentDate,
                rescheduleSelectedTime = "11:00"
            )
        }
    }

    fun closeRescheduleDialog() {
        _uiState.update { it.copy(showRescheduleDialog = false) }
    }

    fun onRescheduleDateSelected(date: String) {
        _uiState.update { it.copy(rescheduleSelectedDate = date) }
    }

    fun setRescheduleDate(date: String) {
        onRescheduleDateSelected(date)
    }

    fun onRescheduleTimeSelected(time: String) {
        _uiState.update { it.copy(rescheduleSelectedTime = time) }
    }

    fun setRescheduleTime(time: String) {
        onRescheduleTimeSelected(time)
    }

    fun submitReschedule() {
        submitRescheduleBooking()
    }

    fun submitRescheduleBooking() {
        val booking = _uiState.value.selectedBookingForDetail ?: return
        val date = _uiState.value.rescheduleSelectedDate.ifBlank { getTodayDateString() }
        val time = _uiState.value.rescheduleSelectedTime.ifBlank { "11:00" }
        val newStart = "${date}T${time}:00"

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingReschedule = true) }
            when (val res = salonRepo.rescheduleBooking(booking.id, newStart)) {
                is SalonResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingReschedule = false,
                            showRescheduleDialog = false,
                            selectedBookingForDetail = booking.copy(startTime = newStart, status = "confirmed"),
                            infoMessage = "Booking rescheduled successfully to $date at $time"
                        )
                    }
                    loadTodayBookings()
                    loadUpcomingBookings()
                    loadDashboard()
                }
                is SalonResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingReschedule = false,
                            errorMessage = res.message
                        )
                    }
                }
            }
        }
    }

    fun openCancelBookingDialog(booking: Booking? = null) {
        val target = booking ?: _uiState.value.selectedBookingForDetail ?: return
        _uiState.update {
            it.copy(
                selectedBookingForDetail = target,
                showCancelBookingDialog = true
            )
        }
    }

    fun closeCancelBookingDialog() {
        _uiState.update { it.copy(showCancelBookingDialog = false) }
    }

    fun confirmCancelBooking() {
        val booking = _uiState.value.selectedBookingForDetail ?: return
        viewModelScope.launch {
            when (val res = salonRepo.updateBookingStatus(booking.id, "cancelled")) {
                is SalonResult.Success -> {
                    _uiState.update {
                        it.copy(
                            showCancelBookingDialog = false,
                            selectedBookingForDetail = booking.copy(status = "cancelled"),
                            infoMessage = "Booking cancelled successfully"
                        )
                    }
                    loadTodayBookings()
                    loadUpcomingBookings()
                    loadPastBookings()
                    loadDashboard()
                }
                is SalonResult.Error -> {
                    _uiState.update {
                        it.copy(
                            showCancelBookingDialog = false,
                            errorMessage = res.message
                        )
                    }
                }
            }
        }
    }

    // --- Staff Time Off & Breaks ---

    fun loadBreaksAndTimeOff() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTimeOff = true) }
            val breaksRes = salonRepo.getRecurringBreaks()
            val timeOffRes = salonRepo.getStaffTimeOff()

            val breaks = (breaksRes as? SalonResult.Success)?.data ?: _uiState.value.recurringBreaks
            val timeOff = (timeOffRes as? SalonResult.Success)?.data ?: _uiState.value.staffTimeOffList

            _uiState.update {
                it.copy(
                    isLoadingTimeOff = false,
                    recurringBreaks = breaks,
                    staffTimeOffList = timeOff
                )
            }
        }
    }

    fun openAddRecurringBreakDialog() {
        _uiState.update { it.copy(showAddRecurringBreakDialog = true) }
    }

    fun closeAddRecurringBreakDialog() {
        _uiState.update { it.copy(showAddRecurringBreakDialog = false) }
    }

    fun submitAddRecurringBreak(
        staffId: String?,
        dayOfWeek: Int?,
        startTime: String,
        endTime: String,
        label: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTimeOff = true) }
            when (val res = salonRepo.addRecurringBreak(staffId, dayOfWeek, startTime, endTime, label)) {
                is SalonResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingTimeOff = false,
                            showAddRecurringBreakDialog = false,
                            infoMessage = "Recurring break added: $label"
                        )
                    }
                    loadBreaksAndTimeOff()
                }
                is SalonResult.Error -> {
                    _uiState.update { it.copy(isLoadingTimeOff = false, errorMessage = res.message) }
                }
            }
        }
    }

    fun deleteRecurringBreak(id: String) {
        viewModelScope.launch {
            salonRepo.deleteRecurringBreak(id)
            _uiState.update { it.copy(infoMessage = "Break removed") }
            loadBreaksAndTimeOff()
        }
    }

    fun openAddTimeOffDialog() {
        _uiState.update { it.copy(showAddTimeOffDialog = true) }
    }

    fun closeAddTimeOffDialog() {
        _uiState.update { it.copy(showAddTimeOffDialog = false) }
    }

    fun prepareAddTimeOff(
        staffId: String?,
        startTime: String,
        endTime: String,
        reason: String?
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingTimeOff = true,
                    pendingTimeOffStaffId = staffId,
                    pendingTimeOffStart = startTime,
                    pendingTimeOffEnd = endTime,
                    pendingTimeOffReason = reason
                )
            }
            when (val res = salonRepo.getTimeOffConflicts(staffId, startTime, endTime)) {
                is SalonResult.Success -> {
                    val conflicts = res.data
                    if (conflicts.isNotEmpty()) {
                        _uiState.update {
                            it.copy(
                                isLoadingTimeOff = false,
                                conflictingBookings = conflicts,
                                showConflictsDialog = true
                            )
                        }
                    } else {
                        confirmAddTimeOff(cancelConflicts = false)
                    }
                }
                is SalonResult.Error -> {
                    confirmAddTimeOff(cancelConflicts = false)
                }
            }
        }
    }

    fun confirmAddTimeOff(cancelConflicts: Boolean) {
        val staffId = _uiState.value.pendingTimeOffStaffId
        val start = _uiState.value.pendingTimeOffStart
        val end = _uiState.value.pendingTimeOffEnd
        val reason = _uiState.value.pendingTimeOffReason

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTimeOff = true) }
            when (val res = salonRepo.addStaffTimeOff(staffId, start, end, reason, cancelConflicts)) {
                is SalonResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingTimeOff = false,
                            showAddTimeOffDialog = false,
                            showConflictsDialog = false,
                            conflictingBookings = emptyList(),
                            infoMessage = "Time off scheduled successfully"
                        )
                    }
                    loadBreaksAndTimeOff()
                    loadTodayBookings()
                    loadUpcomingBookings()
                    loadDashboard()
                }
                is SalonResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingTimeOff = false,
                            showConflictsDialog = false,
                            errorMessage = res.message
                        )
                    }
                }
            }
        }
    }

    fun closeConflictsDialog() {
        _uiState.update {
            it.copy(
                showConflictsDialog = false,
                conflictingBookings = emptyList()
            )
        }
    }

    fun deleteStaffTimeOff(id: String) {
        viewModelScope.launch {
            salonRepo.deleteStaffTimeOff(id)
            _uiState.update { it.copy(infoMessage = "Time off removed") }
            loadBreaksAndTimeOff()
        }
    }

    // ==========================================
    // PART 4: Salon Management (Profile, Catalog, Staff, Timings, Settings, Payout)
    // ==========================================

    fun setSalonSection(section: String) {
        _uiState.update { it.copy(salonActiveSection = section) }
        when (section) {
            "profile" -> initSalonProfileForm()
            "services" -> {
                loadServiceCategories()
                loadStaffAndServices()
            }
            "combos" -> loadCombos()
            "staff" -> loadStaffAndServices()
            "working_hours" -> loadSalonHours()
            "booking_settings" -> initBookingSettings()
            "payout_details" -> loadPayoutDetails()
            "time_off" -> loadBreaksAndTimeOff()
            "reviews" -> loadSalonReviews()
        }

    }

    fun initSalonProfileForm() {
        val salon = _uiState.value.salon
        if (salon != null) {
            _uiState.update {
                it.copy(
                    editSalonName = salon.name,
                    editSalonDescription = salon.description ?: "Welcome to ${salon.name}! Premium salon offering styling, hair care, skin care, grooming, and luxury services with certified stylists.",
                    editSalonAddress = salon.address,
                    editSalonArea = salon.area,
                    editSalonCity = salon.city,
                    editSalonPincode = salon.pincode,
                    editSalonPhone = salon.phone,
                    editSalonGst = salon.gstNumber ?: "",
                    editSalonType = salon.salonType,
                    editSalonPhotos = if (salon.photos.isNotEmpty()) salon.photos else listOf(
                        "https://images.unsplash.com/photo-1560066984-138dadb4c035?w=800",
                        "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=800"
                    ),
                    editCoverPhotoIndex = salon.coverPhotoIndex ?: 0,
                    editSalonIsActive = salon.isActive
                )
            }
        }
    }

    fun updateProfileName(name: String) = _uiState.update { it.copy(editSalonName = name) }
    fun updateProfileDescription(desc: String) = _uiState.update { it.copy(editSalonDescription = desc) }
    fun updateProfileAddress(address: String) = _uiState.update { it.copy(editSalonAddress = address) }
    fun updateProfileArea(area: String) = _uiState.update { it.copy(editSalonArea = area) }
    fun updateProfileCity(city: String) = _uiState.update { it.copy(editSalonCity = city) }
    fun updateProfilePincode(pincode: String) = _uiState.update { it.copy(editSalonPincode = pincode) }
    fun updateProfilePhone(phone: String) = _uiState.update { it.copy(editSalonPhone = phone) }
    fun updateProfileGst(gst: String) = _uiState.update { it.copy(editSalonGst = gst) }
    fun updateProfileType(type: String) = _uiState.update { it.copy(editSalonType = type) }
    fun toggleSalonActiveState(isActive: Boolean) = _uiState.update { it.copy(editSalonIsActive = isActive) }

    fun addSalonPhotoUrl(url: String) {
        val currentPhotos = _uiState.value.editSalonPhotos.toMutableList()
        currentPhotos.add(url.trim())
        _uiState.update { it.copy(editSalonPhotos = currentPhotos) }
    }

    fun removeSalonPhoto(index: Int) {
        val currentPhotos = _uiState.value.editSalonPhotos.toMutableList()
        if (index in currentPhotos.indices) {
            currentPhotos.removeAt(index)
            val newCover = if (_uiState.value.editCoverPhotoIndex >= currentPhotos.size) 0 else _uiState.value.editCoverPhotoIndex
            _uiState.update { it.copy(editSalonPhotos = currentPhotos, editCoverPhotoIndex = newCover) }
        }
    }

    fun setCoverPhotoIndex(index: Int) {
        _uiState.update { it.copy(editCoverPhotoIndex = index) }
    }

    fun saveSalonLocation(latitude: Double, longitude: Double) {
        val salon = _uiState.value.salon ?: return
        viewModelScope.launch {
            when (val res = salonRepo.updateSalonLocation(salon.id, latitude, longitude)) {
                is SalonResult.Success -> _uiState.update {
                    it.copy(
                        salon = it.salon?.copy(latitude = latitude, longitude = longitude),
                        infoMessage = "Salon location saved. Nearby customers will now see your salon."
                    )
                }
                is SalonResult.Error -> _uiState.update { it.copy(infoMessage = res.message) }
            }
            loadSetupStatus()
        }
    }

    fun saveSalonProfile() {
        val s = _uiState.value
        val salon = s.salon ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingSalonProfile = true) }
            val req = UpdateSalonProfileRequest(
                name = s.editSalonName.trim(),
                description = s.editSalonDescription.trim(),
                address = s.editSalonAddress.trim(),
                area = s.editSalonArea.trim(),
                city = s.editSalonCity.trim(),
                pincode = s.editSalonPincode.trim(),
                phone = s.editSalonPhone.trim(),
                gstNumber = s.editSalonGst.trim().ifBlank { null },
                salonType = s.editSalonType,
                photos = s.editSalonPhotos,
                coverPhotoIndex = s.editCoverPhotoIndex,
                isActive = s.editSalonIsActive
            )
            val saveRes = salonRepo.updateSalonProfile(salon.id, req)
            if (saveRes is SalonResult.Error) {
                _uiState.update { it.copy(isSavingSalonProfile = false, errorMessage = saveRes.message) }
                loadSetupStatus()
                return@launch
            }
            val updatedSalon = salon.copy(
                name = req.name,
                description = req.description,
                address = req.address,
                area = req.area,
                city = req.city,
                pincode = req.pincode,
                phone = req.phone,
                gstNumber = req.gstNumber,
                salonType = req.salonType,
                photos = req.photos,
                coverPhotoIndex = req.coverPhotoIndex,
                isActive = req.isActive
            )
            _uiState.update {
                it.copy(
                    isSavingSalonProfile = false,
                    salon = updatedSalon,
                    editSalonIsActive = req.isActive,
                    infoMessage = SalonStrings.get(it.language, "profile_saved_success")
                )
            }
        }
    }

    // Categories
    fun loadServiceCategories() {
        val salonId = _uiState.value.salon?.id ?: "salon-1"
        viewModelScope.launch {
            when (val res = salonRepo.getServiceCategories(salonId)) {
                is SalonResult.Success -> _uiState.update { it.copy(categoriesList = res.data) }
                is SalonResult.Error -> {}
            }
        }
    }

    fun openAddCategoryDialog() {
        _uiState.update {
            it.copy(
                showAddCategoryDialog = true,
                categoryBeingEdited = null,
                categoryFormName = "",
                categoryFormSortOrder = "${(it.categoriesList.maxOfOrNull { c -> c.sortOrder } ?: 0) + 1}"
            )
        }
    }

    fun openEditCategoryDialog(cat: ServiceCategory) {
        _uiState.update {
            it.copy(
                showAddCategoryDialog = true,
                categoryBeingEdited = cat,
                categoryFormName = cat.name,
                categoryFormSortOrder = "${cat.sortOrder}"
            )
        }
    }

    fun closeCategoryDialog() {
        _uiState.update { it.copy(showAddCategoryDialog = false, categoryBeingEdited = null) }
    }

    fun saveCategory(name: String, sortOrderStr: String) {
        val salonId = _uiState.value.salon?.id ?: "salon-1"
        val sortOrder = sortOrderStr.toIntOrNull() ?: 1
        val editing = _uiState.value.categoryBeingEdited
        viewModelScope.launch {
            if (editing == null) {
                salonRepo.addServiceCategory(salonId, name, sortOrder)
            } else {
                salonRepo.updateServiceCategory(editing.id, name, sortOrder)
            }
            closeCategoryDialog()
            loadServiceCategories()
            _uiState.update { it.copy(infoMessage = "Category saved successfully") }
        }
    }

    fun deleteCategory(catId: String) {
        viewModelScope.launch {
            salonRepo.deleteServiceCategory(catId)
            loadServiceCategories()
            loadStaffAndServices()
            _uiState.update { it.copy(infoMessage = "Category removed") }
        }
    }

    // Services
    fun openAddServiceDialog() {
        val allStaffIds = _uiState.value.staffList.map { it.id }.toSet()
        _uiState.update {
            it.copy(
                showAddEditServiceDialog = true,
                serviceBeingEdited = null,
                serviceFormName = "",
                serviceFormCategoryId = it.categoriesList.firstOrNull()?.id,
                serviceFormPrice = "",
                serviceFormDurationMins = 30,
                serviceFormBufferMins = 0,
                serviceFormStaffIds = allStaffIds
            )
        }
    }

    fun openEditServiceDialog(service: SalonService) {
        val assigned = service.assignedStaffIds.toSet().ifEmpty {
            _uiState.value.staffList.map { it.id }.toSet()
        }
        _uiState.update {
            it.copy(
                showAddEditServiceDialog = true,
                serviceBeingEdited = service,
                serviceFormName = service.name,
                serviceFormCategoryId = service.categoryId,
                serviceFormPrice = "${service.price.toInt()}",
                serviceFormDurationMins = service.durationMins ?: 30,
                serviceFormBufferMins = service.bufferMins ?: 0,
                serviceFormStaffIds = assigned
            )
        }
    }

    fun closeServiceDialog() {
        _uiState.update { it.copy(showAddEditServiceDialog = false, serviceBeingEdited = null) }
    }

    fun updateServiceFormName(name: String) = _uiState.update { it.copy(serviceFormName = name) }
    fun updateServiceFormCategory(catId: String?) = _uiState.update { it.copy(serviceFormCategoryId = catId) }
    fun updateServiceFormPrice(price: String) = _uiState.update { it.copy(serviceFormPrice = price) }
    fun updateServiceFormDuration(duration: Int) = _uiState.update { it.copy(serviceFormDurationMins = duration) }
    fun updateServiceFormBuffer(buffer: Int) = _uiState.update { it.copy(serviceFormBufferMins = buffer) }

    fun toggleServiceStaffAssignment(staffId: String) {
        val current = _uiState.value.serviceFormStaffIds.toMutableSet()
        if (current.contains(staffId)) current.remove(staffId) else current.add(staffId)
        _uiState.update { it.copy(serviceFormStaffIds = current) }
    }

    fun selectAllStaffForService() {
        val allIds = _uiState.value.staffList.map { it.id }.toSet()
        _uiState.update { it.copy(serviceFormStaffIds = allIds) }
    }

    fun saveSalonService() {
        val s = _uiState.value
        val salonId = s.salon?.id ?: "salon-1"
        val price = s.serviceFormPrice.toDoubleOrNull() ?: 0.0
        if (s.serviceFormName.isBlank() || price <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Please enter valid service name and price") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingService = true) }
            val existing = s.serviceBeingEdited
            val service = SalonService(
                id = existing?.id ?: "",
                salonId = salonId,
                name = s.serviceFormName.trim(),
                category = s.categoriesList.find { it.id == s.serviceFormCategoryId }?.name ?: "General",
                price = price,
                durationMins = s.serviceFormDurationMins,
                isActive = true,
                categoryId = s.serviceFormCategoryId,
                bufferMins = s.serviceFormBufferMins
            )
            when (val res = salonRepo.saveSalonService(service, s.serviceFormStaffIds.toList())) {
                is SalonResult.Success -> {
                    closeServiceDialog()
                    _uiState.update {
                        it.copy(
                            isSavingService = false,
                            infoMessage = SalonStrings.get(it.language, "service_saved_success")
                        )
                    }
                }
                is SalonResult.Error -> _uiState.update { it.copy(isSavingService = false, errorMessage = res.message) }
            }
            loadStaffAndServices()
        }
    }

    fun deleteSalonService(serviceId: String) {
        viewModelScope.launch {
            salonRepo.deleteSalonService(serviceId)
            loadStaffAndServices()
            _uiState.update { it.copy(infoMessage = "Service removed") }
        }
    }

    // Combos / Packages
    fun loadCombos() {
        val salonId = _uiState.value.salon?.id ?: "salon-1"
        viewModelScope.launch {
            when (val res = salonRepo.getCombosList(salonId)) {
                is SalonResult.Success -> _uiState.update { it.copy(combosList = res.data) }
                is SalonResult.Error -> {}
            }
        }
    }

    fun openAddComboDialog() {
        _uiState.update {
            it.copy(
                showAddEditComboDialog = true,
                comboBeingEdited = null,
                comboFormName = "",
                comboFormPrice = "",
                comboFormServiceIds = emptySet()
            )
        }
    }

    fun openEditComboDialog(combo: Combo) {
        _uiState.update {
            it.copy(
                showAddEditComboDialog = true,
                comboBeingEdited = combo,
                comboFormName = combo.name,
                comboFormPrice = "${combo.price.toInt()}",
                comboFormServiceIds = combo.serviceIds.toSet()
            )
        }
    }

    fun closeComboDialog() {
        _uiState.update { it.copy(showAddEditComboDialog = false, comboBeingEdited = null) }
    }

    fun updateComboFormName(name: String) = _uiState.update { it.copy(comboFormName = name) }
    fun updateComboFormPrice(price: String) = _uiState.update { it.copy(comboFormPrice = price) }

    fun toggleComboServiceId(serviceId: String) {
        val current = _uiState.value.comboFormServiceIds.toMutableSet()
        if (current.contains(serviceId)) current.remove(serviceId) else current.add(serviceId)
        _uiState.update { it.copy(comboFormServiceIds = current) }
    }

    fun saveCombo() {
        val s = _uiState.value
        val salonId = s.salon?.id ?: "salon-1"
        val price = s.comboFormPrice.toDoubleOrNull() ?: 0.0
        if (s.comboFormName.isBlank() || price <= 0.0 || s.comboFormServiceIds.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter combo name, price, and select at least one service") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingCombo = true) }
            val combo = Combo(
                id = s.comboBeingEdited?.id ?: "",
                salonId = salonId,
                name = s.comboFormName.trim(),
                price = price,
                isActive = true,
                serviceIds = s.comboFormServiceIds.toList()
            )
            salonRepo.saveCombo(combo, s.comboFormServiceIds.toList())
            closeComboDialog()
            loadCombos()
            _uiState.update {
                it.copy(
                    isSavingCombo = false,
                    infoMessage = SalonStrings.get(it.language, "combo_saved_success")
                )
            }
        }
    }

    fun deleteCombo(comboId: String) {
        viewModelScope.launch {
            salonRepo.deleteCombo(comboId)
            loadCombos()
            _uiState.update { it.copy(infoMessage = "Combo removed") }
        }
    }

    // Staff
    fun openAddStaffDialog() {
        _uiState.update {
            it.copy(
                showAddStaffDialog = true,
                staffFormName = "",
                staffFormCommission = "20",
                staffFormPhotoUrl = null,
                staffFormAllServices = false,
                staffFormServiceIds = emptySet()
            )
        }
    }

    fun closeAddStaffDialog() {
        _uiState.update { it.copy(showAddStaffDialog = false) }
    }

    fun updateStaffFormName(name: String) = _uiState.update { it.copy(staffFormName = name) }
    fun updateStaffFormCommission(commission: String) = _uiState.update { it.copy(staffFormCommission = commission) }
    fun updateStaffFormPhoto(photo: String?) = _uiState.update { it.copy(staffFormPhotoUrl = photo) }
    fun updateStaffFormAllServices(all: Boolean) = _uiState.update { it.copy(staffFormAllServices = all) }
    fun toggleStaffFormService(serviceId: String) = _uiState.update {
        val cur = it.staffFormServiceIds
        it.copy(staffFormServiceIds = if (serviceId in cur) cur - serviceId else cur + serviceId)
    }

    fun saveStaffMember() {
        val s = _uiState.value
        val salonId = s.salon?.id ?: "salon-1"
        val comm = s.staffFormCommission.toDoubleOrNull() ?: 20.0
        if (s.staffFormName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter staff member name") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingStaff = true) }
            val res = salonRepo.addStaffMember(
                salonId = salonId,
                name = s.staffFormName.trim(),
                commissionPercent = comm,
                photoUrl = s.staffFormPhotoUrl,
                doesAllServices = s.staffFormAllServices,
                serviceIds = s.staffFormServiceIds.toList()
            )
            when (res) {
                is SalonResult.Success -> {
                    closeAddStaffDialog()
                    loadStaffAndServices()
                    _uiState.update {
                        it.copy(
                            isSavingStaff = false,
                            infoMessage = SalonStrings.get(it.language, "staff_saved_success")
                        )
                    }
                }
                is SalonResult.Error -> _uiState.update { it.copy(isSavingStaff = false, errorMessage = res.message) }
            }
        }
    }

    fun openStaffDetail(staff: Staff) {
        viewModelScope.launch {
            val hoursRes = salonRepo.getStaffHours(staff.id)
            val hours = if (hoursRes is SalonResult.Success) hoursRes.data else emptyList()
            val assignedSrvs = _uiState.value.servicesList.filter { srv ->
                srv.assignedStaffIds.contains(staff.id)
            }.map { it.id }.toSet()

            _uiState.update {
                it.copy(
                    selectedStaffForDetail = staff,
                    staffHoursList = hours,
                    staffSelectedServiceIds = assignedSrvs,
                    staffDetailAllServices = staff.doesAllServices,
                    staffServiceRemovalWarning = null
                )
            }
        }
    }

    fun closeStaffDetail() {
        _uiState.update { it.copy(selectedStaffForDetail = null) }
    }

    fun updateStaffWorkingHoursDay(dayOfWeek: Int, isWorking: Boolean, start: String, end: String) {
        val current = _uiState.value.staffHoursList.toMutableList()
        val idx = current.indexOfFirst { it.dayOfWeek == dayOfWeek }
        if (idx >= 0) {
            current[idx] = current[idx].copy(isWorking = isWorking, startTime = start, endTime = end)
        } else {
            current.add(StaffHours(staffId = _uiState.value.selectedStaffForDetail?.id ?: "", dayOfWeek = dayOfWeek, isWorking = isWorking, startTime = start, endTime = end))
        }
        _uiState.update { it.copy(staffHoursList = current) }
    }

    fun saveStaffHours() {
        val staff = _uiState.value.selectedStaffForDetail ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingStaffDetails = true) }
            when (val res = salonRepo.saveStaffHours(staff.id, _uiState.value.staffHoursList)) {
                is SalonResult.Success -> _uiState.update {
                    it.copy(isSavingStaffDetails = false, infoMessage = "Working hours saved for ${staff.name}")
                }
                is SalonResult.Error -> _uiState.update { it.copy(isSavingStaffDetails = false, errorMessage = res.message) }
            }
            loadSetupStatus()
        }
    }

    fun toggleStaffServiceAssignmentForStaff(serviceId: String) {
        val current = _uiState.value.staffSelectedServiceIds.toMutableSet()
        if (current.contains(serviceId)) current.remove(serviceId) else current.add(serviceId)
        _uiState.update { it.copy(staffSelectedServiceIds = current) }
    }

    fun setStaffDetailAllServices(all: Boolean) = _uiState.update { it.copy(staffDetailAllServices = all) }

    fun dismissStaffServiceRemovalWarning() = _uiState.update { it.copy(staffServiceRemovalWarning = null) }

    /**
     * Saves a stylist's services. If the change removes services that already have upcoming bookings
     * with this stylist, the owner is asked first ([confirmed] = true skips the question). Existing
     * bookings are never cancelled by this.
     */
    fun saveStaffAssignedServices(confirmed: Boolean = false) {
        val staff = _uiState.value.selectedStaffForDetail ?: return
        val st = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingStaffDetails = true, staffServiceRemovalWarning = null) }
            if (!confirmed && !st.staffDetailAllServices) {
                val before = st.servicesList.filter { it.assignedStaffIds.contains(staff.id) }.map { it.id }
                val removed = before - st.staffSelectedServiceIds
                val affected = (salonRepo.countFutureBookingsFor(staff.id, removed) as? SalonResult.Success)?.data ?: 0
                if (affected > 0) {
                    _uiState.update { it.copy(isSavingStaffDetails = false, staffServiceRemovalWarning = affected) }
                    return@launch
                }
            }
            when (val res = salonRepo.saveStaffServicesForStaff(staff.id, st.staffSelectedServiceIds.toList(), st.staffDetailAllServices)) {
                is SalonResult.Success -> {
                    loadStaffAndServices()
                    _uiState.update {
                        it.copy(
                            isSavingStaffDetails = false,
                            selectedStaffForDetail = staff.copy(doesAllServices = st.staffDetailAllServices),
                            infoMessage = "Services saved for ${staff.name}"
                        )
                    }
                }
                is SalonResult.Error -> _uiState.update { it.copy(isSavingStaffDetails = false, errorMessage = res.message) }
            }
        }
    }

    fun toggleStaffActive(staffId: String, isActive: Boolean) {
        viewModelScope.launch {
            salonRepo.updateStaffMember(staffId = staffId, isActive = isActive)
            loadStaffAndServices()
        }
    }

    // Salon Working Hours
    fun loadSalonHours() {
        val salonId = _uiState.value.salon?.id ?: "salon-1"
        viewModelScope.launch {
            when (val res = salonRepo.getSalonHours(salonId)) {
                is SalonResult.Success -> _uiState.update { it.copy(salonHoursList = res.data) }
                is SalonResult.Error -> {}
            }
        }
    }

    fun updateSalonHoursDay(dayOfWeek: Int, isClosed: Boolean, openTime: String, closeTime: String) {
        val current = _uiState.value.salonHoursList.toMutableList()
        val idx = current.indexOfFirst { it.dayOfWeek == dayOfWeek }
        if (idx >= 0) {
            current[idx] = current[idx].copy(isClosed = isClosed, openTime = openTime, closeTime = closeTime)
        } else {
            current.add(SalonHours(salonId = _uiState.value.salon?.id ?: "salon-1", dayOfWeek = dayOfWeek, isClosed = isClosed, openTime = openTime, closeTime = closeTime))
        }
        _uiState.update { it.copy(salonHoursList = current) }
    }

    fun saveSalonOperatingHours() {
        val salonId = _uiState.value.salon?.id ?: "salon-1"
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingSalonHours = true) }
            when (val res = salonRepo.saveSalonHours(salonId, _uiState.value.salonHoursList)) {
                is SalonResult.Success -> _uiState.update {
                    it.copy(isSavingSalonHours = false, infoMessage = "Salon operating hours updated successfully")
                }
                is SalonResult.Error -> _uiState.update { it.copy(isSavingSalonHours = false, errorMessage = res.message) }
            }
            loadSetupStatus()
        }
    }

    // Booking Settings
    fun initBookingSettings() {
        val s = _uiState.value.salon
        if (s != null) {
            _uiState.update {
                it.copy(
                    settingSlotInterval = s.slotIntervalMinutes,
                    settingBookingWindowDays = s.bookingWindowDays,
                    settingMinNoticeMinutes = s.minNoticeMinutes,
                    settingLateThresholdMinutes = s.lateThresholdMinutes,
                    settingLateCreditAmount = s.lateCreditAmount
                )
            }
        }
    }

    fun updateSlotInterval(interval: Int) = _uiState.update { it.copy(settingSlotInterval = interval) }
    fun updateBookingWindowDays(days: Int) = _uiState.update { it.copy(settingBookingWindowDays = days) }
    fun updateMinNoticeMinutes(mins: Int) = _uiState.update { it.copy(settingMinNoticeMinutes = mins) }
    fun updateLateThresholdMinutes(mins: Int) = _uiState.update { it.copy(settingLateThresholdMinutes = mins) }
    fun updateLateCreditAmount(amount: Double) = _uiState.update { it.copy(settingLateCreditAmount = amount) }

    fun saveBookingSettings() {
        val s = _uiState.value
        val salonId = s.salon?.id ?: "salon-1"
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingSettings = true) }
            val req = UpdateSalonSettingsRequest(
                slotIntervalMinutes = s.settingSlotInterval,
                bookingWindowDays = s.settingBookingWindowDays,
                minNoticeMinutes = s.settingMinNoticeMinutes,
                lateThresholdMinutes = s.settingLateThresholdMinutes,
                lateCreditAmount = s.settingLateCreditAmount
            )
            salonRepo.updateSalonSettings(salonId, req)
            val updatedSalon = s.salon?.copy(
                slotIntervalMinutes = req.slotIntervalMinutes,
                bookingWindowDays = req.bookingWindowDays,
                minNoticeMinutes = req.minNoticeMinutes,
                lateThresholdMinutes = req.lateThresholdMinutes,
                lateCreditAmount = req.lateCreditAmount
            )
            _uiState.update {
                it.copy(
                    isSavingSettings = false,
                    salon = updatedSalon,
                    infoMessage = "Booking settings saved successfully"
                )
            }
        }
    }

    // Payout Details
    fun loadPayoutDetails() {
        val salonId = _uiState.value.salon?.id ?: "salon-1"
        viewModelScope.launch {
            when (val res = salonRepo.getSalonPayoutDetails(salonId)) {
                is SalonResult.Success -> {
                    val p = res.data
                    if (p != null) {
                        _uiState.update {
                            it.copy(
                                payoutDetails = p,
                                payoutHolderName = p.accountHolderName ?: "",
                                payoutUpiId = p.upiId ?: "",
                                payoutAccountNumber = p.bankAccountNumber ?: "",
                                payoutIfsc = p.bankIfsc ?: ""
                            )
                        }
                    }
                }
                is SalonResult.Error -> {}
            }
        }
    }

    fun updatePayoutHolderName(name: String) = _uiState.update { it.copy(payoutHolderName = name) }
    fun updatePayoutUpiId(upi: String) = _uiState.update { it.copy(payoutUpiId = upi) }
    fun updatePayoutAccountNumber(acc: String) = _uiState.update { it.copy(payoutAccountNumber = acc) }
    fun updatePayoutIfsc(ifsc: String) = _uiState.update { it.copy(payoutIfsc = ifsc) }

    fun savePayoutDetails() {
        val s = _uiState.value
        val salonId = s.salon?.id ?: "salon-1"
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingPayout = true) }
            val details = SalonPayoutDetails(
                id = s.payoutDetails?.id ?: "pod-1",
                salonId = salonId,
                accountHolderName = s.payoutHolderName.trim(),
                upiId = s.payoutUpiId.trim(),
                bankAccountNumber = s.payoutAccountNumber.trim(),
                bankIfsc = s.payoutIfsc.trim().uppercase()
            )
            salonRepo.saveSalonPayoutDetails(details)
            _uiState.update {
                it.copy(
                    isSavingPayout = false,
                    payoutDetails = details,
                    infoMessage = SalonStrings.get(it.language, "payout_saved_success")
                )
            }
        }
    }

    // ==========================================
    // PART 5: Earnings (Kamai), Reviews, Notifications, Language & Profile
    // ==========================================

    fun selectEarningsPreset(preset: String) {
        _uiState.update { it.copy(earningsDatePreset = preset) }
        loadEarningsSummary()
    }

    fun setEarningsCustomDates(from: String, to: String) {
        _uiState.update { it.copy(earningsCustomFrom = from, earningsCustomTo = to, earningsDatePreset = "custom") }
        loadEarningsSummary()
    }

    fun loadEarningsSummary() {
        val s = _uiState.value
        val today = getTodayDateString()
        val (from, to) = when (s.earningsDatePreset) {
            "today" -> today to today
            "week" -> getDateOffset(-7) to today
            "month" -> getDateOffset(-30) to today
            "custom" -> {
                val f = s.earningsCustomFrom.ifBlank { getDateOffset(-7) }
                val t = s.earningsCustomTo.ifBlank { today }
                f to t
            }
            else -> today to today
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingEarnings = true, earningsError = null) }
            when (val res = salonRepo.getEarningsSummary(from, to)) {
                is SalonResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingEarnings = false,
                            staffEarnings = res.data,
                            earningsError = null
                        )
                    }
                }
                is SalonResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingEarnings = false,
                            earningsError = res.message
                        )
                    }
                }
            }
        }
    }

    fun loadSalonReviews() {
        val salonId = _uiState.value.salon?.id ?: "salon-1"
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingReviews = true, reviewsError = null) }
            when (val res = salonRepo.getSalonReviews(salonId)) {
                is SalonResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingReviews = false,
                            salonReviews = res.data,
                            reviewsError = null
                        )
                    }
                }
                is SalonResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingReviews = false,
                            reviewsError = res.message
                        )
                    }
                }
            }
        }
    }

    fun startReplyToReview(reviewId: String) {
        _uiState.update {
            it.copy(
                replyingReviewId = reviewId,
                replyInputText = "",
                replyError = null
            )
        }
    }

    fun cancelReplyToReview() {
        _uiState.update {
            it.copy(
                replyingReviewId = null,
                replyInputText = "",
                replyError = null
            )
        }
    }

    fun updateReplyInputText(text: String) {
        _uiState.update { it.copy(replyInputText = text, replyError = null) }
    }

    fun submitReviewReply() {
        val s = _uiState.value
        val reviewId = s.replyingReviewId ?: return
        val reply = s.replyInputText.trim()
        if (reply.isBlank()) {
            _uiState.update { it.copy(replyError = "Please enter your reply.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingReply = true, replyError = null) }
            when (val res = salonRepo.replyToReview(reviewId, reply)) {
                is SalonResult.Success -> {
                    val updatedReviews = s.salonReviews.map { r ->
                        if (r.id == reviewId) r.copy(ownerReply = reply) else r
                    }
                    _uiState.update {
                        it.copy(
                            isSubmittingReply = false,
                            replyingReviewId = null,
                            replyInputText = "",
                            salonReviews = updatedReviews,
                            infoMessage = "Reply posted successfully!"
                        )
                    }
                    loadDashboard()
                }
                is SalonResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingReply = false,
                            replyError = res.message
                        )
                    }
                }
            }
        }
    }

    fun loadNotifications() {
        val userId = _uiState.value.user?.id ?: "owner-1"
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingNotifications = true) }
            when (val res = salonRepo.getNotifications(userId)) {
                is SalonResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingNotifications = false,
                            notificationsList = res.data
                        )
                    }
                }
                is SalonResult.Error -> {
                    _uiState.update { it.copy(isLoadingNotifications = false) }
                }
            }
        }
    }

    fun openNotificationsSheet() {
        _uiState.update { it.copy(showNotificationsSheet = true) }
        loadNotifications()
    }

    fun closeNotificationsSheet() {
        _uiState.update { it.copy(showNotificationsSheet = false) }
    }

    fun markNotificationAsRead(notificationId: String, bookingId: String? = null) {
        viewModelScope.launch {
            salonRepo.markNotificationRead(notificationId)
            val updated = _uiState.value.notificationsList.map {
                if (it.id == notificationId) it.copy(isRead = true) else it
            }
            _uiState.update { it.copy(notificationsList = updated) }

            if (!bookingId.isNullOrBlank()) {
                closeNotificationsSheet()
                openBookingDetailById(bookingId)
            }
        }
    }

    fun markAllNotificationsRead() {
        val salonId = _uiState.value.salon?.id ?: return
        viewModelScope.launch {
            salonRepo.markAllNotificationsRead(salonId)
            val updated = _uiState.value.notificationsList.map { it.copy(isRead = true) }
            _uiState.update { it.copy(notificationsList = updated) }
        }
    }


    private fun openBookingDetailById(bookingId: String) {
        val allBookings = _uiState.value.todayBookings + _uiState.value.upcomingBookings + _uiState.value.pastBookings
        val booking = allBookings.find { it.id == bookingId }
        if (booking != null) {
            _uiState.update {
                it.copy(
                    selectedBookingForDetail = booking,
                    showBookingDetailSheet = true
                )
            }
        }
    }

    fun openProfileModal() {
        _uiState.update { it.copy(showProfileModal = true) }
    }

    fun closeProfileModal() {
        _uiState.update { it.copy(showProfileModal = false) }
    }

    fun changeAppLanguage(newLang: String) {
        val userId = _uiState.value.user?.id ?: "owner-1"
        _uiState.update { it.copy(language = newLang) }
        viewModelScope.launch {
            salonRepo.updateProfileLanguage(userId, newLang)
        }
    }

    override fun onCleared() {
        super.onCleared()
        realtimeSyncJob?.cancel()
    }
}

