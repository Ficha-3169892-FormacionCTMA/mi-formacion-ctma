package com.example.miformacionctma.ui.viewmodel

import com.example.miformacionctma.data.repository.ActividadRepository
import com.example.miformacionctma.data.repository.PreferenciasRepository
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.ui.state.ListadoUiState
import com.example.miformacionctma.ui.state.OperacionUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActividadesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var viewModel: ActividadesViewModel
    private lateinit var fakeRepository: FakeActividadRepository
    private lateinit var fakePrefs: FakePreferenciasRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeActividadRepository()
        fakePrefs = FakePreferenciasRepository()
        viewModel = ActividadesViewModel(fakeRepository, fakePrefs)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_iniciaCargandoYLuegoMuestraContenido() = runTest {
        // Arrange
        val actividades = listOf(
            ActividadFormativa(1L, "Test", "", "2026", 0, 0, Prioridad.MEDIA)
        )
        fakeRepository.emitirActividades(actividades)

        // Act & Assert
        // El estado inicial debe ser Cargando
        assertEquals(ListadoUiState.Cargando, viewModel.uiState.value)
        
        // Empezar a colectar para activar el WhileSubscribed
        val job = launch { viewModel.uiState.collect {} }
        
        // Avanzar el tiempo para que el combine y flatMapLatest se ejecuten
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue("El estado actual es: $state", state is ListadoUiState.Contenido)
        assertEquals(1, (state as ListadoUiState.Contenido).actividades.size)
        
        job.cancel()
    }

    @Test
    fun cambiarBusqueda_actualizaFiltro() = runTest {
        // Act
        viewModel.cambiarBusqueda("Kotlin")
        testDispatcher.scheduler.runCurrent()
        
        // Assert
        assertEquals("Kotlin", viewModel.busqueda.value)
    }

    @Test
    fun guardarActividad_actualizaEstadoOperacion() = runTest {
        // Arrange
        val actividad = ActividadFormativa(0L, "Nueva", "", "2026", 0, 0, Prioridad.ALTA)

        // Act
        viewModel.guardar(actividad)
        
        // Assert - Necesitamos correr las corrutinas para que llegue a EnCurso
        testDispatcher.scheduler.runCurrent()
        assertTrue("Estado esperado EnCurso, pero es: ${viewModel.operacion.value}", 
            viewModel.operacion.value is OperacionUiState.EnCurso)
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertTrue("Estado esperado Exitosa, pero es: ${viewModel.operacion.value}",
            viewModel.operacion.value is OperacionUiState.Exitosa)
    }

    // --- Fakes ---

    class FakeActividadRepository : ActividadRepository {
        private val _actividades = MutableStateFlow<List<ActividadFormativa>>(emptyList())
        
        fun emitirActividades(lista: List<ActividadFormativa>) {
            _actividades.value = lista
        }

        override fun observarActividades(): Flow<List<ActividadFormativa>> = _actividades
        override fun buscar(texto: String): Flow<List<ActividadFormativa>> = _actividades
        override suspend fun guardar(actividad: ActividadFormativa) {}
        override suspend fun eliminar(id: Long) {}
        override suspend fun refrescarDesdeServidor() {}
        override suspend fun obtenerDesdeServidor(id: Long): ActividadFormativa = 
            ActividadFormativa(id, "", "", "", 0, 0, Prioridad.BAJA)
        
        override suspend fun crearEnServidor(actividad: ActividadFormativa): ActividadFormativa {
            kotlinx.coroutines.delay(10) // Simular trabajo para permitir capturar estado EnCurso
            return actividad
        }
        override suspend fun actualizarEnServidor(actividad: ActividadFormativa): ActividadFormativa = actividad
        override suspend fun eliminarDelServidor(id: Long) {}
    }

    class FakePreferenciasRepository : PreferenciasRepository {
        private val _orden = MutableStateFlow("id")
        override val orden: Flow<String> = _orden
        override suspend fun guardarOrden(orden: String) {
            _orden.value = orden
        }

        override val accessToken: Flow<String?> = flowOf(null)
        override val userId: Flow<String?> = flowOf(null)
        override val userRole: Flow<String?> = flowOf(null)

        override suspend fun guardarSesion(token: String, id: String, rol: String) {}
        override suspend fun borrarSesion() {}
    }
}
