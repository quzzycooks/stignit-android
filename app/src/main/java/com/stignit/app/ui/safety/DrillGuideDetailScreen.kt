package com.stignit.app.ui.safety

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stignit.app.ui.components.*
import com.stignit.app.ui.theme.StignItExtraColors

@Composable
fun DrillGuideDetailScreen(guideId: String, onBack: () -> Unit) {
    val guide = DRILL_GUIDES.find { it.id == guideId }

    Column(modifier = Modifier.fillMaxSize()) {
        Screen(modifier = Modifier.weight(1f)) {
            TopBar(title = guide?.title ?: "Guide", onBack = onBack)

            if (guide == null) {
                Text("This guide isn't available.", color = StignItExtraColors.mutedForeground)
            } else {
                Panel(tone = PanelTone.Muted) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = StignItExtraColors.mutedForeground, modifier = Modifier.size(18.dp))
                        Text(
                            "Draft content, not yet clinically reviewed. In an emergency, call for professional help first.",
                            fontSize = 12.sp,
                            color = StignItExtraColors.mutedForeground,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                StatusPill(tone = Tone.Danger, text = guide.category)
                Text(
                    guide.urgency,
                    modifier = Modifier.padding(top = 8.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StignItExtraColors.mutedForeground,
                )

                SectionTitle("Steps")
                Panel {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        guide.steps.forEachIndexed { i, step ->
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(
                                    modifier = Modifier.size(24.dp).background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("${i + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Text(step, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                if (guide.note != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(guide.note, fontSize = 13.sp, color = StignItExtraColors.mutedForeground)
                }

                SectionTitle("Check yourself")
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 24.dp)) {
                    guide.quiz.forEach { q -> QuizCard(q) }
                }
            }
        }
        BottomNav(current = BottomNavTab.Safety, onSelect = {})
    }
}

@Composable
private fun QuizCard(q: QuizQuestion) {
    var selected by remember { mutableStateOf<Int?>(null) }

    Panel {
        Text(q.question, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            q.options.forEachIndexed { i, option ->
                val isSelected = selected == i
                val isCorrect = i == q.correctIndex
                val revealed = selected != null
                val bg = when {
                    !revealed -> MaterialTheme.colorScheme.secondary
                    isCorrect -> StignItExtraColors.safeSoft
                    isSelected -> StignItExtraColors.dangerSoft
                    else -> MaterialTheme.colorScheme.secondary
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg, RoundedCornerShape(12.dp))
                        .clickableNoRipple { if (!revealed) selected = i }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (revealed && isCorrect) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = StignItExtraColors.safe, modifier = Modifier.size(18.dp))
                    }
                    Text(option, fontSize = 14.sp, modifier = Modifier.weight(1f))
                }
            }
        }
        if (selected != null && q.explanation.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(q.explanation, fontSize = 13.sp, color = StignItExtraColors.mutedForeground)
        }
    }
}
