package com.example.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.auth.AuthUiState
import com.example.ui.auth.AuthViewModel
import com.example.ui.main.salon.SalonBookingSettingsSection
import com.example.ui.main.salon.SalonCombosSection
import com.example.ui.main.salon.SalonPayoutDetailsSection
import com.example.ui.main.salon.SalonProfileSection
import com.example.ui.main.salon.SalonReviewsSection
import com.example.ui.main.salon.SalonServicesSection
import com.example.ui.main.salon.SalonStaffSection
import com.example.ui.main.salon.SalonWorkingHoursSection
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TerracottaPrimary
import com.example.util.SalonStrings

@Composable
fun SalonSettingsTab(
    state: AuthUiState,
    viewModel: AuthViewModel = viewModel(),
    onToggleSalonActive: (Boolean) -> Unit,
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
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = state.language
    val salon = state.salon
    val isActive = state.editSalonIsActive

    // Route to sub-section if not in "menu"
    when (state.salonActiveSection) {
        "profile" -> {
            SalonProfileSection(
                state = state,
                viewModel = viewModel,
                onBack = { viewModel.setSalonSection("menu") }
            )
            return
        }
        "services" -> {
            SalonServicesSection(
                state = state,
                viewModel = viewModel,
                onBack = { viewModel.setSalonSection("menu") }
            )
            return
        }
        "combos" -> {
            SalonCombosSection(
                state = state,
                viewModel = viewModel,
                onBack = { viewModel.setSalonSection("menu") }
            )
            return
        }
        "staff" -> {
            SalonStaffSection(
                state = state,
                viewModel = viewModel,
                onBack = { viewModel.setSalonSection("menu") }
            )
            return
        }
        "working_hours" -> {
            SalonWorkingHoursSection(
                state = state,
                viewModel = viewModel,
                onBack = { viewModel.setSalonSection("menu") }
            )
            return
        }
        "time_off" -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.setSalonSection("menu") }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = SalonStrings.get(lang, "section_time_off"),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TimeOffSection(
                    state = state,
                    onOpenAddBreak = onOpenAddBreak,
                    onDeleteBreak = onDeleteBreak,
                    onOpenAddTimeOff = onOpenAddTimeOff,
                    onDeleteTimeOff = onDeleteTimeOff
                )
            }

            // Dialogs for Time Off
            if (state.showAddRecurringBreakDialog) {
                AddRecurringBreakDialog(
                    staffList = state.staffList,
                    language = lang,
                    onClose = onCloseAddBreak,
                    onSubmit = onSubmitAddBreak
                )
            }
            if (state.showAddTimeOffDialog) {
                AddStaffTimeOffDialog(
                    staffList = state.staffList,
                    language = lang,
                    onClose = onCloseAddTimeOff,
                    onSubmit = onPrepareAddTimeOff
                )
            }
            if (state.showConflictsDialog) {
                ConflictsWarningDialog(
                    conflictingBookings = state.conflictingBookings,
                    language = lang,
                    onCancel = onCloseConflictsDialog,
                    onConfirmAndCancelBookings = { onConfirmAddTimeOff(true) }
                )
            }
            return
        }
        "booking_settings" -> {
            SalonBookingSettingsSection(
                state = state,
                viewModel = viewModel,
                onBack = { viewModel.setSalonSection("menu") }
            )
            return
        }
        "payout_details" -> {
            SalonPayoutDetailsSection(
                state = state,
                viewModel = viewModel,
                onBack = { viewModel.setSalonSection("menu") }
            )
            return
        }
        "reviews" -> {
            SalonReviewsSection(
                state = state,
                viewModel = viewModel,
                onBack = { viewModel.setSalonSection("menu") }
            )
            return
        }
    }


    // Default: Sectioned Navigation Menu Screen
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("salon_settings_menu_screen")
    ) {
        // Salon Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = salon?.name ?: "Salon Partner",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (salon?.verificationStatus == "approved") {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Rating info
                        val rating = salon?.ratingAvg ?: 0.0
                        val count = salon?.ratingCount ?: 0
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.setSalonSection("reviews") }
                                .padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (count == 0) "New" else "%.1f".format(rating),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "($count ${SalonStrings.get(lang, "reviews_count")})",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "· ${salon?.salonType?.replaceFirstChar { it.uppercase() } ?: "Unisex"}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                    }

                    // Salon status toggle
                    Column(horizontalAlignment = Alignment.End) {
                        Switch(
                            checked = isActive,
                            onCheckedChange = {
                                viewModel.toggleSalonActiveState(it)
                                onToggleSalonActive(it)
                            },
                            modifier = Modifier.testTag("switch_salon_active_hero"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SuccessGreen
                            )
                        )
                        Text(
                            text = if (isActive) SalonStrings.get("active", lang) else SalonStrings.get("inactive", lang),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isActive) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                // Address & Phone
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${salon?.address ?: "Market Road"}, ${salon?.area ?: "Central"}, ${salon?.city ?: "Mumbai"} - ${salon?.pincode ?: "400001"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+91 ${salon?.phone ?: "9876543210"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTIONED MENU LIST
        Text(
            text = "SALON CONFIGURATION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                // 1. Salon Profile
                SalonMenuRow(
                    icon = Icons.Default.Storefront,
                    title = SalonStrings.get(lang, "section_salon_profile"),
                    subtitle = SalonStrings.get(lang, "section_salon_profile_desc"),
                    testTag = "menu_salon_profile",
                    onClick = { viewModel.setSalonSection("profile") }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), modifier = Modifier.padding(start = 56.dp))

                // 2. Services & Categories
                SalonMenuRow(
                    icon = Icons.Default.ContentCut,
                    title = SalonStrings.get(lang, "section_services"),
                    subtitle = SalonStrings.get(lang, "section_services_desc"),
                    testTag = "menu_services_categories",
                    badge = "${state.servicesList.size} services",
                    onClick = { viewModel.setSalonSection("services") }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), modifier = Modifier.padding(start = 56.dp))

                // 3. Combos / Packages
                SalonMenuRow(
                    icon = Icons.Default.Inventory2,
                    title = SalonStrings.get(lang, "section_combos"),
                    subtitle = SalonStrings.get(lang, "section_combos_desc"),
                    testTag = "menu_combos_packages",
                    badge = "${state.combosList.size} deals",
                    onClick = { viewModel.setSalonSection("combos") }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), modifier = Modifier.padding(start = 56.dp))

                // 4. Staff
                SalonMenuRow(
                    icon = Icons.Default.People,
                    title = SalonStrings.get(lang, "section_staff"),
                    subtitle = SalonStrings.get(lang, "section_staff_desc"),
                    testTag = "menu_staff_team",
                    badge = "${state.staffList.size} stylists",
                    onClick = { viewModel.setSalonSection("staff") }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), modifier = Modifier.padding(start = 56.dp))

                // 5. Working Hours
                SalonMenuRow(
                    icon = Icons.Default.AccessTime,
                    title = SalonStrings.get(lang, "section_working_hours"),
                    subtitle = SalonStrings.get(lang, "section_working_hours_desc"),
                    testTag = "menu_working_hours",
                    onClick = { viewModel.setSalonSection("working_hours") }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), modifier = Modifier.padding(start = 56.dp))

                // 6. Time Off
                SalonMenuRow(
                    icon = Icons.Default.EventBusy,
                    title = SalonStrings.get(lang, "section_time_off"),
                    subtitle = SalonStrings.get(lang, "section_time_off_desc"),
                    testTag = "menu_time_off",
                    onClick = { viewModel.setSalonSection("time_off") }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), modifier = Modifier.padding(start = 56.dp))

                // 7. Booking Settings
                SalonMenuRow(
                    icon = Icons.Default.Tune,
                    title = SalonStrings.get(lang, "section_booking_settings"),
                    subtitle = SalonStrings.get(lang, "section_booking_settings_desc"),
                    testTag = "menu_booking_settings",
                    onClick = { viewModel.setSalonSection("booking_settings") }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), modifier = Modifier.padding(start = 56.dp))

                // 8. Payout Details
                SalonMenuRow(
                    icon = Icons.Default.AccountBalance,
                    title = SalonStrings.get(lang, "section_payout_details"),
                    subtitle = SalonStrings.get(lang, "section_payout_details_desc"),
                    testTag = "menu_payout_details",
                    onClick = { viewModel.setSalonSection("payout_details") }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), modifier = Modifier.padding(start = 56.dp))

                // 9. Reviews & Ratings
                val pendingReplies = state.salonReviews.count { it.ownerReply.isNullOrBlank() }
                SalonMenuRow(
                    icon = Icons.Default.Star,
                    title = SalonStrings.get(lang, "section_reviews"),
                    subtitle = SalonStrings.get(lang, "section_reviews_desc"),
                    testTag = "menu_reviews_ratings",
                    badge = if (pendingReplies > 0) "$pendingReplies pending" else "${state.salonReviews.size} reviews",
                    onClick = { viewModel.setSalonSection("reviews") }
                )
            }

        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign Out Button
        OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("salon_sign_out_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(SalonStrings.get("sign_out", lang), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun SalonMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    testTag: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(TerracottaPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TerracottaPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (badge != null) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
    }
}
