package com.stignit.app.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.stignit.app.StignItApplication

/**
 * Ambient location ping for the proximity-alerts opt-in, on a much longer
 * interval than crash detection needs (see [com.stignit.app.StignItApplication]
 * for the 15-minute schedule). Never blocks or retries aggressively — a missed
 * cycle just means the next one, 15 minutes later, tries again.
 */
class ProximityLocationWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val hasPermission = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return Result.success()

        when (val fix = LocationTracker(applicationContext).bestEffortFix()) {
            is LocationFix.Fresh ->
                (applicationContext as StignItApplication).locationRepository
                    .reportAmbientLocation(fix.lat, fix.lng, fix.accuracyMeters)
            is LocationFix.Cached ->
                (applicationContext as StignItApplication).locationRepository
                    .reportAmbientLocation(fix.lat, fix.lng, fix.accuracyMeters)
            LocationFix.Unavailable -> Unit
        }
        return Result.success()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "proximity-location-report"
    }
}
