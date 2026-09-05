package com.stignit.app.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/** POST_NOTIFICATIONS doesn't exist pre-Tiramisu — treat older devices as always-granted. */
private fun Context.hasNotificationPermission(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

/** Tracks POST_NOTIFICATIONS permission state and exposes a way to request it. */
class NotificationPermissionState internal constructor(
    private val context: Context,
    private val launcher: ActivityResultLauncher<String>,
) {
    private val grantedState = mutableStateOf(context.hasNotificationPermission())
    val isGranted: State<Boolean> get() = grantedState

    fun request() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            refresh()
        }
    }

    internal fun refresh() {
        grantedState.value = context.hasNotificationPermission()
    }
}

@Composable
fun rememberNotificationPermissionState(): NotificationPermissionState {
    val context = LocalContext.current
    lateinit var state: NotificationPermissionState
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { state.refresh() }
    state = remember(context, launcher) { NotificationPermissionState(context, launcher) }
    return state
}
