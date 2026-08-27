package com.stignit.app.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stignit.app.data.ApiResult
import com.stignit.app.data.rememberAuthRepository
import com.stignit.app.ui.components.Screen
import com.stignit.app.ui.components.clickableNoRipple
import com.stignit.app.ui.components.StignItButton
import com.stignit.app.ui.components.StignItButtonSize
import com.stignit.app.ui.theme.StignItExtraColors
import kotlinx.coroutines.launch

/** Direct port of src/routes/auth.tsx — phone/email toggle, then verification code send. */
private enum class AuthMode { Phone, Email }

@Composable
fun AuthScreen(onCodeSent: (phone: String, devCode: String?, resendInSec: Int) -> Unit) {
    var mode by remember { mutableStateOf(AuthMode.Phone) }
    var value by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var submitting by remember { mutableStateOf(false) }

    val repo = rememberAuthRepository()
    val scope = rememberCoroutineScope()

    fun sendCode() {
        error = null
        if (mode == AuthMode.Email) {
            error = "Email sign-in isn't available yet — use your phone number."
            return
        }
        if (value.trim().length < 10) {
            error = "Enter a valid mobile number."
            return
        }
        submitting = true
        scope.launch {
            when (val r = repo.requestOtp(value)) {
                is ApiResult.Ok -> {
                    submitting = false
                    onCodeSent(value.trim(), r.value.devCode, r.value.resendInSec)
                }
                is ApiResult.Err -> {
                    error = r.message
                    submitting = false
                }
            }
        }
    }

    Screen(modifier = Modifier.imePadding(), scrollable = false) {
        Spacer(Modifier.height(32.dp))
        Text("Set up your safety profile", fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 34.sp)
        Text(
            "Two steps. Then StignIt starts watching over your trips.",
            modifier = Modifier.padding(top = 8.dp),
            fontSize = 16.sp,
            color = StignItExtraColors.mutedForeground,
        )

        Spacer(Modifier.height(28.dp))

        // Phone / Email segmented toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp))
                .padding(4.dp),
        ) {
            listOf(AuthMode.Phone to "Phone number", AuthMode.Email to "Email").forEach { (m, label) ->
                val selected = mode == m
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            if (selected) MaterialTheme.colorScheme.surface else androidx.compose.ui.graphics.Color.Transparent,
                            RoundedCornerShape(10.dp),
                        )
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickableNoRipple { mode = m; value = "" },
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        AnimatedContent(
            targetState = mode,
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(150)) },
            label = "auth-field",
        ) { mode ->
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(if (mode == AuthMode.Phone) "Mobile number" else "Email address") },
                placeholder = { Text(if (mode == AuthMode.Phone) "+234" else "you@example.com") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (mode == AuthMode.Phone) KeyboardType.Phone else KeyboardType.Email,
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Text(
            error ?: "We only use this to verify you and reach you during an incident.",
            modifier = Modifier.padding(top = 8.dp),
            fontSize = 13.sp,
            color = if (error != null) StignItExtraColors.danger else StignItExtraColors.mutedForeground,
        )

        Spacer(Modifier.weight(1f))

        StignItButton(
            text = if (submitting) "Sending…" else "Send verification code",
            onClick = { sendCode() },
            size = StignItButtonSize.Large,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Text(
            "By continuing you agree to StignIt's Terms and Privacy Policy.",
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            fontSize = 12.sp,
            color = StignItExtraColors.mutedForeground,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

