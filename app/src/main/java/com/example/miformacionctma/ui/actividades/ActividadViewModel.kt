package com.example.miformacionctma.ui.actividades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miformacionctma.data.local.entity.EvidenciaEntity
import com.example.miformacionctma.data.remote.DataError
import com.example.miformacionctma.data.remote.NetworkFailure
import com.example.miformacionctma.data.repository.ActividadRepository
import com.example.miformacionctma.data.repository.PreferenciasUsuario
import com.example.miformacionctma.model.ActividadFormativa
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class ActividadViewModel(
    private val repository: ActividadRepository,
    private val preferencias: PreferenciasUsuario
) : ViewModel() {

    private val _queryBusqueda = MutableStateFlow("")
    val queryBusqueda: StateFlow<String> = _queryBusqueda.asStateFlow()

    private val _operacionUiState = MutableStateFlow<OperacionUiState>(OperacionUiState.Inactiva)
    val operacionUiState: StateFlow<OperacionUiState> = _operacionUiState.asStateFlow()

    private val _sincronizacionUiState = MutableStateFlow<SincronizacionUiState>(SincronizacionUiState.Inactiva)
    val sincronizacionUiState: StateFlow<SincronizacionUiState> = _sincronizacionUiState.asStateFlow()

    // Filtro persistido en DataStore: se conserva al cerrar y abrir la app
    val soloCompletadas: StateFlow<Boolean> = preferencias.mostrarSoloCompletadas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // Lista completa sin búsqueda ni filtros: el detalle y la edición deben encontrar la actividad
    // aunque deje de cumplir el filtro activo (por ejemplo, al bajar su progreso con "Solo completadas")
    val todasLasActividades: StateFlow<List<ActividadFormativa>> = repository.observeActividades()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Cada incremento vuelve a suscribirse a Room (botón "Reintentar" de la pantalla de error)
    private val reintentos = MutableStateFlow(0)

    val listadoUiState: StateFlow<ListadoUiState> =
        combine(_queryBusqueda.debounce(300L), reintentos) { query, _ -> query }
            .flatMapLatest { query ->
                combine(repository.observeActividades(), preferencias.mostrarSoloCompletadas) { lista, solo ->
                    filtrarListado(lista, query, solo)
                }.catch { e ->
                    emit(ListadoUiState.Error(e.message ?: "Error al consultar los datos"))
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ListadoUiState.Cargando
            )

    private fun filtrarListado(lista: List<ActividadFormativa>, query: String, soloCompletadas: Boolean): ListadoUiState {
        // Vacio = no hay ninguna actividad guardada. Si los filtros no dejan resultados se muestra Contenido vacío
        if (lista.isEmpty()) return ListadoUiState.Vacio

        val filtrada = lista
            .filter { !soloCompletadas || it.progreso >= 100 }
            .filter { actividad ->
                query.isBlank() ||
                    actividad.titulo.contains(query, ignoreCase = true) ||
                    actividad.descripcion.contains(query, ignoreCase = true)
            }
        return ListadoUiState.Contenido(filtrada)
    }

    // Primero las actividades (las evidencias necesitan que su actividad exista en Room) y luego las fotos
    fun sincronizarConServidor() {
        viewModelScope.launch {
            _sincronizacionUiState.value = SincronizacionUiState.EnCurso
            val error = repository.refresh().exceptionOrNull()
                ?: repository.sincronizarEvidencias().exceptionOrNull()

            _sincronizacionUiState.value = if (error == null) {
                SincronizacionUiState.Inactiva
            } else {
                SincronizacionUiState.Fallida("No se pudo sincronizar con Supabase: ${mensajeDeError(error)}")
            }
        }
    }

    fun descartarErrorSincronizacion() {
        _sincronizacionUiState.value = SincronizacionUiState.Inactiva
    }

    fun reintentarCarga() {
        reintentos.value++
        sincronizarConServidor()
    }

    fun actualizarBusqueda(nuevaQuery: String) {
        _queryBusqueda.value = nuevaQuery
    }

    fun cambiarFiltroCompletadas(mostrar: Boolean) {
        viewModelScope.launch {
            preferencias.guardarFiltroCompletadas(mostrar)
        }
    }

    fun reiniciarEstadoOperacion() {
        _operacionUiState.value = OperacionUiState.Inactiva
    }

    private fun mensajeDeError(error: Throwable): String {
        val causa = (error as? NetworkFailure)?.error
        return when (causa) {
            DataError.NoConnection -> "sin conexión a internet. Se reintentará cuando vuelva la conexión."
            DataError.Timeout -> "el servidor tardó demasiado en responder."
            DataError.Unauthorized -> "credenciales de Supabase no válidas."
            DataError.NotFound -> "recurso no encontrado en el servidor."
            DataError.InvalidPayload -> "el servidor respondió con datos inválidos."
            is DataError.Server -> "error del servidor (${causa.code})."
            is DataError.Unknown -> causa.cause.message ?: "error desconocido"
            null -> error.message ?: "error desconocido"
        }
    }

    private fun normalizarFecha(fechaInput: String): String {
        if (fechaInput.isBlank()) return fechaInput
        return try {
            val separador = if (fechaInput.contains("/")) "/" else if (fechaInput.contains("-")) "-" else ""
            if (separador.isNotEmpty()) {
                val partes = fechaInput.split(separador)
                if (partes.size == 3) {
                    if (partes[0].length == 4) {
                        "${partes[0]}-${partes[1].padStart(2, '0')}-${partes[2].padStart(2, '0')}"
                    } else {
                        "${partes[2]}-${partes[1].padStart(2, '0')}-${partes[0].padStart(2, '0')}"
                    }
                } else fechaInput
            } else fechaInput
        } catch (e: Exception) {
            fechaInput
        }
    }

    fun agregarActividad(actividad: ActividadFormativa) {
        viewModelScope.launch {
            val actividadAjustada = actividad.copy(fecha = normalizarFecha(actividad.fecha))
            repository.insertActividad(actividadAjustada)
            sincronizarConServidor()
        }
    }

    fun actualizarActividad(actividad: ActividadFormativa) {
        viewModelScope.launch {
            val actividadAjustada = actividad.copy(fecha = normalizarFecha(actividad.fecha))
            repository.updateActividad(actividadAjustada)
            sincronizarConServidor()
        }
    }

    fun eliminarActividad(actividad: ActividadFormativa) {
        viewModelScope.launch {
            repository.deleteActividad(actividad)
            sincronizarConServidor()
        }
    }

    // --- Métodos de Gestión de Evidencias ---

    fun observarEvidencia(actividadId: Int): Flow<EvidenciaUi?> =
        repository.observarEvidencia(actividadId).map { it?.toUi() }

    fun subirEvidencia(actividadId: Int, bytes: ByteArray, tipoMime: String, onResultado: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _operacionUiState.value = OperacionUiState.EnCurso
            val resultado = repository.subirEvidencia(actividadId, bytes, tipoMime)

            resultado.fold(
                onSuccess = {
                    _operacionUiState.value = OperacionUiState.Exitosa
                    onResultado(true, "Evidencia guardada y subida a Supabase.")
                },
                onFailure = { error ->
                    val msj = "La foto quedó guardada en el teléfono, pero no se pudo subir: ${mensajeDeError(error)}"
                    _operacionUiState.value = OperacionUiState.Fallida(msj)
                    onResultado(false, msj)
                }
            )
        }
    }

    fun sincronizarEvidencias() {
        viewModelScope.launch {
            repository.sincronizarEvidencias()
        }
    }

    fun eliminarEvidencia(actividadId: Int) {
        viewModelScope.launch {
            repository.eliminarEvidencia(actividadId)
        }
    }
}

private fun EvidenciaEntity.toUi() = EvidenciaUi(
    rutaLocal = uri,
    tipo = tipo,
    tamanoKb = tamano / 1024,
    estado = when (estado) {
        EvidenciaEntity.ESTADO_SINCRONIZADA -> EstadoEvidencia.SINCRONIZADA
        EvidenciaEntity.ESTADO_FALLIDA -> EstadoEvidencia.FALLIDA
        else -> EstadoEvidencia.PENDIENTE
    }
)
