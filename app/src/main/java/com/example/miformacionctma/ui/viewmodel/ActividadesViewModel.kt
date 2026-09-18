package com.example.miformacionctma.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.miformacionctma.data.repository.ActividadRepository
import com.example.miformacionctma.data.repository.PreferenciasRepository
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Competencia
import com.example.miformacionctma.model.ReglasActividad
import com.example.miformacionctma.ui.state.ListadoUiState
import com.example.miformacionctma.ui.state.OperacionUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ActividadesViewModel(
    private val repository: ActividadRepository,
    private val preferenciasRepository: PreferenciasRepository
) : ViewModel() {

    private val _textoBusqueda = MutableStateFlow("")
    val textoBusqueda: StateFlow<String> = _textoBusqueda.asStateFlow()

    private val _operacionUiState = MutableStateFlow<OperacionUiState>(OperacionUiState.Inactiva)
    val operacionUiState: StateFlow<OperacionUiState> = _operacionUiState.asStateFlow()

    fun actualizarBusqueda(texto: String) {
        _textoBusqueda.value = texto
    }

    fun reiniciarOperacion() {
        _operacionUiState.value = OperacionUiState.Inactiva
    }

    val competencias: StateFlow<List<Competencia>> =
        repository.observarCompetencias()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val ordenarPorPrioridad: StateFlow<Boolean> =
        preferenciasRepository.ordenarPorPrioridad
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false
            )

    val uiState: StateFlow<ListadoUiState> = _textoBusqueda
        .flatMapLatest { texto ->
            if (texto.isBlank()) {
                repository.observarActividades()
            } else {
                repository.observarPorTitulo(texto)
            }
        }
        .combine(ordenarPorPrioridad) { lista, porPrioridad ->
            if (porPrioridad) {
                ReglasActividad.ordenarActividades(lista)
            } else {
                lista
            }
        }
        .map { lista ->
            if (lista.isEmpty()) {
                ListadoUiState.Vacio
            } else {
                ListadoUiState.Contenido(lista)
            }
        }
        .catch { e ->
            if (e is CancellationException) throw e
            emit(ListadoUiState.Error("Error al cargar las actividades: ${e.message}"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ListadoUiState.Cargando
        )

    fun insertar(actividad: ActividadFormativa) {
        viewModelScope.launch {
            _operacionUiState.value = OperacionUiState.EnCurso
            try {
                repository.insertar(actividad)
                _operacionUiState.value = OperacionUiState.Exitosa
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _operacionUiState.value = OperacionUiState.Fallida("Error al insertar: ${e.message}")
            }
        }
    }

    fun actualizar(actividad: ActividadFormativa) {
        viewModelScope.launch {
            _operacionUiState.value = OperacionUiState.EnCurso
            try {
                repository.actualizar(actividad)
                _operacionUiState.value = OperacionUiState.Exitosa
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _operacionUiState.value = OperacionUiState.Fallida("Error al actualizar: ${e.message}")
            }
        }
    }

    fun eliminar(actividad: ActividadFormativa) {
        viewModelScope.launch {
            _operacionUiState.value = OperacionUiState.EnCurso
            try {
                repository.eliminar(actividad)
                _operacionUiState.value = OperacionUiState.Exitosa
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _operacionUiState.value = OperacionUiState.Fallida("Error al eliminar: ${e.message}")
            }
        }
    }

    suspend fun obtenerPorId(id: Long): ActividadFormativa? {
        return repository.obtenerPorId(id)
    }

    fun guardarOrdenPorPrioridad(valor: Boolean) {
        viewModelScope.launch {
            preferenciasRepository.guardarOrdenPorPrioridad(valor)
        }
    }

    suspend fun obtenerConCompetencia(
        id: Long
    ): Pair<ActividadFormativa, String?>? {
        return repository.obtenerConCompetencia(id)
    }

    init {
        viewModelScope.launch {
            try {
                repository.inicializarDatos()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
    }
}

class ActividadesViewModelFactory(
    private val repository: ActividadRepository,
    private val preferenciasRepository: PreferenciasRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActividadesViewModel::class.java)) {
            return ActividadesViewModel(
                repository,
                preferenciasRepository
            ) as T
        }

        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}

