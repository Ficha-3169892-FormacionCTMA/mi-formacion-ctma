package com.example.miformacionctma.model

object ReglasActividad {
    fun validarActividad(actividad: ActividadFormativa): List<String> {
        val errores = mutableListOf<String>()

        if (actividad.titulo.isBlank()) {
            errores.add("El título es obligatorio")
        }

        if (actividad.progreso !in 0..100) {
            errores.add("El progreso debe estar entre 0 y 100")
        }

        return errores
    }

    fun estadoActividad(actividad: ActividadFormativa): String {
        return when {
            actividad.progreso >= 100 -> "Completada"
            actividad.diasRestantes < 0 -> "Vencida"
            actividad.progreso > 0 -> "En proceso"
            else -> "Pendiente"
        }
    }

    fun actividadesUrgentes(actividades: List<ActividadFormativa>): List<ActividadFormativa> {
        return actividades.filter { it.progreso < 100 && it.diasRestantes <= 2 }
    }

    fun promedioProgreso(actividades: List<ActividadFormativa>): Double {
        return if (actividades.isEmpty()) {
            0.0
        } else {
            actividades.map { it.progreso }.average()
        }
    }

    fun buscarPorTitulo(
        actividades: List<ActividadFormativa>,
        texto: String
    ): List<ActividadFormativa> {
        val termino = texto.trim().lowercase()

        return actividades.filter {
            it.titulo.trim().lowercase().contains(termino)
        }
    }

    // Reto adicional
    fun ordenarActividades(actividades: List<ActividadFormativa>): List<ActividadFormativa> {
        return actividades.sortedWith(
            compareBy<ActividadFormativa>(
                // Vencidas primero
                { estadoActividad(it) != "Vencida" },
                // Prioridad alta primero
                { -it.prioridad.ordinal },
                // Menos días primero
                { it.diasRestantes }
            )
        )
    }
}