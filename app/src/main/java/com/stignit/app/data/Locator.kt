package com.stignit.app.data

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.stignit.app.StignItApplication

/** Pulls the process-wide AuthRepository out of the Application service locator. */
@Composable
fun rememberAuthRepository(): AuthRepository {
    val context = LocalContext.current
    return remember(context) {
        (context.applicationContext as StignItApplication).authRepository
    }
}

fun Context.sessionStore(): SessionStore =
    (applicationContext as StignItApplication).sessionStore
