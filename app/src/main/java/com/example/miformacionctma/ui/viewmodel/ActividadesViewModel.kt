package com.example.miformacionctma.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.miformacionctma.data.repository.ActividadRepository
import com.example.miformacionctma.data.repository.PreferenciasRepository
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.ReglasActividad
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ActividadesViewModel(
    private val repository: ActividadRepository,
    private val preferenciasRepository: PreferenciasRepository
) : ViewModel() {

    val actividades: StateFlow<List<ActividadFormativa>> =
        repository.observarActividades()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun insertar(actividad: ActividadFormativa) {
        viewModelScope.launch {
            repository.insertar(actividad)
        }
    }

    fun actualizar(actividad: ActividadFormativa) {
        viewModelScope.launch {
            repository.actualizar(actividad)
        }
    }

    fun eliminar(actividad: ActividadFormativa) {
        viewModelScope.launch {
            repository.eliminar(actividad)
        }
    }

    suspend fun obtenerPorId(id: Long): ActividadFormativa? {
        return repository.obtenerPorId(id)
    }

    val ordenarPorPrioridad: StateFlow<Boolean> =
        preferenciasRepository.ordenarPorPrioridad
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false
            )

    val actividadesOrdenadas: StateFlow<List<ActividadFormativa>> =
        combine(
            actividades,
            ordenarPorPrioridad
        ) { lista, porPrioridad ->
            if (porPrioridad) {
                ReglasActividad.ordenarActividades(lista)
            } else {
                lista
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun guardarOrdenPorPrioridad(valor: Boolean) {
        viewModelScope.launch {
            preferenciasRepository.guardarOrdenPorPrioridad(valor)
        }
    }

    init {
        viewModelScope.launch {
            repository.inicializarDatos()
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
