package com.example.miformacionctma.ui.state

import com.example.miformacionctma.model.Prioridad

data class FormularioActividadUiState(
    val titulo: String = "",
    val tituloError: String? = null,
    val tituloTocado: Boolean = false,
    val descripcion: String = "",
    val descripcionError: String? = null,
    val descripcionTocado: Boolean = false,
    val fecha: String = "",
    val fechaError: String? = null,
    val fechaTocado: Boolean = false,
    val prioridad: Prioridad = Prioridad.MEDIA,
    val progreso: Int = 0,
    val progresoError: String? = null
) {
    val puedeGuardar: Boolean
        get() = tituloError == null &&
                descripcionError == null &&
                fechaError == null &&
                progresoError == null &&
                titulo.isNotBlank() &&
                fecha.isNotBlank()
}
