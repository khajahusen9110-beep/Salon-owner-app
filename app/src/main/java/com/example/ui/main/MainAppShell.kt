package com.example.ui.main

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Booking
import com.example.ui.auth.AuthUiState
import com.example.ui.auth.LanguagePillItem
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.TerracottaPrimary
import com.example.util.SalonStrings

enum class SalonAppTab(val id: String, val icon: ImageVector, val labelKey: String) {
    TODAY("today", Icons.Default.Home, "tab_today"),
    BOOKINGS("bookings", Icons.Default.CalendarMonth, "tab_bookings"),
    SALON("salon", Icons.Default.Store, "tab_salon"),
    EARNINGS("earnings", Icons.Default.AccountBalanceWallet, "tab_earnings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppShell(
    state: AuthUiState,
    onLanguageChange: (String) -> Unit,
    onSignOut: () -> Unit,
    onSelectTab: (String) -> Unit,
    // Walk In
    onOpenWalkInModal: () -> Unit,
    onCloseWalkInModal: () -> Unit,
    onUpdateWalkInForm: (staffId: String?, serviceId: String?, name: String?, phone: String?) -> Unit,
    onSubmitWalkIn: () -> Unit,
    onToggleSalonActive: (Boolean) -> Unit,
    // Schedule / Bookings
    onSubTabChange: (String) -> Unit,
    onStaffFilterSelected: (String) -> Unit,
    onStatusChange: (bookingId: String, newStatus: String) -> Unit,
    onOpenBookingDetail: (Booking) -> Unit,
    onCloseBookingDetail: () -> Unit,
    onViewCustomerHistory: (String) -> Unit,
    onCloseCustomerHistory: () -> Unit,
    onOpenReschedule: () -> Unit,
    onCloseReschedule: () -> Unit,
    onSelectRescheduleDate: (String) -> Unit,
    onSelectRescheduleTime: (String) -> Unit,
    onSubmitReschedule: () -> Unit,
    onOpenCancelBookingConfirm: () -> Unit,
    onCloseCancelBookingConfirm: () -> Unit,
    onConfirmCancelBooking: () -> Unit,
    // Time Off & Breaks
    onOpenAddBreak: () -> Unit,
    onCloseAddBreak: () -> Unit,
    onSubmitAddBreak: (staffId: String?, dayOfWeek: Int?, startTime: String, endTime: String, label: String) -> Unit,
    onDeleteBreak: (String) -> Unit,
    onOpenAddTimeOff: () -> Unit,
    onCloseAddTimeOff: () -> Unit,
    onPrepareAddTimeOff: (staffId: String?, startTime: String, endTime: String, reason: String?) -> Unit,
    onConfirmAddTimeOff: (cancelConflicts: Boolean) -> Unit,
    onCloseConflictsDialog: () -> Unit,
    onDeleteTimeOff: (String) -> Unit,
    // System
    onRefreshAllData: () -> Unit,
    onClearMessages: () -> Unit,
    viewModel: com.example.ui.auth.AuthViewModel? = null,
    modifier: Modifier = Modifier
) {
    val lang = state.language
    val salon = state.salon
    val salonName = state.dashboard?.salonName ?: salon?.name ?: "Looks Unisex Salon"
    val snackbarHostState = remember { SnackbarHostState() }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showProfileModal by remember { mutableStateOf(false) }

    val bookingSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)


