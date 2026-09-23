package com.example.miformacionctma

import com.example.miformacionctma.data.local.entity.EvidenciaEntity
import com.example.miformacionctma.data.remote.DataError
import com.example.miformacionctma.data.remote.NetworkFailure
import com.example.miformacionctma.data.repository.ActividadRepository
import com.example.miformacionctma.data.repository.PreferenciasUsuario
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.ui.actividades.ActividadViewModel
import com.example.miformacionctma.ui.actividades.EstadoEvidencia
import com.example.miformacionctma.ui.actividades.ListadoUiState
import com.example.miformacionctma.ui.actividades.SincronizacionUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
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
class ActividadViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repositorio: RepositorioFalso
    private lateinit var preferencias: PreferenciasFalsas
    private lateinit var viewModel: ActividadViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repositorio = RepositorioFalso()
        preferencias = PreferenciasFalsas()
        viewModel = ActividadViewModel(repositorio, preferencias)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    // El StateFlow usa WhileSubscribed: se necesita un colector activo durante la prueba
    private fun TestScope.observarListado() {
        backgroundScope.launch(dispatcher) { viewModel.listadoUiState.collect {} }
    }

    @Test
    fun sinActividades_pasaDeCargandoAVacio() = runTest(dispatcher) {
        assertEquals(ListadoUiState.Cargando, viewModel.listadoUiState.value)
        observarListado()

        advanceUntilIdle()

        assertEquals(ListadoUiState.Vacio, viewModel.listadoUiState.value)
    }

    @Test
    fun busqueda_filtraPorTituloTrasElDebounce() = runTest(dispatcher) {
        repositorio.actividades.value = listOf(actividad(1, "Kotlin básico"), actividad(2, "Scrum"))
        observarListado()
        advanceUntilIdle()

        viewModel.actualizarBusqueda("kotlin")
        advanceTimeBy(100)
        assertEquals("Antes de 300 ms no se aplica el filtro", 2, contenido().size)

        advanceUntilIdle()
        assertEquals(listOf("Kotlin básico"), contenido().map { it.titulo })
    }

    @Test
    fun filtroSoloCompletadas_seGuardaEnPreferenciasYFiltra() = runTest(dispatcher) {
        repositorio.actividades.value = listOf(actividad(1, "Terminada", progreso = 100), actividad(2, "En curso", progreso = 40))
        observarListado()
        advanceUntilIdle()

        viewModel.cambiarFiltroCompletadas(true)
        advanceUntilIdle()

        assertEquals(true, preferencias.mostrarSoloCompletadas.first())
        assertEquals(listOf("Terminada"), contenido().map { it.titulo })
    }

    @Test
    fun todasLasActividades_ignoraFiltrosParaDetalleYEdicion() = runTest(dispatcher) {
        repositorio.actividades.value = listOf(actividad(1, "Terminada", progreso = 100), actividad(2, "En curso", progreso = 40))
        observarListado()
        backgroundScope.launch(dispatcher) { viewModel.todasLasActividades.collect {} }
        viewModel.cambiarFiltroCompletadas(true)
        viewModel.actualizarBusqueda("Terminada")
        advanceUntilIdle()

        assertEquals(listOf("Terminada"), contenido().map { it.titulo })
        assertEquals(2, viewModel.todasLasActividades.value.size)
    }

    @Test
    fun filtrosSinResultados_muestranContenidoVacioYNoEstadoVacio() = runTest(dispatcher) {
        repositorio.actividades.value = listOf(actividad(1, "Kotlin"))
        observarListado()
        advanceUntilIdle()

        viewModel.actualizarBusqueda("zzz")
        advanceUntilIdle()

        assertEquals(ListadoUiState.Contenido(emptyList()), viewModel.listadoUiState.value)
    }

    @Test
    fun errorDeRoom_muestraErrorYReintentarRecupera() = runTest(dispatcher) {
        repositorio.fallarLectura = true
        observarListado()
        advanceUntilIdle()
        assertTrue(viewModel.listadoUiState.value is ListadoUiState.Error)

        repositorio.fallarLectura = false
        repositorio.actividades.value = listOf(actividad(1, "Recuperada"))
        viewModel.reintentarCarga()
        advanceUntilIdle()

        assertEquals(listOf("Recuperada"), contenido().map { it.titulo })
    }

    @Test
    fun sincronizacionSinInternet_muestraErrorYSePuedeDescartar() = runTest(dispatcher) {
        repositorio.resultadoSincronizacion = Result.failure(NetworkFailure(DataError.NoConnection))

        viewModel.sincronizarConServidor()
        advanceUntilIdle()

        val estado = viewModel.sincronizacionUiState.value
        assertTrue(estado is SincronizacionUiState.Fallida)
        assertTrue((estado as SincronizacionUiState.Fallida).mensaje.contains("sin conexión"))

        viewModel.descartarErrorSincronizacion()
        assertEquals(SincronizacionUiState.Inactiva, viewModel.sincronizacionUiState.value)
    }

    @Test
    fun sincronizacionExitosa_sincronizaActividadesAntesQueEvidencias() = runTest(dispatcher) {
        viewModel.sincronizarConServidor()
        advanceUntilIdle()

        assertEquals(listOf("actividades", "evidencias"), repositorio.llamadas)
        assertEquals(SincronizacionUiState.Inactiva, viewModel.sincronizacionUiState.value)
    }

    @Test
    fun agregarActividad_guardaFechaEnFormatoIsoYSincroniza() = runTest(dispatcher) {
        viewModel.agregarActividad(actividad(5, "Nueva", fecha = "25/09/2026"))
        advanceUntilIdle()

        assertEquals("2026-09-25", repositorio.actividades.value.single().fecha)
        assertEquals(listOf("actividades", "evidencias"), repositorio.llamadas)
    }

    @Test
    fun observarEvidencia_entregaModeloDeUiSinEntidadDeRoom() = runTest(dispatcher) {
        repositorio.evidencia = EvidenciaEntity(1, "/fotos/a.jpg", "image/jpeg", 2048, EvidenciaEntity.ESTADO_FALLIDA)

        val evidencia = viewModel.observarEvidencia(1).first()

        assertEquals("/fotos/a.jpg", evidencia?.rutaLocal)
        assertEquals(2L, evidencia?.tamanoKb)
        assertEquals(EstadoEvidencia.FALLIDA, evidencia?.estado)
    }

    private fun contenido(): List<ActividadFormativa> =
        (viewModel.listadoUiState.value as ListadoUiState.Contenido).actividades

    private fun actividad(id: Long, titulo: String, progreso: Int = 0, fecha: String = "2026-09-30") =
        ActividadFormativa(id, titulo, "", fecha, progreso, 5, Prioridad.MEDIA)
}

