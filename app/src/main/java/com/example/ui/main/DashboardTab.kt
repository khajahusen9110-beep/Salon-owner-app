package com.example.ui.main

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.data.model.OwnerDashboard
import com.example.data.model.SalonSetupStatus
import com.example.ui.auth.AuthUiState
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TerracottaPrimary
import com.example.util.SalonStrings

@Composable
fun DashboardTab(
    state: AuthUiState,
    onOpenWalkInModal: () -> Unit,
    onViewSchedule: () -> Unit,
    onToggleSalonActive: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenReviews: () -> Unit = {},
    onGoLive: () -> Unit = {},
    onOpenSalonSection: (String) -> Unit = {}
) {

    val lang = state.language
    val dash = state.dashboard ?: OwnerDashboard()
    val isActive = dash.isActive

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("dashboard_screen")
    ) {
        // Realtime Sync Status & Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = SalonStrings.get("live_sync_active", lang),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (state.lastSyncTime.isNotBlank()) {
                    Text(
                        text = " • ${state.lastSyncTime}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("refresh_dashboard_btn")
            ) {
                if (state.isLoadingDashboard) {
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

        // Setup checklist: shown until the salon is live, and afterwards whenever something needs fixing.
        state.setupStatus?.let { setup ->
            if (!setup.isLive || setup.hasWarnings) {
                SetupChecklistCard(
                    setup = setup,
                    isGoingLive = state.isGoingLive,
                    onGoLive = onGoLive,
                    onOpenSection = onOpenSalonSection
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Salon Active Status Switch & Alert Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("salon_active_banner"),
            colors = CardDefaults.cardColors(
                containerColor = if (isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isActive) SuccessGreen.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isActive) Icons.Default.Store else Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = if (isActive) SuccessGreen else ErrorRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isActive) {
                                SalonStrings.get("salon_is_active_online", lang)
                            } else {
                                SalonStrings.get("salon_off_banner", lang)
                            },
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = if (isActive) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                        )
                        Text(
                            text = if (isActive) "Customers can book online" else "Switch ON to accept customer bookings",
                            fontSize = 11.sp,
                            color = if (isActive) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                }

                Switch(
                    checked = isActive,
                    onCheckedChange = { onToggleSalonActive(it) },
                    modifier = Modifier.testTag("salon_active_toggle"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SuccessGreen,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = ErrorRed
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Actions Section
        Text(
            text = SalonStrings.get("quick_actions", lang),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onOpenWalkInModal,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("quick_add_walkin_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = SalonStrings.get("btn_add_walkin", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            OutlinedButton(
                onClick = onViewSchedule,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("quick_view_schedule_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TerracottaPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = SalonStrings.get("btn_view_schedule", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Grid of 6 Stat Cards
        Text(
            text = SalonStrings.get("dashboard_title", lang),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Row 1: Today's Bookings & Revenue
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = SalonStrings.get("stat_today_bookings", lang),
                value = "${dash.todayBookings}",
                icon = Icons.Default.CalendarToday,
                accentColor = TerracottaPrimary,
                modifier = Modifier.weight(1f),
                testTag = "stat_today_bookings"
            )

            StatCard(
                title = SalonStrings.get("stat_revenue", lang),
                value = "₹${dash.todayRevenue.toInt()}",
                icon = Icons.Default.AttachMoney,
                accentColor = GoldAccent,
                modifier = Modifier.weight(1f),
                testTag = "stat_revenue"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 2: Completed & Upcoming
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = SalonStrings.get("stat_completed", lang),
                value = "${dash.todayCompleted}",
                icon = Icons.Default.CheckCircle,
                accentColor = SuccessGreen,
                modifier = Modifier.weight(1f),
                testTag = "stat_completed"
            )

            StatCard(
                title = SalonStrings.get("stat_upcoming", lang),
                value = "${dash.todayUpcoming}",
                icon = Icons.Default.Schedule,
                accentColor = Color(0xFF1976D2),
                modifier = Modifier.weight(1f),
                testTag = "stat_upcoming"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 3: No-shows & Pending Review Replies
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = SalonStrings.get("stat_no_shows", lang),
                value = "${dash.todayNoShows}",
                icon = Icons.Default.EventBusy,
                accentColor = ErrorRed,
                modifier = Modifier.weight(1f),
                testTag = "stat_no_shows"
            )

            StatCard(
                title = SalonStrings.get("stat_pending_replies", lang),
                value = "${dash.pendingReviewsReply}",
                icon = Icons.Default.RateReview,
                accentColor = CoralAccent,
                modifier = Modifier.weight(1f),
                testTag = "stat_pending_reviews",
                onClick = onOpenReviews
            )
        }


        Spacer(modifier = Modifier.height(24.dp))

        // Quick Jump to Schedule Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onViewSchedule() }
                .testTag("jump_to_schedule_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Manage Today's Timeline",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "View stylist assignments, update status & client check-ins",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = TerracottaPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    testTag: String = "",
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .testTag(testTag)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SetupChecklistCard(
    setup: SalonSetupStatus,
    isGoingLive: Boolean,
    onGoLive: () -> Unit,
    onOpenSection: (String) -> Unit
) {
    val assignMissing = setup.missing.any { it.startsWith("Assign services") }
    Card(
        modifier = Modifier.fillMaxWidth().testTag("card_setup_checklist"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = when {
                    setup.isLive -> "Check your setup"
                    !setup.isVerified -> "Set up your salon (approval pending)"
                    setup.ready -> "Ready to go live!"
                    else -> "Finish setup to go live"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            if (!setup.isLive) {
                Text(
                    text = "Customers see your salon only after these steps.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            SetupStep(1, "Opening hours & days", setup.hoursSet, "working_hours", onOpenSection)
            SetupStep(2, "Services with price & time (${setup.serviceCount})", setup.serviceCount > 0, "services", onOpenSection)
            SetupStep(
                3, "Stylists, their days/hours & services (${setup.staffCount})",
                setup.staffCount > 0 && !assignMissing, "staff", onOpenSection
            )
            SetupStep(4, "Salon location on map", setup.locationSet, "profile", onOpenSection)

            val warnings = buildList {
                if (setup.servicesWithoutStaff.isNotEmpty())
                    add("No stylist does: ${setup.servicesWithoutStaff.joinToString()} — customers can't book it.")
                if (setup.staffWithoutServices.isNotEmpty())
                    add("No services ticked for: ${setup.staffWithoutServices.joinToString()}.")
                if (setup.staffWithoutHours.isNotEmpty())
                    add("No working days set for: ${setup.staffWithoutHours.joinToString()}.")
            }
            warnings.forEach { w ->
                Row(modifier = Modifier.padding(top = 6.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(w, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            if (!setup.isLive) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onGoLive,
                    enabled = setup.ready && setup.isVerified && !isGoingLive,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_go_live"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    if (isGoingLive) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = when {
                                !setup.isVerified -> "Go Live (after admin approval)"
                                !setup.ready -> "Go Live (finish the steps above)"
                                else -> "Go Live"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupStep(number: Int, title: String, done: Boolean, section: String, onOpenSection: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onOpenSection(section) }
            .padding(vertical = 8.dp)
            .testTag("setup_step_$number"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (done) SuccessGreen else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (done) Icon(Icons.Default.CheckCircle, contentDescription = "Done", tint = Color.White, modifier = Modifier.size(16.dp))
            else Text("$number", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(title, fontSize = 13.sp, modifier = Modifier.weight(1f), fontWeight = if (done) FontWeight.Normal else FontWeight.SemiBold)
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
