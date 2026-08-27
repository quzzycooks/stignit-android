package com.stignit.app.ui.welfare

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stignit.app.ui.components.Screen
import com.stignit.app.ui.components.ScreenTone
import com.stignit.app.ui.components.StignItButton
import com.stignit.app.ui.components.StignItButtonSize
import com.stignit.app.ui.components.StignItButtonVariant
import kotlinx.coroutines.delay

/**
 * Direct port of src/routes/welfare-check.tsx — the highest-stakes screen
 * in the app. Full-bleed danger-red background, large countdown, two
 * unmistakable actions. Location text is a placeholder here — milestone 4
 * wires this to the real GPS reading.
 */
@Composable
fun WelfareCheckScreen(
    onImOk: () -> Unit,
    onGetHelp: () -> Unit,
) {
    var seconds by remember { mutableStateOf(30) }

    LaunchedEffect(seconds) {
        if (seconds > 0) {
            delay(1000)
            seconds -= 1
        } else {
            onGetHelp() // countdown hit zero -> auto-escalate, same as navigate("/situation-room")
        }
    }

    val progress = seconds / 30f

    Screen(tone = ScreenTone.Alert, scrollable = false) {
        Column(
            modifier = Modifier.weight(1f).padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            }

            Text(
                "IMPACT DETECTED",
                modifier = Modifier.padding(top = 24.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = Color.White.copy(alpha = 0.9f),
            )
            Text(
                "We detected a possible crash",
                modifier = Modifier.padding(top = 12.dp),
                fontSize = 40.sp,
                lineHeight = 44.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )

            Box(
                modifier = Modifier
                    .padding(top = 36.dp)
                    .size(224.dp)
                    .border(8.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$seconds", fontSize = 72.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        "SECONDS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                }
            }

            // Progress bar counting down
            Box(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth(0.75f)
                    .height(8.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(Color.White, CircleShape),
                )
            }

            Text(
                "If you don't respond, we'll share your location with your emergency contacts and the nearest responders.",
                modifier = Modifier.padding(top = 24.dp).fillMaxWidth(0.85f),
                fontSize = 18.sp,
                lineHeight = 25.sp,
                color = Color.White.copy(alpha = 0.95f),
                textAlign = TextAlign.Center,
            )

            Row(
                modifier = Modifier.padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(16.dp))
                Text("Third Mainland Bridge, Lagos", fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))
            }
        }

        Column(
            modifier = Modifier.padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StignItButton(
                text = "I'm OK — cancel",
                onClick = onImOk,
                variant = StignItButtonVariant.Safe,
                size = StignItButtonSize.ExtraLarge,
            )
            StignItButton(
                text = "Get help now",
                onClick = onGetHelp,
                variant = StignItButtonVariant.Outline,
                size = StignItButtonSize.ExtraLarge,
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
            )
        }
    }
}
