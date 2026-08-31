package com.stignit.app.data.net

import com.stignit.app.data.SessionStore
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * Transparently rotates an expired access token on a 401 and retries once.
 * Access tokens are short-lived (15 min) — without this, any screen open
 * longer than that (e.g. background location tracking) starts failing silently.
 */
class TokenAuthenticator(
    private val session: SessionStore,
    private val apiProvider: () -> StignitApi,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.url.encodedPath.contains("/auth/")) return null
        if (responseCount(response) >= 2) return null

        val refreshToken = session.refreshToken ?: return null
        val failedAccessToken = response.request.header("Authorization")?.removePrefix("Bearer ")

        return synchronized(this) {
            // Another thread may have already refreshed while this one waited on the lock.
            val currentAccessToken = session.accessToken
            if (currentAccessToken != null && currentAccessToken != failedAccessToken) {
                return@synchronized response.request.newBuilder()
                    .header("Authorization", "Bearer $currentAccessToken")
                    .build()
            }

            val rotated = runBlocking {
                runCatching { apiProvider().refresh(RefreshBody(refreshToken)) }.getOrNull()
            }
            if (rotated == null) {
                // Refresh token itself is dead (expired/reused) — nothing left to try.
                session.clear()
                return@synchronized null
            }

            session.updateTokens(rotated.accessToken, rotated.refreshToken)
            response.request.newBuilder()
                .header("Authorization", "Bearer ${rotated.accessToken}")
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
