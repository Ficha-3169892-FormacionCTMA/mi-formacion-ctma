package com.example.miformacionctma.ui.actividades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.miformacionctma.data.repository.ActividadRepository
import com.example.miformacionctma.data.repository.PreferenciasUsuario

class ActividadViewModelFactory(
    private val repository: ActividadRepository,
    private val preferencias: PreferenciasUsuario
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActividadViewModel::class.java)) {
            return ActividadViewModel(repository, preferencias) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
    }
}