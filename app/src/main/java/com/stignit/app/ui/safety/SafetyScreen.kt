package com.stignit.app.ui.safety

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
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

private val GUIDE_ICONS: Map<String, androidx.compose.ui.graphics.vector.ImageVector> = mapOf(
    "cpr-adult" to Icons.Filled.MonitorHeart,
    "severe-bleeding" to Icons.Filled.WaterDrop,
    "choking-adult" to Icons.Filled.Air,
    "burns" to Icons.Filled.LocalFireDepartment,
    "shock" to Icons.Filled.Warning,
    "stroke" to Icons.Filled.Psychology,
    "heart-attack" to Icons.Filled.Favorite,
    "recovery-position" to Icons.Filled.Bed,
    "seizures" to Icons.Filled.Bolt,
    "rta-scene-safety" to Icons.Filled.DirectionsCar,
    "anaphylaxis" to Icons.Filled.Coronavirus,
)

/** Direct port of src/routes/safety.tsx — practice drill + safety guides. */
@Composable
fun SafetyScreen(
    onBack: () -> Unit,
    onStartDrill: () -> Unit,
    onOpenGuide: (guideId: String) -> Unit,
    currentTab: BottomNavTab,
    onSelectTab: (BottomNavTab) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Screen(modifier = Modifier.weight(1f)) {
            TopBar(title = "Safety Knowledge", onBack = onBack)

            Panel {
                Box(
                    modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.PlayCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
                Text("Run a 30-second drill", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
                Text(
                    "Practise the welfare check exactly as it appears after a real impact. Nobody is notified during a drill.",
                    fontSize = 14.sp,
                    color = StignItExtraColors.mutedForeground,
                    modifier = Modifier.padding(top = 8.dp),
                )
                StignItButton(
                    text = "Start drill",
                    onClick = onStartDrill,
                    size = StignItButtonSize.Large,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }

            SectionTitle("Guides")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 20.dp)) {
                DRILL_GUIDES.forEach { guide ->
                    NavTile(
                        icon = GUIDE_ICONS[guide.id] ?: Icons.AutoMirrored.Filled.MenuBook,
                        label = guide.title,
                        hint = guide.category,
                        onClick = { onOpenGuide(guide.id) },
                    )
                }
            }
        }
        BottomNav(current = currentTab, onSelect = onSelectTab)
    }
}
