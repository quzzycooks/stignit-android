package com.stignit.app.data.net

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/** Retrofit surface for the endpoints the app currently needs (auth + registration). */
interface StignitApi {

    @POST("v1/auth/otp/request")
    suspend fun requestOtp(@Body body: RequestOtpBody): RequestOtpResponse

    @POST("v1/auth/otp/verify")
    suspend fun verifyOtp(@Body body: VerifyOtpBody): VerifyOtpResponse

    @POST("v1/users/register")
    suspend fun register(
        @Header("Authorization") bearer: String,
        @Body body: RegisterBody,
    ): RegisterResponse
}