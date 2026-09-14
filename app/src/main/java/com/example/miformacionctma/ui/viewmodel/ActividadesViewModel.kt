package com.example.miformacionctma.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miformacionctma.data.repository.PreferenciasRepository
import com.example.miformacionctma.domain.repository.ActividadRepository
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.ui.state.ListadoUiState
import com.example.miformacionctma.ui.state.OperacionUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class ActividadesViewModel(
    private val repository: ActividadRepository,
    private val preferenciasRepository: PreferenciasRepository
) : ViewModel() {

    private val _textoBusqueda = MutableStateFlow("")
    val textoBusqueda = _textoBusqueda.asStateFlow()

    private val _operacion = MutableStateFlow<OperacionUiState>(OperacionUiState.Inactiva)
    val operacion: StateFlow<OperacionUiState> = _operacion.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ListadoUiState> = combine(
        _textoBusqueda
            .map { it.trim() }
            .distinctUntilChanged()
            .flatMapLatest { texto ->
                if (texto.isEmpty()) repository.observarTodos()
                else repository.buscar(texto)
            },
        preferenciasRepository.preferencias
    ) { actividades, pref ->
        // combine filter / ordering context
        val filtradas = actividades.filter {
            pref.filtroCompetencia == null || it.competenciaId.toString() == pref.filtroCompetencia
        }
        if (filtradas.isEmpty()) {
            ListadoUiState.Vacio
        } else {
            val ordenadas = if (pref.ordenDescendente) {
                filtradas.sortedByDescending { it.titulo } // Example ordering or by specific field
            } else {
                filtradas.sortedBy { it.titulo }
            }
            ListadoUiState.Contenido(ordenadas)
        }
    }
        .catch { error ->
            if (error is CancellationException) throw error
            emit(ListadoUiState.Error(error.message ?: "Error desconocido de persistencia"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ListadoUiState.Cargando
        )

    fun cambiarBusqueda(texto: String) {
        _textoBusqueda.value = texto
    }

    fun guardar(actividad: ActividadFormativa) {
        viewModelScope.launch {
            _operacion.value = OperacionUiState.EnCurso
            try {
                repository.guardar(actividad)
                _operacion.value = OperacionUiState.Exitosa
            } catch (cancelada: CancellationException) {
                throw cancelada
            } catch (error: Exception) {
                _operacion.value = OperacionUiState.Fallida(error.message ?: "Fallo al guardar la actividad")
            }
        }
    }

    fun eliminar(id: String) {
        viewModelScope.launch {
            _operacion.value = OperacionUiState.EnCurso
            try {
                repository.eliminar(id)
                _operacion.value = OperacionUiState.Exitosa
            } catch (cancelada: CancellationException) {
                throw cancelada
            } catch (error: Exception) {
                _operacion.value = OperacionUiState.Fallida(error.message ?: "Fallo al eliminar la actividad")
            }
        }
    }

    fun cambiarModoCuadricula(activo: Boolean) {
        viewModelScope.launch {
            preferenciasRepository.guardarModoCuadricula(activo)
        }
    }

    fun cambiarFiltroCompetencia(valor: String?) {
        viewModelScope.launch {
            preferenciasRepository.guardarFiltro(valor)
        }
    }

    fun reiniciarOperacion() {
        _operacion.value = OperacionUiState.Inactiva
    }
}
