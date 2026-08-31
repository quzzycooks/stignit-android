package com.stignit.app.data.net

import com.stignit.app.BuildConfig
import com.stignit.app.data.SessionStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Builds the single Retrofit instance pointed at BuildConfig.API_BASE_URL. */
object ApiProvider {

    /** [session] backs a [TokenAuthenticator] that transparently rotates expired access tokens. */
    fun create(session: SessionStore): StignitApi {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

        // `api` is read lazily by TokenAuthenticator, only once the Retrofit instance below
        // exists — safe despite the apparent circularity (authenticate() never runs during construction).
        lateinit var api: StignitApi
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .authenticator(TokenAuthenticator(session) { api })
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        api = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StignitApi::class.java)
        return api
    }
}