package com.example.frontend.core.network



import android.content.Context
import com.example.frontend.api.AdminApiService
import com.example.frontend.api.EditeurApiService
import com.example.frontend.api.FestivalApiService
import com.example.frontend.api.JeuApiService
import com.example.frontend.api.MetadataApiService
import com.example.frontend.api.WorkflowApiService
import com.example.frontend.api.auth.AuthApiService
import com.example.frontend.core.auth.AuthManager
import com.example.frontend.core.datastore.UserPreferencesDataStore
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "https://api.mxrjup.fun/api/"
    lateinit var userPreferencesDataStore: UserPreferencesDataStore
        private set
    lateinit var cookieJar: AppCookieJar
        private set
    private val authenticator by lazy { AuthAuthenticator(cookieJar) }

    private val json = Json { ignoreUnknownKeys = true }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
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
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
    val authApi: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }


    val festivalApi: FestivalApiService by lazy {
        retrofit.create(FestivalApiService::class.java)
    }

    val jeuApi: JeuApiService by lazy {
        retrofit.create(JeuApiService::class.java)
    }

    val metadataApi: MetadataApiService by lazy {
        retrofit.create(MetadataApiService::class.java)
    }

    val editeurApi: EditeurApiService by lazy {
        retrofit.create(EditeurApiService::class.java)
    }

    val adminApi: AdminApiService by lazy {
        retrofit.create(AdminApiService::class.java)
    }

    val workflowApi: WorkflowApiService by lazy {
        retrofit.create(WorkflowApiService::class.java)
    }

    val authManager: AuthManager by lazy {
        AuthManager(authApi, cookieJar)
    }

    fun init(context: Context) {
        userPreferencesDataStore = UserPreferencesDataStore(context.applicationContext)
        cookieJar = AppCookieJar(userPreferencesDataStore)
    }

}
