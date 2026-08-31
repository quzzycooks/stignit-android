package com.stignit.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stignit.app.data.ApiResult
import com.stignit.app.data.rememberUserRepository
import com.stignit.app.ui.components.Screen
import com.stignit.app.ui.components.StignItButton
import com.stignit.app.ui.components.StignItButtonSize
import com.stignit.app.ui.components.StignItButtonVariant
import com.stignit.app.ui.components.clickableNoRipple
import com.stignit.app.ui.medical.MedicalInfoForm
import com.stignit.app.ui.medical.rememberMedicalInfoDraft
import com.stignit.app.ui.theme.StignItExtraColors
import kotlinx.coroutines.launch

/**
 * Shown once, right after registration succeeds — entirely optional. Skipping
 * leaves the account fully usable; `medicalInfoComplete` just stays false so
 * Home can nudge the user to finish it later from Settings.
 */
@Composable
fun MedicalInfoStepScreen(onDone: () -> Unit) {
    val repo = rememberUserRepository()
    val scope = rememberCoroutineScope()
    val draft = rememberMedicalInfoDraft()
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun save() {
        if (submitting) return
        submitting = true
        error = null
        scope.launch {
            when (val r = repo.updateMedicalInfo(draft.toMedicalInfo())) {
                is ApiResult.Ok -> onDone()
                is ApiResult.Err -> {
                    error = r.message
                    submitting = false
                }
            }
        }
    }

    Screen(modifier = Modifier.imePadding(), scrollable = false) {
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(8.dp))
            Text("Medical information", fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 34.sp)
            Text(
                "Optional, but it helps a verified responder treat you correctly. Only visible during an open incident you're part of.",
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 15.sp,
                color = StignItExtraColors.mutedForeground,
            )

            Spacer(Modifier.height(20.dp))
            MedicalInfoForm(draft)

            if (error != null) {
                Text(
                    error!!,
                    modifier = Modifier.padding(top = 16.dp),
                    fontSize = 13.sp,
                    color = StignItExtraColors.danger,
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        StignItButton(
            text = if (submitting) "Saving…" else "Continue",
            onClick = { save() },
            size = StignItButtonSize.Large,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Text(
            "Skip for now",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .clickableNoRipple { if (!submitting) onDone() },
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = StignItExtraColors.mutedForeground,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}
