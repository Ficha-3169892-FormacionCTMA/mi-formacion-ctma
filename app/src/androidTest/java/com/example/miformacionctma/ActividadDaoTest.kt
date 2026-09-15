package com.example.miformacionctma

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.miformacionctma.data.local.database.AppDatabase
import com.example.miformacionctma.data.local.entity.ActividadEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActividadDaoTest {

    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertarYObtenerActividad() = runBlocking {
        val actividad = ActividadEntity(
            titulo = "Prueba Unitario",
            descripcion = "Validando DAO en memoria",
            fecha = "15/09/2026"
        )
        database.actividadDao().insertarActividad(actividad)

        val lista = database.actividadDao().obtenerTodasLasActividades().first()
        assertEquals(1, lista.size)
        assertEquals("Prueba Unitario", lista[0].titulo)
    }
}