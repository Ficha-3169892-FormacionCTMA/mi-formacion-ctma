package com.example.miformacionctma.model

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    // Fecha obligatoria con formato YYYY-MM-DD
    fun validarFecha(valor: String): String? {
        val limpio = valor.trim()

        if (limpio.isEmpty()) return "La fecha es obligatoria"

        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        formato.isLenient = false

        return try {
            formato.parse(limpio)
            null
        } catch (_: ParseException) {
            "Fecha inválida (AAAA-MM-DD)"
        }
    }

    // Progreso: entre 0 y 100
    fun validarProgreso(valor: Int): String? {
        return if (valor !in 0..100) "El progreso debe estar entre 0 y 100" else null
    }

    // Días restantes: calculado automáticamente
    private fun diasRestantes(
        actividad: ActividadFormativa, fechaReferencia: Date = Date()
    ): Long {
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        formato.isLenient = false

        val fechaLimite = formato.parse(actividad.fecha) ?: return 0L
        val hoy = formato.parse(formato.format(fechaReferencia)) ?: return 0L

        return (fechaLimite.time - hoy.time) / (1000L * 60 * 60 * 24)
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

    fun estadoActividad(
        actividad: ActividadFormativa, fechaReferencia: Date = Date()
    ): String {
        return when {
            actividad.completada -> "Completada"
            diasRestantes(actividad, fechaReferencia) < 0 -> "Vencida"
            actividad.progreso > 0 -> "En proceso"
            else -> "Pendiente"
        }
    }

    fun actividadesUrgentes(
        actividades: List<ActividadFormativa>, fechaReferencia: Date = Date()
    ): List<ActividadFormativa> {
        return actividades.filter {
            !it.completada && diasRestantes(it, fechaReferencia) <= 2
        }
    }

    fun promedioProgreso(actividades: List<ActividadFormativa>): Double {
        return if (actividades.isEmpty()) {
            0.0
        } else {
            actividades.map { it.progreso }.average()
        }
    }

    fun buscarPorTitulo(
        actividades: List<ActividadFormativa>, texto: String
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
                { diasRestantes(it) })
        )
    }
}
