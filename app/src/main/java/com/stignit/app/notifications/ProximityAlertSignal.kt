package com.stignit.app.notifications

import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Bridges [StignItMessagingService] (an FCM callback, no NavController) to the
 * Compose nav graph while the app is already foregrounded — mirrors
 * [com.stignit.app.detection.CrashSignal] exactly. Emits the incidentId to
 * declare a role for. Cold/background starts instead go through the
 * notification's PendingIntent extras (see [StignItMessagingService]).
 */
object ProximityAlertSignal {
    val events = MutableSharedFlow<String>(extraBufferCapacity = 1)
}
