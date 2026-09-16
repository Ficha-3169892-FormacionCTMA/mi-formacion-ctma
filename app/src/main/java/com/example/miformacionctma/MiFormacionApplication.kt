package com.example.miformacionctma

import android.app.Application
import androidx.room3.Room
import com.example.miformacionctma.data.local.FormacionDatabase

class MiFormacionApplication : Application() {

    val database: FormacionDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            FormacionDatabase::class.java,
            "formacion.db"
        ).build()
    }
}
