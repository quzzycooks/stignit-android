package com.stignit.app.ui.situationroom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stignit.app.data.ApiResult
import com.stignit.app.data.DRILL_INCIDENT_ID
import com.stignit.app.data.IncidentDetails
import com.stignit.app.data.rememberIncidentRepository
import com.stignit.app.data.rememberLocationRepository
import com.stignit.app.data.rememberIncidentSocket
import com.stignit.app.data.sessionStore
import com.stignit.app.location.LocationTracker
import com.stignit.app.location.rememberLocationPermissionState
import com.stignit.app.ui.components.*
import com.stignit.app.ui.theme.StignItExtraColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Direct port of src/routes/situation-room.tsx, since wired to a real incident:
 * live location (own GPS if you're the triggering user, or a socket feed of
 * theirs if you're a watching contact), real incident id/timestamp. The
 * timeline/feed/"Call responder" stay UI-only for now (see TODO below) — but
 * their copy was rewritten to stop asserting things that never happened.
 */
@Composable
fun SituationRoomScreen(incidentId: String, onBack: () -> Unit, onMarkSafe: () -> Unit) {
    val isDrill = incidentId == DRILL_INCIDENT_ID
    val context = LocalContext.current
    val session = context.sessionStore()
    val incidentsRepo = rememberIncidentRepository()
    val locationRepo = rememberLocationRepository()

    var details by remember { mutableStateOf<IncidentDetails?>(null) }
    var livePosition by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(incidentId) {
        if (!isDrill) {
            when (val r = incidentsRepo.getIncident(incidentId)) {
                is ApiResult.Ok -> details = r.value
                is ApiResult.Err -> Unit // header falls back to the raw id below
            }
        }
    }

    val isTriggeringUser = isDrill || details?.triggeringUserId == session.userId

    if (!isDrill && isTriggeringUser) {
        // I'm the one who needs help: run my own GPS and push it — this is what
        // the backend then re-broadcasts to any watchers via the /rt gateway.
        val locationTracker = remember(context) { LocationTracker(context) }
        val permission = rememberLocationPermissionState()
        LaunchedEffect(Unit) { if (!permission.isGranted.value) permission.request() }
        LaunchedEffect(permission.isGranted.value) {
            if (!permission.isGranted.value) return@LaunchedEffect
            locationTracker.locationUpdates().collect { loc ->
                livePosition = loc.latitude to loc.longitude
                locationRepo.pushLocation(loc.latitude, loc.longitude, loc.accuracy)
            }
        }
    } else if (!isDrill) {
        // I'm a contact/watcher: no location permission needed, just listen.
        val socket = rememberIncidentSocket()
        DisposableEffect(incidentId) {
            socket.connect()
            socket.joinIncident(incidentId)
            onDispose {
                socket.leaveIncident(incidentId)
                socket.disconnect()
            }
        }
        LaunchedEffect(incidentId) {
            socket.observeLocation().collect { update -> livePosition = update.lat to update.lng }
        }
    }

    val openedAtText = remember(details?.createdAt) {
        val iso = details?.createdAt ?: return@remember null
        runCatching {
            DateTimeFormatter.ofPattern("MMM d, h:mm a")
                .withZone(ZoneId.systemDefault())
                .format(Instant.parse(iso))
        }.getOrNull()
    }

    Screen {
        TopBar(
            title = "Situation Room",
            onBack = onBack,
            trailing = { StatusPill(if (isDrill) Tone.Muted else Tone.Danger, if (isDrill) "Drill" else "Live") },
        )

        Panel(tone = if (isDrill) PanelTone.Muted else PanelTone.Danger, modifier = Modifier.padding(top = 4.dp)) {
            Text(
                if (isDrill) "DRILL — no real incident" else (details?.incidentId ?: incidentId),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDrill) StignItExtraColors.mutedForeground else StignItExtraColors.danger,
            )
            Text(
                if (isDrill) "Practice run — nothing was sent" else "Your location is being shared",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                when {
                    isDrill -> "This is what your contacts and responders would see during a real incident."
                    openedAtText != null -> "Opened $openedAtText"
                    else -> "Live location sharing is on"
                },
                fontSize = 14.sp,
                color = StignItExtraColors.mutedForeground,
                modifier = Modifier.padding(top = 8.dp),
            )
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StignItButton(
                    text = "Call responder",
                    // TODO(next pass, highest priority): wire to a real responder-dispatch
                    // endpoint once one exists — right now this button does nothing, and
                    // that's a real gap for someone relying on it during an emergency.
                    onClick = { },
                    variant = StignItButtonVariant.Sos,
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Filled.Call, contentDescription = null) },
                )
                StignItButton(
                    text = "Mark me safe",
                    onClick = onMarkSafe,
                    variant = StignItButtonVariant.Safe,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        SectionTitle("Live location")
        Panel {
            LiveLocationMap(
                position = livePosition,
                waitingForFix = !isDrill,
                placeholderText = if (isDrill) "Drill mode — map disabled" else "Waiting for a GPS fix…",
            )
        }

        SectionTitle("Live status")
        Panel {
            TimelineItem(
                Icons.Filled.LocationOn,
                "Location shared",
                if (livePosition != null) "Live GPS updating" else "Waiting for the first GPS fix",
                if (livePosition != null || isDrill) TimelineState.Done else TimelineState.Active,
            )
            TimelineItem(
                Icons.Filled.People,
                "Emergency contacts notified",
                if (isDrill) "Skipped in drill mode" else "Notified by SMS when this incident opened",
                if (isDrill) TimelineState.Pending else TimelineState.Done,
            )
            TimelineItem(
                Icons.Filled.LocalHospital,
                "Responder assignment",
                "Awaiting responder assignment",
                TimelineState.Pending,
            )
            TimelineItem(
                Icons.Filled.Shield,
                "Incident resolution",
                "Closes when you or a responder confirms you're safe",
                TimelineState.Pending,
                isLast = true,
            )
        }

        SectionTitle("Updates")
        Panel(tone = PanelTone.Muted) {
            Text(
                "No updates yet — messages from your contacts and responders will appear here.",
                fontSize = 14.sp,
                color = StignItExtraColors.mutedForeground,
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                placeholder = { Text("Send an update to the room") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = { message = "" },
                modifier = Modifier.size(52.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp)),
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send update", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}
