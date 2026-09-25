package com.example.miformacionctma.data.local

import android.content.Context
import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Before
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

    @Before
    fun borrarBaseTest() {
        context.deleteDatabase("migration-test.db")
    }

    @Test
    fun migracion2a3_creaTablaEvidenciasYPermiteInsertar() = kotlinx.coroutines.test.runTest {
        val databaseV2 = helper.createDatabase(2)

        databaseV2.execSQL(
            """
            INSERT INTO actividades
            (id, titulo, descripcion, fecha, progreso, prioridad, competenciaId, completada)
            VALUES
            (1, 'Actividad v2', 'Descripción', 1700000000000, 100, 'ALTA', NULL, 1)
            """.trimIndent()
        )

        databaseV2.close()

        val databaseV3 = helper.runMigrationsAndValidate(
            3,
            listOf(MIGRATION_2_3)
        )

        databaseV3.execSQL(
            """
            INSERT INTO evidencias
            (id, actividadId, localUri, mimeType, tamano, fecha, estado)
            VALUES
            (1, 1, 'content://media/external/images/media/123', 'image/jpeg', 1024, 1700000000000, 'LOCAL')
            """.trimIndent()
        )

        val statement = databaseV3.prepare(
            """
            SELECT localUri, mimeType, tamano, estado
            FROM evidencias
            WHERE actividadId = 1
            """.trimIndent()
        )

        statement.use {
            assertEquals(true, it.step())
            assertEquals("content://media/external/images/media/123", it.getText(0))
            assertEquals("image/jpeg", it.getText(1))
            assertEquals(1024L, it.getLong(2))
            assertEquals("LOCAL", it.getText(3))
        }

        databaseV3.close()
    }
}
