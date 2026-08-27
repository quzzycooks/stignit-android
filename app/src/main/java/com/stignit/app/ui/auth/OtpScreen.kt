package com.stignit.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stignit.app.BuildConfig
import com.stignit.app.data.ApiResult
import com.stignit.app.data.rememberAuthRepository
import com.stignit.app.ui.components.Screen
import com.stignit.app.ui.components.StignItButton
import com.stignit.app.ui.components.StignItButtonSize
import com.stignit.app.ui.components.TopBar
import com.stignit.app.ui.components.clickableNoRipple
import com.stignit.app.ui.theme.StignItExtraColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val CODE_LEN = 6

/**
 * OTP verification step (PRD 10.1 /auth/otp/verify). Reached from AuthScreen
 * after a code has been requested; on success routes to Home or the profile
 * step depending on `registrationComplete`.
 */
@Composable
fun OtpScreen(
    phone: String,
    initialDevCode: String?,
    initialResendInSec: Int,
    onVerified: (registrationComplete: Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val repo = rememberAuthRepository()
    val scope = rememberCoroutineScope()
    val focus = remember { FocusRequester() }

    var code by remember { mutableStateOf("") }
    var devCode by remember { mutableStateOf(initialDevCode) }
    var error by remember { mutableStateOf<String?>(null) }
    var submitting by remember { mutableStateOf(false) }
    var resendIn by remember { mutableStateOf(initialResendInSec) }

    LaunchedEffect(Unit) { focus.requestFocus() }
    LaunchedEffect(resendIn) {
        if (resendIn > 0) { delay(1000); resendIn-- }
    }

    fun verify() {
        if (code.length != CODE_LEN || submitting) return
        submitting = true
        error = null
        scope.launch {
            when (val r = repo.verifyOtp(phone, code)) {
                is ApiResult.Ok -> onVerified(r.value.registrationComplete)
                is ApiResult.Err -> {
                    error = r.message
                    code = ""
                    submitting = false
                    focus.requestFocus()
                }
            }
        }
    }

    fun resend() {
        if (resendIn > 0 || submitting) return
        error = null
        scope.launch {
            when (val r = repo.requestOtp(phone)) {
                is ApiResult.Ok -> {
                    devCode = r.value.devCode
                    resendIn = r.value.resendInSec
                }
                is ApiResult.Err -> error = r.message
            }
        }
    }

    // Auto-submit once six digits are in.
    LaunchedEffect(code) { if (code.length == CODE_LEN) verify() }

    Screen(modifier = Modifier.imePadding(), scrollable = false) {
        TopBar(onBack = onBack)

        Spacer(Modifier.height(8.dp))
        Text("Enter the 6-digit code", fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 34.sp)
        Text(
            "We sent it by SMS to $phone.",
            modifier = Modifier.padding(top = 8.dp),
            fontSize = 16.sp,
            color = StignItExtraColors.mutedForeground,
        )

        Spacer(Modifier.height(32.dp))

        BasicTextField(
            value = code,
            onValueChange = { new ->
                if (!submitting) {
                    code = new.filter { it.isDigit() }.take(CODE_LEN)
                    error = null
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            // fillMaxWidth so a tap anywhere on the digit-box row lands on the field
            // (without it the field is only as wide as its hidden text and taps on the
            // boxes miss it, leaving the keyboard closed).
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focus),
            decorationBox = {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(CODE_LEN) { i ->
                        val ch = code.getOrNull(i)?.toString() ?: ""
                        val filled = ch.isNotEmpty()
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .background(
                                    if (filled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(12.dp),
                                )
                                .border(
                                    width = if (error != null) 1.5.dp else 1.dp,
                                    color = when {
                                        error != null -> StignItExtraColors.danger
                                        filled -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.outline
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(ch, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
        )

        if (error != null) {
            Text(
                error!!,
                modifier = Modifier.padding(top = 12.dp),
                fontSize = 13.sp,
                color = StignItExtraColors.danger,
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            if (resendIn > 0) "Resend code in ${resendIn}s" else "Resend code",
            modifier = Modifier
                .then(if (resendIn > 0) Modifier else Modifier.clickableNoRipple { resend() }),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (resendIn > 0) StignItExtraColors.mutedForeground else MaterialTheme.colorScheme.primary,
        )

        if (BuildConfig.DEBUG && devCode != null) {
            Text(
                "Dev build — code is $devCode",
                modifier = Modifier.padding(top = 12.dp),
                fontSize = 13.sp,
                color = StignItExtraColors.warning,
            )
        }

        Spacer(Modifier.weight(1f))

        StignItButton(
            text = if (submitting) "Verifying…" else "Verify",
            onClick = { verify() },
            size = StignItButtonSize.Large,
            modifier = Modifier.padding(bottom = 24.dp),
        )
    }
}