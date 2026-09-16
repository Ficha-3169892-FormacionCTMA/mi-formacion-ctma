package com.example.miformacionctma.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miformacionctma.data.repository.ActividadRepository
import com.example.miformacionctma.model.ActividadFormativa
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FormacionViewModel(
    private val repository: ActividadRepository
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
}
