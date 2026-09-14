package com.example.miformacionctma.ui.viewmodel

import com.example.miformacionctma.data.repository.PreferenciasRepository
import com.example.miformacionctma.data.repository.PreferenciasUsuario
import com.example.miformacionctma.domain.repository.ActividadRepository
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.ui.state.ListadoUiState
import com.example.miformacionctma.ui.state.OperacionUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeActividadRepository : ActividadRepository {
    val actividadesFlow = MutableStateFlow<List<ActividadFormativa>>(emptyList())
    val consultasBuscadas = mutableListOf<String>()
    var simularError = false

    override fun observarTodos(): Flow<List<ActividadFormativa>> = actividadesFlow
    
    override fun buscar(texto: String): Flow<List<ActividadFormativa>> {
        consultasBuscadas.add(texto)
        return actividadesFlow
    }
    
    override suspend fun guardar(actividad: ActividadFormativa) {
        if (simularError) throw Exception("Error al guardar")
    }
    
    override suspend fun eliminar(id: String): Boolean {
        if (simularError) throw Exception("Error al eliminar")
        return true
    }
}

class FakePreferenciasRepository : PreferenciasRepository(
    MutableStateFlow(androidx.datastore.preferences.core.emptyPreferences()).let { flow ->
        object : androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> {
            override val data: Flow<androidx.datastore.preferences.core.Preferences> = flow
            override suspend fun updateData(transform: suspend (androidx.datastore.preferences.core.Preferences) -> androidx.datastore.preferences.core.Preferences): androidx.datastore.preferences.core.Preferences {
                return androidx.datastore.preferences.core.emptyPreferences()
            }
        }
    }
) {
    val _prefs = MutableStateFlow(PreferenciasUsuario(null, true, false))
    override val preferencias: Flow<PreferenciasUsuario> = _prefs
}

@OptIn(ExperimentalCoroutinesApi::class)
class ActividadesViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // CA-01: Abrir sin actividades -> Cargando -> Vacio
    @Test
    fun ca01_abrirSinActividades_transitaDeCargandoAVacio() = runTest {
        val fakeActividadRepo = FakeActividadRepository()
        val fakePrefsRepo = FakePreferenciasRepository()
        val vm = ActividadesViewModel(fakeActividadRepo, fakePrefsRepo)

        val job = backgroundScope.launch(testDispatcher) { vm.uiState.collect() }
        advanceUntilIdle()
        
        assertEquals(ListadoUiState.Vacio, vm.uiState.value)
        job.cancel()
    }

    // CA-02: Insertar desde formulario -> Contenido cambia automáticamente sin refresco manual
    @Test
    fun ca02_insertarDesdeFormulario_emiteContenidoDinamico() = runTest {
        val fakeActividadRepo = FakeActividadRepository()
        val fakePrefsRepo = FakePreferenciasRepository()
        val vm = ActividadesViewModel(fakeActividadRepo, fakePrefsRepo)

        val job = backgroundScope.launch(testDispatcher) { vm.uiState.collect() }
        advanceUntilIdle()

        fakeActividadRepo.actividadesFlow.value = listOf(
            ActividadFormativa("ACT-1", "Persistencia Room 3", "Desc", "2026-09-14", 50, 4, Prioridad.MEDIA)
        )
        advanceUntilIdle()

        assertTrue(vm.uiState.value is ListadoUiState.Contenido)
        job.cancel()
    }

    // CA-03: Cambiar filtro y restaurar -> DataStore restaura y combine recalcula de inmediato
    @Test
    fun ca03_cambiarFiltro_recalculaYFiltraContenido() = runTest {
        val fakeActividadRepo = FakeActividadRepository()
        fakeActividadRepo.actividadesFlow.value = listOf(
            ActividadFormativa("ACT-1", "Room 3", "Desc", "2026-09-14", 50, 4, Prioridad.MEDIA, competenciaId = 10L)
        )
        val fakePrefsRepo = FakePreferenciasRepository()
        val vm = ActividadesViewModel(fakeActividadRepo, fakePrefsRepo)

        val job = backgroundScope.launch(testDispatcher) { vm.uiState.collect() }
        advanceUntilIdle()

        // Apply non-matching competencia filter context
        fakePrefsRepo._prefs.value = PreferenciasUsuario("99", true, false)
        advanceUntilIdle()

        assertEquals(ListadoUiState.Vacio, vm.uiState.value)
        job.cancel()
    }

    // CA-04: Escribir búsquedas seguidas -> Se cancela la anterior y se preserva la más reciente
    @Test
    fun ca04_busquedasSeguidas_registraConsultas() = runTest {
        val fakeActividadRepo = FakeActividadRepository()
        val fakePrefsRepo = FakePreferenciasRepository()
        val vm = ActividadesViewModel(fakeActividadRepo, fakePrefsRepo)

        val job = backgroundScope.launch(testDispatcher) { vm.uiState.collect() }
        
        vm.cambiarBusqueda("corrutina")
        vm.cambiarBusqueda("flow")
        advanceUntilIdle()

        assertTrue(fakeActividadRepo.consultasBuscadas.contains("flow"))
        job.cancel()
    }

    // CA-05: Forzar fallo del Repository -> Error legible sin causar cierres destructivos de la app
    @Test
    fun ca05_forzarFalloRepository_publicaError() = runTest {
        val flowConError = flow<List<ActividadFormativa>> { throw Exception("Fallo forzado de Room") }
        val fakeActividadRepo = object : ActividadRepository {
            override fun observarTodos(): Flow<List<ActividadFormativa>> = flowConError
            override fun buscar(texto: String): Flow<List<ActividadFormativa>> = flowConError
            override suspend fun guardar(actividad: ActividadFormativa) {}
            override suspend fun eliminar(id: String): Boolean = true
        }
        val fakePrefsRepo = FakePreferenciasRepository()
        val vm = ActividadesViewModel(fakeActividadRepo, fakePrefsRepo)

        val job = backgroundScope.launch(testDispatcher) { vm.uiState.collect() }
        advanceUntilIdle()

        assertTrue(vm.uiState.value is ListadoUiState.Error)
        job.cancel()
    }

    // CA-07: Girar/recrear pantalla -> StateFlow conserva estado e impide duplicación de guardados
    @Test
    fun ca07_guardadoConEstado_manejaOperacionCorrectamente() = runTest {
        val fakeActividadRepo = FakeActividadRepository()
        val fakePrefsRepo = FakePreferenciasRepository()
        val vm = ActividadesViewModel(fakeActividadRepo, fakePrefsRepo)

        val actividad = ActividadFormativa("ACT-2", "Test", "Desc", "2026-09-14", 0, 7, Prioridad.BAJA)
        
        vm.guardar(actividad)
        assertEquals(OperacionUiState.Exitosa, vm.operacion.value)
    }

    // CA-07 (Extra): Fallo en operación -> OperacionUiState transita a Fallida correctamente
    @Test
    fun ca07_falloEnOperacion_transitaAFallida() = runTest {
        val fakeActividadRepo = FakeActividadRepository().apply { simularError = true }
        val fakePrefsRepo = FakePreferenciasRepository()
        val vm = ActividadesViewModel(fakeActividadRepo, fakePrefsRepo)

        val actividad = ActividadFormativa("ACT-2", "Test", "Desc", "2026-09-14", 0, 7, Prioridad.BAJA)
        
        vm.guardar(actividad)
        assertTrue(vm.operacion.value is OperacionUiState.Fallida)
    }
}
