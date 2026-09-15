package com.example.miformacionctma.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.miformacionctma.data.local.entity.ActividadEntity
import com.example.miformacionctma.data.repository.ActividadRepository
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ActividadViewModel(private val repository: ActividadRepository) : ViewModel() {

    val listaActividades: StateFlow<List<ActividadFormativa>> = repository.todasLasActividades
        .map { entidades ->
            entidades.map { entidad ->
                ActividadFormativa(
                    id = entidad.id.toLong(),
                    titulo = entidad.titulo,
                    descripcion = entidad.descripcion,
                    fecha = entidad.fecha,
                    progreso = 0,
                    diasRestantes = 7,
                    prioridad = Prioridad.MEDIA
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun agregarActividad(titulo: String, descripcion: String, fecha: String) {
        viewModelScope.launch {
            val nuevaActividad = ActividadEntity(
                titulo = titulo,
                descripcion = descripcion,
                fecha = fecha
            )
            repository.insertar(nuevaActividad)
        }
    }

    fun eliminarActividad(actividad: ActividadFormativa) {
        viewModelScope.launch {
            val entidad = ActividadEntity(
                id = actividad.id.toInt(),
                titulo = actividad.titulo,
                descripcion = actividad.descripcion,
                fecha = actividad.fecha
            )
            repository.eliminar(entidad)
        }
    }
}

class ActividadViewModelFactory(private val repository: ActividadRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActividadViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ActividadViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}