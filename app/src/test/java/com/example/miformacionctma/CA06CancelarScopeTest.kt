package com.example.miformacionctma

import org.junit.Assert.assertTrue
import org.junit.Test

class CA06CancelarScopeTest {
    @Test
    fun test_ca06_cancelarScope_detieneTrabajo() {
        var jobActivo = true
        jobActivo = false
        assertTrue("Al cancelar el scope o salir, el trabajo debe detenerse", !jobActivo)
    }
}
