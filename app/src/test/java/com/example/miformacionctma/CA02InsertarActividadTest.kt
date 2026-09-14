package com.example.miformacionctma

import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import org.junit.Assert.assertEquals
import org.junit.Test

class CA02InsertarActividadTest {
    @Test
    fun test_ca02_insertarActividad_actualizaLista() {
        val lista = mutableListOf<ActividadFormativa>()
        val nueva = ActividadFormativa("ACT-01", "Corrutinas", "Estudio", "2026-09-14", 10, 5, Prioridad.ALTA)
        lista.add(nueva)
        assertEquals(1, lista.size)
        assertEquals("ACT-01", lista.first().id)
    }
}
