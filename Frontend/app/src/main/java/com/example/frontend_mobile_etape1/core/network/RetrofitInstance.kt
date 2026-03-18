package com.example.frontend_mobile_etape1.core.network

import com.example.frontend.api.FestivalApiService
import com.example.frontend.api.auth.AuthApiService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "https://api.mxrjup.fun/api/"

    val cookieJar = AppCookieJar()
    private val authenticator = AuthAuthenticator(cookieJar)

    private val json = Json { ignoreUnknownKeys = true }

    private val okHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .authenticator(authenticator)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
        .apply {
            val (sslContext, trustManager) = TrustAllCerts.create()
            sslSocketFactory(sslContext.socketFactory, trustManager)
            hostnameVerifier { _, _ -> true }
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val authApi: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val festivalApi: FestivalApiService by lazy {
        retrofit.create(FestivalApiService::class.java)
    }
}
