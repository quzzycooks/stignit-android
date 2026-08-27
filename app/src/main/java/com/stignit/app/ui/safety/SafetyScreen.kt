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

/** Direct port of src/routes/safety.tsx — practice drill + safety guides. */
@Composable
fun SafetyScreen(
    onBack: () -> Unit,
    onStartDrill: () -> Unit,
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
                NavTile(Icons.AutoMirrored.Filled.MenuBook, "First 60 seconds after a crash", "What to check before you move", onBack)
                NavTile(Icons.Filled.LocalFireDepartment, "Roadside fire risk", "When to leave the vehicle immediately", onBack)
                NavTile(Icons.Filled.WaterDrop, "Basic bleeding control", "Pressure, elevation and what not to do", onBack)
            }
        }
        BottomNav(current = currentTab, onSelect = onSelectTab)
    }
}
