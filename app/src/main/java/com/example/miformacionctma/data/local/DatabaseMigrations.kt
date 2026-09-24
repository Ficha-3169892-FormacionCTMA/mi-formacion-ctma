package com.example.miformacionctma.data.local

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_1_2 = object : Migration(1, 2) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            ALTER TABLE actividades
            ADD COLUMN completada INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `evidencias` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `actividadId` INTEGER NOT NULL,
                `localUri` TEXT NOT NULL,
                `mimeType` TEXT NOT NULL,
                `tamano` INTEGER NOT NULL,
                `fecha` INTEGER NOT NULL,
                `estado` TEXT NOT NULL,
                FOREIGN KEY(`actividadId`) REFERENCES `actividades`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        connection.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `index_evidencias_actividadId` ON `evidencias` (`actividadId`)
            """.trimIndent()
        )
    }
}
