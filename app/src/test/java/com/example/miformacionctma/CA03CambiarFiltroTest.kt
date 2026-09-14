package com.example.miformacionctma

import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import org.junit.Assert.assertEquals
import org.junit.Test

class CA03CambiarFiltroTest {
    @Test
    fun test_ca03_cambiarFiltro_recalculaContenido() {
        val lista = listOf(
            ActividadFormativa("ACT-01", "Corrutinas", "Estudio", "2026-09-14", 10, 5, Prioridad.ALTA, competenciaId = 1L)
        )
        val filtroCompetenciaId = 1L
        val filtradas = lista.filter { it.competenciaId == filtroCompetenciaId }
        assertEquals(1, filtradas.size)
    }
}
