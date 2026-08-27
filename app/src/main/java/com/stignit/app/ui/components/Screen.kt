package com.stignit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stignit.app.ui.theme.StignItExtraColors

enum class ScreenTone { Neutral, Alert, Ink }

/** Mirrors components/stignit/screen.tsx: full-bleed tinted background, content column with horizontal padding. */
@Composable
fun Screen(
    modifier: Modifier = Modifier,
    tone: ScreenTone = ScreenTone.Neutral,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val bg: Color = when (tone) {
        ScreenTone.Neutral -> MaterialTheme.colorScheme.background
        ScreenTone.Alert -> StignItExtraColors.danger
        ScreenTone.Ink -> MaterialTheme.colorScheme.primary
    }
    val scrollModifier = if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .then(scrollModifier)
            .padding(horizontal = 20.dp),
        content = content,
    )
}

enum class PanelTone { Card, Safe, Danger, Muted }

/** Mirrors the Panel component in components/stignit/pieces.tsx, including tone variants. */
@Composable
fun Panel(
    modifier: Modifier = Modifier,
    tone: PanelTone = PanelTone.Card,
    content: @Composable ColumnScope.() -> Unit,
) {
    val bg = when (tone) {
        PanelTone.Card -> MaterialTheme.colorScheme.surface
        PanelTone.Safe -> StignItExtraColors.safeSoft
        PanelTone.Danger -> StignItExtraColors.dangerSoft
        PanelTone.Muted -> MaterialTheme.colorScheme.secondary
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = bg,
        shadowElevation = if (tone == PanelTone.Card) 1.dp else 0.dp,
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

/** Mirrors TopBar in screen.tsx: optional back chevron + title + trailing slot. */
@Composable
fun TopBar(
    title: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Back", modifier = Modifier.size(24.dp))
            }
        }
        title?.let {
            Text(it, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = if (onBack != null) 0.dp else 4.dp))
        }
        Spacer(Modifier.weight(1f))
        trailing?.invoke()
    }
}

/** Mirrors SectionTitle in screen.tsx. */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        modifier = modifier.padding(top = 28.dp, bottom = 12.dp),
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        color = StignItExtraColors.mutedForeground,
    )
}
