package com.stignit.app.data

import com.stignit.app.data.net.FcmTokenBody
import com.stignit.app.data.net.MedicalInfoBody
import com.stignit.app.data.net.StignitApi
import com.stignit.app.data.net.UpdateProfileBody
import com.stignit.app.data.net.apiCall
import retrofit2.HttpException

data class MedicalInfo(
    val bloodType: String? = null,
    val conditions: List<String> = emptyList(),
    val medications: List<String> = emptyList(),
    val allergies: List<String> = emptyList(),
) {
    fun toBody() = MedicalInfoBody(
        bloodType = bloodType,
        conditions = conditions.ifEmpty { null },
        medications = medications.ifEmpty { null },
        allergies = allergies.ifEmpty { null },
    )
}

data class Profile(
    val fullName: String?,
    val medicalInfo: MedicalInfo?,
    val medicalInfoComplete: Boolean,
    val proximityAlertsEnabled: Boolean,
)

class UserRepository(
    private val api: StignitApi,
    private val session: SessionStore,
) {
    suspend fun getMe(): ApiResult<Profile> {
        val bearer = session.bearer() ?: return ApiResult.Err("Not signed in.")
        return apiCall {
            val res = api.getMe(bearer)
            Profile(
                fullName = res.fullName,
                medicalInfo = res.medicalInfo?.let {
                    MedicalInfo(it.bloodType, it.conditions.orEmpty(), it.medications.orEmpty(), it.allergies.orEmpty())
                },
                medicalInfoComplete = res.medicalInfoComplete,
                proximityAlertsEnabled = res.proximityAlertsEnabled,
            )
        }
    }

    suspend fun updateMedicalInfo(info: MedicalInfo): ApiResult<Unit> {
        val bearer = session.bearer() ?: return ApiResult.Err("Not signed in.")
        return apiCall {
            val res = api.updateProfile(bearer, UpdateProfileBody(medicalInfo = info.toBody()))
            if (!res.isSuccessful) throw HttpException(res)
        }
    }

    /** Server-side mirror of the local toggle — the backend gates the proximity-match query on this. */
    suspend fun updateProximityAlertsEnabled(enabled: Boolean): ApiResult<Unit> {
        val bearer = session.bearer() ?: return ApiResult.Err("Not signed in.")
        return apiCall {
            val res = api.updateProfile(bearer, UpdateProfileBody(proximityAlertsEnabled = enabled))
            if (!res.isSuccessful) throw HttpException(res)
        }
    }

    suspend fun updateFcmToken(token: String): ApiResult<Unit> {
        val bearer = session.bearer() ?: return ApiResult.Err("Not signed in.")
        return apiCall {
            val res = api.updateFcmToken(bearer, FcmTokenBody(token))
            if (!res.isSuccessful) throw HttpException(res)
        }
    }
}
