package com.stignit.app.data

import com.stignit.app.data.net.GpsBody
import com.stignit.app.data.net.StignitApi
import com.stignit.app.data.net.UpdateLocationBody
import com.stignit.app.data.net.apiCall

class LocationRepository(
    private val api: StignitApi,
    private val session: SessionStore,
) {
    /**
     * Fire-and-forget position ping (proximity search + live broadcast to any
     * active incident's watchers). Not part of the SOS critical path.
     */
    suspend fun pushLocation(lat: Double, lng: Double, accuracyMeters: Float?): ApiResult<Unit> {
        val bearer = session.bearer() ?: return ApiResult.Err("Not signed in.")
        return apiCall {
            api.updateLocation(bearer, UpdateLocationBody(GpsBody(lat, lng, accuracyMeters)))
            Unit
        }
    }
}
