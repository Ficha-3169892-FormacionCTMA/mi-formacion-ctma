package com.example.miformacionctma.data.local

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.example.miformacionctma.data.local.dao.ActividadDao
import com.example.miformacionctma.data.local.dao.CompetenciaDao
import com.example.miformacionctma.data.local.entity.ActividadEntity
import com.example.miformacionctma.data.local.entity.CompetenciaEntity

@Database(
    entities = [
        ActividadEntity::class,
        CompetenciaEntity::class
    ],
    version = 2,
    exportSchema = true
)
@androidx.room3.ColumnTypeConverters(Converters::class)
abstract class FormacionDatabase : RoomDatabase() {

    abstract fun actividadDao(): ActividadDao

    abstract fun competenciaDao(): CompetenciaDao
}
