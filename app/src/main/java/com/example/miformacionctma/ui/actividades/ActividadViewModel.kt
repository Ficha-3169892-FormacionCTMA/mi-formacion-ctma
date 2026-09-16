package com.example.miformacionctma.ui.actividades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    init {
        sincronizarConServidor()
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

    fun sincronizarConServidor() {
        viewModelScope.launch {
            repository.refresh()
        }
    }

    fun actualizarBusqueda(nuevaQuery: String) {
        _queryBusqueda.value = nuevaQuery
    }

    fun reiniciarEstadoOperacion() {
        _operacionUiState.value = OperacionUiState.Inactiva
    }

    // --- Métodos CRUD utilizando ActividadFormativa ---

    fun agregarActividad(actividad: ActividadFormativa) {
        viewModelScope.launch {
            repository.insertActividad(actividad)
        }
    }

    fun actualizarActividad(actividad: ActividadFormativa) {
        viewModelScope.launch {
            repository.updateActividad(actividad)
        }
    }

    fun eliminarActividad(actividad: ActividadFormativa) {
        viewModelScope.launch {
            repository.deleteActividad(actividad)
        }
    }
}