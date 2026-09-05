package com.stignit.app.detection

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.stignit.app.MainActivity
import com.stignit.app.StignItApplication

/**
 * Foreground service that keeps crash detection alive — starts on demand, shows
 * the required persistent notification, holds a partial wake lock, and survives
 * Doze / App Standby.
 *
 * [SensorFusionEngine] is started in [onStartCommand] and stopped in [onDestroy].
 * MEDIUM/HIGH candidates are pushed onto [CrashSignal] for the nav graph to pick
 * up — see [onDetectionCandidate].
 */
class CrashDetectionService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var sensorFusion: SensorFusionEngine? = null

    override fun onCreate() {
        super.onCreate()
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "StignIt::CrashDetectionWakeLock",
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        wakeLock?.acquire(WAKE_LOCK_TIMEOUT_MS)

        // START_STICKY can redeliver onStartCommand; only wire the engine once.
        if (sensorFusion == null) {
            val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
            sensorFusion = SensorFusionEngine(
                sensorManager = sensorManager,
                onCandidateEvent = ::onDetectionCandidate,
                // Milestone 4: supply a FusedLocation speed source here.
            ).also { it.start() }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        sensorFusion?.stop()
        sensorFusion = null
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Invoked on [SensorFusionEngine]'s background thread. MEDIUM/HIGH candidates
     * are pushed onto [CrashSignal]; [com.stignit.app.ui.nav.StignItNavHost]
     * collects it while the app is foregrounded and routes to the real
     * (non-drill) welfare check. LOW stays log-only.
     */
    private fun onDetectionCandidate(confidence: DetectionConfidence) {
        when (confidence) {
            DetectionConfidence.LOW ->
                Log.d(TAG, "Detection candidate: LOW — logged only, no welfare check")
            DetectionConfidence.MEDIUM, DetectionConfidence.HIGH -> {
                Log.d(TAG, "Detection candidate: $confidence — signaling welfare check")
                CrashSignal.events.tryEmit(confidence)
            }
        }
    }

    private fun buildNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, StignItApplication.DETECTION_CHANNEL_ID)
            .setContentTitle("StignIt is monitoring")
            .setContentText("Crash detection is active in the background.")
            .setSmallIcon(android.R.drawable.ic_menu_compass) // placeholder, real icon in ui polish pass
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        private const val TAG = "StignIt/CrashService"
        private const val NOTIFICATION_ID = 1001
        private const val WAKE_LOCK_TIMEOUT_MS = 10L * 60L * 60L * 1000L // 10h safety cap

        fun start(context: android.content.Context) {
            val intent = Intent(context, CrashDetectionService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: android.content.Context) {
            context.stopService(Intent(context, CrashDetectionService::class.java))
        }
    }
}
