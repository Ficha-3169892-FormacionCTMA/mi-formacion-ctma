package com.example.miformacionctma.data.remote

import com.example.miformacionctma.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class SupabaseAuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestWithHeaders = originalRequest.newBuilder()
            .header("apiKey", BuildConfig.SUPABASE_ANON_KEY)
            .header("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
            .build()
        return chain.proceed(requestWithHeaders)
    }
}