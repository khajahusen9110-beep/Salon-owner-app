package com.example.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.data.model.Staff
import com.example.ui.auth.AuthUiState
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TerracottaPrimary
import com.example.util.SalonStrings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ScheduleTab(
    state: AuthUiState,
    onSubTabChange: (String) -> Unit,
    onStaffFilterSelected: (String) -> Unit,
    onStatusChange: (bookingId: String, newStatus: String) -> Unit,
    onOpenBookingDetail: (Booking) -> Unit,
    onOpenWalkInModal: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lang = state.language
    val subTab = state.bookingsSubTab // "upcoming" or "past"
    val staffList = state.staffList
    val selectedFilter = state.selectedStaffFilter

    val upcomingList = state.upcomingBookings
    val pastList = state.pastBookings

    val currentList = if (subTab == "upcoming") upcomingList else pastList
    val isLoading = if (subTab == "upcoming") state.isLoadingUpcoming else state.isLoadingPast

    Box(modifier = modifier.fillMaxSize().testTag("schedule_screen")) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with Stylist Filter Chips & Refresh Action
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Top Title & Sub-tabs Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = SalonStrings.get("schedule_title", lang),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (subTab == "upcoming") {
                                "${upcomingList.size} ${SalonStrings.get("subtab_upcoming", lang).lowercase()} bookings"
                            } else {
                                "${pastList.size} ${SalonStrings.get("subtab_past", lang).lowercase()} bookings"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("refresh_schedule_btn")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = TerracottaPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = TerracottaPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sub-tabs: Upcoming vs Past
                TabRow(
                    selectedTabIndex = if (subTab == "upcoming") 0 else 1,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = TerracottaPrimary,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = subTab == "upcoming",
                        onClick = { onSubTabChange("upcoming") },
                        text = {
                            Text(
                                text = "${SalonStrings.get("subtab_upcoming", lang)} (${upcomingList.size})",
                                fontSize = 13.sp,
                                fontWeight = if (subTab == "upcoming") FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("subtab_upcoming")
                    )
                    Tab(
                        selected = subTab == "past",
                        onClick = { onSubTabChange("past") },
                        text = {
                            Text(
                                text = "${SalonStrings.get("subtab_past", lang)} (${pastList.size})",
                                fontSize = 13.sp,
                                fontWeight = if (subTab == "past") FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("subtab_past")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stylist Filter Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "all",
                        onClick = { onStaffFilterSelected("all") },
                        label = { Text(SalonStrings.get("all_stylists", lang)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TerracottaPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("filter_staff_all")
                    )

                    staffList.forEach { staff ->
                        val staffBookingCount = currentList.count { it.staffId == staff.id }
                        FilterChip(
                            selected = selectedFilter == staff.id,
                            onClick = { onStaffFilterSelected(staff.id) },
                            label = { Text("${staff.name} ($staffBookingCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TerracottaPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_staff_${staff.id}")
                        )
                    }
                }
            }

            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Bookings List or Empty State
            if (currentList.isEmpty()) {
                EmptyScheduleView(
                    language = lang,
                    isUpcoming = subTab == "upcoming",
                    onAddWalkIn = onOpenWalkInModal
                )
            } else {
                // Group bookings by date for clear organization
                val groupedBookings = remember(currentList) {
                    currentList.groupBy { booking ->
                        getDateHeaderLabel(booking.startTime)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 14.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    groupedBookings.forEach { (dateHeader, bookingsInDate) ->
                        // Date Group Sticky / Section Header
                        item(key = "header_$dateHeader") {
                            DateSectionHeader(title = dateHeader)
                        }

                        items(bookingsInDate, key = { it.id }) { booking ->
                            BookingCard(
                                booking = booking,
                                language = lang,
                                onStatusChange = onStatusChange,
                                onClick = { onOpenBookingDetail(booking) },
                                onCallCustomer = { phone ->
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button for Walk-ins
        FloatingActionButton(
            onClick = onOpenWalkInModal,
            containerColor = TerracottaPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_walkin")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = SalonStrings.get("btn_add_walkin", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun DateSectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = TerracottaPrimary.copy(alpha = 0.12f)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TerracottaPrimary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    language: String,
    onStatusChange: (bookingId: String, newStatus: String) -> Unit,
    onClick: () -> Unit,
    onCallCustomer: (String) -> Unit
) {
    val formattedTime = remember(booking.startTime, booking.endTime) {
        formatTimeRange(booking.startTime, booking.endTime)
    }

    val isPastStartTime = remember(booking.startTime) {
        checkIsPastStartTime(booking.startTime)
    }

    val isToday = remember(booking.startTime) {
        checkIsToday(booking.startTime)
    }

    val (statusLabelKey, badgeBgColor, badgeTextColor) = when (booking.status.lowercase()) {
        "confirmed" -> Triple("status_confirmed", Color(0xFFE3F2FD), Color(0xFF1565C0))
        "arrived" -> Triple("status_arrived", Color(0xFFFFF8E1), Color(0xFFF57F17))
        "in_service" -> Triple("status_in_service", Color(0xFFF3E5F5), Color(0xFF7B1FA2))
        "completed" -> Triple("status_completed", Color(0xFFE8F5E9), Color(0xFF2E7D32))
        "cancelled" -> Triple("status_cancelled", Color(0xFFECEFF1), Color(0xFF546E7A))
        "no_show" -> Triple("status_no_show", Color(0xFFFFEBEE), Color(0xFFC62828))
        else -> Triple(booking.status, Color(0xFFECEFF1), Color(0xFF37474F))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("booking_card_${booking.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Time Range & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formattedTime,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBgColor
                ) {
                    Text(
                        text = SalonStrings.get(statusLabelKey, language),
                        color = badgeTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Customer Name & Phone Click-to-call
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.customerName ?: "Walk-in Guest",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!booking.customerPhone.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { onCallCustomer(booking.customerPhone) }
                                .padding(top = 2.dp)
                                .testTag("call_customer_${booking.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call",
                                tint = SuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = booking.customerPhone,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Text(
                    text = "₹${booking.price?.toInt() ?: 450}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TerracottaPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Service name & Stylist
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.ContentCut,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${booking.serviceName ?: "Hair & Styling"}${if (booking.durationMinutes != null) " (${booking.durationMinutes}m)" else ""}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = booking.staffName ?: "Any",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TerracottaPrimary
                    )
                }
            }

            // Quick Status Actions for Today's Bookings
            if (isToday) {
                when (booking.status.lowercase()) {
                    "confirmed" -> {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onStatusChange(booking.id, "arrived") },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(40.dp)
                                    .testTag("btn_arrived_${booking.id}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                            ) {
                                Text(
                                    text = SalonStrings.get("btn_arrived", language),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3E2723)
                                )
                            }

                            OutlinedButton(
                                onClick = { onStatusChange(booking.id, "no_show") },
                                enabled = isPastStartTime,
                                modifier = Modifier
                                    .weight(1.1f)
                                    .height(40.dp)
                                    .testTag("btn_noshow_${booking.id}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFB71C1C),
                                    disabledContentColor = Color.LightGray
                                )
                            ) {
                                Text(
                                    text = SalonStrings.get("btn_noshow", language),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (!isPastStartTime) {
                            Text(
                                text = "(No-show activates after start time)",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }
                    }

                    "arrived" -> {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { onStatusChange(booking.id, "in_service") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("btn_start_service_${booking.id}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = SalonStrings.get("btn_start_service", language),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    "in_service" -> {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { onStatusChange(booking.id, "completed") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("btn_mark_done_${booking.id}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = SalonStrings.get("btn_mark_done", language),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Tap hint at bottom of card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Tap to view details ›",
                    fontSize = 11.sp,
                    color = TerracottaPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun EmptyScheduleView(
    language: String,
    isUpcoming: Boolean,
    onAddWalkIn: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(TerracottaPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = TerracottaPrimary,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = if (isUpcoming) SalonStrings.get("empty_upcoming", language) else SalonStrings.get("empty_past", language),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isUpcoming) SalonStrings.get("empty_upcoming_desc", language) else SalonStrings.get("empty_past_desc", language),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (isUpcoming) {
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onAddWalkIn,
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("empty_schedule_add_walkin")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(SalonStrings.get("btn_add_walkin", language), fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun getDateHeaderLabel(isoDate: String): String {
    return try {
        val datePart = if (isoDate.contains("T")) isoDate.substringBefore("T") else isoDate.take(10)
        val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdfInput.parse(datePart) ?: return datePart

        val calToday = Calendar.getInstance()
        val todayStr = sdfInput.format(calToday.time)

        val calTom = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomStr = sdfInput.format(calTom.time)

        when (datePart) {
            todayStr -> "Today (${SimpleDateFormat("d MMM", Locale.getDefault()).format(date)})"
            tomStr -> "Tomorrow (${SimpleDateFormat("d MMM", Locale.getDefault()).format(date)})"
            else -> SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(date)
        }
    } catch (_: Exception) {
        isoDate.take(10)
    }
}

private fun checkIsToday(isoDate: String): Boolean {
    return try {
        val datePart = if (isoDate.contains("T")) isoDate.substringBefore("T") else isoDate.take(10)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        datePart == today
    } catch (_: Exception) {
        true
    }
}

private fun formatTimeRange(start: String, end: String?): String {
    val startTime = parseTimeTo12Hr(start)
    val endTime = end?.let { parseTimeTo12Hr(it) }
    return if (endTime != null) "$startTime – $endTime" else startTime
}

private fun parseTimeTo12Hr(isoOrTime: String): String {
    return try {
        val timePart = if (isoOrTime.contains("T")) {
            isoOrTime.substringAfter("T").substringBefore("+").substringBefore("Z")
        } else {
            isoOrTime
        }
        val parts = timePart.split(":")
        val hour = parts[0].toInt()
        val min = parts.getOrNull(1)?.toInt() ?: 0
        val isPm = hour >= 12
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val minStr = if (min < 10) "0$min" else "$min"
        val amPm = if (isPm) "PM" else "AM"
        "$displayHour:$minStr $amPm"
    } catch (_: Exception) {
        isoOrTime
    }
}

private fun checkIsPastStartTime(isoOrTime: String): Boolean {
    return try {
        val timePart = if (isoOrTime.contains("T")) {
            isoOrTime.substringAfter("T").substringBefore("+").substringBefore("Z")
        } else {
            isoOrTime
        }
        val parts = timePart.split(":")
        val startHour = parts[0].toInt()
        val startMin = parts.getOrNull(1)?.toInt() ?: 0

        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        val currentMin = cal.get(Calendar.MINUTE)

        if (currentHour > startHour) true
        else if (currentHour == startHour && currentMin >= startMin) true
        else false
    } catch (_: Exception) {
        true
    }
}
