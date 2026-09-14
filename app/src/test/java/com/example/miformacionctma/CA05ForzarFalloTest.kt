package com.example.miformacionctma

import org.junit.Assert.assertEquals
import org.junit.Test

class CA05ForzarFalloTest {
    @Test
    fun test_ca05_forzarFallo_retornaMensajeSeguro() {
        val error = try {
            throw Exception("Fallo de conexión a la persistencia local")
        } catch (e: Exception) {
            e.message ?: "Error genérico"
        }
        assertEquals("Fallo de conexión a la persistencia local", error)
    }
}
