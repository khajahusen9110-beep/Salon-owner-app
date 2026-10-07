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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Staff
import com.example.ui.auth.AuthUiState
import com.example.ui.auth.AuthViewModel
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TerracottaPrimary
import com.example.util.SalonStrings

@Composable
fun SalonStaffSection(
    state: AuthUiState,
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = state.language
    val staffList = state.staffList
    val selectedStaff = state.selectedStaffForDetail

    if (selectedStaff != null) {
        // Detailed Stylist Management: Working Hours & Services Performed
        StaffDetailScreen(
            staff = selectedStaff,
            state = state,
            viewModel = viewModel,
            onBack = { viewModel.closeStaffDetail() },
            lang = lang
        )
    } else {
        // Main Staff List
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("salon_staff_screen")
        ) {
            // Back Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("btn_back_to_menu")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = SalonStrings.get(lang, "staff_title"),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${staffList.size} ${SalonStrings.get(lang, "staff_members_count")}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { viewModel.openAddStaffDialog() },
                    modifier = Modifier.testTag("btn_add_staff"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(SalonStrings.get(lang, "btn_add_staff"), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (staffList.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No staff added yet. Tap '+ Add Staff' to add your team.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                staffList.forEach { staff ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("staff_card_${staff.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar / Photo
                                if (staff.photoUrl != null) {
                                    AsyncImage(
                                        model = staff.photoUrl,
                                        contentDescription = staff.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(48.dp).clip(CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(TerracottaPrimary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = staff.name.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = TerracottaPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = staff.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${staff.role ?: "Stylist"} · ${staff.commissionPercent.toInt()}% ${SalonStrings.get(lang, "staff_commission_suffix")}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Active switch
                                Switch(
                                    checked = staff.isActive,
                                    onCheckedChange = { viewModel.toggleStaffActive(staff.id, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = SuccessGreen
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Manage Hours & Services Button
                            OutlinedButton(
                                onClick = { viewModel.openStaffDetail(staff) },
                                modifier = Modifier.fillMaxWidth().testTag("btn_manage_staff_${staff.id}"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = SalonStrings.get(lang, "manage_staff_hours_services"),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Add Staff Dialog
    if (state.showAddStaffDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeAddStaffDialog() },
            title = { Text(SalonStrings.get(lang, "add_staff_title"), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = state.staffFormName,
                        onValueChange = { viewModel.updateStaffFormName(it) },
                        label = { Text(SalonStrings.get(lang, "staff_name_label")) },
                        modifier = Modifier.fillMaxWidth().testTag("input_staff_name"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = state.staffFormCommission,
                        onValueChange = { viewModel.updateStaffFormCommission(it) },
                        label = { Text(SalonStrings.get(lang, "staff_commission_label")) },
                        suffix = { Text("%") },
                        modifier = Modifier.fillMaxWidth().testTag("input_staff_commission"),
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = state.staffFormPhotoUrl ?: "",
                        onValueChange = { viewModel.updateStaffFormPhoto(it.ifBlank { null }) },
                        label = { Text("Photo URL (Optional)") },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.fillMaxWidth().testTag("input_staff_photo"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.saveStaffMember() },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    modifier = Modifier.testTag("btn_save_staff_submit"),
                    enabled = !state.isSavingStaff
                ) {
                    if (state.isSavingStaff) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text(SalonStrings.get(lang, "save"))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeAddStaffDialog() }) {
                    Text(SalonStrings.get(lang, "cancel"))
                }
            }
        )
    }
}

@Composable
fun StaffDetailScreen(
    staff: Staff,
    state: AuthUiState,
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    lang: String
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val daysOfWeek = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("staff_detail_screen")
    ) {
        // Back Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = staff.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${staff.role ?: "Stylist"} · ${staff.commissionPercent.toInt()}% commission",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs: Working Hours (0) vs Services Performed (1)
        TabRow(
            selectedTabIndex = selectedTab,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = TerracottaPrimary
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(SalonStrings.get(lang, "staff_tab_hours"), fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(SalonStrings.get(lang, "staff_tab_services"), fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            // Working Hours (7 rows: Sun - Sat)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Working Hours (Sun - Sat)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "A stylist's slots are limited to both the salon's hours AND their own working hours — whichever is shorter.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    for (day in 0..6) {
                        val hours = state.staffHoursList.find { it.dayOfWeek == day }
                        val isWorking = hours?.isWorking ?: true
                        val startTime = hours?.startTime ?: "09:00"
                        val endTime = hours?.endTime ?: "20:00"

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
                                    text = if (isWorking) "Working" else "Off",
                                    fontSize = 11.sp,
                                    color = if (isWorking) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = isWorking,
                                onCheckedChange = { checked ->
                                    viewModel.updateStaffWorkingHoursDay(day, checked, startTime, endTime)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = SuccessGreen
                                )
                            )

                            if (isWorking) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TimePickerDropdown(
                                        time = startTime,
                                        onTimeSelected = { newStart ->
                                            viewModel.updateStaffWorkingHoursDay(day, true, newStart, endTime)
                                        }
                                    )
                                    Text(" - ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    TimePickerDropdown(
                                        time = endTime,
                                        onTimeSelected = { newEnd ->
                                            viewModel.updateStaffWorkingHoursDay(day, true, startTime, newEnd)
                                        }
                                    )
                                }
                            } else {
                                Text(
                                    text = "Weekly Off",
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

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.saveStaffHours() },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_save_staff_hours"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                enabled = !state.isSavingStaffDetails
            ) {
                if (state.isSavingStaffDetails) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(SalonStrings.get(lang, "save_staff_hours"), fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Services Performed (Checklist)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Services ${staff.name} Can Perform",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Only selected services can be booked with this stylist.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (state.servicesList.isEmpty()) {
                        Text(text = "No services exist yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        state.servicesList.forEach { srv ->
                            val isChecked = state.staffSelectedServiceIds.contains(srv.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleStaffServiceAssignmentForStaff(srv.id) }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { viewModel.toggleStaffServiceAssignmentForStaff(srv.id) },
                                    colors = CheckboxDefaults.colors(checkedColor = TerracottaPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = srv.name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text(text = "₹${srv.price.toInt()} · ${srv.durationMins} mins", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.saveStaffAssignedServices() },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_save_staff_services"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                enabled = !state.isSavingStaffDetails
            ) {
                if (state.isSavingStaffDetails) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(SalonStrings.get(lang, "save_staff_services"), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDropdown(
    time: String,
    onTimeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val times = listOf(
        "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00",
        "18:00", "18:30", "19:00", "19:30", "20:00", "20:30", "21:00", "21:30", "22:00"
    )

    Box {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clickable { expanded = true }
        ) {
            Text(
                text = time,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            times.forEach { t ->
                DropdownMenuItem(
                    text = { Text(t, fontSize = 12.sp) },
                    onClick = {
                        onTimeSelected(t)
                        expanded = false
                    }
                )
            }
        }
    }
}
