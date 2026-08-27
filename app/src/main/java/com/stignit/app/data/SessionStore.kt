package com.stignit.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Persists the signed-in session (JWT pair + who the user is) in an
 * AES-encrypted preferences file. Cleared on logout.
 */
class SessionStore(context: Context) {

    private val prefs: SharedPreferences = run {
        val key = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "stignit_session",
            key,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS, null)
        private set(v) = prefs.edit().putString(KEY_ACCESS, v).apply()

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH, null)
        private set(v) = prefs.edit().putString(KEY_REFRESH, v).apply()

    var userId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        private set(v) = prefs.edit().putString(KEY_USER_ID, v).apply()

    var registrationComplete: Boolean
        get() = prefs.getBoolean(KEY_REG_COMPLETE, false)
        private set(v) = prefs.edit().putBoolean(KEY_REG_COMPLETE, v).apply()

    val isSignedIn: Boolean get() = accessToken != null

    fun bearer(): String? = accessToken?.let { "Bearer $it" }

    fun save(accessToken: String, refreshToken: String, userId: String, registrationComplete: Boolean) {
        prefs.edit()
            .putString(KEY_ACCESS, accessToken)
            .putString(KEY_REFRESH, refreshToken)
            .putString(KEY_USER_ID, userId)
            .putBoolean(KEY_REG_COMPLETE, registrationComplete)
            .apply()
    }

    fun markRegistrationComplete() {
        registrationComplete = true
    }

    fun clear() = prefs.edit().clear().apply()

    private companion object {
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
        const val KEY_USER_ID = "user_id"
        const val KEY_REG_COMPLETE = "registration_complete"
    }
}