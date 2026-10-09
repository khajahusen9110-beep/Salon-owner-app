package com.example.ui.main.salon

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
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
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SalonReview
import com.example.ui.auth.AuthUiState
import com.example.ui.auth.AuthViewModel
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TerracottaPrimary
import com.example.util.SalonStrings

@Composable
fun SalonReviewsSection(
    state: AuthUiState,
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = state.language
    var filterType by remember { mutableStateOf("all") } // "all", "pending", "5", "4"

    val reviews = state.salonReviews
    val pendingCount = reviews.count { it.ownerReply.isNullOrBlank() }
    val avgRating = if (reviews.isNotEmpty()) reviews.map { it.rating }.average() else 0.0

    val displayedReviews = when (filterType) {
        "pending" -> reviews.filter { it.ownerReply.isNullOrBlank() }
        "5" -> reviews.filter { it.rating >= 4.5 }
        "4" -> reviews.filter { it.rating in 3.5..4.4 }
        else -> reviews
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("reviews_section_screen")
    ) {
        // Header with Back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("reviews_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = SalonStrings.get(lang, "reviews_title"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${reviews.size} ${SalonStrings.get(lang, "reviews_count")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = { viewModel.loadSalonReviews() },
                modifier = Modifier.testTag("refresh_reviews_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Reviews",
                    tint = TerracottaPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Overall Rating Hero Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("overall_rating_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = SalonStrings.get(lang, "reviews_avg_rating"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "%.1f".format(avgRating),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        RatingStarsRow(rating = avgRating, starSize = 18)
                    }
                    Text(
                        text = "Based on verified completed appointments",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (pendingCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(TerracottaPrimary.copy(alpha = 0.12f))
                            .border(1.dp, TerracottaPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$pendingCount",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TerracottaPrimary
                            )
                            Text(
                                text = SalonStrings.get(lang, "reply_pending_badge"),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TerracottaPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Filter Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterType == "all",
                onClick = { filterType = "all" },
                label = { Text("All (${reviews.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TerracottaPrimary,
                    selectedLabelColor = Color.White
                )
            )

            FilterChip(
                selected = filterType == "pending",
                onClick = { filterType = "pending" },
                label = { Text("Pending ($pendingCount)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TerracottaPrimary,
                    selectedLabelColor = Color.White
                )
            )

            FilterChip(
                selected = filterType == "5",
                onClick = { filterType = "5" },
                label = { Text("5 ★") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TerracottaPrimary,
                    selectedLabelColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content: Loading, Error, Empty or List
        if (state.isLoadingReviews) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TerracottaPrimary)
            }
        } else if (state.reviewsError != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
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
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.reviewsError ?: "Failed to load reviews",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { viewModel.loadSalonReviews() }) {
                        Text(SalonStrings.get(lang, "offline_retry"))
                    }
                }
            }
        } else if (displayedReviews.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RateReview,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = SalonStrings.get(lang, "reviews_empty"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayedReviews, key = { it.id }) { review ->
                    ReviewItemCard(
                        review = review,
                        lang = lang,
                        onReplyClick = { viewModel.startReplyToReview(review.id) }
                    )
                }
            }
        }
    }

    // Reply Dialog
    if (state.replyingReviewId != null) {
        val targetReview = reviews.find { it.id == state.replyingReviewId }
        AlertDialog(
            onDismissRequest = { viewModel.cancelReplyToReview() },
            title = {
                Text(
                    text = SalonStrings.get(lang, "reply_dialog_title"),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    if (targetReview != null) {
                        Text(
                            text = "Customer: ${targetReview.customerName}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "\"${targetReview.comment}\"",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = state.replyInputText,
                        onValueChange = { viewModel.updateReplyInputText(it) },
                        placeholder = { Text(SalonStrings.get(lang, "reply_input_placeholder")) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("reply_input_field"),
                        maxLines = 5,
                        isError = state.replyError != null
                    )

                    if (state.replyError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.replyError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.submitReviewReply() },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    enabled = !state.isSubmittingReply,
                    modifier = Modifier.testTag("submit_reply_button")
                ) {
                    if (state.isSubmittingReply) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(SalonStrings.get(lang, "btn_submit_reply"))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.cancelReplyToReview() }
                ) {
                    Text(SalonStrings.get(lang, "btn_cancel"))
                }
            }
        )
    }
}

@Composable
fun ReviewItemCard(
    review: SalonReview,
    lang: String,
    onReplyClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("review_card_${review.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Customer Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val custName = review.customerName ?: "Customer"
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TerracottaPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = custName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = custName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val formattedDate = review.createdAt?.take(10) ?: ""
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                RatingStarsRow(rating = review.rating, starSize = 14)
            }

            // Stylist info tag
            if (!review.staffName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Stylist: ",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = review.staffName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TerracottaPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Review comment
            Text(
                text = review.comment ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )


            // Owner Reply if available, or Reply button
            Spacer(modifier = Modifier.height(12.dp))

            if (!review.ownerReply.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = SalonStrings.get(lang, "review_reply_label"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = review.ownerReply,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFFF3E0))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = SalonStrings.get(lang, "reply_pending_badge"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    }

                    Button(
                        onClick = onReplyClick,
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("reply_btn_${review.id}")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Reply,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = SalonStrings.get(lang, "btn_reply"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RatingStarsRow(rating: Double, starSize: Int = 16) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val fullStars = rating.toInt()
        val hasHalf = (rating - fullStars) >= 0.5

        for (i in 1..5) {
            when {
                i <= fullStars -> {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(starSize.dp)
                    )
                }
                i == fullStars + 1 && hasHalf -> {
                    Icon(
                        imageVector = Icons.Default.StarHalf,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(starSize.dp)
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(starSize.dp)
                    )
                }
            }
        }
    }
}
