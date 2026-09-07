package com.example.miformacionctma.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miformacionctma.data.repository.ActividadRepository
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.ui.state.ListadoUiState
import com.example.miformacionctma.ui.state.OperacionUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ActividadesViewModel(
    private val repository: ActividadRepository
) : ViewModel() {

    private val textoBusqueda = MutableStateFlow("")

    val busqueda: StateFlow<String> =
        textoBusqueda.asStateFlow()

    private val actividadesBuscadas:
            kotlinx.coroutines.flow.Flow<List<ActividadFormativa>> =
        textoBusqueda
            .map(String::trim)
            .distinctUntilChanged()
            .flatMapLatest(repository::buscar)


    val uiState: StateFlow<ListadoUiState> =
        actividadesBuscadas
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

    fun cambiarBusqueda(texto: String) {
        textoBusqueda.value = texto
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
                _operacion.value =
                    OperacionUiState.Fallida(
                        "No se pudo guardar la actividad."
                    )
            }
        }
    }


}