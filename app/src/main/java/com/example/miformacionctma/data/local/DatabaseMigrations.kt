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
