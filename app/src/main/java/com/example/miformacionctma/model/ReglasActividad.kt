package com.example.miformacionctma.model

object ReglasActividad {
    // - Validaciones de campos para el formulario de la semana 4) -

    // Título obligatorio: 3 a 80 caracteres
    fun validarTitulo(valor: String, mostrarVacio: Boolean = true): String? {
        val limpio = valor.trim()
        return when {
            limpio.isEmpty() && mostrarVacio -> "El título es obligatorio"
            limpio.isNotEmpty() && limpio.length < 3 -> "Usa al menos 3 caracteres"
            limpio.length > 80 -> "Usa máximo 80 caracteres"
            else -> null
        }
    }

    // Descripción opcional: máximo 240 caracteres
    fun validarDescripcion(valor: String): String? {
        return if (valor.trim().length > 240) "Máximo 240 caracteres" else null
    }

    // Fecha obligatoria: el usuario la ingresa como DD/MM/AAAA.
    // También se acepta AAAA-MM-DD, que es el formato en que se guarda.
    fun validarFecha(fecha: String): String? {
        if (fecha.isBlank()) return "La fecha es obligatoria"

        val regexDiaMesAnio = Regex("""^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/\d{4}$""")
        val regexAnioMesDia = Regex("""^\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$""")

        return if (!regexDiaMesAnio.matches(fecha) && !regexAnioMesDia.matches(fecha)) {
            "Formato inválido (DD/MM/AAAA)"
        } else {
            null
        }
    }

    // Convierte AAAA-MM-DD (formato guardado) a DD/MM/AAAA para mostrar en el formulario
    fun fechaParaFormulario(fecha: String): String {
        val partes = fecha.split("-")
        return if (partes.size == 3 && partes[0].length == 4) {
            "${partes[2]}/${partes[1]}/${partes[0]}"
        } else {
            fecha
        }
    }

    // Progreso: entre 0 y 100
    fun validarProgreso(valor: Int): String? {
        return if (valor !in 0..100) "El progreso debe estar entre 0 y 100" else null
    }

    // - Validaciones de semanas anteriores -

    fun validarActividad(actividad: ActividadFormativa): List<String> {
        val errores = mutableListOf<String>()
        validarTitulo(actividad.titulo)?.let { errores.add(it) }
        validarDescripcion(actividad.descripcion)?.let { errores.add(it) }
        validarFecha(actividad.fecha)?.let { errores.add(it) }
        validarProgreso(actividad.progreso)?.let { errores.add(it) }
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
        return actividades.filter { it.titulo.trim().lowercase().contains(termino) }
    }

    fun ordenarActividades(actividades: List<ActividadFormativa>): List<ActividadFormativa> {
        return actividades.sortedWith(
            compareBy(
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