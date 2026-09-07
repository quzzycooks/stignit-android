package com.stignit.app.ui.incident

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stignit.app.ui.components.Panel
import com.stignit.app.ui.components.Screen
import com.stignit.app.ui.components.TopBar
import com.stignit.app.ui.components.clickableNoRipple
import com.stignit.app.ui.theme.StignItExtraColors
import kotlinx.coroutines.delay

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

private const val COUNTDOWN_SECONDS = 10

/**
 * Shown either when someone joins a Situation Room via a proximity-alert
 * notification they didn't trigger (no countdown — they're not the one who
 * called for help, so there's no urgency forcing a choice), or when the
 * person who just triggered their own incident hasn't yet said why they're
 * on scene ([showCountdown] = true): a genuinely injured/unconscious trigger
 * can't be expected to answer a prompt, so a [COUNTDOWN_SECONDS]-second
 * countdown auto-assigns "victim" (the safe default for someone who called
 * for help on themselves) rather than leaving them stuck here indefinitely.
 */
@Composable
fun DeclareRoleScreen(
    onRoleDeclared: (role: String) -> Unit,
    onBack: () -> Unit,
    showCountdown: Boolean = false,
) {
    var resolved by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableStateOf(COUNTDOWN_SECONDS) }

    fun choose(role: String) {
        if (resolved) return
        resolved = true
        onRoleDeclared(role)
    }

    LaunchedEffect(showCountdown, secondsLeft, resolved) {
        if (!showCountdown || resolved) return@LaunchedEffect
        if (secondsLeft > 0) {
            delay(1000)
            secondsLeft -= 1
        } else {
            choose("victim") // countdown hit zero -> same as tapping "I'm involved"
        }
    }

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
            modifier = Modifier.padding(top = 8.dp, bottom = if (showCountdown) 16.dp else 24.dp),
            fontSize = 16.sp,
            color = StignItExtraColors.mutedForeground,
        )

        if (showCountdown) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(StignItExtraColors.danger.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "$secondsLeft",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = StignItExtraColors.danger,
                    )
                }
                Text(
                    "No answer in ${secondsLeft}s and we'll assume you're involved.",
                    modifier = Modifier.weight(1f),
                    fontSize = 13.sp,
                    color = StignItExtraColors.mutedForeground,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .height(4.dp)
                    .background(StignItExtraColors.mutedForeground.copy(alpha = 0.2f), CircleShape),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(secondsLeft / COUNTDOWN_SECONDS.toFloat())
                        .background(StignItExtraColors.danger, CircleShape),
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            options.forEach { option ->
                Panel(modifier = Modifier.clickableNoRipple { choose(option.role) }) {
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
