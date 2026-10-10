package com.example.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SalonWallet
import com.example.data.model.StaffEarningsSummary
import com.example.ui.auth.AuthUiState
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TerracottaPrimary
import com.example.util.SalonStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EarningsTab(
    state: AuthUiState,
    modifier: Modifier = Modifier,
    onSelectPreset: (String) -> Unit = {},
    onApplyCustomRange: (String, String) -> Unit = { _, _ -> },
    onRefresh: () -> Unit = {},
    onRequestWithdrawal: (String, String, (Boolean) -> Unit) -> Unit = { _, _, _ -> },
    onCancelWithdrawal: (String) -> Unit = {},
    onOpenPayoutDetails: () -> Unit = {}
) {
    val lang = state.language
    var customFrom by remember(state.earningsCustomFrom) {
        mutableStateOf(
            state.earningsCustomFrom.ifBlank {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            }
        )
    }
    var customTo by remember(state.earningsCustomTo) {
        mutableStateOf(
            state.earningsCustomTo.ifBlank {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("earnings_screen")
    ) {
        WalletSection(
            state = state,
            onRequestWithdrawal = onRequestWithdrawal,
            onCancelWithdrawal = onCancelWithdrawal,
            onOpenPayoutDetails = onOpenPayoutDetails
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = SalonStrings.get(lang, "earnings_title"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = SalonStrings.get(lang, "earnings_subtitle"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .testTag("earnings_refresh_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Earnings",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Date Range Selector (Today, This Week, This Month, Custom)
        Text(
            text = "Select Period",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val presets = listOf(
                "today" to SalonStrings.get(lang, "date_range_today"),
                "week" to SalonStrings.get(lang, "date_range_week"),
                "month" to SalonStrings.get(lang, "date_range_month"),
                "custom" to SalonStrings.get(lang, "date_range_custom")
            )

            presets.forEach { (key, label) ->
                val selected = state.earningsDatePreset == key
                FilterChip(
                    selected = selected,
                    onClick = { onSelectPreset(key) },
                    label = {
                        Text(
                            text = label,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    leadingIcon = if (key == "custom") {
                        {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TerracottaPrimary,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    modifier = Modifier.testTag("date_chip_$key")
                )
            }
        }

        // Custom Date Range Inputs
        AnimatedVisibility(visible = state.earningsDatePreset == "custom") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = SalonStrings.get(lang, "date_range_custom"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customFrom,
                            onValueChange = { customFrom = it },
                            label = { Text("From (YYYY-MM-DD)", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("custom_date_from_input")
                        )
                        OutlinedTextField(
                            value = customTo,
                            onValueChange = { customTo = it },
                            label = { Text("To (YYYY-MM-DD)", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("custom_date_to_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onApplyCustomRange(customFrom, customTo) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("apply_custom_range_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(SalonStrings.get(lang, "apply_custom_range"))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Loading or Error State Handling
        if (state.isLoadingEarnings) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = TerracottaPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Loading earnings data...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (state.earningsError != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.earningsError ?: "Could not load earnings",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onRefresh,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(SalonStrings.get(lang, "offline_retry"))
                    }
                }
            }
        } else {
            // Main Summary Card
            val totalRev = state.totalEarningsRevenue
            val totalCompleted = state.totalEarningsCompleted
            val totalNoShows = state.totalEarningsNoShows
            val totalCommission = state.totalEarningsCommission

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("earnings_summary_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TerracottaPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = SalonStrings.get(lang, "earnings_total_revenue"),
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = state.earningsDatePreset.uppercase(Locale.getDefault()),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "₹${totalRev.toInt()}",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.25f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // 3 Stat Tiles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Completed
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = SalonStrings.get(lang, "earnings_completed_bookings"),
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = "$totalCompleted",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        // No Shows
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EventBusy,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = SalonStrings.get(lang, "earnings_no_shows"),
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = "$totalNoShows",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        // Commission Owed
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = SalonStrings.get(lang, "stylist_owed"),
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = "₹${totalCommission.toInt()}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bar Chart Section
            if (state.staffEarnings.isNotEmpty()) {
                StylistRevenueBarChart(
                    summaries = state.staffEarnings,
                    totalRevenue = totalRev,
                    lang = lang
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Per Stylist Breakdown Header
            Text(
                text = SalonStrings.get(lang, "earnings_stylist_breakdown"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (state.staffEarnings.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = SalonStrings.get(lang, "earnings_empty"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                state.staffEarnings.forEach { summary ->
                    StylistEarningsCard(summary = summary, lang = lang)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun StylistRevenueBarChart(
    summaries: List<StaffEarningsSummary>,
    totalRevenue: Double,
    lang: String
) {
    val maxRev = summaries.maxOfOrNull { it.revenue }?.coerceAtLeast(1.0) ?: 1.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("earnings_bar_chart"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SalonStrings.get(lang, "revenue_chart_title"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Performance",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            summaries.forEach { item ->
                val fraction = (item.revenue / maxRev).toFloat().coerceIn(0.05f, 1f)
                val sharePercent = if (totalRevenue > 0) ((item.revenue / totalRevenue) * 100).toInt() else 0

                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.staffName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "₹${item.revenue.toInt()} ($sharePercent%)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Bar Track & Fill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(TerracottaPrimary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StylistEarningsCard(
    summary: StaffEarningsSummary,
    lang: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stylist_earnings_${summary.staffName.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Stylist Name & Commission Rate Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TerracottaPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = summary.staffName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${summary.commissionPercent.toInt()}% ${SalonStrings.get(lang, "stylist_commission")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Commission Owed
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = SalonStrings.get(lang, "stylist_owed"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${summary.commissionAmount.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = SuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            // 4 Mini Metrics: Revenue, Completed, No-shows, Cancellations
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Revenue
                Column {
                    Text(
                        text = SalonStrings.get(lang, "stylist_revenue"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${summary.revenue.toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Completed
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = SalonStrings.get(lang, "stylist_completed"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${summary.completedCount}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // No Shows
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = SalonStrings.get(lang, "stylist_no_shows"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${summary.noShowCount}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.noShowCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Cancelled
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = SalonStrings.get(lang, "stylist_cancellations"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${summary.cancelledCount}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun rupees(v: Double): String =
    if (v == Math.floor(v)) "₹${v.toLong()}" else "₹${"%.2f".format(v)}"

/** Withdrawable balance, Withdraw button and the history of withdrawal requests. */
@Composable
private fun WalletSection(
    state: AuthUiState,
    onRequestWithdrawal: (String, String, (Boolean) -> Unit) -> Unit,
    onCancelWithdrawal: (String) -> Unit,
    onOpenPayoutDetails: () -> Unit
) {
    val wallet = state.wallet
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("card_wallet"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Wallet", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (wallet == null) {
                Spacer(modifier = Modifier.height(8.dp))
                if (state.isLoadingWallet) CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                else Text("Couldn't load wallet. Pull to refresh.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                return@Column
            }
            Text("Available to withdraw", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp))
            Text(rupees(wallet.available), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary,
                modifier = Modifier.testTag("wallet_available"))
            if (wallet.owed > 0) {
                Text(
                    "You owe the platform ${rupees(wallet.owed)} in commission (from bookings paid at the salon). " +
                        "It is adjusted from your next online payments.",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            WalletRow("Online payments for completed visits", rupees(wallet.earned))
            WalletRow("Platform commission (${wallet.commissionRate.let { if (it == Math.floor(it)) it.toInt().toString() else it.toString() }}%)", "− ${rupees(wallet.commission)}")
            WalletRow("Already withdrawn", "− ${rupees(wallet.withdrawn)}")
            if (wallet.pending > 0) WalletRow("Withdrawal in progress", "− ${rupees(wallet.pending)}")
            if (wallet.held > 0) WalletRow("Advance for upcoming visits (added after the visit)", rupees(wallet.held))

            Spacer(modifier = Modifier.height(12.dp))
            if (!wallet.hasUpi && !wallet.hasBank) {
                OutlinedButton(onClick = onOpenPayoutDetails, modifier = Modifier.fillMaxWidth().testTag("btn_add_payout_method")) {
                    Text("Add bank account / UPI to withdraw")
                }
            } else {
                Button(
                    onClick = { showDialog = true },
                    enabled = wallet.available >= wallet.minWithdrawal && wallet.pending <= 0,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_withdraw"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    Text(
                        when {
                            wallet.pending > 0 -> "Withdrawal in progress"
                            wallet.available < wallet.minWithdrawal -> "Withdraw (min ${rupees(wallet.minWithdrawal)})"
                            else -> "Withdraw"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
                TextButton(onClick = onOpenPayoutDetails) { Text("Change bank / UPI details", fontSize = 12.sp) }
            }

            if (wallet.withdrawals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Withdrawals", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                wallet.withdrawals.take(10).forEach { w ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "${rupees(w.amount)} · " + if (w.method == "upi") "UPI ${w.upiId.orEmpty()}" else "Bank ••••${w.bankAccountLast4.orEmpty()}",
                                fontSize = 13.sp, fontWeight = FontWeight.Medium
                            )
                            Text(w.createdAt.replace("T", " ").take(16), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            w.payoutReference?.let { Text("Ref: $it", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            w.adminNote?.let { Text(it, fontSize = 11.sp, color = MaterialTheme.colorScheme.error) }
                        }
                        val (label, color) = when (w.status) {
                            "paid" -> "Paid" to SuccessGreen
                            "rejected" -> "Rejected" to ErrorRed
                            "cancelled" -> "Cancelled" to MaterialTheme.colorScheme.onSurfaceVariant
                            else -> "In progress" to GoldAccent
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
                            if (w.status == "requested") {
                                TextButton(onClick = { onCancelWithdrawal(w.id) }) { Text("Cancel", fontSize = 11.sp) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog && wallet != null) {
        WithdrawDialog(
            wallet = wallet,
            payoutUpi = state.payoutDetails?.upiId.orEmpty(),
            payoutAccount = state.payoutDetails?.bankAccountNumber.orEmpty(),
            isSubmitting = state.isRequestingWithdrawal,
            errorMessage = state.errorMessage,
            onDismiss = { showDialog = false },
            onSubmit = { amount, method -> onRequestWithdrawal(amount, method) { ok -> if (ok) showDialog = false } }
        )
    }
}

@Composable
private fun WalletRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun WithdrawDialog(
    wallet: SalonWallet,
    payoutUpi: String,
    payoutAccount: String,
    isSubmitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var amount by remember { mutableStateOf(if (wallet.available == Math.floor(wallet.available)) wallet.available.toLong().toString() else "%.2f".format(wallet.available)) }
    var method by remember { mutableStateOf(if (wallet.hasUpi) "upi" else "bank") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Withdraw money", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Available: ${rupees(wallet.available)}", fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { v -> amount = v.filter { it.isDigit() || it == '.' }.take(10) },
                    label = { Text("Amount (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("input_withdraw_amount")
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text("Send to", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                if (wallet.hasUpi) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { method = "upi" }) {
                        RadioButton(selected = method == "upi", onClick = { method = "upi" })
                        Text("UPI · $payoutUpi", fontSize = 13.sp)
                    }
                }
                if (wallet.hasBank) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { method = "bank" }) {
                        RadioButton(selected = method == "bank", onClick = { method = "bank" })
                        Text("Bank account ••••${payoutAccount.takeLast(4)}", fontSize = 13.sp)
                    }
                }
                Text(
                    "Usually sent within 1-2 working days. Minimum ${rupees(wallet.minWithdrawal)}.",
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                errorMessage?.let { Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 6.dp)) }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(amount, method) },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                modifier = Modifier.testTag("btn_confirm_withdraw")
            ) {
                if (isSubmitting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Request")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
