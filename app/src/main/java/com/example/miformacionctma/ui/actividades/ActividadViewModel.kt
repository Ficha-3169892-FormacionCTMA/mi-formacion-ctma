package com.example.miformacionctma.ui.actividades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miformacionctma.data.remote.DataError
import com.example.miformacionctma.data.remote.NetworkFailure
import com.example.miformacionctma.data.repository.ActividadRepository
import com.example.miformacionctma.model.ActividadFormativa
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class ActividadViewModel(
    private val repository: ActividadRepository
) : ViewModel() {

    private val _queryBusqueda = MutableStateFlow("")
    val queryBusqueda: StateFlow<String> = _queryBusqueda.asStateFlow()

    private val _operacionUiState = MutableStateFlow<OperacionUiState>(OperacionUiState.Inactiva)
    val operacionUiState: StateFlow<OperacionUiState> = _operacionUiState.asStateFlow()

    private fun mensajeDeError(error: Throwable): String {
        val causa = (error as? NetworkFailure)?.error
        return when (causa) {
            DataError.NoConnection -> "sin conexión a internet. Se reintentará al volver a abrir la app."
            DataError.Timeout -> "el servidor tardó demasiado en responder. Se reintentará al volver a abrir la app."
            is DataError.Unknown -> causa.cause.message ?: "error desconocido"
            null -> error.message ?: "error desconocido"
            else -> causa.toString()
        }
    }

    val listadoUiState: StateFlow<ListadoUiState> = _queryBusqueda
        .debounce(300L)
        .flatMapLatest { query ->
            repository.observeActividades().map { lista: List<ActividadFormativa> ->
                val listaFiltrada = if (query.isBlank()) {
                    lista
                } else {
                    lista.filter { actividad ->
                        actividad.titulo.contains(query, ignoreCase = true) ||
                                actividad.descripcion.contains(query, ignoreCase = true)
                    }
                }

                if (listaFiltrada.isEmpty()) {
                    ListadoUiState.Vacio
                } else {
                    ListadoUiState.Contenido(listaFiltrada)
                }
            }
        }
        .catch { e ->
            emit(ListadoUiState.Error(e.message ?: "Error al consultar los datos"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ListadoUiState.Cargando
        )

    // Primero las actividades (las evidencias necesitan que su actividad exista en Room) y luego las fotos
    fun sincronizarConServidor() {
        viewModelScope.launch {
            repository.refresh()
            repository.sincronizarEvidencias()
        }
    }

    fun actualizarBusqueda(nuevaQuery: String) {
        _queryBusqueda.value = nuevaQuery
    }

    fun reiniciarEstadoOperacion() {
        _operacionUiState.value = OperacionUiState.Inactiva
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
            repository.refresh()
        }
    }

    fun actualizarActividad(actividad: ActividadFormativa) {
        viewModelScope.launch {
            val actividadAjustada = actividad.copy(fecha = normalizarFecha(actividad.fecha))
            repository.updateActividad(actividadAjustada)
            repository.refresh()
        }
    }

    fun eliminarActividad(actividad: ActividadFormativa) {
        viewModelScope.launch {
            repository.deleteActividad(actividad)
            repository.refresh()
        }
    }

    // --- Métodos de Gestión de Evidencias ---

    fun observarEvidencia(actividadId: Int): kotlinx.coroutines.flow.Flow<com.example.miformacionctma.data.local.entity.EvidenciaEntity?> {
        return repository.observarEvidencia(actividadId)
    }

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