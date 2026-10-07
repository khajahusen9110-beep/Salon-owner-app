package com.example.ui.main.salon

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.auth.AuthUiState
import com.example.ui.auth.AuthViewModel
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TerracottaPrimary
import com.example.util.SalonStrings

// ==========================================
// 1. SALON WORKING HOURS SECTION
// ==========================================
@Composable
fun SalonWorkingHoursSection(
    state: AuthUiState,
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = state.language
    val daysOfWeek = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("salon_hours_screen")
    ) {
        // Back Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_to_menu")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = SalonStrings.get(lang, "working_hours_title"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = SalonStrings.get(lang, "working_hours_subtitle"),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Important Note Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = TerracottaPrimary.copy(alpha = 0.08f))
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "A stylist's booking slots are limited to both the salon's hours AND their own working hours — whichever is shorter.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7 Days Working Hours (Sunday to Saturday)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                for (day in 0..6) {
                    val entry = state.salonHoursList.find { it.dayOfWeek == day }
                    val isClosed = entry?.isClosed ?: false
                    val openTime = entry?.openTime ?: "09:00"
                    val closeTime = entry?.closeTime ?: "21:00"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.width(90.dp)) {
                            Text(
                                text = daysOfWeek[day],
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (!isClosed) "Open" else "Closed",
                                fontSize = 11.sp,
                                color = if (!isClosed) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = !isClosed,
                            onCheckedChange = { isOpen ->
                                viewModel.updateSalonHoursDay(day, !isOpen, openTime, closeTime)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SuccessGreen
                            )
                        )

                        if (!isClosed) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TimePickerDropdown(
                                    time = openTime,
                                    onTimeSelected = { newOpen ->
                                        viewModel.updateSalonHoursDay(day, false, newOpen, closeTime)
                                    }
                                )
                                Text(" - ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                TimePickerDropdown(
                                    time = closeTime,
                                    onTimeSelected = { newClose ->
                                        viewModel.updateSalonHoursDay(day, false, openTime, newClose)
                                    }
                                )
                            }
                        } else {
                            Text(
                                text = "Closed All Day",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                        }
                    }

                    if (day < 6) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.saveSalonOperatingHours() },
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("btn_save_salon_hours"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
            enabled = !state.isSavingSalonHours
        ) {
            if (state.isSavingSalonHours) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text(SalonStrings.get(lang, "save_salon_hours"), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

// ==========================================
// 2. SALON BOOKING SETTINGS SECTION
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalonBookingSettingsSection(
    state: AuthUiState,
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = state.language
    var slotIntervalExpanded by remember { mutableStateOf(false) }
    var windowExpanded by remember { mutableStateOf(false) }
    var noticeExpanded by remember { mutableStateOf(false) }
    var lateThresholdExpanded by remember { mutableStateOf(false) }

    val slotIntervals = listOf(15, 30, 45, 60)
    val bookingWindows = listOf(3, 7, 14, 30, 60)
    val minNotices = listOf(0, 15, 30, 60, 120)
    val lateThresholds = listOf(10, 15, 20, 30)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("booking_settings_screen")
    ) {
        // Back Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_to_menu")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = SalonStrings.get(lang, "booking_settings_title"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = SalonStrings.get(lang, "booking_settings_subtitle"),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Slot Interval
                ExposedDropdownMenuBox(
                    expanded = slotIntervalExpanded,
                    onExpandedChange = { slotIntervalExpanded = !slotIntervalExpanded }
                ) {
                    OutlinedTextField(
                        value = "${state.settingSlotInterval} minutes",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(SalonStrings.get(lang, "slot_interval_label")) },
                        supportingText = { Text(SalonStrings.get(lang, "slot_interval_hint"), fontSize = 10.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = slotIntervalExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = slotIntervalExpanded,
                        onDismissRequest = { slotIntervalExpanded = false }
                    ) {
                        slotIntervals.forEach { i ->
                            DropdownMenuItem(
                                text = { Text("$i minutes") },
                                onClick = {
                                    viewModel.updateSlotInterval(i)
                                    slotIntervalExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Booking Window Days
                ExposedDropdownMenuBox(
                    expanded = windowExpanded,
                    onExpandedChange = { windowExpanded = !windowExpanded }
                ) {
                    OutlinedTextField(
                        value = "${state.settingBookingWindowDays} days in advance",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(SalonStrings.get(lang, "booking_window_label")) },
                        supportingText = { Text(SalonStrings.get(lang, "booking_window_hint"), fontSize = 10.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = windowExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = windowExpanded,
                        onDismissRequest = { windowExpanded = false }
                    ) {
                        bookingWindows.forEach { w ->
                            DropdownMenuItem(
                                text = { Text("$w days") },
                                onClick = {
                                    viewModel.updateBookingWindowDays(w)
                                    windowExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Minimum Notice Minutes
                ExposedDropdownMenuBox(
                    expanded = noticeExpanded,
                    onExpandedChange = { noticeExpanded = !noticeExpanded }
                ) {
                    OutlinedTextField(
                        value = if (state.settingMinNoticeMinutes == 0) "Immediate (No notice required)" else "${state.settingMinNoticeMinutes} minutes",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(SalonStrings.get(lang, "min_notice_label")) },
                        supportingText = { Text(SalonStrings.get(lang, "min_notice_hint"), fontSize = 10.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = noticeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = noticeExpanded,
                        onDismissRequest = { noticeExpanded = false }
                    ) {
                        minNotices.forEach { n ->
                            DropdownMenuItem(
                                text = { Text(if (n == 0) "Immediate" else "$n minutes") },
                                onClick = {
                                    viewModel.updateMinNoticeMinutes(n)
                                    noticeExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Zero-Wait Promise / Late Arrival Credit
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = SalonStrings.get(lang, "zero_wait_title"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TerracottaPrimary
                )
                Text(
                    text = SalonStrings.get(lang, "zero_wait_desc"),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Late threshold
                ExposedDropdownMenuBox(
                    expanded = lateThresholdExpanded,
                    onExpandedChange = { lateThresholdExpanded = !lateThresholdExpanded }
                ) {
                    OutlinedTextField(
                        value = "${state.settingLateThresholdMinutes} minutes delay",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Delay Threshold") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lateThresholdExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = lateThresholdExpanded,
                        onDismissRequest = { lateThresholdExpanded = false }
                    ) {
                        lateThresholds.forEach { t ->
                            DropdownMenuItem(
                                text = { Text("$t minutes") },
                                onClick = {
                                    viewModel.updateLateThresholdMinutes(t)
                                    lateThresholdExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Credit amount
                OutlinedTextField(
                    value = "${state.settingLateCreditAmount.toInt()}",
                    onValueChange = {
                        val amt = it.toDoubleOrNull() ?: 0.0
                        viewModel.updateLateCreditAmount(amt)
                    },
                    label = { Text("Compensation Credit to Customer") },
                    prefix = { Text("₹ ") },
                    supportingText = { Text("Set to 0 to disable automatic zero-wait compensation credit.", fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.saveBookingSettings() },
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("btn_save_booking_settings"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
            enabled = !state.isSavingSettings
        ) {
            if (state.isSavingSettings) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text(SalonStrings.get(lang, "save_booking_settings"), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

// ==========================================
// 3. SALON PAYOUT DETAILS SECTION
// ==========================================
@Composable
fun SalonPayoutDetailsSection(
    state: AuthUiState,
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = state.language

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("payout_details_screen")
    ) {
        // Back Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_to_menu")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = SalonStrings.get(lang, "payout_title"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = SalonStrings.get(lang, "payout_subtitle"),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Important Note: "This is only used when online payments/deposits are enabled — not required yet."
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = SalonStrings.get(lang, "payout_notice"),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Account Holder Name
                OutlinedTextField(
                    value = state.payoutHolderName,
                    onValueChange = { viewModel.updatePayoutHolderName(it) },
                    label = { Text(SalonStrings.get(lang, "account_holder_label")) },
                    modifier = Modifier.fillMaxWidth().testTag("input_account_holder"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // UPI ID
                OutlinedTextField(
                    value = state.payoutUpiId,
                    onValueChange = { viewModel.updatePayoutUpiId(it) },
                    label = { Text(SalonStrings.get(lang, "upi_id_label")) },
                    placeholder = { Text("e.g. salonowner@upi") },
                    modifier = Modifier.fillMaxWidth().testTag("input_upi_id"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Bank Account Number
                OutlinedTextField(
                    value = state.payoutAccountNumber,
                    onValueChange = { viewModel.updatePayoutAccountNumber(it) },
                    label = { Text(SalonStrings.get(lang, "bank_acc_label")) },
                    modifier = Modifier.fillMaxWidth().testTag("input_bank_account"),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // IFSC Code
                OutlinedTextField(
                    value = state.payoutIfsc,
                    onValueChange = { viewModel.updatePayoutIfsc(it.uppercase()) },
                    label = { Text(SalonStrings.get(lang, "ifsc_label")) },
                    placeholder = { Text("e.g. HDFC0001234") },
                    modifier = Modifier.fillMaxWidth().testTag("input_ifsc"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.savePayoutDetails() },
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("btn_save_payout_details"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
            enabled = !state.isSavingPayout
        ) {
            if (state.isSavingPayout) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text(SalonStrings.get(lang, "save_payout_details"), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
