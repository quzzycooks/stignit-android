package com.stignit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotation
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.stignit.app.ui.theme.StignItExtraColors

/**
 * A small rounded map card showing a single live-updating position marker.
 * `position` is null before the first fix arrives — shows a muted placeholder.
 */
@Composable
fun LiveLocationMap(
    position: Pair<Double, Double>?, // lat, lng
    modifier: Modifier = Modifier,
    placeholderText: String = "Waiting for a GPS fix…",
) {
    val viewportState = rememberMapViewportState()

    LaunchedEffect(position) {
        val p = position ?: return@LaunchedEffect
        viewportState.easeTo(
            cameraOptions = com.mapbox.maps.CameraOptions.Builder()
                .center(Point.fromLngLat(p.second, p.first))
                .zoom(15.0)
                .build(),
            animationOptions = MapAnimationOptions.mapAnimationOptions { duration(600) },
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp)),
    ) {
        if (position == null) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center,
            ) {
                Text(placeholderText, color = StignItExtraColors.mutedForeground)
            }
        } else {
            MapboxMap(
                modifier = Modifier.fillMaxSize(),
                mapViewportState = viewportState,
            ) {
                CircleAnnotation(point = Point.fromLngLat(position.second, position.first)) {
                    circleRadius = 8.0
                    circleColor = Color(0xFF1E3A5F)
                    circleStrokeWidth = 2.0
                    circleStrokeColor = Color.White
                }
            }
        }
    }
}
