package com.example.ui.main.salon

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SalonService
import com.example.data.model.ServiceCategory
import com.example.ui.auth.AuthUiState
import com.example.ui.auth.AuthViewModel
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.TerracottaPrimary
import com.example.util.SalonStrings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SalonServicesSection(
    state: AuthUiState,
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = state.language
    val categories = state.categoriesList
    val services = state.servicesList
    val staffList = state.staffList

    var categoryToDelete by remember { mutableStateOf<ServiceCategory?>(null) }
    var serviceToDelete by remember { mutableStateOf<SalonService?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("salon_services_management_screen")
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
            Column {
                Text(
                    text = SalonStrings.get(lang, "section_services"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${services.size} ${SalonStrings.get(lang, "services_count")} · ${categories.size} ${SalonStrings.get(lang, "categories_count")}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Categories Management Card
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
                    Text(
                        text = SalonStrings.get(lang, "categories_title"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Button(
                        onClick = { viewModel.openAddCategoryDialog() },
                        modifier = Modifier.testTag("btn_add_category"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(SalonStrings.get(lang, "btn_add_category"), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (categories.isEmpty()) {
                    Text(
                        text = "No categories yet. Click + Add Category to organize your catalog.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.testTag("cat_chip_${cat.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${cat.name} (${cat.sortOrder})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { viewModel.openEditCategoryDialog(cat) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Category",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { categoryToDelete = cat },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Category",
                                            tint = ErrorRed,
                                            modifier = Modifier.size(14.dp)
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

        // Services Catalog Header with Add Service Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = SalonStrings.get(lang, "services_catalog_title"),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Button(
                onClick = { viewModel.openAddServiceDialog() },
                modifier = Modifier.testTag("btn_add_service"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(SalonStrings.get(lang, "btn_add_service"), fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Group services by category
        val categoryGroups = mutableMapOf<String, MutableList<SalonService>>()
        categories.forEach { categoryGroups[it.name] = mutableListOf() }
        val uncategorized = mutableListOf<SalonService>()

        services.forEach { srv ->
            val catName = categories.find { it.id == srv.categoryId }?.name ?: srv.category
            if (categoryGroups.containsKey(catName)) {
                categoryGroups[catName]?.add(srv)
            } else {
                uncategorized.add(srv)
            }
        }

        if (services.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No services added yet. Tap '+ Add Service' to create your menu.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            // Render each category group
            categoryGroups.forEach { (catName, srvList) ->
                if (srvList.isNotEmpty()) {
                    CategoryServiceGroupView(
                        categoryName = catName,
                        services = srvList,
                        allStaff = staffList,
                        lang = lang,
                        onEditService = { viewModel.openEditServiceDialog(it) },
                        onDeleteService = { serviceToDelete = it }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            if (uncategorized.isNotEmpty()) {
                CategoryServiceGroupView(
                    categoryName = "General / Other",
                    services = uncategorized,
                    allStaff = staffList,
                    lang = lang,
                    onEditService = { viewModel.openEditServiceDialog(it) },
                    onDeleteService = { serviceToDelete = it }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Add / Edit Category Dialog
    if (state.showAddCategoryDialog) {
        var catName by remember { mutableStateOf(state.categoryFormName) }
        var sortOrder by remember { mutableStateOf(state.categoryFormSortOrder) }

        AlertDialog(
            onDismissRequest = { viewModel.closeCategoryDialog() },
            title = {
                Text(
                    text = if (state.categoryBeingEdited == null) SalonStrings.get(lang, "add_category_title") else SalonStrings.get(lang, "edit_category_title"),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        label = { Text(SalonStrings.get(lang, "category_name_label")) },
                        modifier = Modifier.fillMaxWidth().testTag("input_category_name"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = sortOrder,
                        onValueChange = { sortOrder = it },
                        label = { Text(SalonStrings.get(lang, "category_sort_order_label")) },
                        modifier = Modifier.fillMaxWidth().testTag("input_category_sort_order"),
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (catName.isNotBlank()) {
                            viewModel.saveCategory(catName, sortOrder)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    modifier = Modifier.testTag("btn_save_category")
                ) {
                    Text(SalonStrings.get(lang, "save"))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeCategoryDialog() }) {
                    Text(SalonStrings.get(lang, "cancel"))
                }
            }
        )
    }

    // Delete Category Confirmation Dialog
    if (categoryToDelete != null) {
        val cat = categoryToDelete!!
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text(SalonStrings.get(lang, "delete_category_confirm")) },
            text = { Text("Are you sure you want to delete category '${cat.name}'? Services in this category will remain as uncategorized.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCategory(cat.id)
                        categoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text(SalonStrings.get(lang, "delete"))
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text(SalonStrings.get(lang, "cancel"))
                }
            }
        )
    }

    // Delete Service Confirmation Dialog
    if (serviceToDelete != null) {
        val srv = serviceToDelete!!
        AlertDialog(
            onDismissRequest = { serviceToDelete = null },
            title = { Text(SalonStrings.get(lang, "delete_service_confirm")) },
            text = { Text("Are you sure you want to remove '${srv.name}' from your service menu?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSalonService(srv.id)
                        serviceToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text(SalonStrings.get(lang, "delete"))
                }
            },
            dismissButton = {
                TextButton(onClick = { serviceToDelete = null }) {
                    Text(SalonStrings.get(lang, "cancel"))
                }
            }
        )
    }

    // Add / Edit Service Dialog
    if (state.showAddEditServiceDialog) {
        AddEditServiceDialog(
            state = state,
            viewModel = viewModel,
            onDismiss = { viewModel.closeServiceDialog() },
            lang = lang
        )
    }
}

@Composable
fun CategoryServiceGroupView(
    categoryName: String,
    services: List<SalonService>,
    allStaff: List<com.example.data.model.Staff>,
    lang: String,
    onEditService: (SalonService) -> Unit,
    onDeleteService: (SalonService) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = categoryName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TerracottaPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            services.forEachIndexed { index, srv ->
                if (index > 0) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = srv.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "₹${srv.price.toInt()}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${srv.durationMins ?: 30}m",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val buf = srv.bufferMins ?: 0
                            if (buf > 0) {
                                Text(
                                    text = " (+${buf}m buffer)",
                                    fontSize = 11.sp,
                                    color = TerracottaPrimary
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Stylists assigned count
                            val assignedCount = srv.assignedStaffIds.size
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (assignedCount == allStaff.size && allStaff.isNotEmpty()) {
                                    SalonStrings.get(lang, "all_staff_assigned")
                                } else {
                                    "$assignedCount ${SalonStrings.get(lang, "staff_assigned_count")}"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Action buttons
                    Row {
                        IconButton(
                            onClick = { onEditService(srv) },
                            modifier = Modifier.size(32.dp).testTag("btn_edit_service_${srv.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Service",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { onDeleteService(srv) },
                            modifier = Modifier.size(32.dp).testTag("btn_delete_service_${srv.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Service",
                                tint = ErrorRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditServiceDialog(
    state: AuthUiState,
    viewModel: AuthViewModel,
    onDismiss: () -> Unit,
    lang: String
) {
    val isEditing = state.serviceBeingEdited != null
    val categories = state.categoriesList
    val staffList = state.staffList

    // Fixed duration options: strictly 30, 60, 90, 120, 150, 180, 210, 240 mins
    // Any 5-minute step is allowed by the database (5-480); these are the common choices.
    val validDurations = listOf(15, 20, 30, 40, 45, 60, 75, 90, 105, 120, 150, 180, 210, 240, 300)
    val bufferOptions = listOf(0, 5, 10, 15, 20, 30)

    var durationExpanded by remember { mutableStateOf(false) }
    var bufferExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) SalonStrings.get(lang, "edit_service_title") else SalonStrings.get(lang, "add_service_title"),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Name
                OutlinedTextField(
                    value = state.serviceFormName,
                    onValueChange = { viewModel.updateServiceFormName(it) },
                    label = { Text(SalonStrings.get(lang, "service_name_label")) },
                    modifier = Modifier.fillMaxWidth().testTag("input_service_name"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    val currentCatName = categories.find { it.id == state.serviceFormCategoryId }?.name ?: "Select Category"
                    OutlinedTextField(
                        value = currentCatName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(SalonStrings.get(lang, "service_category_dropdown")) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    viewModel.updateServiceFormCategory(cat.id)
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Price (₹)
                OutlinedTextField(
                    value = state.serviceFormPrice,
                    onValueChange = { viewModel.updateServiceFormPrice(it) },
                    label = { Text(SalonStrings.get(lang, "service_price_label")) },
                    prefix = { Text("₹ ") },
                    modifier = Modifier.fillMaxWidth().testTag("input_service_price"),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // FIXED Duration Dropdown (Enforced strictly: 30, 60, 90, 120, 150, 180, 210, 240 mins)
                ExposedDropdownMenuBox(
                    expanded = durationExpanded,
                    onExpandedChange = { durationExpanded = !durationExpanded }
                ) {
                    OutlinedTextField(
                        value = "${state.serviceFormDurationMins} ${SalonStrings.get(lang, "minutes_abbr")}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(SalonStrings.get(lang, "service_duration_label")) },
                        supportingText = { Text(SalonStrings.get(lang, "service_duration_hint"), fontSize = 10.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = durationExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth().testTag("dropdown_service_duration"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = durationExpanded,
                        onDismissRequest = { durationExpanded = false }
                    ) {
                        validDurations.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(if (d < 60) "$d min" else if (d % 60 == 0) "${d / 60} hr" else "${d / 60} hr ${d % 60} min") },
                                onClick = {
                                    viewModel.updateServiceFormDuration(d)
                                    durationExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Buffer Time Dropdown
                ExposedDropdownMenuBox(
                    expanded = bufferExpanded,
                    onExpandedChange = { bufferExpanded = !bufferExpanded }
                ) {
                    OutlinedTextField(
                        value = "${state.serviceFormBufferMins} ${SalonStrings.get(lang, "minutes_abbr")}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(SalonStrings.get(lang, "service_buffer_label")) },
                        supportingText = { Text(SalonStrings.get(lang, "service_buffer_hint"), fontSize = 10.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bufferExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = bufferExpanded,
                        onDismissRequest = { bufferExpanded = false }
                    ) {
                        bufferOptions.forEach { b ->
                            DropdownMenuItem(
                                text = { Text("$b minutes") },
                                onClick = {
                                    viewModel.updateServiceFormBuffer(b)
                                    bufferExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Staff Assignment Section with "Select All Staff" Shortcut
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = SalonStrings.get(lang, "assign_staff_title"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    TextButton(
                        onClick = { viewModel.selectAllStaffForService() },
                        modifier = Modifier.testTag("btn_select_all_staff")
                    ) {
                        Text(SalonStrings.get(lang, "select_all_staff"), fontSize = 12.sp, color = TerracottaPrimary)
                    }
                }

                staffList.forEach { staff ->
                    // All-rounders always do every service; their tick is fixed.
                    val isChecked = staff.doesAllServices || state.serviceFormStaffIds.contains(staff.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !staff.doesAllServices) { viewModel.toggleServiceStaffAssignment(staff.id) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { viewModel.toggleServiceStaffAssignment(staff.id) },
                            enabled = !staff.doesAllServices,
                            colors = CheckboxDefaults.colors(checkedColor = TerracottaPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = staff.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (staff.doesAllServices) "(all services)" else "(${staff.role ?: "Stylist"})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.saveSalonService() },
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                modifier = Modifier.testTag("btn_save_service_submit"),
                enabled = !state.isSavingService
            ) {
                if (state.isSavingService) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(SalonStrings.get(lang, "save"))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(SalonStrings.get(lang, "cancel"))
            }
        }
    )
}
