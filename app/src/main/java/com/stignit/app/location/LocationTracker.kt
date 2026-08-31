package com.stignit.app.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withTimeoutOrNull

/** How the GPS fix behind a [LocationFix] was obtained — sent to the backend for telemetry. */
sealed interface LocationFix {
    data class Fresh(val lat: Double, val lng: Double, val accuracyMeters: Float?) : LocationFix
    data class Cached(val lat: Double, val lng: Double, val accuracyMeters: Float?) : LocationFix
    data object Unavailable : LocationFix
}

/**
 * Wraps FusedLocationProviderClient. The SOS flow must never block on GPS, so
 * [bestEffortFix] tries the near-instant cached fix first and only falls back
 * to a fresh request with a short timeout — never throws.
 */
class LocationTracker(context: Context) {
    private val client: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun bestEffortFix(freshFixTimeoutMs: Long = 8_000): LocationFix {
        val cached = runCatching { client.lastLocation.await() }.getOrNull()
        if (cached != null) return LocationFix.Cached(cached.latitude, cached.longitude, cached.accuracy)

        val fresh = withTimeoutOrNull(freshFixTimeoutMs) {
            runCatching {
                client.getCurrentLocation(
                    CurrentLocationRequest.Builder()
                        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                        .build(),
                    null,
                ).await()
            }.getOrNull()
        }
        return if (fresh != null) {
            LocationFix.Fresh(fresh.latitude, fresh.longitude, fresh.accuracy)
        } else {
            LocationFix.Unavailable
        }
    }

    @SuppressLint("MissingPermission")
    fun locationUpdates(intervalMs: Long = LOCATION_UPDATE_INTERVAL_MS): Flow<Location> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs).build()
        val callback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                result.lastLocation?.let { trySend(it) }
            }
        }
        client.requestLocationUpdates(request, callback, null)
        awaitClose { client.removeLocationUpdates(callback) }
    }

    companion object {
        const val LOCATION_UPDATE_INTERVAL_MS = 7_000L
    }
}

/** Adapts a Play Services Task to a suspend call without pulling in kotlinx-coroutines-play-services. */
private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T? {
    val deferred = CompletableDeferred<T?>()
    addOnSuccessListener { deferred.complete(it) }
    addOnFailureListener { deferred.complete(null) }
    return deferred.await()
}
