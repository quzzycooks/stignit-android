package com.stignit.app.data.realtime

import com.stignit.app.BuildConfig
import com.stignit.app.data.SessionStore
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONObject

data class LocationUpdate(val userId: String, val lat: Double, val lng: Double, val accuracyMeters: Double?)

/**
 * Thin wrapper over the /rt Socket.IO gateway. Screen-lifecycle-scoped — create
 * one per Situation Room visit via `rememberIncidentSocket()`, never process-wide.
 */
class IncidentSocket(private val session: SessionStore) {
    private var socket: Socket? = null

    fun connect() {
        if (socket != null) return
        val token = session.accessToken ?: return
        val baseUrl = BuildConfig.API_BASE_URL.trimEnd('/')
        val options = IO.Options().apply {
            transports = arrayOf("websocket")
            auth = mapOf("token" to token)
        }
        socket = IO.socket("$baseUrl/rt", options).also { it.connect() }
    }

    fun joinIncident(incidentId: String) {
        socket?.emit("incident:join", JSONObject().put("incidentId", incidentId))
    }

    fun leaveIncident(incidentId: String) {
        socket?.emit("incident:leave", JSONObject().put("incidentId", incidentId))
    }

    fun observeLocation(): Flow<LocationUpdate> = callbackFlow {
        val listener = io.socket.emitter.Emitter.Listener { args ->
            val payload = args.firstOrNull() as? JSONObject ?: return@Listener
            trySend(
                LocationUpdate(
                    userId = payload.optString("userId"),
                    lat = payload.optDouble("lat"),
                    lng = payload.optDouble("lng"),
                    accuracyMeters = payload.optDouble("accuracyMeters").takeIf { !it.isNaN() },
                ),
            )
        }
        socket?.on("incident:location", listener)
        awaitClose { socket?.off("incident:location", listener) }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
    }
}
