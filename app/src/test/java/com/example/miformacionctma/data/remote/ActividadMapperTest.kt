package com.example.miformacionctma.data.remote

import com.example.miformacionctma.data.local.ActividadEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class ActividadMapperTest {

    @Test
    fun `dto to entity mapping is correct`() {
        // Arrange
        val dto = ActividadDto(
            id = 1L,
            titulo = "Test DTO",
            descripcion = "Desc DTO",
            fecha = "2026-09-11",
            progreso = 50,
            diasRestantes = 10,
            prioridad = "ALTA"
        )

        // Act
        val entity = dto.toEntity()

        // Assert
        assertEquals(dto.id, entity.id)
        assertEquals(dto.titulo, entity.titulo)
        assertEquals(dto.descripcion, entity.descripcion)
        assertEquals(dto.fecha, entity.fecha)
        assertEquals(dto.progreso, entity.progreso)
        assertEquals(dto.diasRestantes, entity.diasRestantes)
        assertEquals(dto.prioridad, entity.prioridad)
    }

    @Test
    fun `entity to dto mapping is correct`() {
        // Arrange
        val entity = ActividadEntity(
            id = 2L,
            titulo = "Test Entity",
            descripcion = "Desc Entity",
            fecha = "2026-09-12",
            progreso = 80,
            diasRestantes = 5,
            prioridad = "MEDIA"
        )

        // Act
        val dto = entity.toDto()

        // Assert
        assertEquals(entity.id, dto.id)
        assertEquals(entity.titulo, dto.titulo)
        assertEquals(entity.descripcion, dto.descripcion)
        assertEquals(entity.fecha, dto.fecha)
        assertEquals(entity.progreso, dto.progreso)
        assertEquals(entity.diasRestantes, dto.diasRestantes)
        assertEquals(entity.prioridad, dto.prioridad)
    }
}
