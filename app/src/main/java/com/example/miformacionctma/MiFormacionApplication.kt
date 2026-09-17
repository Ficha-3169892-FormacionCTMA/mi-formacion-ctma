package com.example.miformacionctma

import android.app.Application
import androidx.room3.Room
import com.example.miformacionctma.data.local.FormacionDatabase
import com.example.miformacionctma.data.local.MIGRATION_1_2
import com.example.miformacionctma.data.repository.ActividadRepository
import com.example.miformacionctma.data.repository.PreferenciasRepository
import com.example.miformacionctma.data.repository.RoomActividadRepository

class MiFormacionApplication : Application() {

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
            database.actividadDao(),
            database.competenciaDao()
        )
    }

    val preferenciasRepository: PreferenciasRepository by lazy {
        PreferenciasRepository(applicationContext)
    }
}
