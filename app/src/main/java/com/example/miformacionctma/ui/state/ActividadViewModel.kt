package com.example.miformacionctma.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miformacionctma.data.local.ActividadEntity
import com.example.miformacionctma.data.repository.ActividadRepository
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ActividadViewModel(
    private val repository: ActividadRepository
) : ViewModel() {

    val listaActividades: StateFlow<List<ActividadFormativa>> =
        repository.todasLasActividades
            .map { listaEntities ->
                listaEntities.map { entity ->
                    ActividadFormativa(
                        id = entity.id.toLong(),
                        titulo = entity.titulo,
                        descripcion = entity.descripcion,
                        fecha = entity.fecha,
                        progreso = entity.progreso,
                        diasRestantes = 7,
                        prioridad = try {
                            Prioridad.valueOf(entity.prioridad)
                        } catch (_: Exception) {
                            Prioridad.MEDIA
                        }
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun guardarActividad(
        titulo: String,
        descripcion: String,
        fecha: String,
        prioridad: Prioridad,
        progreso: Int
    ) {
        viewModelScope.launch {
            val nuevaEntity = ActividadEntity(
                id = 0,
                titulo = titulo,
                descripcion = descripcion,
                fecha = fecha,
                prioridad = prioridad.name,
                progreso = progreso,
                completada = false
            )
            repository.insertar(nuevaEntity)
        }
    }
}