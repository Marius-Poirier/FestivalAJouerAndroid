package com.example.frontend_mobile_etape1.core.network

import com.example.frontend_mobile_etape1.api.*
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

    val userApi: UserApiService by lazy {
        retrofit.create(UserApiService::class.java)
    }

    val festivalApi: FestivalApiService by lazy {
        retrofit.create(FestivalApiService::class.java)
    }

    val jeuApi: JeuApiService by lazy {
        retrofit.create(JeuApiService::class.java)
    }

    val editeurApi: EditeurApiService by lazy {
        retrofit.create(EditeurApiService::class.java)
    }

    val reservationApi: ReservationApiService by lazy {
        retrofit.create(ReservationApiService::class.java)
    }

    val planApi: PlanApiService by lazy {
        retrofit.create(PlanApiService::class.java)
    }

    val assignationApi: AssignationApiService by lazy {
        retrofit.create(AssignationApiService::class.java)
    }

    val metadataApi: MetadataApiService by lazy {
        retrofit.create(MetadataApiService::class.java)
    }
}
