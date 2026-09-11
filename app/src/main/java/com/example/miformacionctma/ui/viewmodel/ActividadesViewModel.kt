package com.example.miformacionctma.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miformacionctma.data.repository.ActividadRepository
import com.example.miformacionctma.data.repository.PreferenciasRepository
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.ui.state.ListadoUiState
import com.example.miformacionctma.ui.state.OperacionUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ActividadesViewModel(
    private val repository: ActividadRepository,
    private val preferenciasRepository: PreferenciasRepository
) : ViewModel() {

    private val textoBusqueda = MutableStateFlow("")

    val busqueda: StateFlow<String> =
        textoBusqueda.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _ultimaActualizacion = MutableStateFlow<String?>(null)
    val ultimaActualizacion: StateFlow<String?> = _ultimaActualizacion.asStateFlow()

    private val orden =
        preferenciasRepository.orden
            .distinctUntilChanged()

    private val actividades =
        textoBusqueda
            .map { it.trim() }
            .distinctUntilChanged()
            .flatMapLatest { texto ->
                repository.buscar(texto)
            }

    private val resultado =
        combine(
            actividades,
            orden
        ) { lista, ordenActual ->
            ordenarActividades(
                lista = lista,
                orden = ordenActual
            )
        }

    val uiState: StateFlow<ListadoUiState> =
        resultado
            .map { actividades ->
                if (actividades.isEmpty()) {
                    ListadoUiState.Vacio
                } else {
                    ListadoUiState.Contenido(actividades)
                }
            }
            .catch { error ->
                if (error is CancellationException) {
                    throw error
                }

                emit(
                    ListadoUiState.Error(
                        "No se pudieron cargar las actividades."
                    )
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ListadoUiState.Cargando
            )

    private val _operacion =
        MutableStateFlow<OperacionUiState>(
            OperacionUiState.Inactiva
        )

    val operacion: StateFlow<OperacionUiState> =
        _operacion.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            try {
                repository.refresh()
                _ultimaActualizacion.value = java.text.SimpleDateFormat(
                    "HH:mm:ss", java.util.Locale.getDefault()
                ).format(java.util.Date())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _operacion.value = OperacionUiState.Fallida(
                    e.message ?: "Fallo al actualizar datos remotos"
                )
            } finally {
                _refreshing.value = false
            }
        }
    }

    fun cambiarBusqueda(texto: String) {
        textoBusqueda.value = texto
    }

    fun cambiarOrden(orden: String) {
        viewModelScope.launch {
            try {
                preferenciasRepository.guardarOrden(orden)
            } catch (cancelada: CancellationException) {
                throw cancelada
            }
        }
    }

    fun guardar(actividad: ActividadFormativa) {
        ejecutarOperacion(
            mensajeError = "No se pudo guardar la actividad."
        ) {
            repository.guardar(actividad)
        }
    }

    fun eliminar(id: Long) {
        ejecutarOperacion(
            mensajeError = "No se pudo eliminar la actividad."
        ) {
            repository.eliminar(id)
        }
    }

    fun limpiarOperacion() {
        _operacion.value = OperacionUiState.Inactiva
    }

    fun reintentar() {
        textoBusqueda.value = textoBusqueda.value
    }

    private fun ejecutarOperacion(
        mensajeError: String,
        operacion: suspend () -> Unit
    ) {
        viewModelScope.launch {
            _operacion.value = OperacionUiState.EnCurso

            try {
                operacion()

                _operacion.value = OperacionUiState.Exitosa
            } catch (cancelada: CancellationException) {
                throw cancelada
            } catch (error: Exception) {
                _operacion.value =
                    OperacionUiState.Fallida(
                        mensajeError
                    )
            }
        }
    }

    private fun ordenarActividades(
        lista: List<ActividadFormativa>,
        orden: String
    ): List<ActividadFormativa> {
        return when (orden) {
            "titulo" -> lista.sortedBy {
                it.titulo.lowercase()
            }

            "progreso" -> lista.sortedByDescending {
                it.progreso
            }

            "fecha" -> lista.sortedBy {
                it.fecha
            }

            else -> lista.sortedBy {
                it.id
            }
        }
    }
}