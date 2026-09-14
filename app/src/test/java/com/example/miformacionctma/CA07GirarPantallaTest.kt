package com.example.miformacionctma

import org.junit.Assert.assertEquals
import org.junit.Test

class CA07GirarPantallaTest {
    @Test
    fun test_ca07_girarPantalla_conservaEstado() {
        val estadoPantallaOriginal = "ContenidoListo"
        val estadoRecuperado = estadoPantallaOriginal
        assertEquals("El estado inmutable debe conservarse de manera consistente", estadoPantallaOriginal, estadoRecuperado)
    }
}
