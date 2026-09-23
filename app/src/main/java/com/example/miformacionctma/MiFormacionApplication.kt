package com.example.miformacionctma

import android.app.Application
import androidx.room3.Room
import com.example.miformacionctma.data.local.FormacionDatabase
import com.example.miformacionctma.data.local.MIGRATION_1_2
import com.example.miformacionctma.data.remote.NetworkModule
import com.example.miformacionctma.data.remote.auth.SessionTokenProvider
import com.example.miformacionctma.data.remote.auth.TokenProvider
import com.example.miformacionctma.data.repository.ActividadRepository
import com.example.miformacionctma.data.repository.PreferenciasRepository
import com.example.miformacionctma.data.repository.RoomActividadRepository

class MiFormacionApplication : Application() {

    private val tokenProvider: TokenProvider by lazy {
        SessionTokenProvider()
    }

    private val okHttpClient by lazy {
        NetworkModule.provideOkHttpClient(tokenProvider)
    }

    val actividadesApi by lazy {
        NetworkModule.provideActividadesApi(okHttpClient)
    }

    val database: FormacionDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            FormacionDatabase::class.java,
            "formacion.db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    val actividadRepository: ActividadRepository by lazy {
        RoomActividadRepository(
            dao = database.actividadDao(),
            competenciaDao = database.competenciaDao(),
            db = database,
            api = actividadesApi
        )
    }

    val preferenciasRepository: PreferenciasRepository by lazy {
        PreferenciasRepository(applicationContext)
    }
}
