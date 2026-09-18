package com.example.miformacionctma.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ActividadEntity::class,
        EvidenciaEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun actividadDao(): ActividadDao
    abstract fun evidenciaDao(): EvidenciaDao

    companion object {

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `evidencias` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `actividadId` INTEGER NOT NULL, 
                        `uriLocal` TEXT NOT NULL, 
                        `nombreArchivo` TEXT NOT NULL, 
                        `mimeType` TEXT NOT NULL, 
                        `tamanio` INTEGER NOT NULL, 
                        `fechaCreacion` INTEGER NOT NULL, 
                        `estado` TEXT NOT NULL, 
                        `remoteUrl` TEXT, 
                        `finalidad` TEXT, 
                        FOREIGN KEY(`actividadId`) REFERENCES `actividades`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_evidencias_actividadId` ON `evidencias` (`actividadId`)")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mi_formacion_ctma.db"
                ).addMigrations(MIGRATION_1_2)
                    .build().also {
                    INSTANCE = it
                }
            }
        }
    }
}
