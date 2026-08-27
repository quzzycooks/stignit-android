package com.stignit.app.ui.welfarehistory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stignit.app.ui.components.*
import com.stignit.app.ui.theme.StignItExtraColors

private data class HistoryEntry(val title: String, val date: String, val outcome: String)

private val history = listOf(
    HistoryEntry("Hard braking on Lekki–Epe Expressway", "12 Aug · 18:42", "Cancelled by you in 6 seconds"),
    HistoryEntry("Possible impact near Yaba", "27 Jul · 07:15", "Cancelled by you in 11 seconds"),
    HistoryEntry("Drill: simulated crash", "14 Jul · 12:03", "Practice run completed"),
)

/** Direct port of src/routes/welfare-history.tsx. */
@Composable
fun WelfareHistoryScreen(onBack: () -> Unit, currentTab: BottomNavTab, onSelectTab: (BottomNavTab) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Screen(modifier = Modifier.weight(1f)) {
            TopBar(title = "Welfare Checks", onBack = onBack)

            Panel(tone = PanelTone.Safe) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        modifier = Modifier.size(44.dp).background(StignItExtraColors.safeSoft, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = StignItExtraColors.safe, modifier = Modifier.size(24.dp))
                    }
                    Column {
                        Text("No unresolved checks", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text("Every check so far ended safely.", fontSize = 14.sp, color = StignItExtraColors.mutedForeground)
                    }
                }
            }

            SectionTitle("Past 90 days")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 20.dp)) {
                history.forEach { h ->
                    Panel {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(
                                modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text(h.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                Text(h.date, fontSize = 14.sp, color = StignItExtraColors.mutedForeground, modifier = Modifier.padding(top = 2.dp))
                                StatusPill(Tone.Safe, h.outcome, modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                }
            }
        }
        BottomNav(current = currentTab, onSelect = onSelectTab)
    }
}
