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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stignit.app.ui.components.*
import com.stignit.app.ui.theme.StignItExtraColors

private data class FeedMessage(val from: String, val system: Boolean, val text: String, val time: String)

private val feed = listOf(
    FeedMessage("StignIt", true, "Incident opened after impact detected at 09:12. Live location sharing is on.", "09:12"),
    FeedMessage("Chidi (Brother)", false, "I've seen the alert. I'm 15 minutes away, heading there now.", "09:13"),
    FeedMessage("Responder — LASAMBUS Unit 4", false, "Unit dispatched. Please stay in the vehicle if it is safe to do so.", "09:14"),
)

/** Direct port of src/routes/situation-room.tsx — live incident view: status, timeline, updates feed. */
@Composable
fun SituationRoomScreen(onBack: () -> Unit, onMarkSafe: () -> Unit) {
    var message by remember { mutableStateOf("") }

    Screen {
        TopBar(title = "Situation Room", onBack = onBack, trailing = { StatusPill(Tone.Danger, "Live") })

        Panel(tone = PanelTone.Danger, modifier = Modifier.padding(top = 4.dp)) {
            Text("INCIDENT #SG-2481", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = StignItExtraColors.danger)
            Text("Help is on the way", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            Text(
                "Opened 09:12 · Third Mainland Bridge, Lagos · Nearest unit 6 minutes away",
                fontSize = 14.sp,
                color = StignItExtraColors.mutedForeground,
                modifier = Modifier.padding(top = 8.dp),
            )
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StignItButton(
                    text = "Call responder",
                    onClick = { /* wired to backend in milestone 4 */ },
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

        SectionTitle("Live status")
        Panel {
            TimelineItem(Icons.Filled.LocationOn, "Location shared", "Live GPS updating every 10 seconds · accuracy 8m", TimelineState.Done)
            TimelineItem(Icons.Filled.People, "3 emergency contacts notified", "Chidi delivered · Amaka delivered · Mum ringing", TimelineState.Done)
            TimelineItem(Icons.Filled.LocalHospital, "Nearest responder en route", "LASAMBUS Unit 4 · ETA 6 min", TimelineState.Active)
            TimelineItem(Icons.Filled.Shield, "Incident resolution", "Closes when you or a responder confirms you're safe", TimelineState.Pending, isLast = true)
        }

        SectionTitle("Updates")
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            feed.forEach { m ->
                Panel(tone = if (m.system) PanelTone.Muted else PanelTone.Card) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(m.from, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text(m.time, fontSize = 12.sp, color = StignItExtraColors.mutedForeground)
                    }
                    Text(m.text, fontSize = 15.sp, color = StignItExtraColors.mutedForeground, modifier = Modifier.padding(top = 6.dp))
                }
            }
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
