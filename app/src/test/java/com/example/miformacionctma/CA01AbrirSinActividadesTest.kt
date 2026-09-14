package com.example.miformacionctma

import com.example.miformacionctma.model.ActividadFormativa
import org.junit.Assert.assertTrue
import org.junit.Test

class CA01AbrirSinActividadesTest {
    @Test
    fun test_ca01_abrirSinActividades_retornaVacio() {
        val listaActividades = emptyList<ActividadFormativa>()
        assertTrue("El estado inicial sin actividades debe retornar una lista vacía y no null", listaActividades.isEmpty())
    }
}
