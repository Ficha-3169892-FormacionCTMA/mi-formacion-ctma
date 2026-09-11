package com.example.miformacionctma

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.miformacionctma.data.local.FormacionDatabase
import com.example.miformacionctma.data.local.MIGRATION_1_2
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val testDb = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FormacionDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrar_1_a_2_conserva_datos() {
        helper.createDatabase(testDb, 1).apply {
            execSQL("INSERT INTO competencias (id, nombre) VALUES (1, 'Desarrollo móvil')")
            execSQL(
                "INSERT INTO actividades (id, titulo, descripcion, fecha, prioridad, progreso, competenciaId) " +
                        "VALUES (1, 'Persistencia local', 'Laboratorio Room', '2026-09-11', 'ALTA', 40, 1)"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(testDb, 2, true, MIGRATION_1_2)

        val cursor = db.query("SELECT completada FROM actividades WHERE id = 1")
        assert(cursor.moveToFirst())
        assert(cursor.getInt(0) == 0)
        cursor.close()
    }
}