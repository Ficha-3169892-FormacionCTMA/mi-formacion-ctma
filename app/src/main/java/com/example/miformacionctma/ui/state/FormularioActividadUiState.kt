package com.example.miformacionctma.ui.state

import com.example.miformacionctma.model.Prioridad

data class FormularioActividadUiState(
    val titulo: String = "",
    val tituloTocado: Boolean = false,
    val tituloError: String? = null,

    val descripcion: String = "",
    val descripcionTocado: Boolean = false,
    val descripcionError: String? = null,

    val fecha: String = "",
    val fechaTocado: Boolean = false,
    val fechaError: String? = null,

    val prioridad: Prioridad = Prioridad.MEDIA,
    val progreso: Int = 0,
    val progresoError: String? = null
) {
    // Se calcula a partir de los errores: si no hay ninguno, se habilita el botón Guardar
    val puedeGuardar: Boolean
        get() = tituloError == null &&
                descripcionError == null &&
                fechaError == null &&
                progresoError == null &&
                titulo.isNotBlank() &&
                fecha.isNotBlank()
}
