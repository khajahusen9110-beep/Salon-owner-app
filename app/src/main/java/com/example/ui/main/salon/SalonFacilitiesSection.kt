package com.example.ui.main.salon

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.auth.AuthUiState
import com.example.ui.auth.AuthViewModel
import com.example.ui.theme.TerracottaPrimary

/** Owner ticks the facilities the salon offers; customers see them and can filter by them. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SalonFacilitiesSection(
    state: AuthUiState,
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("salon_facilities_screen")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("Facilities", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Tick what your salon offers. Customers see these and can search by them.",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (state.amenities.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp).size(24.dp), strokeWidth = 2.dp)
        }

        state.amenities.groupBy { it.groupName }.forEach { (group, items) ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(group, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    if (items.any { it.exclusiveGroup != null }) {
                        Text("Choose one", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items.forEach { a ->
                            val selected = a.id in state.selectedAmenityIds
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.toggleAmenity(a) },
                                label = { Text("${a.icon} ${a.name}", fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TerracottaPrimary.copy(alpha = 0.15f),
                                    selectedLabelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.testTag("amenity_${a.name}")
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = { viewModel.saveAmenities() },
            enabled = !state.isSavingAmenities && state.amenities.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("btn_save_facilities"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
        ) {
            if (state.isSavingAmenities) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            else Text("Save facilities (${state.selectedAmenityIds.size} selected)", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}
