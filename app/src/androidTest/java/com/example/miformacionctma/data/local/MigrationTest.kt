package com.example.miformacionctma.data.local

import android.content.Context
import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val context =
        ApplicationProvider.getApplicationContext<Context>()

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation = InstrumentationRegistry.getInstrumentation(),
        databaseClass = FormacionDatabase::class,
        driver = BundledSQLiteDriver(),
        file = context.getDatabasePath("migration-test.db")
    )

    @Test
    fun migracion1a2_conservaDatosYAgregaCompletada() = kotlinx.coroutines.test.runTest {
        val databaseV1 = helper.createDatabase(1)

        databaseV1.execSQL(
            """
            INSERT INTO actividades
            (id, titulo, descripcion, fecha, progreso, prioridad, competenciaId)
            VALUES
            (1, 'Actividad antigua', 'Prueba de migración',
             '2026-09-20', 50, 'MEDIA', NULL)
            """.trimIndent()
        )

        databaseV1.close()

        val databaseV2 = helper.runMigrationsAndValidate(
            2,
            listOf(MIGRATION_1_2)
        )

        val statement = databaseV2.prepare(
            """
            SELECT titulo, completada
            FROM actividades
            WHERE id = 1
            """.trimIndent()
        )

        statement.use {
            assertEquals(true, it.step())
            assertEquals("Actividad antigua", it.getText(0))
            assertEquals(0L, it.getLong(1))
        }

        databaseV2.close()
    }
}
