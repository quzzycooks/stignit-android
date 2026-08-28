package com.stignit.app.data

import com.google.gson.Gson
import com.stignit.app.data.net.ApiErrorBody
import com.stignit.app.data.net.EmergencyContactBody
import com.stignit.app.data.net.RegisterBody
import com.stignit.app.data.net.RequestOtpBody
import com.stignit.app.data.net.StignitApi
import com.stignit.app.data.net.VerifyOtpBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

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
    private val gson = Gson()

    suspend fun requestOtp(phone: String): ApiResult<OtpRequested> = call {
        val res = api.requestOtp(RequestOtpBody(phone.trim()))
        OtpRequested(res.resendInSec, res.devCode)
    }

    suspend fun verifyOtp(phone: String, code: String): ApiResult<SignedIn> = call {
        val res = api.verifyOtp(VerifyOtpBody(phone.trim(), code.trim()))
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
        return call {
            api.register(bearer, RegisterBody(fullName.trim(), dateOfBirth, stateLga.trim(), contacts))
            session.markRegistrationComplete()
        }
    }

    private suspend inline fun <T> call(crossinline block: suspend () -> T): ApiResult<T> =
        withContext(Dispatchers.IO) {
            try {
                ApiResult.Ok(block())
            } catch (e: HttpException) {
                ApiResult.Err(parseError(e), e.code())
            } catch (e: IOException) {
                ApiResult.Err("Can't reach StignIt right now. Check your connection and try again.")
            } catch (e: Exception) {
                ApiResult.Err(e.message ?: "Something went wrong.")
            }
        }

    private fun parseError(e: HttpException): String {
        val raw = e.response()?.errorBody()?.string().orEmpty()
        val parsed = runCatching { gson.fromJson(raw, ApiErrorBody::class.java) }.getOrNull()
        val apiMessage = parsed?.readableMessage()
        // The API always answers with a JSON envelope. A response with neither a
        // statusCode nor a message came from the gateway / edge (e.g. Railway
        // returning plain-text "rate limited"), or the service is down — not
        // something the user did, so don't blame their input.
        val fromApi = parsed?.statusCode != null || apiMessage != null

        return when (e.code()) {
            401 -> "Incorrect or expired code."
            429 ->
                if (fromApi) "Too many attempts. Wait a moment and try again."
                else "StignIt's servers are busy right now. Try again in a minute."
            in 500..599 -> "StignIt's servers are having trouble. Try again shortly."
            else -> apiMessage ?: "Request failed (${e.code()})."
        }
    }
}