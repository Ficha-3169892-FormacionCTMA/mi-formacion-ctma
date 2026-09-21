package com.example.miformacionctma

import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.model.ReglasActividad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    @Test
    fun validarDescripcion_limitaCaracteresCorrectamente() {
        val descripcionCorta = "Una descripción normal"
        assertNull(ReglasActividad.validarDescripcion(descripcionCorta))

        val descripcionMuyLarga = "A".repeat(241)
        assertEquals("Máximo 240 caracteres", ReglasActividad.validarDescripcion(descripcionMuyLarga))
    }

    @Test
    fun ordenarActividades_prioridadYVencimiento() {
        val a1 = ActividadFormativa(1, "Baja", "", "2026-09-10", 0, 5, Prioridad.BAJA)
        val a2 = ActividadFormativa(2, "Alta", "", "2026-09-10", 0, 5, Prioridad.ALTA)
        val a3 = ActividadFormativa(3, "Vencida", "", "2026-08-01", 10, -5, Prioridad.MEDIA)

        val lista = listOf(a1, a2, a3)
        val ordenada = ReglasActividad.ordenarActividades(lista)

        // El orden esperado según ReglasActividad.kt es:
        // 1. Vencidas (estado != "Vencida" is false, so it comes first in ascending sort)
        // 2. Prioridad alta (-prioridad.ordinal)
        // 3. Menos días restantes
        
        assertEquals("Vencida", ordenada[0].titulo)
        assertEquals("Alta", ordenada[1].titulo)
        assertEquals("Baja", ordenada[2].titulo)
    }

    @Test
    fun validarActividad_objetoCompleto() {
        val valida = ActividadFormativa(1, "Válida", "Desc", "2026-09-15", 50, 2, Prioridad.MEDIA)
        assertTrue(ReglasActividad.validarActividad(valida).isEmpty())

        val invalida = ActividadFormativa(2, "X", "Desc", "fecha-mal", 150, 0, Prioridad.BAJA)
        val errores = ReglasActividad.validarActividad(invalida)
        
        assertTrue(errores.size >= 3) // Título corto, fecha mal, progreso mal
    }

    @Test
    fun buscarPorTitulo_esInsensibleAMayusculasYEspacios() {
        val lista = listOf(
            ActividadFormativa(1, "KOTLIN Básico", "", "", 0, 0, Prioridad.MEDIA),
            ActividadFormativa(2, "Java avanzado", "", "", 0, 0, Prioridad.MEDIA)
        )
        
        val resultado = ReglasActividad.buscarPorTitulo(lista, "  kotlin  ")
        assertEquals(1, resultado.size)
        assertEquals("KOTLIN Básico", resultado[0].titulo)
    }

    @Test
    fun estadoActividad_cambiaSegunProgreso() {
        val activa = ActividadFormativa(1, "P", "", "", 50, 1, Prioridad.MEDIA)
        assertEquals("En proceso", ReglasActividad.estadoActividad(activa))

        val completada = ActividadFormativa(2, "C", "", "", 100, 1, Prioridad.MEDIA)
        assertEquals("Completada", ReglasActividad.estadoActividad(completada))
    }
}
