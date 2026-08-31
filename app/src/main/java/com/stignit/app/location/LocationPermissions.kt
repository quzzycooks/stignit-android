package com.stignit.app.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

/** Tracks fine/coarse location permission state and exposes a way to request it. */
class LocationPermissionState internal constructor(
    private val context: Context,
    private val launcher: ActivityResultLauncher<Array<String>>,
) {
    private val grantedState = mutableStateOf(context.hasLocationPermission())
    val isGranted: State<Boolean> get() = grantedState

    fun request() {
        launcher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
        )
    }

    internal fun refresh() {
        grantedState.value = context.hasLocationPermission()
    }
}

@Composable
fun rememberLocationPermissionState(): LocationPermissionState {
    val context = LocalContext.current
    lateinit var state: LocationPermissionState
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { state.refresh() }
    state = remember(context, launcher) { LocationPermissionState(context, launcher) }
    return state
}
