package com.stignit.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stignit.app.StignItApplication
import com.stignit.app.data.ApiResult
import com.stignit.app.data.MedicalInfo
import com.stignit.app.data.rememberUserRepository
import com.stignit.app.data.sessionStore
import com.stignit.app.location.rememberLocationPermissionState
import com.stignit.app.notifications.rememberNotificationPermissionState
import com.stignit.app.ui.components.Screen
import com.stignit.app.ui.components.SectionTitle
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
    val context = LocalContext.current
    val session = context.sessionStore()
    val locationPermission = rememberLocationPermissionState()
    val notificationPermission = rememberNotificationPermissionState()
    var proximityAlertsEnabled by remember { mutableStateOf(session.proximityAlertsEnabled) }

    fun setProximityAlerts(enabled: Boolean) {
        proximityAlertsEnabled = enabled
        session.proximityAlertsEnabled = enabled
        val app = context.applicationContext as StignItApplication
        if (enabled) app.startProximityLocationWork() else app.stopProximityLocationWork()
        scope.launch { repo.updateProximityAlertsEnabled(enabled) }
    }

    var loading by remember { mutableStateOf(true) }
    var draft by remember { mutableStateOf<MedicalInfoDraft?>(null) }
    var saving by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        when (val r = repo.getMe()) {
            is ApiResult.Ok -> {
                draft = MedicalInfoDraft(r.value.medicalInfo ?: MedicalInfo())
                // Local cache can be stale (fresh install, or toggled from another
                // device) — the server's copy is the source of truth for whether
                // ambient location reporting should actually be running.
                val serverValue = r.value.proximityAlertsEnabled
                if (serverValue != session.proximityAlertsEnabled) {
                    session.proximityAlertsEnabled = serverValue
                    proximityAlertsEnabled = serverValue
                    val app = context.applicationContext as StignItApplication
                    if (serverValue) app.startProximityLocationWork() else app.stopProximityLocationWork()
                }
            }
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
        TopBar(title = "Medical Profile", onBack = onBack)

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(8.dp))
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

            SectionTitle("Proximity alerts", modifier = Modifier.padding(top = 32.dp))
            Row(
                modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Get notified about nearby incidents", fontSize = 15.sp)
                    Text(
                        "You might be able to help — off by default. Separate from crash detection.",
                        modifier = Modifier.padding(top = 2.dp),
                        fontSize = 13.sp,
                        color = StignItExtraColors.mutedForeground,
                    )
                }
                Switch(
                    checked = proximityAlertsEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            if (!notificationPermission.isGranted.value) notificationPermission.request()
                            if (!locationPermission.isGranted.value) locationPermission.request()
                        }
                        setProximityAlerts(enabled)
                    },
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
