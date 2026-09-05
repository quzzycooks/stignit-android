package com.stignit.app.ui.incident

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stignit.app.ui.components.Panel
import com.stignit.app.ui.components.Screen
import com.stignit.app.ui.components.TopBar
import com.stignit.app.ui.components.clickableNoRipple
import com.stignit.app.ui.theme.StignItExtraColors

private data class DeclareOption(
    val role: String,
    val icon: ImageVector,
    val title: String,
    val detail: String,
)

private val options = listOf(
    DeclareOption(
        "victim",
        Icons.Filled.Warning,
        "I'm involved",
        "You're part of this incident and may need help.",
    ),
    DeclareOption(
        "observer",
        Icons.Filled.Visibility,
        "I'm nearby and can help",
        "You're not directly involved but can offer assistance or information.",
    ),
)

/**
 * Shown when someone joins a Situation Room via a proximity-alert notification
 * they didn't trigger — never shown to whoever actually triggered the incident
 * (that person is auto-assigned "victim" server-side, no prompt).
 */
@Composable
fun DeclareRoleScreen(
    onRoleDeclared: (role: String) -> Unit,
    onBack: () -> Unit,
) {
    Screen(scrollable = false) {
        TopBar(onBack = onBack)

        Text(
            "Are you involved, or are you observing?",
            modifier = Modifier.padding(top = 8.dp),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 34.sp,
        )
        Text(
            "This helps responders understand who's on scene.",
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            fontSize = 16.sp,
            color = StignItExtraColors.mutedForeground,
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            options.forEach { option ->
                Panel(modifier = Modifier.clickableNoRipple { onRoleDeclared(option.role) }) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(option.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text(option.title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                option.detail,
                                modifier = Modifier.padding(top = 4.dp),
                                fontSize = 14.sp,
                                lineHeight = 19.sp,
                                color = StignItExtraColors.mutedForeground,
                            )
                        }
                    }
                }
            }
        }
    }
}
