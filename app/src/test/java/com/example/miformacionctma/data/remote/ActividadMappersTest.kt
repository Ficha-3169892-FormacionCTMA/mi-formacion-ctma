package com.example.miformacionctma.data.remote

import com.example.miformacionctma.model.Prioridad
import org.junit.Assert.assertEquals
import org.junit.Test

class ActividadMappersTest {

    @Test
    fun toDomain_mapeaTodosLosCamposCorrectamente() {
        // Arrange
        val dto = ActividadDto(
            id = 1L,
            titulo = "Test Actividad",
            descripcion = "Una descripción de prueba",
            fecha = "2026-09-20",
            progreso = 45,
            diasRestantes = 5,
            prioridad = "ALTA"
        )

        // Act
        val domain = dto.toDomain()

        // Assert
        assertEquals(dto.id, domain.id)
        assertEquals(dto.titulo, domain.titulo)
        assertEquals(dto.descripcion, domain.descripcion)
        assertEquals(dto.fecha, domain.fecha)
        assertEquals(dto.progreso, domain.progreso)
        assertEquals(dto.diasRestantes, domain.diasRestantes)
        assertEquals(Prioridad.ALTA, domain.prioridad)
    }

    @Test
    fun toDomain_manejaDescripcionNula_convirtiendolaAStringVacio() {
        // Arrange
        val dto = ActividadDto(
            id = 2L,
            titulo = "Test Sin Desc",
            descripcion = null,
            fecha = "2026-09-21",
            progreso = 0,
            diasRestantes = 10,
            prioridad = "BAJA"
        )

        // Act
        val domain = dto.toDomain()

        // Assert
        assertEquals("", domain.descripcion)
        assertEquals(Prioridad.BAJA, domain.prioridad)
    }
}
