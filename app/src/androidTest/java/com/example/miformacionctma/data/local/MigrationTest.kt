package com.example.miformacionctma.data.local

import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation = instrumentation,
        databaseClass = FormacionDatabase::class,
        driver = AndroidSQLiteDriver(),
        file = instrumentation.targetContext.getDatabasePath("migration-test")
    )

    @Test fun migrar_1_a_2_conserva_datos() = runTest {
        val c1 = helper.createDatabase(1)
        c1.execSQL(
            "INSERT INTO competencias (id,nombre) VALUES (1,'Desarrollo móvil')"
        )
        c1.execSQL(
            "INSERT INTO actividades " +
            "(id,titulo,descripcion,progreso,prioridad,competenciaId,fechaLimiteEpochMillis) " +
            "VALUES ('ACT-001','Persistencia local','Completar laboratorio Room',40,'ALTA',1,0)"
        )
        c1.close()

        val c2 = helper.runMigrationsAndValidate(2, listOf(MIGRATION_1_2))
        val conservado = c2.prepare(
            "SELECT completada FROM actividades WHERE id='ACT-001'"
        ).use { stmt -> stmt.step() && stmt.getLong(0) == 0L }
        
        assertTrue(conservado)
        c2.close()
    }
}
