package com.example.miformacionctma.data.remote

import com.example.miformacionctma.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException

object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val okHttpClient = OkHttpClient.Builder()
        .build()

    private val supabaseOkHttpClient = OkHttpClient.Builder()
        .addInterceptor(SupabaseAuthInterceptor())
        .build()

    fun createActividadesApi(baseUrl: String = "https://api.ejemplo.com/"): ActividadesApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ActividadesApi::class.java)
    }

    fun createSupabaseApiService(): SupabaseApiService {
        val baseUrl = if (BuildConfig.SUPABASE_URL.endsWith("/")) {
            BuildConfig.SUPABASE_URL
        } else {
            "${BuildConfig.SUPABASE_URL}/"
        }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(supabaseOkHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(SupabaseApiService::class.java)
    }
}

suspend fun <T> classifyNetworkCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (timeout: SocketTimeoutException) {
    Result.failure(NetworkFailure(DataError.Timeout))
} catch (io: IOException) {
    Result.failure(NetworkFailure(DataError.NoConnection))
} catch (e: Exception) {
    Result.failure(NetworkFailure(DataError.Unknown(e)))
}