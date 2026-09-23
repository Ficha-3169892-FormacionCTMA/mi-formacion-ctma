package com.example.miformacionctma.data.remote

import com.example.miformacionctma.BuildConfig
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    // Timeouts explícitos: conexión 10 s, lectura 20 s y llamada completa 30 s
    private fun clienteBase(): OkHttpClient.Builder = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)

    private val okHttpClient = clienteBase().build()

    private val supabaseOkHttpClient = clienteBase()
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

    // baseUrl se puede cambiar en las pruebas (MockWebServer)
    fun createSupabaseApiService(baseUrl: String = BuildConfig.SUPABASE_URL): SupabaseApiService {
        return Retrofit.Builder()
            .baseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
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
} catch (payload: SerializationException) {
    Result.failure(NetworkFailure(DataError.InvalidPayload))
} catch (e: Exception) {
    Result.failure(NetworkFailure(DataError.Unknown(e)))
}