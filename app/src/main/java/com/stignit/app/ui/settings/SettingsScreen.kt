package com.stignit.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stignit.app.data.ApiResult
import com.stignit.app.data.MedicalInfo
import com.stignit.app.data.rememberUserRepository
import com.stignit.app.ui.components.Screen
import com.stignit.app.ui.components.StignItButton
import com.stignit.app.ui.components.StignItButtonSize
import com.stignit.app.ui.components.TopBar
import com.stignit.app.ui.medical.MedicalInfoDraft
import com.stignit.app.ui.medical.MedicalInfoForm
import com.stignit.app.ui.theme.StignItExtraColors
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val repo = rememberUserRepository()
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var draft by remember { mutableStateOf<MedicalInfoDraft?>(null) }
    var saving by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        when (val r = repo.getMe()) {
            is ApiResult.Ok -> draft = MedicalInfoDraft(r.value.medicalInfo ?: MedicalInfo())
            is ApiResult.Err -> error = r.message
        }
        loading = false
    }

    fun save() {
        val current = draft ?: return
        if (saving) return
        saving = true
        error = null
        saved = false
        scope.launch {
            when (val r = repo.updateMedicalInfo(current.toMedicalInfo())) {
                is ApiResult.Ok -> {
                    saving = false
                    saved = true
                }
                is ApiResult.Err -> {
                    saving = false
                    error = r.message
                }
            }
        }
    }

    Screen(modifier = Modifier.imePadding(), scrollable = false) {
        TopBar(title = "Settings", onBack = onBack)

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(8.dp))
            Text("Medical profile", fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Text(
                "Only visible to a verified Skilled Responder while you're a participant in an open incident.",
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                fontSize = 14.sp,
                color = StignItExtraColors.mutedForeground,
            )

            when {
                loading -> Text("Loading…", color = StignItExtraColors.mutedForeground)
                draft != null -> MedicalInfoForm(draft!!)
                else -> Text(error ?: "Couldn't load your profile.", color = StignItExtraColors.danger)
            }

            if (error != null && !loading && draft != null) {
                Text(
                    error!!,
                    modifier = Modifier.padding(top = 16.dp),
                    fontSize = 13.sp,
                    color = StignItExtraColors.danger,
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        if (draft != null) {
            StignItButton(
                text = if (saving) "Saving…" else if (saved) "Saved" else "Save changes",
                onClick = { save() },
                size = StignItButtonSize.Large,
                modifier = Modifier.padding(bottom = 24.dp),
            )
        }
    }
}
