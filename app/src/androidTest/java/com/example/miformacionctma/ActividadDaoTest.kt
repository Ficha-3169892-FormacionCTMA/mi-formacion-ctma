package com.example.miformacionctma

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.miformacionctma.data.local.database.AppDatabase
import com.example.miformacionctma.data.local.entity.ActividadEntity
import com.example.miformacionctma.data.local.entity.EvidenciaEntity
import com.example.miformacionctma.model.Prioridad
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
            fecha = "2026-09-15"
        )
        database.actividadDao().insertarActividad(actividad)

        val lista = database.actividadDao().obtenerTodasLasActividades().first()
        assertEquals(1, lista.size)
        assertEquals("Prueba Unitario", lista[0].titulo)
    }

    @Test
    fun insertarMultiplesActividadesYVerificarTamano() = runBlocking {
        database.actividadDao().insertarActividad(ActividadEntity(titulo = "A", descripcion = "", fecha = ""))
        database.actividadDao().insertarActividad(ActividadEntity(titulo = "B", descripcion = "", fecha = ""))
        database.actividadDao().insertarActividad(ActividadEntity(titulo = "C", descripcion = "", fecha = ""))

        val lista = database.actividadDao().obtenerTodasLasActividades().first()
        assertEquals(3, lista.size)
    }

    @Test
    fun actualizarCamposDeActividad() = runBlocking {
        val actividad = ActividadEntity(id = 1, titulo = "Original", descripcion = "", fecha = "", progreso = 0)
        database.actividadDao().insertarActividad(actividad)
        
        val editada = actividad.copy(titulo = "Editada", progreso = 50, prioridad = Prioridad.ALTA)
        database.actividadDao().actualizarActividad(editada)

        val recuperada = database.actividadDao().obtenerTodasLasActividades().first()[0]
        assertEquals("Editada", recuperada.titulo)
        assertEquals(50, recuperada.progreso)
        assertEquals(Prioridad.ALTA, recuperada.prioridad)
    }

    @Test
    fun eliminarActividadYVerificarVacio() = runBlocking {
        val actividad = ActividadEntity(id = 1, titulo = "Borrar", descripcion = "", fecha = "")
        database.actividadDao().insertarActividad(actividad)
        database.actividadDao().eliminarActividad(actividad)

        val lista = database.actividadDao().obtenerTodasLasActividades().first()
        assertEquals(0, lista.size)
    }

    @Test
    fun insertarEvidenciaAsociadaYObtenerla() = runBlocking {
        val actividad = ActividadEntity(id = 10, titulo = "Actividad", descripcion = "", fecha = "")
        database.actividadDao().insertarActividad(actividad)

        val evidencia = EvidenciaEntity(actividadId = 10, uri = "content://path", tipo = "image/jpeg", tamano = 1024, estado = "LOCAL")
        database.evidenciaDao().insertarEvidencia(evidencia)

        val recuperada = database.evidenciaDao().obtenerEvidenciaPorActividad(10)
        assertNotNull(recuperada)
        assertEquals("content://path", recuperada?.uri)
    }

    @Test
    fun observarEvidenciaConFlowReal() = runBlocking {
        database.actividadDao().insertarActividad(ActividadEntity(id = 20, titulo = "Flow Test", descripcion = "", fecha = ""))
        val evidencia = EvidenciaEntity(actividadId = 20, uri = "uri1", tipo = "t", tamano = 1, estado = "LOCAL")
        database.evidenciaDao().insertarEvidencia(evidencia)

        val flujo = database.evidenciaDao().observarEvidenciaPorActividad(20).first()
        assertEquals("uri1", flujo?.uri)
    }

    @Test
    fun eliminarEvidenciaPorActividadExplicito() = runBlocking {
        database.actividadDao().insertarActividad(ActividadEntity(id = 30, titulo = "Eliminar Ev", descripcion = "", fecha = ""))
        database.evidenciaDao().insertarEvidencia(EvidenciaEntity(30, "uri", "t", 1, "LOCAL"))
        
        database.evidenciaDao().eliminarEvidenciaPorActividad(30)
        val recuperada = database.evidenciaDao().obtenerEvidenciaPorActividad(30)
        assertNull(recuperada)
    }

    @Test
    fun verificarBorradoEnCascadaDeEvidenciaAlEliminarActividad() = runBlocking {
        val actividad = ActividadEntity(id = 40, titulo = "Cascada", descripcion = "", fecha = "")
        database.actividadDao().insertarActividad(actividad)
        database.evidenciaDao().insertarEvidencia(EvidenciaEntity(40, "uri", "t", 1, "LOCAL"))

        // Eliminar la actividad padre
        database.actividadDao().eliminarActividad(actividad)

        // La evidencia debería borrarse automáticamente por la ForeignKey CASCADE
        val evidenciaRestante = database.evidenciaDao().obtenerEvidenciaPorActividad(40)
        assertNull("La evidencia debe ser nula tras borrar la actividad (Cascada)", evidenciaRestante)
    }

    @Test
    fun reemplazarActividadOnConflictStrategy() = runBlocking {
        val a1 = ActividadEntity(id = 50, titulo = "Primera", descripcion = "", fecha = "")
        val a2 = ActividadEntity(id = 50, titulo = "Segunda", descripcion = "", fecha = "")

        database.actividadDao().insertarActividad(a1)
        database.actividadDao().insertarActividad(a2) // Debe reemplazar

        val lista = database.actividadDao().obtenerTodasLasActividades().first()
        assertEquals(1, lista.size)
        assertEquals("Segunda", lista[0].titulo)
    }

    @Test
    fun reemplazarEvidenciaOnConflictStrategy() = runBlocking {
        database.actividadDao().insertarActividad(ActividadEntity(id = 60, titulo = "Conflicto Ev", descripcion = "", fecha = ""))
        
        val e1 = EvidenciaEntity(60, "uri1", "t", 1, "LOCAL")
        val e2 = EvidenciaEntity(60, "uri2", "t", 1, "LOCAL")

        database.evidenciaDao().insertarEvidencia(e1)
        database.evidenciaDao().insertarEvidencia(e2) // Debe reemplazar

        val recuperada = database.evidenciaDao().obtenerEvidenciaPorActividad(60)
        assertEquals("uri2", recuperada?.uri)
    }

    @Test
    fun insertarActividadesConDiferentesPrioridades() = runBlocking {
        database.actividadDao().insertarActividad(ActividadEntity(id = 71, titulo = "Baja", descripcion = "", fecha = "", prioridad = Prioridad.BAJA))
        database.actividadDao().insertarActividad(ActividadEntity(id = 72, titulo = "Alta", descripcion = "", fecha = "", prioridad = Prioridad.ALTA))

        val lista = database.actividadDao().obtenerTodasLasActividades().first().sortedBy { it.id }
        assertEquals(Prioridad.BAJA, lista[0].prioridad)
        assertEquals(Prioridad.ALTA, lista[1].prioridad)
    }

    @Test
    fun obtenerEvidenciaInexistenteDevuelveNull() = runBlocking {
        val recuperada = database.evidenciaDao().obtenerEvidenciaPorActividad(999)
        assertNull(recuperada)
    }
}
