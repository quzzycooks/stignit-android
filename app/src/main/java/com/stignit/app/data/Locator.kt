package com.stignit.app.data

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.stignit.app.StignItApplication
import com.stignit.app.data.realtime.IncidentSocket

/** Pulls the process-wide AuthRepository out of the Application service locator. */
@Composable
fun rememberAuthRepository(): AuthRepository {
    val context = LocalContext.current
    return remember(context) {
        (context.applicationContext as StignItApplication).authRepository
    }
}

@Composable
fun rememberIncidentRepository(): IncidentRepository {
    val context = LocalContext.current
    return remember(context) {
        (context.applicationContext as StignItApplication).incidentRepository
    }
}

@Composable
fun rememberUserRepository(): UserRepository {
    val context = LocalContext.current
    return remember(context) {
        (context.applicationContext as StignItApplication).userRepository
    }
}

@Composable
fun rememberLocationRepository(): LocationRepository {
    val context = LocalContext.current
    return remember(context) {
        (context.applicationContext as StignItApplication).locationRepository
    }
}

/** Connection-lifecycle-scoped, unlike the repositories above — a fresh one per screen visit. */
@Composable
fun rememberIncidentSocket(): IncidentSocket {
    val session = LocalContext.current.sessionStore()
    return remember(session) { IncidentSocket(session) }
}

fun Context.sessionStore(): SessionStore =
    (applicationContext as StignItApplication).sessionStore
