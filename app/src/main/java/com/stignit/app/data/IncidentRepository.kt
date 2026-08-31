package com.stignit.app.data

import com.stignit.app.data.net.CreateIncidentBody
import com.stignit.app.data.net.GpsBody
import com.stignit.app.data.net.StignitApi
import com.stignit.app.data.net.apiCall
import com.stignit.app.location.LocationFix

/** Sentinel incidentId used by drill/simulate flows — never a real incident, never hits the backend. */
const val DRILL_INCIDENT_ID = "DRILL"

data class CreatedIncident(val incidentId: String, val status: String)
data class ActiveIncident(val incidentId: String, val status: String)
data class IncidentDetails(
    val incidentId: String,
    val triggeringUserId: String?,
    val status: String,
    val createdAt: String,
)
data class IncidentHistoryItem(
    val incidentId: String,
    val incidentType: String,
    val status: String,
    val createdAt: String,
    val closedAt: String?,
)

class IncidentRepository(
    private val api: StignitApi,
    private val session: SessionStore,
) {
    /** SOS trigger. `fix` may be [LocationFix.Unavailable] — the incident must still be created. */
    suspend fun createIncident(incidentType: String, fix: LocationFix): ApiResult<CreatedIncident> {
        val bearer = session.bearer()
            ?: return ApiResult.Err("Your session expired — sign in again.")
        val (gps, locationSource) = when (fix) {
            is LocationFix.Fresh -> GpsBody(fix.lat, fix.lng, fix.accuracyMeters) to "fresh"
            is LocationFix.Cached -> GpsBody(fix.lat, fix.lng, fix.accuracyMeters) to "cached"
            LocationFix.Unavailable -> null to "unavailable"
        }
        return apiCall {
            val res = api.createIncident(bearer, CreateIncidentBody(incidentType, gps, locationSource))
            CreatedIncident(res.incidentId, res.status)
        }
    }

    suspend fun getActiveIncident(): ApiResult<ActiveIncident?> {
        val bearer = session.bearer() ?: return ApiResult.Ok(null)
        return apiCall {
            val res = api.getActiveIncident(bearer)
            res.incidentId?.let { ActiveIncident(it, res.status ?: "ACTIVE") }
        }
    }

    suspend fun getIncident(incidentId: String): ApiResult<IncidentDetails> {
        val bearer = session.bearer()
            ?: return ApiResult.Err("Your session expired — sign in again.")
        return apiCall {
            val res = api.getIncident(bearer, incidentId)
            IncidentDetails(res.incidentId, res.triggeringUserId, res.status, res.createdAt)
        }
    }

    /** Caller's own incident history, most recent first — backs the welfare-check history screen. */
    suspend fun getMyIncidents(): ApiResult<List<IncidentHistoryItem>> {
        val bearer = session.bearer() ?: return ApiResult.Ok(emptyList())
        return apiCall {
            api.getMyIncidents(bearer).map {
                IncidentHistoryItem(it.incidentId, it.incidentType, it.status, it.createdAt, it.closedAt)
            }
        }
    }
}
