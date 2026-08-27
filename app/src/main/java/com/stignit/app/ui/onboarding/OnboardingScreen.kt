package com.stignit.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stignit.app.ui.components.Panel
import com.stignit.app.ui.components.Screen
import com.stignit.app.ui.components.StignItButton
import com.stignit.app.ui.components.StignItButtonSize
import com.stignit.app.ui.components.clickableNoRipple
import com.stignit.app.ui.theme.StignItExtraColors

/**
 * Direct port of src/routes/index.tsx (the Lovable onboarding flow) —
 * same four slides, same copy, same progression logic.
 */
private data class Bullet(val icon: ImageVector, val label: String, val detail: String)
private data class Slide(
    val icon: ImageVector,
    val eyebrow: String,
    val title: String,
    val body: String,
    val bullets: List<Bullet> = emptyList(),
)

private val slides = listOf(
    Slide(
        icon = Icons.Filled.Shield,
        eyebrow = "Welcome to StignIt",
        title = "Help finds you, even if you can't ask for it",
        body = "StignIt stays on quietly in the background while you drive. If something happens, you don't need to unlock your phone, find a number or explain where you are.",
    ),
    Slide(
        icon = Icons.Filled.MonitorHeart,
        eyebrow = "How detection works",
        title = "Your phone senses the impact",
        body = "Motion, speed and impact sensors work together to recognise a likely crash. When the pattern matches, StignIt opens a welfare check and starts a short countdown.",
    ),
    Slide(
        icon = Icons.Filled.Timer,
        eyebrow = "What happens next",
        title = "You have 30 seconds to say you're fine",
        body = "Tap \"I'm OK\" and everything stops there. If you don't respond, your emergency contacts and the nearest verified responders receive your live location and incident details.",
    ),
    Slide(
        icon = Icons.Filled.NotificationsActive,
        eyebrow = "Before we continue",
        title = "Why StignIt needs these permissions",
        body = "Each one exists for a single reason: to reach you when it matters. Nothing is used for advertising, and you can review activity any time.",
        bullets = listOf(
            Bullet(Icons.Filled.MonitorHeart, "Background activity", "Detection has to keep running when your screen is off."),
            Bullet(Icons.Filled.LocationOn, "Location", "Responders need an accurate place to come to."),
            Bullet(Icons.Filled.Notifications, "Notifications", "So the welfare check reaches you loudly and fast."),
        ),
    ),
)

@Composable
fun OnboardingScreen(
    onSkip: () -> Unit,
    onDone: () -> Unit,
) {
    var index by remember { mutableStateOf(0) }
    val slide = slides[index]
    val isLast = index == slides.lastIndex

    Screen(scrollable = false) {
        // Top bar: wordmark + Skip
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("StignIt", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (!isLast) {
                Text(
                    "Skip",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StignItExtraColors.mutedForeground,
                    modifier = Modifier.clickableNoRipple(onSkip),
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(slide.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            }

            Text(
                slide.eyebrow.uppercase(),
                modifier = Modifier.padding(top = 32.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = StignItExtraColors.mutedForeground,
            )
            Text(
                slide.title,
                modifier = Modifier.padding(top = 12.dp),
                fontSize = 32.sp,
                lineHeight = 37.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                slide.body,
                modifier = Modifier.padding(top = 16.dp),
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = StignItExtraColors.mutedForeground,
            )

            if (slide.bullets.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(top = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    slide.bullets.forEach { b ->
                        Panel {
                            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(b.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                                Column {
                                    Text(b.label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                    Text(b.detail, fontSize = 14.sp, color = StignItExtraColors.mutedForeground, modifier = Modifier.padding(top = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Progress dots + CTA
        Column(
            modifier = Modifier.padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                slides.forEachIndexed { i, _ ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(6.dp)
                            .width(if (i == index) 28.dp else 6.dp)
                            .background(
                                if (i == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                CircleShape,
                            ),
                    )
                }
            }
            StignItButton(
                text = if (isLast) "Allow and continue" else "Continue",
                onClick = { if (isLast) onDone() else index++ },
                size = StignItButtonSize.Large,
            )
            if (isLast) {
                Text(
                    "Not now — set up later",
                    modifier = Modifier.fillMaxWidth().clickableNoRipple(onDone),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StignItExtraColors.mutedForeground,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
