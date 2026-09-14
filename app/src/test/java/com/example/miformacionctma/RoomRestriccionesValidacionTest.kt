package com.example.miformacionctma

import com.example.miformacionctma.data.local.entities.CompetenciaEntity
import com.example.miformacionctma.model.Prioridad
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomRestriccionesValidacionTest {

    @Test
    fun competenciaEntity_idPorDefecto_esCero() {
        val competencia = CompetenciaEntity(nombre = "Desarrollo de Software")
        assertEquals(0L, competencia.id)
        assertEquals("Desarrollo de Software", competencia.nombre)
    }

    @Test
    fun validacionRestriccion_errorMapeoEnum_lanzaExcepcion() {
        val errorLanzado = try {
            // Simula error de persistencia cuando un string no coincide con el Enum en el dominio
            Prioridad.valueOf("URGENTE")
            false
        } catch (e: IllegalArgumentException) {
            true
        }
        assertEquals(true, errorLanzado)
    }
}
