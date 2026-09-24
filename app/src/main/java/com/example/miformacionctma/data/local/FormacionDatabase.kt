package com.example.miformacionctma.data.local

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.example.miformacionctma.data.local.dao.ActividadDao
import com.example.miformacionctma.data.local.dao.CompetenciaDao
import com.example.miformacionctma.data.local.dao.EvidenciaDao
import com.example.miformacionctma.data.local.entity.ActividadEntity
import com.example.miformacionctma.data.local.entity.CompetenciaEntity
import com.example.miformacionctma.data.local.entity.EvidenciaEntity

@Database(
    entities = [
        ActividadEntity::class,
        CompetenciaEntity::class,
        EvidenciaEntity::class
    ],
    version = 4,
    exportSchema = true
)
@androidx.room3.ColumnTypeConverters(Converters::class)
abstract class FormacionDatabase : RoomDatabase() {

    abstract fun actividadDao(): ActividadDao

    abstract fun competenciaDao(): CompetenciaDao

    abstract fun evidenciaDao(): EvidenciaDao
}
