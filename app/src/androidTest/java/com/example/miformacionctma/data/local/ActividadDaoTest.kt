package com.example.miformacionctma.data.local

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.miformacionctma.data.local.dao.ActividadDao
import com.example.miformacionctma.data.local.entities.ActividadEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActividadDaoTest {
    private lateinit var db: FormacionDatabase
    private lateinit var dao: ActividadDao

    @Before fun crearDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder<FormacionDatabase>(context)
            .setDriver(AndroidSQLiteDriver())
            .build()
        dao = db.actividadDao()
    }

    @After fun cerrarDb() = db.close()

    @Test fun insertar_y_observar() = runTest {
        val entity = ActividadEntity(
            id = "ACT-001",
            titulo = "Persistencia local",
            descripcion = "Laboratorio Room",
            progreso = 40,
            prioridad = "ALTA",
            competenciaId = 1L,
            fechaLimiteEpochMillis = 0L
        )
        dao.insertar(entity)
        val lista = dao.observarTodos().first()
        assertEquals("ACT-001", lista.single().id)
    }
}
