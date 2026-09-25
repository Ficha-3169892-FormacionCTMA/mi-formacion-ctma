package com.example.miformacionctma.data.local

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import com.example.miformacionctma.data.local.dao.ActividadDao
import com.example.miformacionctma.data.local.dao.CompetenciaDao
import com.example.miformacionctma.data.local.dao.EvidenciaDao
import com.example.miformacionctma.data.local.entities.ActividadEntity
import com.example.miformacionctma.data.local.entities.CompetenciaEntity
import com.example.miformacionctma.data.local.entities.EvidenciaEntity

@Database(
    entities = [
        ActividadEntity::class,
        CompetenciaEntity::class,
        EvidenciaEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class FormacionDatabase : RoomDatabase() {
    abstract fun actividadDao(): ActividadDao
    abstract fun competenciaDao(): CompetenciaDao
    abstract fun evidenciaDao(): EvidenciaDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "ALTER TABLE actividades " +
                    "ADD COLUMN completada INTEGER NOT NULL DEFAULT 0"
        )
    }
}

fun crearDatabase(context: Context): FormacionDatabase =
    Room.databaseBuilder<FormacionDatabase>(
        context.applicationContext,
        name = "mi_formacion_ctma.db"
    )
        .setDriver(AndroidSQLiteDriver())
        .addMigrations(MIGRATION_1_2)
        .fallbackToDestructiveMigration()
        .addCallback(object : RoomDatabase.Callback() {
            override suspend fun onCreate(connection: SQLiteConnection) { // <-- Añadido 'suspend' aquí
                super.onCreate(connection)
                // Insertamos competencias iniciales para que el formulario nunca arranque vacío
                connection.execSQL("INSERT INTO competencias (nombre) VALUES ('Desarrollo de Software')")
                connection.execSQL("INSERT INTO competencias (nombre) VALUES ('Inglés')")
                connection.execSQL("INSERT INTO competencias (nombre) VALUES ('Ética y Cívica')")
            }
        })
        .build()