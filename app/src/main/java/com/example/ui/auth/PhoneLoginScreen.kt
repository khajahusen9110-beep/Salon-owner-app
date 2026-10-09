package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TerracottaDark
import com.example.ui.theme.TerracottaPrimary
import kotlinx.coroutines.delay

private fun t(lang: String, en: String, hi: String) = if (lang == "hi") hi else en

/**
 * Owner login/registration with mobile number + SMS OTP. A number that has no account yet gets one
 * after the OTP is verified and continues to salon registration.
 */
@Composable
fun PhoneLoginScreen(
    state: AuthUiState,
    onSendOtp: (String) -> Unit,
    onVerifyOtp: (String) -> Unit,
    onChangeNumber: () -> Unit,
    onUseEmail: () -> Unit,
    onLanguageChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = state.language
    var mobile by remember { mutableStateOf(state.otpMobile) }
    var otp by remember { mutableStateOf("") }
    var resendIn by remember { mutableIntStateOf(0) }

    // Start the resend timer whenever an OTP has just been sent.
    LaunchedEffect(state.otpSent, state.otpMobile) {
        if (state.otpSent) {
            otp = ""
            resendIn = 30
        }
    }
    LaunchedEffect(resendIn) {
        if (resendIn > 0) {
            delay(1000)
            resendIn -= 1
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
                    LanguagePillItem(label = "EN", selected = lang == "en", onClick = { onLanguageChange("en") })
                    Spacer(modifier = Modifier.width(4.dp))
                    LanguagePillItem(label = "हिंदी", selected = lang == "hi", onClick = { onLanguageChange("hi") })
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(TerracottaPrimary, TerracottaDark))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ContentCut, contentDescription = null, tint = Color(0xFFFDE8E1), modifier = Modifier.size(42.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = t(lang, "Salon Partner", "सैलून पार्टनर"),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 26.sp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (!state.otpSent) t(lang, "Log in or register with your mobile number", "मोबाइल नंबर से लॉग इन या रजिस्टर करें")
                else t(lang, "Enter the OTP sent to +91 ${state.otpMobile}", "+91 ${state.otpMobile} पर भेजा गया OTP डालें"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth().testTag("phone_login_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    state.errorMessage?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                    if (!state.otpSent) {
                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { v -> mobile = v.filter { it.isDigit() }.take(10) },
                            label = { Text(t(lang, "Mobile number", "मोबाइल नंबर")) },
                            prefix = { Text("+91 ", fontWeight = FontWeight.SemiBold) },
                            leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth().testTag("owner_phone_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PrimaryButton(t(lang, "Send OTP", "OTP भेजें"), state.isLoading, "owner_send_otp") { onSendOtp(mobile) }
                        Text(
                            t(lang, "New here? The same OTP creates your account.", "नए हैं? इसी OTP से आपका अकाउंट बन जाएगा।"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    } else {
                        OutlinedTextField(
                            value = otp,
                            onValueChange = { v -> otp = v.filter { it.isDigit() }.take(6) },
                            label = { Text("OTP") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            textStyle = LocalTextStyle.current.copy(letterSpacing = 6.sp, fontSize = 20.sp),
                            modifier = Modifier.fillMaxWidth().testTag("owner_otp_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PrimaryButton(t(lang, "Verify & continue", "वेरिफाई करें"), state.isLoading, "owner_verify_otp") { onVerifyOtp(otp) }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = onChangeNumber, enabled = !state.isLoading) {
                                Text(t(lang, "Change number", "नंबर बदलें"))
                            }
                            TextButton(onClick = { onSendOtp(state.otpMobile) }, enabled = !state.isLoading && resendIn == 0) {
                                Text(
                                    if (resendIn > 0) t(lang, "Resend in ${resendIn}s", "${resendIn}s में दोबारा भेजें")
                                    else t(lang, "Resend OTP", "OTP दोबारा भेजें")
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onUseEmail, modifier = Modifier.testTag("use_email_login")) {
                Text(
                    t(lang, "Registered earlier with email? Log in with email", "पहले ईमेल से रजिस्टर किया था? ईमेल से लॉग इन करें"),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PrimaryButton(text: String, loading: Boolean, tag: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !loading,
        modifier = Modifier.fillMaxWidth().height(52.dp).testTag(tag),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
    ) {
        if (loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
        else Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