    // Surface errors or info messages via snackbar
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onClearMessages()
        }
    }

    LaunchedEffect(state.infoMessage) {
        state.infoMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onClearMessages()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showProfileModal = true }
                            .padding(vertical = 4.dp)
                    ) {
                        Column {
                            Text(
                                text = salonName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (state.dashboard?.isActive == false) ErrorRed else Color(0xFF2E7D32))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (state.dashboard?.isActive == false) "Salon OFF" else "Salon OPEN",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (state.dashboard?.isActive == false) ErrorRed else Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Language Switcher
                    Surface(
                        modifier = Modifier.padding(end = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(modifier = Modifier.padding(2.dp)) {
                            LanguagePillItem(
                                label = "EN",
                                selected = lang == "en",
                                onClick = { onLanguageChange("en") }
                            )
                            LanguagePillItem(
                                label = "हिन्दी",
                                selected = lang == "hi",
                                onClick = { onLanguageChange("hi") }
                            )
                        }
                    }

                    // Notification Bell (Live unread notifications + review replies)
                    val unreadCount = state.unreadNotificationCount.coerceAtLeast(state.dashboard?.pendingReviewsReply ?: 0)
                    IconButton(
                        onClick = { showNotificationsDialog = true },
                        modifier = Modifier.testTag("notification_bell_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(containerColor = TerracottaPrimary) {
                                        Text(text = unreadCount.toString(), color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Owner Avatar Profile Button
                    IconButton(
                        onClick = { showProfileModal = true },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("owner_avatar_top_btn")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(TerracottaPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (state.profile?.fullName?.take(1) ?: salonName.take(1)).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )

                        }
                    }
                }

            )
        },
        bottomBar = {
            // 4 Bottom Tabs Navigation
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                SalonAppTab.values().forEach { tab ->
                    val isSelected = state.selectedTab == tab.id
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { onSelectTab(tab.id) },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = SalonStrings.get(tab.labelKey, lang)
                            )
                        },
                        label = {
                            Text(
                                text = SalonStrings.get(tab.labelKey, lang),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TerracottaPrimary,
                            selectedTextColor = TerracottaPrimary,
                            indicatorColor = TerracottaPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("tab_${tab.id}")
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Crossfade(targetState = state.selectedTab, label = "TabTransition") { tabId ->
                when (tabId) {
                    "today" -> DashboardTab(
                        state = state,
                        onOpenWalkInModal = onOpenWalkInModal,
                        onViewSchedule = { onSelectTab("bookings") },
                        onToggleSalonActive = onToggleSalonActive,
                        onRefresh = onRefreshAllData,
                        onOpenReviews = {
                            onSelectTab("salon")
                            viewModel?.setSalonSection("reviews")
                        }
                    )


                    "bookings" -> ScheduleTab(
                        state = state,
                        onSubTabChange = onSubTabChange,
                        onStaffFilterSelected = onStaffFilterSelected,
                        onStatusChange = onStatusChange,
                        onOpenBookingDetail = onOpenBookingDetail,
                        onOpenWalkInModal = onOpenWalkInModal,
                        onRefresh = onRefreshAllData
                    )

                    "salon" -> if (viewModel != null) {
                        SalonSettingsTab(
                            state = state,
                            viewModel = viewModel,
                            onToggleSalonActive = onToggleSalonActive,
                            onOpenAddBreak = onOpenAddBreak,
                            onCloseAddBreak = onCloseAddBreak,
                            onSubmitAddBreak = onSubmitAddBreak,
                            onDeleteBreak = onDeleteBreak,
                            onOpenAddTimeOff = onOpenAddTimeOff,
                            onCloseAddTimeOff = onCloseAddTimeOff,
                            onPrepareAddTimeOff = onPrepareAddTimeOff,
                            onConfirmAddTimeOff = onConfirmAddTimeOff,
                            onCloseConflictsDialog = onCloseConflictsDialog,
                            onDeleteTimeOff = onDeleteTimeOff,
                            onSignOut = onSignOut
                        )
                    } else {
                        SalonSettingsTab(
                            state = state,
                            onToggleSalonActive = onToggleSalonActive,
                            onOpenAddBreak = onOpenAddBreak,
                            onCloseAddBreak = onCloseAddBreak,
                            onSubmitAddBreak = onSubmitAddBreak,
                            onDeleteBreak = onDeleteBreak,
                            onOpenAddTimeOff = onOpenAddTimeOff,
                            onCloseAddTimeOff = onCloseAddTimeOff,
                            onPrepareAddTimeOff = onPrepareAddTimeOff,
                            onConfirmAddTimeOff = onConfirmAddTimeOff,
                            onCloseConflictsDialog = onCloseConflictsDialog,
                            onDeleteTimeOff = onDeleteTimeOff,
                            onSignOut = onSignOut
                        )
                    }

                    "earnings" -> EarningsTab(
                        state = state,
                        onSelectPreset = { preset -> viewModel?.selectEarningsPreset(preset) },
                        onApplyCustomRange = { from, to -> viewModel?.setEarningsCustomDates(from, to) },
                        onRefresh = { viewModel?.loadEarningsSummary() }
                    )

                }
            }

            // Booking Detail Sheet (Part 3)
            val selectedBooking = state.selectedBookingForDetail
            if (state.showBookingDetailSheet && selectedBooking != null) {
                BookingDetailModal(
                    state = state,
                    booking = selectedBooking,
                    onClose = onCloseBookingDetail,
                    onViewHistory = { onViewCustomerHistory(selectedBooking.id) },
                    onCloseHistory = onCloseCustomerHistory,
                    onOpenReschedule = onOpenReschedule,
                    onCloseReschedule = onCloseReschedule,
                    onSelectRescheduleDate = onSelectRescheduleDate,
                    onSelectRescheduleTime = onSelectRescheduleTime,
                    onSubmitReschedule = onSubmitReschedule,
                    onOpenCancelConfirm = onOpenCancelBookingConfirm,
                    onCloseCancelConfirm = onCloseCancelBookingConfirm,
                    onConfirmCancel = onConfirmCancelBooking,
                    sheetState = bookingSheetState
                )
            }

            // Walk-in modal
            WalkInModal(
                isOpen = state.isWalkInModalOpen,
                staffList = state.staffList,
                servicesList = state.servicesList,
                selectedStaffId = state.walkInStaffId,
                selectedServiceId = state.walkInServiceId,
                customerName = state.walkInCustomerName,
                customerPhone = state.walkInCustomerPhone,
                error = state.walkInError,
                isSubmitting = state.isSubmittingWalkIn,
                language = lang,
                onStaffChange = { onUpdateWalkInForm(it, null, null, null) },
                onServiceChange = { onUpdateWalkInForm(null, it, null, null) },
                onNameChange = { onUpdateWalkInForm(null, null, it, null) },
                onPhoneChange = { onUpdateWalkInForm(null, null, null, it) },
                onSubmit = onSubmitWalkIn,
                onDismiss = onCloseWalkInModal
            )

            // Notifications Modal
            if (showNotificationsDialog) {
                NotificationsModal(
                    state = state,
                    onDismiss = { showNotificationsDialog = false },
                    onMarkAsRead = { notifId -> viewModel?.markNotificationAsRead(notifId) },
                    onMarkAllAsRead = { viewModel?.markAllNotificationsRead() },
                    onNavigateToBookings = {
                        showNotificationsDialog = false
                        onSelectTab("bookings")
                    },
                    onNavigateToReviews = {
                        showNotificationsDialog = false
                        onSelectTab("salon")
                        viewModel?.setSalonSection("reviews")
                    }
                )
            }

            // Profile & Settings Modal
            if (showProfileModal || state.showProfileModal) {
                ProfileSettingsModal(
                    state = state,
                    onDismiss = {
                        showProfileModal = false
                        viewModel?.closeProfileModal()
                    },
                    onLanguageChange = onLanguageChange,
                    onToggleSalonActive = onToggleSalonActive,
                    onLogout = onSignOut
                )
            }
        }
    }
}

