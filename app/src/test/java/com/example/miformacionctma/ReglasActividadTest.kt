package com.example.miformacionctma

import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.model.ReglasActividad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReglasActividadTest {

    @Test
    fun promedioProgreso_calculaCorrectamente() {
        val actividades = listOf(
            ActividadFormativa(1, "A", "Sin descripción", "2026-09-01", 100, 0, Prioridad.ALTA),
            ActividadFormativa(2, "B", "Sin descripción", "2026-09-02", 50, 0, Prioridad.MEDIA)
        )

        val promedio = ReglasActividad.promedioProgreso(actividades)

        assertEquals(75.0, promedio, 0.01)
    }

    @Test
    fun actividadesUrgentes_filtraCorrectamente() {
        val actividades = listOf(
            ActividadFormativa(1, "Urgente", "Sin descripción", "2026-09-01", 20, 1, Prioridad.ALTA),
            ActividadFormativa(2, "Normal", "Sin descripción", "2026-09-05", 20, 5, Prioridad.MEDIA),
            ActividadFormativa(3, "Completada", "Sin descripción", "2026-09-01", 100, 1, Prioridad.BAJA)
        )

        val urgentes = ReglasActividad.actividadesUrgentes(actividades)

        assertEquals(1, urgentes.size)
        assertEquals("Urgente", urgentes.first().titulo)
    }

    @Test
    fun estadoActividad_devuelvePendiente() {
        val actividad = ActividadFormativa(
            id = 1,
            titulo = "Pendiente",
            descripcion = "Sin descripción",
            fecha = "2026-09-01",
            progreso = 0,
            diasRestantes = 3,
            prioridad = Prioridad.BAJA
        )

        val estado = ReglasActividad.estadoActividad(actividad)

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
