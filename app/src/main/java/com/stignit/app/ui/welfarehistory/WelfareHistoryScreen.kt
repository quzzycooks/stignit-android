package com.stignit.app.ui.welfarehistory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stignit.app.data.ApiResult
import com.stignit.app.data.IncidentHistoryItem
import com.stignit.app.data.rememberIncidentRepository
import com.stignit.app.ui.components.*
import com.stignit.app.ui.theme.StignItExtraColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val OPEN_STATUSES = setOf("ACTIVE", "UNDER_CONTROL", "TRANSFERRED")

private fun incidentTypeLabel(type: String): String = when (type) {
    "RTA" -> "Road traffic incident"
    "MEDICAL_COLLAPSE" -> "Medical collapse"
    "FIRE" -> "Fire"
    "DROWNING" -> "Drowning"
    "BUILDING_COLLAPSE" -> "Building collapse"
    "CROWD_CRUSH" -> "Crowd crush"
    else -> "Incident"
}

private fun outcomeFor(status: String): Pair<String, Tone> = when (status) {
    "FALSE_ALARM" -> "Marked as a false alarm" to Tone.Safe
    "CLOSED" -> "Resolved" to Tone.Safe
    "ACTIVE" -> "Still active" to Tone.Danger
    "UNDER_CONTROL" -> "Under control" to Tone.Warning
    "TRANSFERRED" -> "Transferred to another responder" to Tone.Warning
    else -> status to Tone.Muted
}

private fun formatDate(iso: String): String = runCatching {
    DateTimeFormatter.ofPattern("d MMM · HH:mm")
        .withZone(ZoneId.systemDefault())
        .format(Instant.parse(iso))
}.getOrDefault(iso)

/** Direct port of src/routes/welfare-history.tsx, wired to the real incident-history API. */
@Composable
fun WelfareHistoryScreen(onBack: () -> Unit, currentTab: BottomNavTab, onSelectTab: (BottomNavTab) -> Unit) {
    val repo = rememberIncidentRepository()
    var loading by remember { mutableStateOf(true) }
    var history by remember { mutableStateOf<List<IncidentHistoryItem>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        when (val r = repo.getMyIncidents()) {
            is ApiResult.Ok -> history = r.value
            is ApiResult.Err -> error = r.message
        }
        loading = false
    }

    val hasUnresolved = history.any { it.status in OPEN_STATUSES }

    Column(modifier = Modifier.fillMaxSize()) {
        Screen(modifier = Modifier.weight(1f)) {
            TopBar(title = "Welfare Checks", onBack = onBack)

            when {
                loading -> Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                error != null -> Text(error!!, color = StignItExtraColors.danger)
                else -> {
                    Panel(tone = if (hasUnresolved) PanelTone.Danger else PanelTone.Safe) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(
                                modifier = Modifier.size(44.dp).background(StignItExtraColors.safeSoft, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = StignItExtraColors.safe, modifier = Modifier.size(24.dp))
                            }
                            Column {
                                Text(
                                    if (hasUnresolved) "You have an unresolved check" else "No unresolved checks",
                                    fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    if (hasUnresolved) "Open the Situation Room for the latest details." else "Every check so far ended safely.",
                                    fontSize = 14.sp, color = StignItExtraColors.mutedForeground,
                                )
                            }
                        }
                    }

                    SectionTitle("History")
                    if (history.isEmpty()) {
                        Text(
                            "Nothing here yet — welfare checks and incidents will show up as soon as one happens.",
                            color = StignItExtraColors.mutedForeground,
                            modifier = Modifier.padding(bottom = 20.dp),
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 20.dp)) {
                            history.forEach { h ->
                                val (outcomeText, tone) = outcomeFor(h.status)
                                Panel {
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Box(
                                            modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(Icons.Filled.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                        }
                                        Column {
                                            Text(incidentTypeLabel(h.incidentType), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                            Text(formatDate(h.createdAt), fontSize = 14.sp, color = StignItExtraColors.mutedForeground, modifier = Modifier.padding(top = 2.dp))
                                            StatusPill(tone, outcomeText, modifier = Modifier.padding(top = 8.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        BottomNav(current = currentTab, onSelect = onSelectTab)
    }
}
