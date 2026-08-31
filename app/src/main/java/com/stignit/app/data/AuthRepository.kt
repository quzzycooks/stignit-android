package com.stignit.app.data

import com.stignit.app.data.net.EmergencyContactBody
import com.stignit.app.data.net.RegisterBody
import com.stignit.app.data.net.RequestEmailOtpBody
import com.stignit.app.data.net.RequestOtpBody
import com.stignit.app.data.net.StignitApi
import com.stignit.app.data.net.VerifyEmailOtpBody
import com.stignit.app.data.net.VerifyOtpBody
import com.stignit.app.data.net.apiCall

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
        SignedIn(res.registrationComplete)
    }

    suspend fun requestEmailOtp(email: String): ApiResult<OtpRequested> = apiCall {
        val res = api.requestEmailOtp(RequestEmailOtpBody(email.trim()))
        OtpRequested(res.resendInSec, res.devCode)
    }

    suspend fun verifyEmailOtp(email: String, code: String): ApiResult<SignedIn> = apiCall {
        val res = api.verifyEmailOtp(VerifyEmailOtpBody(email.trim(), code.trim()))
        session.save(res.accessToken, res.refreshToken, res.userId, res.registrationComplete)
        SignedIn(res.registrationComplete)
    }

    suspend fun register(
        fullName: String,
        dateOfBirth: String,
        stateLga: String,
        contacts: List<EmergencyContactBody>,
    ): ApiResult<Unit> {
        val bearer = session.bearer()
            ?: return ApiResult.Err("Your session expired — verify your number again.")
        val trimmedName = fullName.trim()
        return apiCall {
            api.register(bearer, RegisterBody(trimmedName, dateOfBirth, stateLga.trim(), contacts))
            session.markRegistrationComplete(trimmedName)
        }
    }
}
