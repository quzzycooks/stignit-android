package com.stignit.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stignit.app.data.ApiResult
import com.stignit.app.data.rememberLocationRepository
import com.stignit.app.data.rememberUserRepository
import com.stignit.app.location.LocationTracker
import com.stignit.app.location.rememberLocationPermissionState
import com.stignit.app.ui.components.*
import com.stignit.app.ui.theme.StignItExtraColors

/**
 * Direct port of src/routes/home.tsx — the post-login dashboard: monitoring
 * status card, SOS button, recent-activity empty state, and nav tiles to
 * every other screen.
 */
@Composable
fun HomeScreen(
    userName: String,
    onOpenSituationRoom: () -> Unit,
    onOpenContacts: () -> Unit,
    onOpenWelfareHistory: () -> Unit,
    onOpenSafety: () -> Unit,
    onSimulateImpact: () -> Unit,
    onOpenSettings: () -> Unit,
    onSelectTab: (BottomNavTab) -> Unit,
) {
    var monitoring by remember { mutableStateOf(true) }

    val userRepo = rememberUserRepository()
    var showMedicalReminder by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        when (val r = userRepo.getMe()) {
            is ApiResult.Ok -> showMedicalReminder = !r.value.medicalInfoComplete
            is ApiResult.Err -> Unit // not worth surfacing on Home; Settings will show it if visited
        }
    }

    val context = LocalContext.current
    val locationTracker = remember(context) { LocationTracker(context) }
    val locationRepo = rememberLocationRepository()
    val locationPermission = rememberLocationPermissionState()
    var livePosition by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    LaunchedEffect(monitoring) {
        if (!monitoring) {
            livePosition = null
            return@LaunchedEffect
        }
        if (!locationPermission.isGranted.value) locationPermission.request()
    }
    LaunchedEffect(monitoring, locationPermission.isGranted.value) {
        if (!monitoring || !locationPermission.isGranted.value) return@LaunchedEffect
        locationTracker.locationUpdates().collect { loc ->
            livePosition = loc.latitude to loc.longitude
            locationRepo.pushLocation(loc.latitude, loc.longitude, loc.accuracy)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Screen(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth().height(64.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Good morning,", fontSize = 14.sp, color = StignItExtraColors.mutedForeground)
                    Text(userName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondary)
                        .clickableNoRipple(onOpenSettings),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }

            if (showMedicalReminder) {
                Panel(tone = PanelTone.Muted) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Finish your medical profile", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Helps a verified responder treat you correctly during an incident.",
                                modifier = Modifier.padding(top = 2.dp),
                                fontSize = 13.sp,
                                color = StignItExtraColors.mutedForeground,
                            )
                        }
                        Text(
                            "Add",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickableNoRipple(onOpenSettings),
                        )
                        Spacer(Modifier.width(12.dp))
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Dismiss",
                            tint = StignItExtraColors.mutedForeground,
                            modifier = Modifier.size(18.dp).clickableNoRipple { showMedicalReminder = false },
                        )
                    }
                }
            }

            Panel(tone = if (monitoring) PanelTone.Safe else PanelTone.Muted) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        StatusPill(
                            tone = if (monitoring) Tone.Safe else Tone.Muted,
                            text = if (monitoring) "Monitoring active" else "Monitoring paused",
                        )
                        Text(
                            if (monitoring) "You're covered" else "Detection is off",
                            modifier = Modifier.padding(top = 16.dp),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            if (monitoring)
                                "Crash detection is running in the background. Sensors last checked 12 seconds ago."
                            else
                                "StignIt won't detect an impact until you turn monitoring back on.",
                            modifier = Modifier.padding(top = 8.dp),
                            fontSize = 14.sp,
                            color = StignItExtraColors.mutedForeground,
                        )
                    }
                    Switch(checked = monitoring, onCheckedChange = { monitoring = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    StatCell("3", "Contacts")
                    StatCell("2", "Trips today")
                    StatCell("0", "Incidents")
                }
            }

            SectionTitle("Your location")
            Panel {
                if (monitoring) {
                    LiveLocationMap(
                        position = livePosition,
                        placeholderText = if (locationPermission.isGranted.value) {
                            "Waiting for a GPS fix…"
                        } else {
                            "Location permission needed to show your position"
                        },
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Location paused — turn monitoring on to see it here", color = StignItExtraColors.mutedForeground)
                    }
                }
            }

            SectionTitle("Emergency")
            Panel {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(StignItExtraColors.danger)
                            .clickableNoRipple(onSimulateImpact),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Shield, contentDescription = null, tint = StignItExtraColors.dangerForeground, modifier = Modifier.size(32.dp))
                            Text("SOS", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = StignItExtraColors.dangerForeground, modifier = Modifier.padding(top = 4.dp))
                            Text("Press and hold", fontSize = 12.sp, color = StignItExtraColors.dangerForeground.copy(alpha = 0.9f))
                        }
                    }
                    Text(
                        "Hold for 3 seconds to open a live incident. A short hold prevents accidental alerts.",
                        modifier = Modifier.padding(top = 16.dp).fillMaxWidth(0.85f),
                        fontSize = 14.sp,
                        color = StignItExtraColors.mutedForeground,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }

            SectionTitle("Recent activity")
            Panel {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Inbox, contentDescription = null, tint = StignItExtraColors.mutedForeground, modifier = Modifier.size(24.dp))
                    }
                    Text("Nothing to show — that's good news", modifier = Modifier.padding(top = 16.dp), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Welfare checks and incidents will appear here. Your last 14 days have been clear.",
                        modifier = Modifier.padding(top = 4.dp).fillMaxWidth(0.85f),
                        fontSize = 14.sp,
                        color = StignItExtraColors.mutedForeground,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                    StignItButton(
                        text = "View welfare check history",
                        onClick = onOpenWelfareHistory,
                        variant = StignItButtonVariant.Outline,
                        size = StignItButtonSize.Default,
                        modifier = Modifier.padding(top = 20.dp).fillMaxWidth(0.8f),
                    )
                }
            }

            SectionTitle("Go to")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 24.dp)) {
                NavTile(Icons.Filled.Radio, "Situation Room", "Live incident view and responder updates", onOpenSituationRoom)
                NavTile(Icons.Filled.People, "Emergency Contacts", "3 people notified when you can't respond", onOpenContacts)
                NavTile(Icons.Filled.MonitorHeart, "Welfare Checks", "Every check StignIt has raised for you", onOpenWelfareHistory)
                NavTile(Icons.AutoMirrored.Filled.MenuBook, "Safety Knowledge & Drills", "Practice the flow before you ever need it", onOpenSafety)
                NavTile(Icons.Filled.DirectionsCar, "Simulate impact detection", "Preview the welfare check screen", onSimulateImpact)
            }
        }

        BottomNav(current = BottomNavTab.Home, onSelect = onSelectTab)
    }
}

@Composable
private fun StatCell(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 12.sp, color = StignItExtraColors.mutedForeground, modifier = Modifier.padding(top = 2.dp))
    }
}
