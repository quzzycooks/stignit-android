package com.stignit.app.data

import com.google.firebase.messaging.FirebaseMessaging
import com.stignit.app.data.net.EmergencyContactBody
import com.stignit.app.data.net.FcmTokenBody
import com.stignit.app.data.net.MedicalPersonnelProfileBody
import com.stignit.app.data.net.RegisterBody
import com.stignit.app.data.net.RequestEmailOtpBody
import com.stignit.app.data.net.RequestOtpBody
import com.stignit.app.data.net.ResponderProfileBody
import com.stignit.app.data.net.StignitApi
import com.stignit.app.data.net.VerifyEmailOtpBody
import com.stignit.app.data.net.VerifyOtpBody
import com.stignit.app.data.net.apiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed interface ApiResult<out T> {
    data class Ok<T>(val value: T) : ApiResult<T>
    data class Err(val message: String, val code: Int? = null) : ApiResult<Nothing>
}

data class OtpRequested(val resendInSec: Int, val devCode: String?)

/** Result of a successful OTP verification — tells the caller whether a profile step is still needed. */
data class SignedIn(val registrationComplete: Boolean)

class AuthRepository(
    private val api: StignitApi,
    private val session: SessionStore,
) {
    suspend fun requestOtp(phone: String): ApiResult<OtpRequested> = apiCall {
        val res = api.requestOtp(RequestOtpBody(phone.trim()))
        OtpRequested(res.resendInSec, res.devCode)
    }

    suspend fun verifyOtp(phone: String, code: String): ApiResult<SignedIn> = apiCall {
        val res = api.verifyOtp(VerifyOtpBody(phone.trim(), code.trim()))
        session.save(res.accessToken, res.refreshToken, res.userId, res.registrationComplete)
        pushCurrentFcmToken()
        SignedIn(res.registrationComplete)
    }

    suspend fun requestEmailOtp(email: String): ApiResult<OtpRequested> = apiCall {
        val res = api.requestEmailOtp(RequestEmailOtpBody(email.trim()))
        OtpRequested(res.resendInSec, res.devCode)
    }

    suspend fun verifyEmailOtp(email: String, code: String): ApiResult<SignedIn> = apiCall {
        val res = api.verifyEmailOtp(VerifyEmailOtpBody(email.trim(), code.trim()))
        session.save(res.accessToken, res.refreshToken, res.userId, res.registrationComplete)
        pushCurrentFcmToken()
        SignedIn(res.registrationComplete)
    }

    suspend fun register(
        fullName: String,
        dateOfBirth: String,
        stateLga: String,
        contacts: List<EmergencyContactBody>,
        role: AccountRole,
        medicalPersonnelProfile: MedicalPersonnelProfileBody? = null,
        responderProfile: ResponderProfileBody? = null,
    ): ApiResult<Unit> {
        val bearer = session.bearer()
            ?: return ApiResult.Err("Your session expired — verify your number again.")
        val trimmedName = fullName.trim()
        return apiCall {
            api.register(
                bearer,
                RegisterBody(
                    fullName = trimmedName,
                    dateOfBirth = dateOfBirth,
                    stateLga = stateLga.trim(),
                    emergencyContacts = contacts,
                    role = role.name,
                    medicalPersonnelProfile = medicalPersonnelProfile,
                    responderProfile = responderProfile,
                ),
            )
            session.markRegistrationComplete(trimmedName, role)
        }
    }

    /** FCM generates its token before sign-in, so [com.stignit.app.notifications.StignItMessagingService.onNewToken]
     *  usually fires while there's no session yet and its push silently no-ops. Re-push the
     *  current token here, right after a session exists, so the backend always has one. */
    private fun pushCurrentFcmToken() {
        val bearer = session.bearer() ?: return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                apiCall {
                    val res = api.updateFcmToken(bearer, FcmTokenBody(token))
                    if (!res.isSuccessful) throw HttpException(res)
                }
            }
        }
    }
}
