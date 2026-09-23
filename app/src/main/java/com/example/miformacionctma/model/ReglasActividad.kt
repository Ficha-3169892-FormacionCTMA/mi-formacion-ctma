package com.example.miformacionctma.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

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

        return try {
            LocalDate.parse(limpio, DateTimeFormatter.ISO_LOCAL_DATE)
            null
        } catch (e: DateTimeParseException) {
            "Fecha inválida (AAAA-MM-DD)"
        }
    }

    // Progreso: entre 0 y 100
    fun validarProgreso(valor: Int): String? {
        return if (valor !in 0..100) "El progreso debe estar entre 0 y 100" else null
    }

    // Días restantes: calculado automáticamente
    private fun diasRestantes(
        actividad: ActividadFormativa, fechaReferencia: Instant = Instant.now()
    ): Long {
        val zona = ZoneId.systemDefault()
        val fechaLimite = actividad.fecha.atZone(zona).toLocalDate()
        val hoy = fechaReferencia.atZone(zona).toLocalDate()

        return ChronoUnit.DAYS.between(hoy, fechaLimite)
    }

    // - Validaciones de semanas anteriores -

    fun validarActividad(actividad: ActividadFormativa): List<String> {
        val errores = mutableListOf<String>()
        validarTitulo(actividad.titulo)?.let { errores.add(it) }
        validarDescripcion(actividad.descripcion)?.let { errores.add(it) }
        // Nota: validarFecha espera String, aquí validamos el Instant implícitamente
        validarProgreso(actividad.progreso)?.let { errores.add(it) }
        return errores
    }

    fun estadoActividad(
        actividad: ActividadFormativa, fechaReferencia: Instant = Instant.now()
    ): String {
        return when {
            actividad.completada -> "Completada"
            diasRestantes(actividad, fechaReferencia) < 0 -> "Vencida"
            actividad.progreso > 0 -> "En proceso"
            else -> "Pendiente"
        }
    }

    fun actividadesUrgentes(
        actividades: List<ActividadFormativa>, fechaReferencia: Instant = Instant.now()
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
