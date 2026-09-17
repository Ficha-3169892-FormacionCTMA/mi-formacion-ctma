package com.example.miformacionctma.data.local

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.miformacionctma.data.local.dao.ActividadDao
import com.example.miformacionctma.data.local.dao.CompetenciaDao
import com.example.miformacionctma.data.local.entity.ActividadEntity
import com.example.miformacionctma.data.local.entity.CompetenciaEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActividadDaoTest {

    private lateinit var database: FormacionDatabase
    private lateinit var actividadDao: ActividadDao
    private lateinit var competenciaDao: CompetenciaDao

    @Before
    fun crearBase() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            FormacionDatabase::class.java
        )
            .setDriver(BundledSQLiteDriver())
            .build()

        actividadDao = database.actividadDao()
        competenciaDao = database.competenciaDao()
    }

    @After
    fun cerrarBase() {
        database.close()
    }

    @Test
    fun insertarYObtenerActividad() = runTest {
        val actividad = ActividadEntity(
            id = 1L,
            titulo = "Actividad de prueba",
            descripcion = "Prueba DAO",
            fecha = "2026-09-20",
            progreso = 50,
            prioridad = "MEDIA",
            competenciaId = null
        )

        actividadDao.insertar(actividad)

        val resultado = actividadDao.obtenerPorId(1L)

        assertNotNull(resultado)
        assertEquals("Actividad de prueba", resultado?.titulo)
        assertEquals(false, resultado?.completada)
    }

    @Test
    fun actualizarActividad() = runTest {
        val actividad = ActividadEntity(
            id = 1L,
            titulo = "Original",
            descripcion = "Descripción",
            fecha = "2026-09-20",
            progreso = 0,
            prioridad = "BAJA"
        )

        actividadDao.insertar(actividad)

        val actualizada = actividad.copy(
            titulo = "Actualizada",
            progreso = 100,
            completada = true
        )

        actividadDao.actualizar(actualizada)

        val resultado = actividadDao.obtenerPorId(1L)

        assertEquals("Actualizada", resultado?.titulo)
        assertEquals(100, resultado?.progreso)
        assertEquals(true, resultado?.completada)
    }

    @Test
    fun eliminarActividad() = runTest {
        val actividad = ActividadEntity(
            id = 1L,
            titulo = "Eliminar",
            descripcion = "",
            fecha = "2026-09-20",
            progreso = 0,
            prioridad = "BAJA"
        )

        actividadDao.insertar(actividad)
        actividadDao.eliminar(actividad)

        assertEquals(null, actividadDao.obtenerPorId(1L))
    }

    @Test
    fun buscarPorTitulo() = runTest {
        actividadDao.insertar(
            ActividadEntity(
                id = 1L,
                titulo = "Aprender Kotlin",
                descripcion = "",
                fecha = "2026-09-20",
                progreso = 0,
                prioridad = "MEDIA"
            )
        )

        actividadDao.insertar(
            ActividadEntity(
                id = 2L,
                titulo = "Diseño Compose",
                descripcion = "",
                fecha = "2026-09-20",
                progreso = 0,
                prioridad = "BAJA"
            )
        )

        val resultado = actividadDao.buscarPorTitulo("kotlin").first()

        assertEquals(1, resultado.size)
        assertEquals("Aprender Kotlin", resultado.first().titulo)
    }

    @Test
    fun relacionActividadCompetencia() = runTest {
        competenciaDao.insertar(
            CompetenciaEntity(
                id = 1L,
                nombre = "Desarrollo de software"
            )
        )

        actividadDao.insertar(
            ActividadEntity(
                id = 1L,
                titulo = "Actividad relacionada",
                descripcion = "",
                fecha = "2026-09-20",
                progreso = 0,
                prioridad = "MEDIA",
                competenciaId = 1L
            )
        )

        val resultado = actividadDao.obtenerConCompetencia(1L)

        assertNotNull(resultado)
        assertEquals(
            "Desarrollo de software",
            resultado?.competencia?.nombre
        )
    }
}
