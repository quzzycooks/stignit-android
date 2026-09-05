package com.stignit.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.stignit.app.detection.CrashDetectionService
import com.stignit.app.notifications.StignItMessagingService
import com.stignit.app.ui.nav.StignItNavHost
import com.stignit.app.ui.theme.StignItTheme

class MainActivity : ComponentActivity() {

    // Compose-observed so a warm-but-backgrounded relaunch (onNewIntent) also
    // triggers navigation, not just a fresh cold start.
    private var pendingIncidentId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Accelerometer/gyroscope need no runtime permission, so detection can
        // start unconditionally. Gating this to signed-in sessions is a follow-up.
        CrashDetectionService.start(this)
        pendingIncidentId = incidentIdFrom(intent)
        setContent {
            StignItTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    StignItNavHost(pendingIncidentId = pendingIncidentId)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        incidentIdFrom(intent)?.let { pendingIncidentId = it }
    }

    private fun incidentIdFrom(intent: Intent?): String? =
        if (intent?.getBooleanExtra(StignItMessagingService.EXTRA_NEEDS_ROLE_DECLARATION, false) == true) {
            intent.getStringExtra(StignItMessagingService.EXTRA_INCIDENT_ID)
        } else {
            null
        }
}
