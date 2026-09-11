package com.example.miformacionctma

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.miformacionctma.data.local.ActividadDao
import com.example.miformacionctma.data.local.ActividadEntity
import com.example.miformacionctma.data.local.CompetenciaEntity
import com.example.miformacionctma.data.local.FormacionDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActividadDaoTest {

    private lateinit var db: FormacionDatabase
    private lateinit var dao: ActividadDao

    @Before
    fun crearDb() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, FormacionDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        // Insertamos la competencia requerida por la restricción de clave foránea
        db.competenciaDao().insertar(CompetenciaEntity(id = 1, nombre = "Desarrollo Móvil"))
        dao = db.actividadDao()
    }

    @After
    fun cerrarDb() {
        db.close()
    }

    @Test
    fun insertar_y_observar_actividad() = runBlocking {
        val actividad = ActividadEntity(
            id = 1,
            titulo = "Prueba Persistencia",
            descripcion = "Validar Room DAO",
            fecha = "2026-09-11",
            prioridad = "ALTA",
            progreso = 50,
            competenciaId = 1,
            completada = false
        )

        dao.insertarActividad(actividad)
        val lista = dao.obtenerTodasLasActividades().first()

        assertEquals(1, lista.size)
        assertEquals("Prueba Persistencia", lista[0].titulo)
    }
}