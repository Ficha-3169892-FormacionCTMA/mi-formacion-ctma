package com.example.miformacionctma.ui.actividades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miformacionctma.data.local.entity.ActividadEntity
import com.example.miformacionctma.data.repository.ActividadRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
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

    val listadoUiState: StateFlow<ListadoUiState> = _queryBusqueda
        .debounce(300L)
        .flatMapLatest { query ->
            repository.obtenerTodasLasActividades().map { lista: List<ActividadEntity> ->
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
            emit(ListadoUiState.Error(e.message ?: "Error al consultar la base de datos"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ListadoUiState.Cargando
        )

    fun actualizarBusqueda(nuevaQuery: String) {
        _queryBusqueda.value = nuevaQuery
    }

    fun agregarActividad(actividad: ActividadEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            _operacionUiState.value = OperacionUiState.EnCurso
            try {
                repository.insertarActividad(actividad)
                _operacionUiState.value = OperacionUiState.Exitosa
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _operacionUiState.value = OperacionUiState.Fallida(e.message ?: "Error al guardar")
            }
        }
    }

    fun actualizarActividad(actividad: ActividadEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            _operacionUiState.value = OperacionUiState.EnCurso
            try {
                repository.actualizarActividad(actividad)
                _operacionUiState.value = OperacionUiState.Exitosa
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _operacionUiState.value = OperacionUiState.Fallida(e.message ?: "Error al actualizar")
            }
        }
    }

    fun eliminarActividad(actividad: ActividadEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.eliminarActividad(actividad)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _operacionUiState.value = OperacionUiState.Fallida(e.message ?: "Error al eliminar")
            }
        }
    }

    fun reiniciarEstadoOperacion() {
        _operacionUiState.value = OperacionUiState.Inactiva
    }
}