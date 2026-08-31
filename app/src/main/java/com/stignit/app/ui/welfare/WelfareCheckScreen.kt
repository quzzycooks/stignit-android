package com.stignit.app.ui.welfare

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stignit.app.data.ApiResult
import com.stignit.app.data.DRILL_INCIDENT_ID
import com.stignit.app.data.rememberIncidentRepository
import com.stignit.app.location.LocationTracker
import com.stignit.app.ui.components.Screen
import com.stignit.app.ui.components.ScreenTone
import com.stignit.app.ui.components.StignItButton
import com.stignit.app.ui.components.StignItButtonSize
import com.stignit.app.ui.components.StignItButtonVariant
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val INCIDENT_TYPE_UNKNOWN = "UNKNOWN"

/**
 * Direct port of src/routes/welfare-check.tsx — the highest-stakes screen
 * in the app. Full-bleed danger-red background, large countdown, two
 * unmistakable actions.
 *
 * GPS must never block getting help: [LocationTracker.bestEffortFix] tries a
 * cached fix first (near-instant), falls back to a short-timeout fresh fix,
 * and the incident is created either way — even with no location at all.
 *
 * [isDrill] short-circuits all of that: no real incident, no location call, no
 * emergency-contact SMS — just the countdown UI. Every current entry point
 * (Home's "Simulate impact", Safety's "Start Drill") is a drill; a real
 * crash-trigger entry point would pass `isDrill = false`.
 */
@Composable
fun WelfareCheckScreen(
    onImOk: () -> Unit,
    onGetHelp: (incidentId: String) -> Unit,
    isDrill: Boolean = true,
) {
    var seconds by remember { mutableStateOf(30) }
    var triggering by remember { mutableStateOf(false) }
    var triggerError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val locationTracker = remember(context) { LocationTracker(context) }
    val incidents = rememberIncidentRepository()
    val scope = rememberCoroutineScope()

    fun triggerHelp() {
        if (triggering) return
        if (isDrill) {
            onGetHelp(DRILL_INCIDENT_ID)
            return
        }
        triggering = true
        triggerError = null
        scope.launch {
            val fix = locationTracker.bestEffortFix()
            // Never give up silently — a real emergency can't dead-end on a flaky network.
            var attempt = 0
            while (true) {
                when (val r = incidents.createIncident(INCIDENT_TYPE_UNKNOWN, fix)) {
                    is ApiResult.Ok -> {
                        onGetHelp(r.value.incidentId)
                        return@launch
                    }
                    is ApiResult.Err -> {
                        attempt++
                        if (attempt >= 4) {
                            triggering = false
                            triggerError = "Couldn't reach StignIt — ${r.message} Tap to try again."
                            return@launch
                        }
                        delay(1500L * attempt)
                    }
                }
            }
        }
    }

    LaunchedEffect(seconds) {
        if (seconds > 0) {
            delay(1000)
            seconds -= 1
        } else {
            triggerHelp() // countdown hit zero -> auto-escalate
        }
    }

    val progress = seconds / 30f

    Screen(tone = ScreenTone.Alert, scrollable = false) {
        Column(
            modifier = Modifier.weight(1f).padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            }

            Text(
                "IMPACT DETECTED",
                modifier = Modifier.padding(top = 24.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = Color.White.copy(alpha = 0.9f),
            )
            Text(
                "We detected a possible crash",
                modifier = Modifier.padding(top = 12.dp),
                fontSize = 40.sp,
                lineHeight = 44.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )

            Box(
                modifier = Modifier
                    .padding(top = 36.dp)
                    .size(224.dp)
                    .border(8.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$seconds", fontSize = 72.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        "SECONDS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                }
            }

            // Progress bar counting down
            Box(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth(0.75f)
                    .height(8.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(Color.White, CircleShape),
                )
            }

            Text(
                "If you don't respond, we'll share your location with your emergency contacts and the nearest responders.",
                modifier = Modifier.padding(top = 24.dp).fillMaxWidth(0.85f),
                fontSize = 18.sp,
                lineHeight = 25.sp,
                color = Color.White.copy(alpha = 0.95f),
                textAlign = TextAlign.Center,
            )

            Row(
                modifier = Modifier.padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(16.dp))
                Text(
                    when {
                        isDrill -> "Drill mode — no real alert will be sent"
                        triggering -> "Getting your location…"
                        else -> "Your live location is shared if you need help"
                    },
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                )
            }

            if (triggerError != null) {
                Text(
                    triggerError!!,
                    modifier = Modifier.padding(top = 12.dp).fillMaxWidth(0.85f),
                    fontSize = 14.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Column(
            modifier = Modifier.padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StignItButton(
                text = "I'm OK — cancel",
                onClick = onImOk,
                variant = StignItButtonVariant.Safe,
                size = StignItButtonSize.ExtraLarge,
            )
            StignItButton(
                text = if (triggering) "Getting help…" else if (triggerError != null) "Retry — get help now" else "Get help now",
                onClick = { triggerHelp() },
                variant = StignItButtonVariant.Outline,
                size = StignItButtonSize.ExtraLarge,
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
            )
        }
    }
}
