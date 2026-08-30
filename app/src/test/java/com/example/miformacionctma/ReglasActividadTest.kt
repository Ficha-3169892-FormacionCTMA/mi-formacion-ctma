package com.example.miformacionctma

import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.model.ReglasActividad
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

class ReglasActividadTest {
    @Test
    fun promedioProgreso_calculaCorrectamente() {
        val actividades = listOf(
            ActividadFormativa(1, "A", null, 100, 0, Prioridad.ALTA),
            ActividadFormativa(2, "B", null, 50, 0, Prioridad.MEDIA)
        )

        val promedio = ReglasActividad.promedioProgreso(actividades)

        assertEquals(75.0, promedio, 0.01)
    }

    @Test
    fun actividadesUrgentes_filtraCorrectamente() {
        val actividades = listOf(
            ActividadFormativa(1, "Urgente", null, 20, 1, Prioridad.ALTA),
            ActividadFormativa(2, "Normal", null, 20, 5, Prioridad.MEDIA),
            ActividadFormativa(3, "Completada", null, 100, 1, Prioridad.BAJA)
        )

        val urgentes = ReglasActividad.actividadesUrgentes(actividades)

        assertEquals(1, urgentes.size)
        assertEquals("Urgente", urgentes.first().titulo)
    }

    @Test
    fun estadoActividad_devuelvePendiente() {
        val actividad = ActividadFormativa(
            1,
            "Pendiente",
            null,
            0,
            3,
            Prioridad.BAJA
        )

        val estado = ReglasActividad.estadoActividad(actividad)

        assertEquals("PENDIENTE", estado)
    }

    @Test
    fun validarTitulo_devuelve() {
        val actividad = ActividadFormativa(
            1,
            "Pendiente",
            null,
            0,
            3,
            Prioridad.BAJA
        )

        val estado = ReglasActividad.estadoActividad(actividad)

        assertEquals("PENDIENTE", estado)
    }

    @Test
    fun validarTitulo_evaluaLimitesCorrectamente() {
        val errorVacio = ReglasActividad.validarTitulo("", mostrarVacio = true)
        assertEquals("Escribe un título", errorVacio)

        val errorCorto = ReglasActividad.validarTitulo("AB")
        assertEquals("Usa al menos 3 caracteres", errorCorto)

        val valido = ReglasActividad.validarTitulo("ABC")
        assertNull(valido)

        val textoLargo = "A".repeat(81)
        val errorLargo = ReglasActividad.validarTitulo(textoLargo)
        assertEquals("Usa máximo 80 caracteres", errorLargo)

        val errorEspacios = ReglasActividad.validarTitulo("   ", mostrarVacio = true)
        assertEquals("Escribe un título", errorEspacios)
    }
}