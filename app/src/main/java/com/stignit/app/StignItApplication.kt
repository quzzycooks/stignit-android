package com.stignit.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mapbox.common.MapboxOptions
import com.stignit.app.data.AuthRepository
import com.stignit.app.data.ContactsRepository
import com.stignit.app.data.IncidentRepository
import com.stignit.app.data.LocationRepository
import com.stignit.app.data.SessionStore
import com.stignit.app.data.UserRepository
import com.stignit.app.data.net.ApiProvider
import com.stignit.app.location.ProximityLocationWorker
import java.util.concurrent.TimeUnit

class StignItApplication : Application() {

    // Lightweight service locator — no DI framework for a project this size.
    val sessionStore: SessionStore by lazy { SessionStore(this) }
    private val api by lazy { ApiProvider.create(sessionStore) }
    val authRepository: AuthRepository by lazy { AuthRepository(api, sessionStore) }
    val incidentRepository: IncidentRepository by lazy { IncidentRepository(api, sessionStore) }
    val locationRepository: LocationRepository by lazy { LocationRepository(api, sessionStore) }
    val userRepository: UserRepository by lazy { UserRepository(api, sessionStore) }
    val contactsRepository: ContactsRepository by lazy { ContactsRepository(api, sessionStore) }

    override fun onCreate() {
        super.onCreate()
        MapboxOptions.accessToken = BuildConfig.MAPBOX_ACCESS_TOKEN
        createDetectionNotificationChannel()
        createProximityAlertChannel()
        // Idempotent (ExistingPeriodicWorkPolicy.UPDATE) — safe to call every
        // process start. Resumes reporting after an app/device restart if the
        // user had proximity alerts on last session.
        if (sessionStore.proximityAlertsEnabled) startProximityLocationWork()
    }

    fun startProximityLocationWork() {
        val request = PeriodicWorkRequestBuilder<ProximityLocationWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            ProximityLocationWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun stopProximityLocationWork() {
        WorkManager.getInstance(this).cancelUniqueWork(ProximityLocationWorker.UNIQUE_WORK_NAME)
    }

    private fun createDetectionNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                DETECTION_CHANNEL_ID,
                "Crash Detection",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Shows while StignIt is actively monitoring for a crash."
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createProximityAlertChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                PROXIMITY_ALERT_CHANNEL_ID,
                "Nearby Incidents",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Alerts you when an incident is reported near you."
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val DETECTION_CHANNEL_ID = "stignit_detection_channel"
        const val PROXIMITY_ALERT_CHANNEL_ID = "stignit_proximity_channel"
    }
}
