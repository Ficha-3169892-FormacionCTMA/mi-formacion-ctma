package com.example.miformacionctma.data.remote

import com.example.miformacionctma.BuildConfig
import com.example.miformacionctma.data.remote.api.ActividadesApi
import com.example.miformacionctma.data.remote.auth.TokenProvider
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Provee y configura las instancias de red (OkHttp, Retrofit).
 */
object NetworkModule {

    private const val BASE_URL = BuildConfig.SUPABASE_URL
    private const val SUPABASE_PUBLISHABLE_KEY = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    fun provideOkHttpClient(tokenProvider: TokenProvider): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.ENVIRONMENT == "PROD") {
                HttpLoggingInterceptor.Level.NONE
            } else {
                HttpLoggingInterceptor.Level.HEADERS
            }
            redactHeader("apikey")
            redactHeader("Authorization")
            redactHeader("Idempotency-Key")
        }

        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("apikey", SUPABASE_PUBLISHABLE_KEY)
                    .header("Authorization", "Bearer ${tokenProvider.getToken() ?: SUPABASE_PUBLISHABLE_KEY}")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    fun provideActividadesApi(okHttpClient: OkHttpClient): ActividadesApi {
        val contentType = "application/json".toMediaType()
        
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .callFactory(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(ActividadesApi::class.java)
    }
}
