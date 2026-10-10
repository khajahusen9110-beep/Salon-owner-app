package com.example.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppInfo
import com.example.data.model.SupportTicket

private val CATEGORIES = listOf(
    "payment" to "Payment / payout", "booking" to "Booking", "account" to "My account",
    "app" to "App problem", "salon" to "Salon listing", "other" to "Other"
)

/**
 * Help & Support for salon owners: contact the platform team, raise a request, see replies.
 * Data comes through the view model ([load] / [send]); nothing is stored locally.
 */
@Composable
fun SupportModal(
    load: ((AppInfo?, List<SupportTicket>?, String?) -> Unit) -> Unit,
    send: (category: String, subject: String, message: String, onDone: (String?) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    var info by remember { mutableStateOf<AppInfo?>(null) }
    var tickets by remember { mutableStateOf<List<SupportTicket>?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableStateOf(0) }
    var category by remember { mutableStateOf("payment") }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    LaunchedEffect(reloadKey) {
        load { i, t, e -> info = i; tickets = t; loadError = e }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Help & Support", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("support_close")) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                info?.let { SupportContactCard(it) }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Raise a request", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CATEGORIES.forEach { (key, label) ->
                                FilterChip(selected = category == key, onClick = { category = key },
                                    label = { Text(label, fontSize = 12.sp) }, modifier = Modifier.testTag("support_cat_$key"))
                            }
                        }
                        OutlinedTextField(
                            value = subject, onValueChange = { subject = it.take(120) }, label = { Text("Subject") },
                            singleLine = true, modifier = Modifier.fillMaxWidth().testTag("support_subject"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = message, onValueChange = { message = it.take(2000) }, label = { Text("Describe the problem") },
                            minLines = 4, modifier = Modifier.fillMaxWidth().testTag("support_message"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        result?.let { (ok, text) ->
                            Text(text, fontSize = 12.sp, color = if (ok) Color(0xFF059669) else MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 6.dp))
                        }
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = {
                                sending = true
                                result = null
                                send(category, subject, message) { err ->
                                    sending = false
                                    if (err == null) {
                                        result = true to "Sent. Our team will reply here and in your notifications."
                                        subject = ""
                                        message = ""
                                        reloadKey++
                                    } else result = false to err
                                }
                            },
                            enabled = !sending && subject.trim().length >= 3 && message.trim().length >= 5,
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("support_send"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (sending) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            else Text("Send", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text("My requests", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                when {
                    loadError != null -> Text(loadError ?: "", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    tickets == null -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    tickets!!.isEmpty() -> Text("You have not raised any requests.", fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    else -> tickets!!.forEach { SupportTicketCard(it) }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

/** Support contact in a small dialog (login screen). */
@Composable
fun SupportContactDialog(info: AppInfo?, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Need help?", fontWeight = FontWeight.Bold) },
        text = {
            when {
                info == null -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
                !info.hasContact -> Text("Please try again in a few minutes. If the OTP still does not arrive, check that the number is correct.")
                else -> SupportContactCard(info)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun SupportContactCard(info: AppInfo) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().testTag("support_contact")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Contact us", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            info.supportHours?.let { Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            if (!info.hasContact) {
                Text("Raise a request below and our team will get back to you.", fontSize = 13.sp)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                info.supportPhone?.let { phone ->
                    OutlinedButton(onClick = { context.open("tel:${phone.filter { it.isDigit() || it == '+' }}") }) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp)); Text("Call", fontSize = 12.sp)
                    }
                }
                info.supportWhatsapp?.let { wa ->
                    OutlinedButton(onClick = { context.open("https://wa.me/${wa.filter { it.isDigit() }}") }) {
                        Text("WhatsApp", fontSize = 12.sp)
                    }
                }
                info.supportEmail?.let { mail ->
                    OutlinedButton(onClick = { context.open("mailto:$mail") }) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp)); Text("Email", fontSize = 12.sp)
                    }
                }
            }
            if (info.termsUrl != null || info.privacyUrl != null) {
                Row {
                    info.termsUrl?.let { url -> TextButton(onClick = { context.open(url) }) { Text("Terms", fontSize = 12.sp) } }
                    info.privacyUrl?.let { url -> TextButton(onClick = { context.open(url) }) { Text("Privacy policy", fontSize = 12.sp) } }
                }
            }
        }
    }
}

@Composable
private fun SupportTicketCard(t: SupportTicket) {
    val (label, color) = when (t.status) {
        "resolved" -> "Resolved" to Color(0xFF059669)
        "closed" -> "Closed" to Color(0xFF6B7280)
        "in_progress" -> "In progress" to Color(0xFFD97706)
        else -> "Open" to Color(0xFF2563EB)
    }
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().testTag("ticket_${t.ticketNo}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("#${t.ticketNo} · ${t.subject}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
            }
            Text(t.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3,
                modifier = Modifier.padding(top = 4.dp))
            t.adminReply?.let {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Support team", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(it, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

private fun Context.open(uri: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: Exception) {
        // No app to handle it (e.g. no email app installed).
    }
}
