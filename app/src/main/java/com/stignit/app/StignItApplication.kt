package com.stignit.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.mapbox.common.MapboxOptions
import com.stignit.app.data.AuthRepository
import com.stignit.app.data.ContactsRepository
import com.stignit.app.data.IncidentRepository
import com.stignit.app.data.LocationRepository
import com.stignit.app.data.SessionStore
import com.stignit.app.data.UserRepository
import com.stignit.app.data.net.ApiProvider

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

    companion object {
        const val DETECTION_CHANNEL_ID = "stignit_detection_channel"
    }
}
