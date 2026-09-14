package com.example.miformacionctma

import com.example.miformacionctma.data.local.entities.ActividadEntity
import com.example.miformacionctma.data.local.toDomain
import com.example.miformacionctma.data.local.toEntity
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomEntidadesMapeoTest {

    @Test
    fun entidad_a_dominio_mapeaCamposCorrectamente() {
        val entity = ActividadEntity(
            id = "ACT-100",
            titulo = "Laboratorio Room 3",
            descripcion = "Mapeo explícito",
            progreso = 80,
            prioridad = "ALTA",
            competenciaId = 5L,
            fechaLimiteEpochMillis = 1718239200000L,
            completada = true
        )

        val domain = entity.toDomain()

        assertEquals("ACT-100", domain.id)
        assertEquals("Laboratorio Room 3", domain.titulo)
        assertEquals("Mapeo explícito", domain.descripcion)
        assertEquals(80, domain.progreso)
        assertEquals(Prioridad.ALTA, domain.prioridad)
        assertEquals(5L, domain.competenciaId)
        assertEquals(1718239200000L, domain.fechaLimiteEpochMillis)
        assertEquals(true, domain.completada)
    }

    @Test
    fun dominio_a_entidad_preservaEstructura() {
        val domain = ActividadFormativa(
            id = "ACT-200",
            titulo = "DataStore Preferencias",
            descripcion = "Persistencia liviana",
            fecha = "2026-09-14",
            progreso = 100,
            diasRestantes = 0,
            prioridad = Prioridad.MEDIA,
            competenciaId = 2L,
            fechaLimiteEpochMillis = 0L,
            completada = true
        )

        val entity = domain.toEntity()

        assertEquals("ACT-200", entity.id)
        assertEquals("DataStore Preferencias", entity.titulo)
        assertEquals("Persistencia liviana", entity.descripcion)
        assertEquals(100, entity.progreso)
        assertEquals("MEDIA", entity.prioridad)
        assertEquals(2L, entity.competenciaId)
        assertEquals(true, entity.completada)
    }
}
