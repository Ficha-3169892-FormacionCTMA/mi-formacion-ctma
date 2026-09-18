package com.example.miformacionctma.data.remote

import com.example.miformacionctma.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.logging.HttpLoggingInterceptor

interface SessionTokenProvider {
    fun obtenerToken(): String
}

object RetrofitInstance {

    private const val BASE_URL = BuildConfig.SUPABASE_URL

    val PROJECT_URL = if (BASE_URL.contains("/rest/v1/")) {
        BASE_URL.substringBefore("/rest/v1/") + "/"
    } else {
        BASE_URL
    }

    var tokenProvider: SessionTokenProvider = object : SessionTokenProvider {
        override fun obtenerToken(): String {
            return BuildConfig.SUPABASE_KEY
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
        redactHeader("apikey")
        redactHeader("Authorization")
    }

    val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .addInterceptor { chain ->
            val token = tokenProvider.obtenerToken()
            val requestBuilder = chain.request().newBuilder()
                .addHeader("apikey", token)
            
            // Supabase REST y Storage requieren Authorization: Bearer
            requestBuilder.addHeader("Authorization", "Bearer $token")

            chain.proceed(requestBuilder.build())
        }
        .build()

    val api: ActividadApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(
                json.asConverterFactory(
                    "application/json".toMediaType()
                )
            )
            .build()
            .create(ActividadApi::class.java)
    }

    val evidenciaApi: EvidenciaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(
                json.asConverterFactory(
                    "application/json".toMediaType()
                )
            )
            .build()
            .create(EvidenciaApi::class.java)
    }

    val storageApi: SupabaseStorageApi by lazy {
        val storageUrl = if (BASE_URL.contains("/rest/v1/")) {
            BASE_URL.replace("/rest/v1/", "/storage/v1/")
        } else {
            // Manejo de URL sin slash final
            val base = BASE_URL.removeSuffix("/")
            if (base.endsWith("/rest/v1")) {
                base.replace("/rest/v1", "/storage/v1/")
            } else {
                base + "/storage/v1/"
            }
        }
        Retrofit.Builder()
            .baseUrl(storageUrl)
            .client(client)
            .build()
            .create(SupabaseStorageApi::class.java)
    }
}
