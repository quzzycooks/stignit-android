package com.stignit.app.data.net

import com.google.gson.annotations.SerializedName

/**
 * Wire models for stignit-api (§10). Field names match the NestJS DTOs exactly;
 * see stignit-api/src/auth and src/users.
 */

// --- POST /v1/auth/otp/request ---
data class RequestOtpBody(val phone: String)

data class RequestOtpResponse(
    /** Present only when the API runs with NODE_ENV != production. */
    @SerializedName("devCode") val devCode: String? = null,
    @SerializedName("resendInSec") val resendInSec: Int = 30,
)

// --- POST /v1/auth/otp/verify ---
data class VerifyOtpBody(val phone: String, val code: String)

data class VerifyOtpResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val userId: String,
    val accessLevel: String,
    val registrationComplete: Boolean,
)

// --- POST /v1/users/register ---
data class EmergencyContactBody(
    val name: String,
    val phone: String,
    val relationship: String,
    val priority: Int? = null,
)

data class RegisterBody(
    val fullName: String,
    /** ISO-8601 date, e.g. "1998-05-12". Backend rejects under-16. */
    val dateOfBirth: String,
    val stateLga: String,
    val emergencyContacts: List<EmergencyContactBody>,
    val profilePhotoUrl: String? = null,
)

data class RegisterResponse(val id: String, val accessLevel: String)

/** NestJS error envelope ({ statusCode, message, error }). `message` can be a string or string[]. */
data class ApiErrorBody(
    val statusCode: Int? = null,
    val error: String? = null,
    val message: Any? = null,
) {
    fun readableMessage(): String? = when (val m = message) {
        is String -> m
        is List<*> -> m.filterIsInstance<String>().joinToString("\n").ifBlank { null }
        else -> null
    }
}