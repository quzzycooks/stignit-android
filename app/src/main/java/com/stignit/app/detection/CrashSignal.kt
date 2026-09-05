package com.stignit.app.detection

import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Bridges [CrashDetectionService] — a plain [android.app.Service] with no
 * NavController — to the Compose nav graph. The service `tryEmit`s from its
 * own background thread; [com.stignit.app.ui.nav.StignItNavHost] collects
 * while the app is in the foreground and routes MEDIUM/HIGH to the real
 * (non-drill) welfare check.
 */
object CrashSignal {
    val events = MutableSharedFlow<DetectionConfidence>(extraBufferCapacity = 1)
}
