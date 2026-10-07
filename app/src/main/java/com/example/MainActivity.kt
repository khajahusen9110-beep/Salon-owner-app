package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.auth.AuthViewModel
import com.example.ui.auth.LoginScreen
import com.example.ui.auth.SignUpScreen
import com.example.ui.components.SupabaseConfigDialog
import com.example.ui.main.MainAppShell
import com.example.ui.registration.SalonRegistrationScreen
import com.example.ui.status.PendingScreen
import com.example.ui.status.RejectedScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SalonOwnerApp()
                }
            }
        }
    }
}

@Composable
fun SalonOwnerApp(
    viewModel: AuthViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state.showConfigDialog) {
        SupabaseConfigDialog(
            onDismiss = { viewModel.toggleConfigDialog(false) }
        )
    }

    Crossfade(targetState = state.destinationRoute, label = "RouteTransition") { route ->
        when (route) {
            "login" -> LoginScreen(
                state = state,
                onLogin = { email, pass -> viewModel.login(email, pass) },
                onNavigateToSignUp = { viewModel.navigateToSignUp() },
                onLanguageChange = { lang -> viewModel.setLanguage(lang) },
                onOpenConfig = { viewModel.toggleConfigDialog(true) }
            )

            "signup" -> SignUpScreen(
                state = state,
                onSignUp = { email, pass, confirm -> viewModel.signUp(email, pass, confirm) },
                onNavigateToLogin = { viewModel.navigateToLogin() },
                onLanguageChange = { lang -> viewModel.setLanguage(lang) }
            )

            "register" -> SalonRegistrationScreen(
                state = state,
                onOwnerNameChange = { name -> viewModel.updateOwnerName(name) },
                onRegLanguageChange = { lang -> viewModel.updateRegLanguage(lang) },
                onSalonDetailsChange = { name, type, address, area, city, pincode, phone, lat, lng, gst ->
                    viewModel.updateSalonDetails(name, type, address, area, city, pincode, phone, lat, lng, gst)
                },
                onDocTypeChange = { docType -> viewModel.updateDocType(docType) },
                onSelectDocument = { name, bytes, mimeType ->
                    viewModel.selectDocument(name, bytes, mimeType)
                },
                onAcceptTermsChange = { accept -> viewModel.updateAcceptTerms(accept) },
                onNextStep = { viewModel.goToNextStep() },
                onPreviousStep = { viewModel.goToPreviousStep() },
                onSubmitVerification = { viewModel.submitVerification() },
                onSignOut = { viewModel.signOut() }
            )

            "pending" -> PendingScreen(
                state = state,
                onRefreshStatus = { viewModel.refreshStatus() },
                onSignOut = { viewModel.signOut() },
                onSimulateApproval = { viewModel.simulateStatusChange("approved") },
                onSimulateRejection = { viewModel.simulateStatusChange("rejected", "Shop license photo was unclear. Please upload a high-resolution copy.") }
            )

            "rejected" -> RejectedScreen(
                state = state,
                onEditAndResubmit = { viewModel.editAndResubmit() },
                onSignOut = { viewModel.signOut() }
            )

            "main" -> MainAppShell(
                state = state,
                onLanguageChange = { lang -> viewModel.setLanguage(lang) },
                onSignOut = { viewModel.signOut() },
                onSelectTab = { tab -> viewModel.selectTab(tab) },
                onOpenWalkInModal = { viewModel.openWalkInModal() },
                onCloseWalkInModal = { viewModel.closeWalkInModal() },
                onUpdateWalkInForm = { staffId, serviceId, name, phone ->
                    viewModel.updateWalkInForm(staffId, serviceId, name, phone)
                },
                onSubmitWalkIn = { viewModel.submitWalkIn() },
                onToggleSalonActive = { isActive -> viewModel.toggleSalonActive(isActive) },
                // Part 3: Bookings List & Detail
                onSubTabChange = { tab -> viewModel.setBookingsSubTab(tab) },
                onStaffFilterSelected = { staffId -> viewModel.selectStaffFilter(staffId) },
                onStatusChange = { bookingId, newStatus -> viewModel.updateBookingStatus(bookingId, newStatus) },
                onOpenBookingDetail = { booking -> viewModel.openBookingDetail(booking) },
                onCloseBookingDetail = { viewModel.closeBookingDetail() },
                onViewCustomerHistory = { bookingId -> viewModel.viewCustomerHistory(bookingId) },
                onCloseCustomerHistory = { viewModel.closeCustomerHistory() },
                onOpenReschedule = { viewModel.openRescheduleDialog() },
                onCloseReschedule = { viewModel.closeRescheduleDialog() },
                onSelectRescheduleDate = { date -> viewModel.setRescheduleDate(date) },
                onSelectRescheduleTime = { time -> viewModel.setRescheduleTime(time) },
                onSubmitReschedule = { viewModel.submitReschedule() },
                onOpenCancelBookingConfirm = { viewModel.openCancelBookingDialog() },
                onCloseCancelBookingConfirm = { viewModel.closeCancelBookingDialog() },
                onConfirmCancelBooking = { viewModel.confirmCancelBooking() },
                // Part 3: Time Off & Breaks
                onOpenAddBreak = { viewModel.openAddRecurringBreakDialog() },
                onCloseAddBreak = { viewModel.closeAddRecurringBreakDialog() },
                onSubmitAddBreak = { staffId, dayOfWeek, start, end, label ->
                    viewModel.submitAddRecurringBreak(staffId, dayOfWeek, start, end, label)
                },
                onDeleteBreak = { breakId -> viewModel.deleteRecurringBreak(breakId) },
                onOpenAddTimeOff = { viewModel.openAddTimeOffDialog() },
                onCloseAddTimeOff = { viewModel.closeAddTimeOffDialog() },
                onPrepareAddTimeOff = { staffId, start, end, reason ->
                    viewModel.prepareAddTimeOff(staffId, start, end, reason)
                },
                onConfirmAddTimeOff = { cancelConflicts -> viewModel.confirmAddTimeOff(cancelConflicts) },
                onCloseConflictsDialog = { viewModel.closeConflictsDialog() },
                onDeleteTimeOff = { timeOffId -> viewModel.deleteStaffTimeOff(timeOffId) },
                // System
                onRefreshAllData = { viewModel.refreshAllTodayData() },
                onClearMessages = { viewModel.clearMessages() },
                viewModel = viewModel
            )

            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Salon Owner") }
}
