package com.example.miformacionctma.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miformacionctma.data.repository.ActividadRepository
import com.example.miformacionctma.data.repository.PreferenciasRepository
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.ui.state.ListadoUiState
import com.example.miformacionctma.ui.state.OperacionUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.miformacionctma.model.Prioridad
import kotlinx.coroutines.delay

class ActividadesViewModel(
    private val repository: ActividadRepository,
    private val preferenciasRepository: PreferenciasRepository
) : ViewModel() {

    private val textoBusqueda = MutableStateFlow("")

    val busqueda: StateFlow<String> =
        textoBusqueda.asStateFlow()

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

    private var actualizacionJob: Job? = null

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
        viewModelScope.launch {
            _operacion.value = OperacionUiState.EnCurso

            try {
                if (actividad.id == 0L) {
                    repository.crearEnServidor(actividad)
                } else {
                    repository.actualizarEnServidor(actividad)
                }

                _operacion.value = OperacionUiState.Exitosa(
                    fechaActualizacion = obtenerFechaActual()
                )

            } catch (cancelada: CancellationException) {
                throw cancelada

            } catch (error: Exception) {
                _operacion.value =
                    OperacionUiState.Fallida(
                        error.message
                            ?: "No se pudo guardar la actividad."
                    )
            }
        }
    }

    fun eliminar(id: Long) {
        viewModelScope.launch {
            _operacion.value = OperacionUiState.EnCurso
            try {
                repository.eliminarDelServidor(id)
                _operacion.value = OperacionUiState.Exitosa(
                    fechaActualizacion = obtenerFechaActual()
                )
            } catch (cancelada: CancellationException) {
                throw cancelada
            } catch (error: Exception) {
                _operacion.value = OperacionUiState.Fallida(
                    error.message ?: "No se pudo eliminar la actividad."
                )
            }
        }
    }

    fun refrescarDesdeServidor() {

        if (actualizacionJob?.isActive == true) {
            return
        }

        actualizacionJob = viewModelScope.launch {
            _operacion.value = OperacionUiState.EnCurso

            try {
                repository.refrescarDesdeServidor()

                _operacion.value = OperacionUiState.Exitosa(
                    fechaActualizacion = obtenerFechaActual()
                )

            } catch (cancelada: CancellationException) {
                throw cancelada

            } catch (error: Exception) {
                _operacion.value =
                    OperacionUiState.Fallida(
                        error.message
                            ?: "No se pudieron actualizar las actividades."
                    )
            }
        }
    }

    fun limpiarOperacion() {
        _operacion.value = OperacionUiState.Inactiva
    }

    fun reintentar() {
        refrescarDesdeServidor()
    }

    private fun ejecutarOperacion(
        mensajeError: String,
        operacion: suspend () -> Unit
    ) {
        viewModelScope.launch {
            _operacion.value = OperacionUiState.EnCurso

            try {
                operacion()

                _operacion.value = OperacionUiState.Exitosa(
                    fechaActualizacion = obtenerFechaActual()
                )

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

    private fun obtenerFechaActual(): String {
        val formato = SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale.getDefault()
        )

        formato.timeZone = java.util.TimeZone.getTimeZone("America/Bogota")

        return formato.format(Date())
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