package com.stignit.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stignit.app.ui.theme.StignItExtraColors

enum class BottomNavTab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Filled.Home),
    Situation("Situation", Icons.Filled.Radio),
    Contacts("Contacts", Icons.Filled.People),
    Safety("Safety", Icons.AutoMirrored.Filled.MenuBook),
}

/** Mirrors components/stignit/bottom-nav.tsx: 4-tab bar, active tab tinted primary. */
@Composable
fun BottomNav(current: BottomNavTab, onSelect: (BottomNavTab) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BottomNavTab.values().forEach { tab ->
                val selected = tab == current
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clickableNoRipple { onSelect(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        tab.icon,
                        contentDescription = tab.label,
                        tint = if (selected) MaterialTheme.colorScheme.primary else StignItExtraColors.mutedForeground,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        tab.label,
                        fontSize = 11.sp,
                        color = if (selected) MaterialTheme.colorScheme.primary else StignItExtraColors.mutedForeground,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }
}
