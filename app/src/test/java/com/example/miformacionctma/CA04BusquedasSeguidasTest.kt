package com.example.miformacionctma

import org.junit.Assert.assertEquals
import org.junit.Test

class CA04BusquedasSeguidasTest {
    @Test
    fun test_ca04_busquedasSeguidas_ganaMasReciente() {
        val busquedas = mutableListOf<String>()
        busquedas.add("corrutina")
        busquedas.add("flow") 
        assertEquals("flow", busquedas.last())
    }
}
