package com.stignit.app.data.net

import com.google.gson.Gson
import com.stignit.app.data.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

private val gson = Gson()

/** Shared IO-dispatched try/catch wrapper for repositories talking to [StignitApi]. */
suspend inline fun <T> apiCall(crossinline block: suspend () -> T): ApiResult<T> =
    withContext(Dispatchers.IO) {
        try {
            ApiResult.Ok(block())
        } catch (e: HttpException) {
            ApiResult.Err(parseApiError(e), e.code())
        } catch (e: IOException) {
            ApiResult.Err("Can't reach StignIt right now. Check your connection and try again.")
        } catch (e: Exception) {
            ApiResult.Err(e.message ?: "Something went wrong.")
        }
    }

fun parseApiError(e: HttpException): String {
    val raw = e.response()?.errorBody()?.string().orEmpty()
    val parsed = runCatching { gson.fromJson(raw, ApiErrorBody::class.java) }.getOrNull()
    val apiMessage = parsed?.readableMessage()
    // The API always answers with a JSON envelope. A response with neither a
    // statusCode nor a message came from the gateway / edge (e.g. Railway
    // returning plain-text "rate limited"), or the service is down — not
    // something the user did, so don't blame their input.
    val fromApi = parsed?.statusCode != null || apiMessage != null

    return when (e.code()) {
        // TokenAuthenticator already retries an expired-session 401 transparently, so a 401
        // that reaches here is either a genuinely bad/expired OTP code or a dead refresh
        // token — prefer the backend's own (already distinct, already accurate) message.
        401 -> apiMessage ?: "Incorrect or expired code."
        429 ->
            if (fromApi) "Too many attempts. Wait a moment and try again."
            else "StignIt's servers are busy right now. Try again in a minute."
        // 503 is a known, foreseeable failure the backend deliberately threw with a
        // real explanation (e.g. SMS delivery down) — show it. A bare 500 is an
        // unhandled crash with no useful message, so keep the generic reassurance.
        503 -> apiMessage ?: "StignIt's servers are having trouble. Try again shortly."
        in 500..599 -> "StignIt's servers are having trouble. Try again shortly."
        else -> apiMessage ?: "Request failed (${e.code()})."
    }
}
