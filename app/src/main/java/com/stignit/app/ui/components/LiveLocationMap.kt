package com.stignit.app.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.google.gson.Gson
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotation
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.stignit.app.BuildConfig
import com.stignit.app.ui.theme.StignItExtraColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Locale

private val geocodeClient = OkHttpClient()
private val gson = Gson()

private data class GeocodeResponse(val features: List<GeocodeFeature>?)
private data class GeocodeFeature(val properties: GeocodeProperties?)
private data class GeocodeProperties(val full_address: String? = null, val place_formatted: String? = null, val name: String? = null)

/** Reverse-geocodes via Mapbox's public geocoding API; falls back to null (caller shows raw lat/lng) on any failure. */
private suspend fun reverseGeocode(lat: Double, lng: Double): String? = withContext(Dispatchers.IO) {
    runCatching {
        val url = "https://api.mapbox.com/search/geocode/v6/reverse" +
            "?longitude=$lng&latitude=$lat&access_token=${BuildConfig.MAPBOX_ACCESS_TOKEN}"
        val request = Request.Builder().url(url).build()
        geocodeClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext null
            val body = response.body?.string() ?: return@withContext null
            val parsed = gson.fromJson(body, GeocodeResponse::class.java)
            val props = parsed.features?.firstOrNull()?.properties
            props?.full_address ?: props?.place_formatted ?: props?.name
        }
    }.getOrNull()
}

private fun formatLatLng(lat: Double, lng: Double): String =
    String.format(Locale.US, "%.5f, %.5f", lat, lng)

/** Equirectangular approximation — plenty accurate at the city-block distances this gates on. */
private fun distanceMeters(a: Pair<Double, Double>, b: Pair<Double, Double>): Double {
    val earthRadiusM = 6_371_000.0
    val dLat = Math.toRadians(b.first - a.first)
    val dLng = Math.toRadians(b.second - a.second)
    val meanLat = Math.toRadians((a.first + b.first) / 2)
    val x = dLng * kotlin.math.cos(meanLat)
    return earthRadiusM * kotlin.math.sqrt(x * x + dLat * dLat)
}

/** Lightweight breathing-dot indicator — deliberately understated for a safety-critical screen. */
@Composable
private fun PulsingDot() {
    val transition = rememberInfiniteTransition(label = "gps-pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "gps-pulse-alpha",
    )
    Box(
        modifier = Modifier
            .size(14.dp)
            .alpha(alpha)
            .background(StignItExtraColors.mutedForeground, CircleShape),
    )
}

/**
 * A small rounded map card showing a single live-updating position marker.
 * `position` is null before the first fix arrives — shows a muted placeholder.
 * Tapping the marker shows a callout with the reverse-geocoded address, falling
 * back to raw coordinates if geocoding is unavailable or fails.
 */
@Composable
fun LiveLocationMap(
    position: Pair<Double, Double>?, // lat, lng
    modifier: Modifier = Modifier,
    /** False for a known, non-resolving reason (permission denied, drill mode) — shows [placeholderText] as a static message with no pulse or timeout. */
    waitingForFix: Boolean = true,
    placeholderText: String = "Waiting for a GPS fix…",
) {
    val viewportState = rememberMapViewportState()
    var fixTimedOut by remember { mutableStateOf(false) }

    LaunchedEffect(waitingForFix, position == null) {
        fixTimedOut = false
        if (waitingForFix && position == null) {
            delay(15_000)
            fixTimedOut = true
        }
    }
    var showCallout by remember { mutableStateOf(false) }
    var address by remember { mutableStateOf<String?>(null) }
    var geocoding by remember { mutableStateOf(false) }
    // The position a live location keeps ticking (~every 7s) shouldn't reset an
    // open callout — only geocode again if the pin has actually moved meaningfully.
    var geocodedFor by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    LaunchedEffect(position) {
        val p = position
        if (p == null) {
            showCallout = false
            address = null
            geocodedFor = null
            return@LaunchedEffect
        }
        viewportState.easeTo(
            cameraOptions = com.mapbox.maps.CameraOptions.Builder()
                .center(Point.fromLngLat(p.second, p.first))
                .zoom(15.0)
                .build(),
            animationOptions = MapAnimationOptions.mapAnimationOptions { duration(600) },
        )
    }

    LaunchedEffect(showCallout, position) {
        val p = position ?: return@LaunchedEffect
        if (!showCallout) return@LaunchedEffect
        val moved = geocodedFor?.let { distanceMeters(it, p) > 50.0 } ?: true
        if (!moved) return@LaunchedEffect
        geocoding = true
        address = reverseGeocode(p.first, p.second)
        geocodedFor = p
        geocoding = false
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (waitingForFix && !fixTimedOut) {
                        PulsingDot()
                        androidx.compose.foundation.layout.Spacer(Modifier.height(10.dp))
                    }
                    Text(
                        if (waitingForFix && fixTimedOut) "Location unavailable — check your GPS signal" else placeholderText,
                        color = StignItExtraColors.mutedForeground,
                    )
                }
            }
        } else {
            MapboxMap(
                modifier = Modifier.fillMaxSize(),
                mapViewportState = viewportState,
            ) {
                CircleAnnotation(
                    point = Point.fromLngLat(position.second, position.first),
                    onClick = { showCallout = !showCallout; true },
                ) {
                    circleRadius = 8.0
                    circleColor = Color(0xFF1E3A5F)
                    circleStrokeWidth = 2.0
                    circleStrokeColor = Color.White
                }
            }

            AnimatedVisibility(
                visible = showCallout,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .animateContentSize(),
                ) {
                    Box(modifier = Modifier.padding(start = 14.dp, top = 10.dp, end = 10.dp, bottom = 10.dp)) {
                        androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                when {
                                    geocoding -> "Locating…"
                                    else -> address ?: formatLatLng(position.first, position.second)
                                },
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(end = 8.dp),
                            )
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Dismiss",
                                tint = StignItExtraColors.mutedForeground,
                                modifier = Modifier.size(18.dp).clickableNoRipple { showCallout = false },
                            )
                        }
                    }
                }
            }
        }
    }
}
