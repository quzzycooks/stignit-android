package com.stignit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stignit.app.ui.theme.StignItExtraColors

enum class Tone { Safe, Danger, Warning, Muted }

/** Mirrors StatusPill in pieces.tsx: dot + label, colored by tone. */
@Composable
fun StatusPill(tone: Tone, text: String, modifier: Modifier = Modifier) {
    val (bg, fg) = when (tone) {
        Tone.Safe -> StignItExtraColors.safeSoft to StignItExtraColors.safe
        Tone.Danger -> StignItExtraColors.dangerSoft to StignItExtraColors.danger
        Tone.Warning -> StignItExtraColors.warningSoft to StignItExtraColors.warning
        Tone.Muted -> MaterialTheme.colorScheme.secondary to StignItExtraColors.mutedForeground
    }
    Surface(modifier = modifier, shape = RoundedCornerShape(50), color = bg) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(modifier = Modifier.size(8.dp).background(fg, CircleShape))
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = fg)
        }
    }
}

/** Mirrors NavTile in pieces.tsx: icon + label/hint row with a chevron, used as a nav link. */
@Composable
fun NavTile(icon: ImageVector, label: String, hint: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.clickableNoRipple(onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(hint, fontSize = 14.sp, color = StignItExtraColors.mutedForeground, maxLines = 1)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = StignItExtraColors.mutedForeground, modifier = Modifier.size(20.dp))
        }
    }
}

enum class TimelineState { Done, Active, Pending }

/** Mirrors TimelineItem in pieces.tsx: icon dot + connecting line + title/meta. */
@Composable
fun TimelineItem(icon: ImageVector, title: String, meta: String, state: TimelineState = TimelineState.Done, isLast: Boolean = false) {
    val (bg, fg) = when (state) {
        TimelineState.Done -> StignItExtraColors.safeSoft to StignItExtraColors.safe
        TimelineState.Active -> StignItExtraColors.dangerSoft to StignItExtraColors.danger
        TimelineState.Pending -> MaterialTheme.colorScheme.secondary to StignItExtraColors.mutedForeground
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(36.dp).background(bg, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
            }
            if (!isLast) {
                Box(modifier = Modifier.width(1.dp).weight(1f).background(MaterialTheme.colorScheme.outline))
            }
        }
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(meta, fontSize = 14.sp, color = StignItExtraColors.mutedForeground, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
