package com.example.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.data.model.CustomerSummary
import com.example.ui.auth.AuthUiState
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TerracottaPrimary
import com.example.util.SalonStrings
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailModal(
    state: AuthUiState,
    booking: Booking,
    onClose: () -> Unit,
    onViewHistory: () -> Unit,
    onCloseHistory: () -> Unit,
    onOpenReschedule: () -> Unit,
    onCloseReschedule: () -> Unit,
    onSelectRescheduleDate: (String) -> Unit,
    onSelectRescheduleTime: (String) -> Unit,
    onSubmitReschedule: () -> Unit,
    onOpenCancelConfirm: () -> Unit,
    onCloseCancelConfirm: () -> Unit,
    onConfirmCancel: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lang = state.language
    val summary = state.customerSummary

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("booking_detail_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header: Title and Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = SalonStrings.get("booking_details_title", lang),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Booking ID: #${booking.id.takeLast(6).uppercase()}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onClose, modifier = Modifier.testTag("btn_close_booking_detail")) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Customer Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(TerracottaPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = booking.customerName ?: "Customer",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = booking.customerPhone?.ifBlank { "No phone registered" } ?: "No phone registered",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Status Badge
                        StatusPill(status = booking.status ?: "confirmed", language = lang)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Deep Links: Call Customer & WhatsApp Customer
                    if (!booking.customerPhone.isNullOrBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${booking.customerPhone}"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("btn_call_customer"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = SalonStrings.get("btn_call_customer", lang),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = {
                                    val cleanPhone = booking.customerPhone.replace("[^0-9]".toRegex(), "")
                                    val phoneWithCountry = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
                                    val message = "Hello ${booking.customerName}, regarding your appointment at our salon."
                                    val url = "https://wa.me/$phoneWithCountry?text=${URLEncoder.encode(message, "UTF-8")}"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("btn_whatsapp_customer"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366).copy(alpha = 0.12f))
                            ) {
                                Text(
                                    text = "💬 " + SalonStrings.get("btn_whatsapp_customer", lang),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B8A42)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Service & Appointment Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Appointment Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DetailRowItem(
                        icon = Icons.Default.ContentCut,
                        title = "Service",
                        value = "${booking.serviceName ?: "Hair & Styling"}${if (booking.durationMinutes != null) " (${booking.durationMinutes} mins)" else ""}"
                    )

                    DetailRowItem(
                        icon = Icons.Default.Person,
                        title = "Stylist",
                        value = booking.staffName ?: "Any Available Stylist"
                    )

                    val formattedTime = formatDateTimeRange(booking.startTime, booking.endTime)
                    DetailRowItem(
                        icon = Icons.Default.Schedule,
                        title = "Scheduled Time",
                        value = formattedTime
                    )

                    DetailRowItem(
                        icon = Icons.Default.DateRange,
                        title = "Source",
                        value = if (booking.source == "walk_in") "Walk-in Booking" else "Online Booking"
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Price",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹${booking.price?.toInt() ?: 450}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Customer History Card / Button
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = SalonStrings.get("customer_history_title", lang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        if (summary == null) {
                            TextButton(
                                onClick = onViewHistory,
                                modifier = Modifier.testTag("btn_view_customer_history")
                            ) {
                                if (state.isLoadingCustomerSummary) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Text(
                                        text = SalonStrings.get("btn_view_history", lang),
                                        fontWeight = FontWeight.Bold,
                                        color = TerracottaPrimary
                                    )
                                }
                            }
                        }
                    }

                    if (summary != null) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Loyalty tag
                        val loyaltyBadge = when {
                            summary.noShowCount > 1 -> Pair(SalonStrings.get("badge_attention", lang), ErrorRed)
                            summary.totalVisits >= 3 -> Pair(SalonStrings.get("badge_regular", lang), SuccessGreen)
                            else -> Pair(SalonStrings.get("badge_new", lang), GoldAccent)
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = loyaltyBadge.second.copy(alpha = 0.15f),
                            modifier = Modifier.padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (summary.noShowCount > 1) Icons.Default.Warning else Icons.Default.Star,
                                    contentDescription = null,
                                    tint = loyaltyBadge.second,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = loyaltyBadge.first,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = loyaltyBadge.second
                                )
                            }
                        }

                        // Stats Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatMiniBox(
                                label = SalonStrings.get("customer_total_visits", lang),
                                value = summary.totalVisits.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            StatMiniBox(
                                label = SalonStrings.get("customer_no_shows", lang),
                                value = summary.noShowCount.toString(),
                                valueColor = if (summary.noShowCount > 0) ErrorRed else null,
                                modifier = Modifier.weight(1f)
                            )
                            StatMiniBox(
                                label = SalonStrings.get("customer_total_spent", lang),
                                value = "₹${summary.totalSpent.toInt()}",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (!summary.lastVisitDate.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${SalonStrings.get("customer_last_visit", lang)}: ${summary.lastVisitDate}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons: Reschedule & Cancel
            val canReschedule = booking.status == "confirmed" || booking.status == "arrived"
            val canCancel = booking.status != "cancelled" && booking.status != "completed"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (canCancel) {
                    OutlinedButton(
                        onClick = onOpenCancelConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_cancel_booking"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                    ) {
                        Icon(imageVector = Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = SalonStrings.get("btn_cancel_booking", lang),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (canReschedule) {
                    Button(
                        onClick = onOpenReschedule,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_reschedule_booking"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = SalonStrings.get("btn_reschedule", lang),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Reschedule Dialog
    if (state.showRescheduleDialog) {
        RescheduleBookingDialog(
            state = state,
            booking = booking,
            onClose = onCloseReschedule,
            onSelectDate = onSelectRescheduleDate,
            onSelectTime = onSelectRescheduleTime,
            onConfirm = onSubmitReschedule
        )
    }

    // Cancel Confirmation Dialog
    if (state.showCancelBookingDialog) {
        AlertDialog(
            onDismissRequest = onCloseCancelConfirm,
            title = {
                Text(
                    text = SalonStrings.get("cancel_booking_confirm_title", lang),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = SalonStrings.get("cancel_booking_confirm_desc", lang),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_cancel_btn")
                ) {
                    Text(SalonStrings.get("confirm_cancel_action", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = onCloseCancelConfirm) {
                    Text(SalonStrings.get("btn_cancel", lang))
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RescheduleBookingDialog(
    state: AuthUiState,
    booking: Booking,
    onClose: () -> Unit,
    onSelectDate: (String) -> Unit,
    onSelectTime: (String) -> Unit,
    onConfirm: () -> Unit
) {
    val lang = state.language
    val selectedDate = state.rescheduleSelectedDate
    val selectedTime = state.rescheduleSelectedTime

    // Generate next 7 days for quick date selection
    val availableDates = remember {
        val list = mutableListOf<Pair<String, String>>()
        val cal = Calendar.getInstance()
        val sdfKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfDisplay = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
        for (i in 0..6) {
            val date = cal.time
            val key = sdfKey.format(date)
            val display = if (i == 0) "Today" else if (i == 1) "Tomorrow" else sdfDisplay.format(date)
            list.add(Pair(key, display))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val timeSlots = listOf(
        "10:00", "11:00", "12:00", "13:00", "14:00", "15:30", "16:30", "17:30", "18:30"
    )

    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text(
                text = SalonStrings.get("reschedule_dialog_title", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "${SalonStrings.get("select_new_date", lang)}:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableDates.forEach { (dateKey, display) ->
                        val isSelected = selectedDate == dateKey
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) TerracottaPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { onSelectDate(dateKey) }
                                .testTag("reschedule_date_$dateKey")
                        ) {
                            Text(
                                text = display,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${SalonStrings.get("select_new_time", lang)}:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    timeSlots.forEach { slot ->
                        val isSelected = selectedTime == slot
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) TerracottaPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { onSelectTime(slot) }
                                .testTag("reschedule_time_$slot")
                        ) {
                            Text(
                                text = formatTime12h(slot),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !state.isSubmittingReschedule && selectedDate.isNotBlank() && selectedTime.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_reschedule_btn")
            ) {
                if (state.isSubmittingReschedule) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(SalonStrings.get("confirm_reschedule_btn", lang))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text(SalonStrings.get("btn_cancel", lang))
            }
        }
    )
}

@Composable
private fun DetailRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TerracottaPrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun StatMiniBox(
    label: String,
    value: String,
    valueColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor ?: MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatusPill(status: String, language: String) {
    val (label, bg, fg) = when (status.lowercase()) {
        "confirmed" -> Triple("Confirmed", TerracottaPrimary.copy(alpha = 0.15f), TerracottaPrimary)
        "arrived" -> Triple("Arrived", GoldAccent.copy(alpha = 0.2f), Color(0xFFB57000))
        "in_service" -> Triple("In Service", CoralAccent.copy(alpha = 0.2f), CoralAccent)
        "completed" -> Triple("Completed", SuccessGreen.copy(alpha = 0.15f), SuccessGreen)
        "cancelled" -> Triple("Cancelled", ErrorRed.copy(alpha = 0.15f), ErrorRed)
        "no_show" -> Triple("No-show", Color(0xFF757575).copy(alpha = 0.15f), Color(0xFF616161))
        else -> Triple(status.replaceFirstChar { it.uppercase() }, Color.LightGray.copy(alpha = 0.3f), Color.DarkGray)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun formatDateTimeRange(startTime: String, endTime: String?): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        val outputDate = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
        val outputTime = SimpleDateFormat("h:mm a", Locale.getDefault())

        val cleanStart = if (startTime.length >= 16) startTime.substring(0, 16) else startTime
        val startDate = input.parse(cleanStart)

        val dateStr = if (startDate != null) outputDate.format(startDate) else startTime
        val startStr = if (startDate != null) outputTime.format(startDate) else startTime

        val endStr = if (!endTime.isNullOrBlank()) {
            val cleanEnd = if (endTime.length >= 16) endTime.substring(0, 16) else endTime
            val endDate = input.parse(cleanEnd)
            if (endDate != null) " – ${outputTime.format(endDate)}" else ""
        } else ""

        "$dateStr | $startStr$endStr"
    } catch (_: Exception) {
        "$startTime${if (endTime != null) " - $endTime" else ""}"
    }
}

private fun formatTime12h(time24: String): String {
    return try {
        val parts = time24.split(":")
        val h = parts[0].toInt()
        val m = parts.getOrNull(1)?.toInt() ?: 0
        val amPm = if (h >= 12) "PM" else "AM"
        val h12 = if (h == 0) 12 else if (h > 12) h - 12 else h
        String.format(Locale.getDefault(), "%d:%02d %s", h12, m, amPm)
    } catch (_: Exception) {
        time24
    }
}
