package com.example.miformacionctma.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ActividadEntity::class,
        EvidenciaEntity::class // <-- Agregamos la nueva entidad de evidencia
    ],
    version = 2, // <-- Incrementamos la versión de la base de datos
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun actividadDao(): ActividadDao
    abstract fun evidenciaDao(): EvidenciaDao // <-- Exponemos el nuevo DAO
}