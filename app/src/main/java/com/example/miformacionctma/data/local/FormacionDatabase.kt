package com.example.miformacionctma.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Objeto de migración v1 a v2 (Sección 10 de la guía)
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE actividades ADD COLUMN completada INTEGER NOT NULL DEFAULT 0"
        )
    }
}

@Database(
    entities = [ActividadEntity::class, CompetenciaEntity::class],
    version = 2, // Se incrementa la versión a 2
    exportSchema = true
)
abstract class FormacionDatabase : RoomDatabase() {

    abstract fun actividadDao(): ActividadDao
    abstract fun competenciaDao(): CompetenciaDao

    companion object {
        @Volatile
        private var INSTANCE: FormacionDatabase? = null

        fun getDatabase(context: Context): FormacionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FormacionDatabase::class.java,
                    "formacion_db"
                )
                    .addMigrations(MIGRATION_1_2) // Se registra la migración
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}