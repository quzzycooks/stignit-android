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

    var fullName: String?
        get() = prefs.getString(KEY_FULL_NAME, null)
        private set(v) = prefs.edit().putString(KEY_FULL_NAME, v).apply()

    var role: AccountRole
        get() = AccountRole.fromWire(prefs.getString(KEY_ROLE, null))
        private set(v) = prefs.edit().putString(KEY_ROLE, v.name).apply()

    // Separate from crash-detection monitoring — a user can have one without the
    // other. Opt-in, defaults false. Publicly settable (unlike the props above)
    // since a Settings toggle mutates this directly, not through a compound flow.
    var proximityAlertsEnabled: Boolean
        get() = prefs.getBoolean(KEY_PROXIMITY_ALERTS, false)
        set(v) = prefs.edit().putBoolean(KEY_PROXIMITY_ALERTS, v).apply()

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

    /** Rotates just the JWT pair after a [/auth/refresh] call — leaves who-the-user-is untouched. */
    fun updateTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS, accessToken)
            .putString(KEY_REFRESH, refreshToken)
            .apply()
    }

    fun markRegistrationComplete(fullName: String, role: AccountRole) {
        registrationComplete = true
        this.fullName = fullName
        this.role = role
    }

    fun clear() = prefs.edit().clear().apply()

    private companion object {
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
        const val KEY_USER_ID = "user_id"
        const val KEY_REG_COMPLETE = "registration_complete"
        const val KEY_FULL_NAME = "full_name"
        const val KEY_ROLE = "account_role"
        const val KEY_PROXIMITY_ALERTS = "proximity_alerts_enabled"
    }
}