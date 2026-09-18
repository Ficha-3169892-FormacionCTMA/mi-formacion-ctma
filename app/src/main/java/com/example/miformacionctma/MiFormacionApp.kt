package com.example.miformacionctma

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.example.miformacionctma.data.remote.RetrofitInstance

class MiFormacionApp : Application(), SingletonImageLoader.Factory {
    override fun newImageLoader(context: coil3.PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                // Usamos el cliente de OkHttp que ya tiene los interceptores de Supabase (apikey y Bearer)
                add(OkHttpNetworkFetcherFactory(RetrofitInstance.client))
            }
            .build()
    }
}
