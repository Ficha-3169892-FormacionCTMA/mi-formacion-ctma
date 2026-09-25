package com.example.miformacionctma

import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.model.ReglasActividad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ReglasActividadTest {

    private fun parseInstant(fechaStr: String): Instant {
        return LocalDate.parse(fechaStr, DateTimeFormatter.ISO_LOCAL_DATE)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
    }

    @Test
    fun promedioProgreso_calculaCorrectamente() {
        val actividades = listOf(
            ActividadFormativa(1, "A", "Sin descripción", parseInstant("2026-09-01"), 100, Prioridad.ALTA),
            ActividadFormativa(2, "B", "Sin descripción", parseInstant("2026-09-02"), 50, Prioridad.MEDIA)
        )

        val promedio = ReglasActividad.promedioProgreso(actividades)

        assertEquals(75.0, promedio, 0.01)
    }

    @Test
    fun actividadesUrgentes_filtraCorrectamente() {
        val fechaPrueba = parseInstant("2026-09-01")

        val actividades = listOf(
            ActividadFormativa(
                1,
                "Urgente",
                "Sin descripción",
                parseInstant("2026-09-02"),
                20,
                Prioridad.ALTA,
                completada = false
            ), ActividadFormativa(
                2,
                "Normal",
                "Sin descripción",
                parseInstant("2026-09-10"),
                20,
                Prioridad.MEDIA,
                completada = false
            ), ActividadFormativa(
                3,
                "Completada",
                "Sin descripción",
                parseInstant("2026-09-02"),
                100,
                Prioridad.BAJA,
                completada = true
            )
        )

        val urgentes = ReglasActividad.actividadesUrgentes(
            actividades, fechaPrueba
        )

        assertEquals(1, urgentes.size)
        assertEquals("Urgente", urgentes.first().titulo)
    }

    @Test
    fun estadoActividad_devuelvePendiente() {
        val fechaPrueba = parseInstant("2026-09-01")

        val actividad = ActividadFormativa(
            id = 1,
            titulo = "Pendiente",
            descripcion = "Sin descripción",
            fecha = parseInstant("2026-09-10"),
            progreso = 0,
            prioridad = Prioridad.BAJA,
            completada = false
        )

        val estado = ReglasActividad.estadoActividad(
            actividad, fechaPrueba
        )

        assertEquals("Pendiente", estado)
    }

    @Test
    fun validarTitulo_evaluaLimitesCorrectamente() {
        val errorVacio = ReglasActividad.validarTitulo("", mostrarVacio = true)
        assertEquals("El título es obligatorio", errorVacio)

        val errorCorto = ReglasActividad.validarTitulo("AB")
        assertEquals("Usa al menos 3 caracteres", errorCorto)

        val valido = ReglasActividad.validarTitulo("ABC")
        assertNull(valido)

        val textoLargo = "A".repeat(81)
        val errorLargo = ReglasActividad.validarTitulo(textoLargo)
        assertEquals("Usa máximo 80 caracteres", errorLargo)

        val errorEspacios = ReglasActividad.validarTitulo("   ", mostrarVacio = true)
        assertEquals("El título es obligatorio", errorEspacios)
    }
}
