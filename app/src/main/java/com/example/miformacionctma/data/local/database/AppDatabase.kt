package com.example.miformacionctma.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.miformacionctma.data.local.dao.ActividadDao
import com.example.miformacionctma.data.local.dao.EvidenciaDao
import com.example.miformacionctma.data.local.entity.ActividadEntity
import com.example.miformacionctma.data.local.entity.EvidenciaEntity

@Database(
    entities = [ActividadEntity::class, EvidenciaEntity::class],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun actividadDao(): ActividadDao
    abstract fun evidenciaDao(): EvidenciaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // La tabla debe coincidir exactamente con EvidenciaEntity (PK = actividadId), si no Room cierra la app
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `evidencias` (
                        `actividadId` INTEGER NOT NULL,
                        `uri` TEXT NOT NULL,
                        `tipo` TEXT NOT NULL,
                        `tamano` INTEGER NOT NULL,
                        `estado` TEXT NOT NULL,
                        PRIMARY KEY(`actividadId`),
                        FOREIGN KEY(`actividadId`) REFERENCES `actividades`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
            }
        }

        // Agrega la URL remota de Supabase sin borrar las evidencias existentes
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `evidencias` ADD COLUMN `urlRemota` TEXT")
            }
        }

        // Campos para sincronizar actividades con Supabase. Las existentes quedan pendientes de subir (sincronizada = 0)
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `actividades` ADD COLUMN `sincronizada` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `actividades` ADD COLUMN `eliminada` INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "miformacion_db"
                )
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    // Se remueve fallbackToDestructiveMigration() para preservar los datos guardados
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}