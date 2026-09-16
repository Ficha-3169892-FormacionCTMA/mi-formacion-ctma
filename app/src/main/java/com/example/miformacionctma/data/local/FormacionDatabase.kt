package com.example.miformacionctma.data.local

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.example.miformacionctma.data.local.dao.ActividadDao
import com.example.miformacionctma.data.local.entity.ActividadEntity

@Database(
    entities = [ActividadEntity::class],
    version = 1,
    exportSchema = true
)
abstract class FormacionDatabase : RoomDatabase() {

    abstract fun actividadDao(): ActividadDao
}
