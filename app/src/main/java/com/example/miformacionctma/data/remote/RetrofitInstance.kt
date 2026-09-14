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

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .addInterceptor { chain ->
            val request: Request = chain.request()
                .newBuilder()
                .addHeader("apikey", tokenProvider.obtenerToken())
                .build()

            chain.proceed(request)
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
}