package com.stignit.app.notifications

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.stignit.app.MainActivity
import com.stignit.app.StignItApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Receives proximity-alert pushes. Two delivery paths on tap, mirroring
 * [com.stignit.app.detection.CrashSignal]'s cold/warm split: a foregrounded app
 * picks this up via [ProximityAlertSignal]; a cold/backgrounded app is launched
 * fresh via the notification's PendingIntent extras, read in
 * [com.stignit.app.MainActivity].
 */
class StignItMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        val app = applicationContext as StignItApplication
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            app.userRepository.updateFcmToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val incidentId = message.data["incidentId"] ?: return
        val title = message.notification?.title ?: "StignIt"
        val body = message.notification?.body ?: "An incident was reported near you."

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                putExtra(EXTRA_INCIDENT_ID, incidentId)
                putExtra(EXTRA_NEEDS_ROLE_DECLARATION, true)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(this, StignItApplication.PROXIMITY_ALERT_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // placeholder, real icon in ui polish pass
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(this).notify(incidentId.hashCode(), notification)

        ProximityAlertSignal.events.tryEmit(incidentId)
    }

    companion object {
        const val EXTRA_INCIDENT_ID = "incidentId"
        const val EXTRA_NEEDS_ROLE_DECLARATION = "needsRoleDeclaration"
    }
}