private class RepositorioFalso : ActividadRepository {
    val actividades = MutableStateFlow<List<ActividadFormativa>>(emptyList())
    var fallarLectura = false
    var resultadoSincronizacion: Result<Unit> = Result.success(Unit)
    var evidencia: EvidenciaEntity? = null
    val llamadas = mutableListOf<String>()

    override fun observeActividades(): Flow<List<ActividadFormativa>> = flow {
        if (fallarLectura) throw IllegalStateException("Base de datos no disponible")
        actividades.collect { emit(it) }
    }

    override suspend fun refresh(): Result<Unit> {
        llamadas += "actividades"
        return resultadoSincronizacion
    }

    override suspend fun sincronizarEvidencias(): Result<Unit> {
        llamadas += "evidencias"
        return Result.success(Unit)
    }

    override suspend fun insertActividad(actividad: ActividadFormativa) {
        actividades.value = actividades.value + actividad
    }

    override suspend fun updateActividad(actividad: ActividadFormativa) {
        actividades.value = actividades.value.map { if (it.id == actividad.id) actividad else it }
    }

    override suspend fun deleteActividad(actividad: ActividadFormativa) {
        actividades.value = actividades.value.filterNot { it.id == actividad.id }
    }

    override fun observarEvidencia(actividadId: Int): Flow<EvidenciaEntity?> = flowOf(evidencia)
    override suspend fun subirEvidencia(actividadId: Int, bytes: ByteArray, tipoMime: String) = Result.success(Unit)
    override suspend fun eliminarEvidencia(actividadId: Int) {}
}

private class PreferenciasFalsas : PreferenciasUsuario {
    private val soloCompletadas = MutableStateFlow(false)
    override val mostrarSoloCompletadas: Flow<Boolean> = soloCompletadas.map { it }
    override suspend fun guardarFiltroCompletadas(mostrar: Boolean) {
        soloCompletadas.value = mostrar
    }
}
