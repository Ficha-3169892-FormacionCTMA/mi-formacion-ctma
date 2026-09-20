package com.example.miformacionctma.data.repository

import com.example.miformacionctma.data.local.ActividadDao
import com.example.miformacionctma.data.local.ActividadEntity
import com.example.miformacionctma.data.remote.ActividadApi
import com.example.miformacionctma.data.remote.ActividadDto
import com.example.miformacionctma.data.remote.CrearActividadDto
import com.example.miformacionctma.data.remote.RemoteActividadDataSource
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class ActividadRepositoryTest {

    private lateinit var repository: ActividadRepository
    private lateinit var fakeDao: FakeActividadDao
    private lateinit var fakeRemote: RemoteActividadDataSource
    private lateinit var fakeApi: FakeActividadApi

    @Before
    fun setup() {
        fakeDao = FakeActividadDao()
        fakeApi = FakeActividadApi()
        fakeRemote = RemoteActividadDataSource(fakeApi)
        repository = ActividadRepositoryImpl(fakeDao, fakeRemote)
    }

    @Test
    fun refrescarDesdeServidor_obtieneDatosYActualizaLocal() = runTest {
        // Arrange
        val remoteDto = ActividadDto(1L, "Remota", "", "2026-09-10", 0, 5, "ALTA")
        fakeApi.actividades = listOf(remoteDto)

        // Act
        repository.refrescarDesdeServidor()

        // Assert
        val local = fakeDao.observarTodas().first()
        assertEquals(1, local.size)
        assertEquals("Remota", local.first().titulo)
    }

    @Test
    fun crearEnServidor_guardaEnRemotoYLuegoEnLocal() = runTest {
        // Arrange
        val nueva = ActividadFormativa(0L, "Nueva", "Desc", "2026-09-10", 10, 2, Prioridad.MEDIA)
        
        // Act
        val creada = repository.crearEnServidor(nueva)

        // Assert
        assertEquals(101L, creada.id) // ID asignado por el fake de la API
        val local = fakeDao.observarTodas().first()
        assertTrue(local.any { it.id == 101L })
    }

    @Test
    fun eliminarDelServidor_borraEnRemotoYLocal() = runTest {
        // Arrange
        val entidad = ActividadEntity(1L, "A borrar", "", "2026", 0, 0, "BAJA")
        fakeDao.guardar(entidad)
        
        // Act
        repository.eliminarDelServidor(1L)

        // Assert
        val local = fakeDao.observarTodas().first()
        assertTrue(local.isEmpty())
    }

    // --- Fakes para las pruebas ---

    class FakeActividadDao : ActividadDao {
        private val data = MutableStateFlow<List<ActividadEntity>>(emptyList())

        override fun observarTodas(): Flow<List<ActividadEntity>> = data
        
        override fun buscar(texto: String): Flow<List<ActividadEntity>> = 
            data.map { list -> list.filter { it.titulo.contains(texto) } }

        override suspend fun guardar(actividad: ActividadEntity) {
            val current = data.value.toMutableList()
            current.removeAll { it.id == actividad.id }
            current.add(actividad)
            data.value = current
        }

        override suspend fun guardarTodas(actividades: List<ActividadEntity>) {
            data.value = actividades
        }

        override suspend fun borrarTodas() {
            data.value = emptyList()
        }

        override suspend fun eliminar(id: Long) {
            data.value = data.value.filter { it.id != id }
        }
    }

    class FakeActividadApi : ActividadApi {
        var actividades = listOf<ActividadDto>()

        override suspend fun obtenerActividades(select: String): Response<List<ActividadDto>> =
            Response.success(actividades)

        override suspend fun obtenerActividad(id: String, select: String): Response<List<ActividadDto>> =
            Response.success(actividades.filter { "eq.${it.id}" == id })

        override suspend fun crearActividad(prefer: String, select: String, actividad: CrearActividadDto): Response<List<ActividadDto>> {
            val nuevoDto = ActividadDto(101L, actividad.titulo, actividad.descripcion, actividad.fecha, actividad.progreso, actividad.diasRestantes, actividad.prioridad)
            return Response.success(listOf(nuevoDto))
        }

        override suspend fun actualizarActividad(prefer: String, id: String, select: String, actividad: CrearActividadDto): Response<List<ActividadDto>> {
            val editado = ActividadDto(101L, actividad.titulo, actividad.descripcion, actividad.fecha, actividad.progreso, actividad.diasRestantes, actividad.prioridad)
            return Response.success(listOf(editado))
        }

        override suspend fun eliminarActividad(id: String): Response<Unit> =
            Response.success(Unit)
    }
}
