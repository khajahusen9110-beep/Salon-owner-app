package com.example.ui.main.salon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.auth.AuthUiState
import com.example.ui.auth.AuthViewModel
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TerracottaPrimary
import com.example.util.SalonStrings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SalonProfileSection(
    state: AuthUiState,
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = state.language
    val salon = state.salon
    val isVerified = salon?.verificationStatus == "approved"
    var showAddPhotoDialog by remember { mutableStateOf(false) }
    var newPhotoUrlInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("salon_profile_edit_screen")
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
            Text(
                text = SalonStrings.get(lang, "profile_edit_title"),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Verification & Rating Status Card (Read-Only Badges)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Verification Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isVerified) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = SalonStrings.get(lang, "verified_partner_badge"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SuccessGreen
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = SalonStrings.get(lang, "under_review_badge"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TerracottaPrimary
                        )
                    }
                }

                // Rating & Review Count
                val rating = salon?.ratingAvg ?: 0.0
                val count = salon?.ratingCount ?: 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (count == 0) "New" else "%.1f".format(rating),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "($count ${SalonStrings.get(lang, "reviews_count")})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Salon Active / Booking Toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = SalonStrings.get(lang, "salon_status_toggle"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = SalonStrings.get(lang, "salon_status_toggle_desc"),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.editSalonIsActive,
                    onCheckedChange = { viewModel.toggleSalonActiveState(it) },
                    modifier = Modifier.testTag("switch_salon_active_profile"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SuccessGreen
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Photos Showcase & Cover Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = SalonStrings.get(lang, "profile_photos_title"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = SalonStrings.get(lang, "profile_photos_desc"),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { showAddPhotoDialog = true },
                        modifier = Modifier.testTag("btn_open_add_photo"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(SalonStrings.get(lang, "btn_add_photo"), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Photos Grid
                if (state.editSalonPhotos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No photos uploaded yet. Tap + Add Photo.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        state.editSalonPhotos.forEachIndexed { index, photoUrl ->
                            val isCover = index == state.editCoverPhotoIndex
                            Box(
                                modifier = Modifier
                                    .size(105.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = if (isCover) 3.dp else 1.dp,
                                        color = if (isCover) TerracottaPrimary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = "Salon Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxWidth().height(105.dp)
                                )

                                // Cover badge
                                if (isCover) {
                                    Surface(
                                        color = TerracottaPrimary,
                                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                                        modifier = Modifier.align(Alignment.TopStart)
                                    ) {
                                        Text(
                                            text = SalonStrings.get(lang, "badge_cover_photo"),
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                // Delete Button
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .clickable { viewModel.removeSalonPhoto(index) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                // Set Cover button if not cover
                                if (!isCover) {
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(topStart = 8.dp),
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .clickable { viewModel.setCoverPhotoIndex(index) }
                                    ) {
                                        Text(
                                            text = SalonStrings.get(lang, "btn_set_cover"),
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Core Profile Form Fields
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Salon Name
                OutlinedTextField(
                    value = state.editSalonName,
                    onValueChange = { viewModel.updateProfileName(it) },
                    label = { Text("Salon Name") },
                    modifier = Modifier.fillMaxWidth().testTag("input_edit_salon_name"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Salon Type Dropdown
                var salonTypeExpanded by remember { mutableStateOf(false) }
                val types = listOf("unisex" to "Unisex Salon", "women" to "Women Only", "men" to "Men / Barber")
                ExposedDropdownMenuBox(
                    expanded = salonTypeExpanded,
                    onExpandedChange = { salonTypeExpanded = !salonTypeExpanded }
                ) {
                    OutlinedTextField(
                        value = types.find { it.first == state.editSalonType }?.second ?: "Unisex Salon",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Salon Category / Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = salonTypeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = salonTypeExpanded,
                        onDismissRequest = { salonTypeExpanded = false }
                    ) {
                        types.forEach { (typeKey, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    viewModel.updateProfileType(typeKey)
                                    salonTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                OutlinedTextField(
                    value = state.editSalonDescription,
                    onValueChange = { viewModel.updateProfileDescription(it) },
                    label = { Text(SalonStrings.get(lang, "profile_description_label")) },
                    placeholder = { Text(SalonStrings.get(lang, "profile_description_hint")) },
                    modifier = Modifier.fillMaxWidth().testTag("input_edit_salon_desc"),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Phone
                OutlinedTextField(
                    value = state.editSalonPhone,
                    onValueChange = { viewModel.updateProfilePhone(it) },
                    label = { Text("Contact Phone") },
                    modifier = Modifier.fillMaxWidth().testTag("input_edit_salon_phone"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Address
                OutlinedTextField(
                    value = state.editSalonAddress,
                    onValueChange = { viewModel.updateProfileAddress(it) },
                    label = { Text("Address / Shop No / Street") },
                    modifier = Modifier.fillMaxWidth().testTag("input_edit_salon_address"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Area & City
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.editSalonArea,
                        onValueChange = { viewModel.updateProfileArea(it) },
                        label = { Text("Area / Locality") },
                        modifier = Modifier.weight(1f).testTag("input_edit_salon_area"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = state.editSalonCity,
                        onValueChange = { viewModel.updateProfileCity(it) },
                        label = { Text("City") },
                        modifier = Modifier.weight(1f).testTag("input_edit_salon_city"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pincode & GST
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.editSalonPincode,
                        onValueChange = { viewModel.updateProfilePincode(it) },
                        label = { Text("Pincode") },
                        modifier = Modifier.weight(1f).testTag("input_edit_salon_pincode"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = state.editSalonGst,
                        onValueChange = { viewModel.updateProfileGst(it) },
                        label = { Text("GST Number (Optional)") },
                        modifier = Modifier.weight(1f).testTag("input_edit_salon_gst"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Save Button
        Button(
            onClick = { viewModel.saveSalonProfile() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_save_salon_profile"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
            enabled = !state.isSavingSalonProfile
        ) {
            if (state.isSavingSalonProfile) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(
                    text = SalonStrings.get(lang, "btn_save_profile"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Add Photo Dialog
    if (showAddPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showAddPhotoDialog = false },
            title = { Text("Add Showcase Photo") },
            text = {
                Column {
                    Text(
                        text = "Enter direct image URL (or choose from curated showcase options below):",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPhotoUrlInput,
                        onValueChange = { newPhotoUrlInput = it },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Quick Presets:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    val presets = listOf(
                        "Salon Interior" to "https://images.unsplash.com/photo-1560066984-138dadb4c035?w=800",
                        "Styling Stations" to "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=800",
                        "Spa & Wash Area" to "https://images.unsplash.com/photo-1580618672591-eb180b1a973f?w=800",
                        "Barber Chair" to "https://images.unsplash.com/photo-1503951914875-452162b0f3f1?w=800"
                    )

                    presets.forEach { (label, url) ->
                        OutlinedButton(
                            onClick = { newPhotoUrlInput = url },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(label, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPhotoUrlInput.isNotBlank()) {
                            viewModel.addSalonPhotoUrl(newPhotoUrlInput)
                            newPhotoUrlInput = ""
                            showAddPhotoDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    Text("Add Photo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPhotoDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
