package com.stignit.app.ui.components

import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stignit.app.ui.theme.StignItExtraColors
import kotlin.math.roundToInt

private const val FIRE_THRESHOLD = 0.85f

/**
 * Drag-left-to-right-to-confirm track (PRD: prevents accidental fires the way
 * a plain tap can't). Fires [onConfirmed] the instant the thumb crosses
 * [FIRE_THRESHOLD] of the track — no confirmation dialog on top of this,
 * since the drag itself *is* the confirmation. Releasing early animates the
 * thumb back to the start instead of firing.
 */
@Composable
fun SlideToConfirmButton(
    text: String,
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
    trackColor: Color = StignItExtraColors.danger,
    thumbColor: Color = Color.White,
    height: Dp = 64.dp,
) {
    val density = LocalDensity.current
    val thumbSizePx = with(density) { height.toPx() }
    var trackWidthPx by remember { mutableStateOf(0f) }
    val maxDrag = (trackWidthPx - thumbSizePx).coerceAtLeast(0f)
    var offsetX by remember { mutableStateOf(0f) }
    var fired by remember { mutableStateOf(false) }

    val fillWidthPx = (offsetX + thumbSizePx).coerceIn(thumbSizePx, trackWidthPx.coerceAtLeast(thumbSizePx))

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(trackColor.copy(alpha = 0.15f))
            .onSizeChanged { trackWidthPx = it.width.toFloat() },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(with(density) { fillWidthPx.toDp() })
                .background(trackColor, RoundedCornerShape(height / 2)),
        )
        Text(
            text,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = trackColor,
        )
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .size(height)
                .padding(4.dp)
                .clip(CircleShape)
                .background(thumbColor)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        if (!fired) offsetX = (offsetX + delta).coerceIn(0f, maxDrag)
                    },
                    onDragStopped = {
                        if (maxDrag > 0f && offsetX >= maxDrag * FIRE_THRESHOLD) {
                            fired = true
                            offsetX = maxDrag
                            onConfirmed()
                        } else {
                            animate(offsetX, 0f) { value, _ -> offsetX = value }
                        }
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "Slide to confirm", tint = trackColor)
        }
    }
}
