package com.example.miformacionctma.ui.viewmodel

import android.content.Context
import android.net.Uri
import com.example.miformacionctma.data.local.entity.EvidenciaEntity
import com.example.miformacionctma.data.repository.ActividadRepository
import com.example.miformacionctma.data.repository.AuthRepository
import com.example.miformacionctma.data.repository.PreferenciasRepository
import com.example.miformacionctma.data.repository.RepositoryResult
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Competencia
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.ui.state.ListadoUiState
import com.example.miformacionctma.ui.state.OperacionUiState
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
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
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ActividadesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // 1. Fake ActividadRepository
    private class FakeActividadRepository : ActividadRepository {
        val actividadesFlow = MutableStateFlow<List<ActividadFormativa>>(emptyList())
        val competenciasFlow = MutableStateFlow<List<Competencia>>(emptyList())

        override fun observarActividades(): Flow<List<ActividadFormativa>> = actividadesFlow
        override fun observarCompetencias(): Flow<List<Competencia>> = competenciasFlow

        override suspend fun obtenerPorId(id: Long): ActividadFormativa? {
            return actividadesFlow.value.find { it.id == id }
        }

        override suspend fun insertar(actividad: ActividadFormativa) {
            actividadesFlow.value = actividadesFlow.value + actividad
        }

        override suspend fun actualizar(actividad: ActividadFormativa) {
            actividadesFlow.value = actividadesFlow.value.map { if (it.id == actividad.id) actividad else it }
        }

        override suspend fun eliminar(actividad: ActividadFormativa) {
            actividadesFlow.value = actividadesFlow.value.filter { it.id != actividad.id }
        }

        override suspend fun insertarConCompetencia(actividad: ActividadFormativa, competenciaId: Long?) {
            insertar(actividad.copy(competenciaId = competenciaId))
        }

        override fun observarPorTitulo(texto: String): Flow<List<ActividadFormativa>> {
            return flowOf(actividadesFlow.value.filter { it.titulo.contains(texto, ignoreCase = true) })
        }

        override suspend fun obtenerConCompetencia(id: Long): Pair<ActividadFormativa, String?>? {
            val act = obtenerPorId(id) ?: return null
            return act to "Competencia Fake"
        }

        override suspend fun inicializarDatos() {}

        override suspend fun refresh(): RepositoryResult<Unit> {
            return com.example.miformacionctma.data.util.Result.Success(Unit)
        }

        override fun observarEvidencias(actividadId: Long, usuarioId: String): Flow<List<EvidenciaEntity>> {
            return flowOf(emptyList())
        }

        override fun observarEvidenciasInstructor(actividadId: Long): Flow<List<EvidenciaEntity>> {
            return flowOf(emptyList())
        }

        override suspend fun guardarEvidenciaLocal(
            actividadId: Long,
            localUri: String,
            mimeType: String,
            tamano: Long,
            usuarioId: String
        ): EvidenciaEntity {
            return EvidenciaEntity(1L, actividadId, localUri, mimeType, tamano, Instant.now())
        }

        override suspend fun subirEvidencia(context: Context, evidenciaId: Long): RepositoryResult<Unit> {
            return com.example.miformacionctma.data.util.Result.Success(Unit)
        }

        override suspend fun eliminarEvidencia(context: Context, evidenciaId: Long) {}
    }

    // 2. Fake PreferenciasRepository subclass
    private class FakePreferenciasRepository : PreferenciasRepository(null) {
        private val _ordenarPorPrioridad = MutableStateFlow(false)
        override val ordenarPorPrioridad: Flow<Boolean> = _ordenarPorPrioridad

        override suspend fun guardarOrdenPorPrioridad(valor: Boolean) {
            _ordenarPorPrioridad.value = valor
        }
    }

    private val authRepository = mockk<AuthRepository>(relaxed = true)

    @Test
    fun uiState_inicial_esCargando() = runTest(testDispatcher) {
        val repo = FakeActividadRepository()
        val prefs = FakePreferenciasRepository()
        val viewModel = ActividadesViewModel(repo, prefs, authRepository)

        assertEquals(ListadoUiState.Cargando, viewModel.uiState.value)
    }

    @Test
    fun uiState_transicionaAVacio_cuandoNoHayDatos() = runTest(testDispatcher) {
        val repo = FakeActividadRepository()
        val prefs = FakePreferenciasRepository()
        val viewModel = ActividadesViewModel(repo, prefs, authRepository)

        backgroundScope.launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        assertEquals(ListadoUiState.Vacio, viewModel.uiState.value)
    }

    @Test
    fun uiState_transicionaAContenido_cuandoSeInsertanDatos() = runTest(testDispatcher) {
        val repo = FakeActividadRepository()
        val prefs = FakePreferenciasRepository()
        val viewModel = ActividadesViewModel(repo, prefs, authRepository)

        backgroundScope.launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        assertEquals(ListadoUiState.Vacio, viewModel.uiState.value)

        val nuevaActividad = ActividadFormativa(
            id = 1L,
            titulo = "Aprender Corrutinas",
            descripcion = "Guía de estudio 07",
            fecha = Instant.now(),
            progreso = 50,
            prioridad = Prioridad.ALTA
        )
        viewModel.insertar(nuevaActividad)
        
        advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertTrue(currentState is ListadoUiState.Contenido)
        assertEquals(1, (currentState as ListadoUiState.Contenido).actividades.size)
        assertEquals("Aprender Corrutinas", currentState.actividades.first().titulo)
    }

    @Test
    fun operacionUiState_cambiaAExitosa_cuandoInsercionEsCorrecta() = runTest(testDispatcher) {
        val repo = FakeActividadRepository()
        val prefs = FakePreferenciasRepository()
        val viewModel = ActividadesViewModel(repo, prefs, authRepository)

        backgroundScope.launch { viewModel.operacionUiState.collect() }
        advanceUntilIdle()

        assertEquals(OperacionUiState.Inactiva, viewModel.operacionUiState.value)

        val actividad = ActividadFormativa(2L, "Test", "Desc", Instant.now(), 0, Prioridad.MEDIA)
        viewModel.insertar(actividad)
        
        advanceUntilIdle()

        assertEquals(OperacionUiState.Exitosa, viewModel.operacionUiState.value)
    }
}
