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

// --- POST /v1/auth/otp/email/request ---
data class RequestEmailOtpBody(val email: String)

// --- POST /v1/auth/otp/email/verify ---
data class VerifyEmailOtpBody(val email: String, val code: String)

data class VerifyOtpResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val userId: String,
    val accessLevel: String,
    val registrationComplete: Boolean,
)

// --- POST /v1/auth/refresh ---
data class RefreshBody(val refreshToken: String)

data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
)

// --- POST /v1/users/register ---
data class EmergencyContactBody(
    val name: String,
    val phone: String,
    val relationship: String,
    val priority: Int? = null,
)

data class MedicalPersonnelProfileBody(
    val licenseNumber: String,
    val affiliation: String,
)

data class ResponderProfileBody(
    val vehicleType: String?,
    val affiliation: String?,
    val equipment: List<String>,
)

data class RegisterBody(
    val fullName: String,
    /** ISO-8601 date, e.g. "1998-05-12". Backend rejects under-16. */
    val dateOfBirth: String,
    val stateLga: String,
    val emergencyContacts: List<EmergencyContactBody>,
    /** Wire value of [com.stignit.app.data.AccountRole], e.g. "CIVILIAN". */
    val role: String,
    val profilePhotoUrl: String? = null,
    val medicalPersonnelProfile: MedicalPersonnelProfileBody? = null,
    val responderProfile: ResponderProfileBody? = null,
)

data class RegisterResponse(val id: String, val accessLevel: String, val role: String)

// --- GET/POST/DELETE /v1/users/me/emergency-contacts ---
data class EmergencyContactResponse(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val relationship: String,
    val priority: Int,
    val verified: Boolean,
)

// --- POST /v1/incidents ---
data class GpsBody(val lat: Double, val lng: Double, val accuracyMeters: Float? = null)

data class CreateIncidentBody(
    val incidentType: String,
    val gps: GpsBody? = null,
    val locationSource: String? = null,
)

data class CreateIncidentResponse(val incidentId: String, val status: String)

// --- GET /v1/incidents/mine/active ---
data class ActiveIncidentResponse(val incidentId: String?, val status: String?)

// --- GET /v1/incidents/{incidentId} ---
data class IncidentDetailsResponse(
    val incidentId: String,
    val triggeringUserId: String?,
    val status: String,
    val createdAt: String,
)

// --- GET /v1/incidents/mine ---
data class IncidentHistoryEntry(
    val incidentId: String,
    val incidentType: String,
    val status: String,
    val createdAt: String,
    val closedAt: String?,
)

// --- POST /v1/incidents/location ---
data class UpdateLocationBody(val gps: GpsBody)
data class UpdateLocationResponse(val ok: Boolean)

// --- GET/PUT /v1/users/me ---
data class MedicalInfoBody(
    val bloodType: String? = null,
    val conditions: List<String>? = null,
    val medications: List<String>? = null,
    val allergies: List<String>? = null,
)

data class UpdateProfileBody(val medicalInfo: MedicalInfoBody)

data class MeResponse(
    val fullName: String?,
    val medicalInfo: MedicalInfoBody?,
    val medicalInfoComplete: Boolean,
    val role: String? = null,
)

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