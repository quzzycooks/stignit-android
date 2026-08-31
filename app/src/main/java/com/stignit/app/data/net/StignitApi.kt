package com.stignit.app.data.net

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Retrofit surface for the endpoints the app currently needs (auth + registration). */
interface StignitApi {

    @POST("v1/auth/otp/request")
    suspend fun requestOtp(@Body body: RequestOtpBody): RequestOtpResponse

    @POST("v1/auth/otp/verify")
    suspend fun verifyOtp(@Body body: VerifyOtpBody): VerifyOtpResponse

    @POST("v1/auth/otp/email/request")
    suspend fun requestEmailOtp(@Body body: RequestEmailOtpBody): RequestOtpResponse

    @POST("v1/auth/otp/email/verify")
    suspend fun verifyEmailOtp(@Body body: VerifyEmailOtpBody): VerifyOtpResponse

    @POST("v1/auth/refresh")
    suspend fun refresh(@Body body: RefreshBody): RefreshResponse

    @POST("v1/users/register")
    suspend fun register(
        @Header("Authorization") bearer: String,
        @Body body: RegisterBody,
    ): RegisterResponse

    @POST("v1/incidents")
    suspend fun createIncident(
        @Header("Authorization") bearer: String,
        @Body body: CreateIncidentBody,
    ): CreateIncidentResponse

    @GET("v1/incidents/mine/active")
    suspend fun getActiveIncident(@Header("Authorization") bearer: String): ActiveIncidentResponse

    @GET("v1/incidents/mine")
    suspend fun getMyIncidents(@Header("Authorization") bearer: String): List<IncidentHistoryEntry>

    @GET("v1/incidents/{incidentId}")
    suspend fun getIncident(
        @Header("Authorization") bearer: String,
        @Path("incidentId") incidentId: String,
    ): IncidentDetailsResponse

    @POST("v1/incidents/location")
    suspend fun updateLocation(
        @Header("Authorization") bearer: String,
        @Body body: UpdateLocationBody,
    ): UpdateLocationResponse

    @GET("v1/users/me")
    suspend fun getMe(@Header("Authorization") bearer: String): MeResponse

    @PUT("v1/users/me")
    suspend fun updateProfile(
        @Header("Authorization") bearer: String,
        @Body body: UpdateProfileBody,
    ): Response<Unit>

    @GET("v1/users/me/emergency-contacts")
    suspend fun listContacts(@Header("Authorization") bearer: String): List<EmergencyContactResponse>

    @POST("v1/users/me/emergency-contacts")
    suspend fun addContact(
        @Header("Authorization") bearer: String,
        @Body body: EmergencyContactBody,
    ): List<EmergencyContactResponse>

    @DELETE("v1/users/me/emergency-contacts/{contactId}")
    suspend fun removeContact(
        @Header("Authorization") bearer: String,
        @Path("contactId") contactId: String,
    ): Response<Unit>
}