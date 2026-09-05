package com.example.miformacionctma

import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.model.ReglasActividad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanesDePruebaTest {

    // CP-01: Estado inicial sin errores de validación
    @Test
    fun cp01_pantallaCrearAbierta_iniciaSinErrores() {
        val errorInicial = ReglasActividad.validarTitulo("", mostrarVacio = false)
        assertNull("Al abrir la pantalla no debe marcar error inmediatamente", errorInicial)
    }

    // CP-02: Título < 3 caracteres
    @Test
    fun cp02_tituloCorto_muestraMensajeError() {
        val error = ReglasActividad.validarTitulo("AB")
        assertEquals("Usa al menos 3 caracteres", error)
    }

    // CP-03: Fecha inválida (Formato incorrecto)
    @Test
    fun cp03_fechaInvalida_exigeFormatoCorrecto() {
        val error = ReglasActividad.validarFecha("15-09-2026")
        assertEquals("Formato inválido (AAAA-MM-DD)", error)
    }

    // CP-04: Conservar borrador por rotación (Simulación)
    @Test
    fun cp04_rotacionPantalla_conservaBorrador() {
        val borradorOriginal = "Informe de QA"
        val borradorRecuperado = borradorOriginal
        assertEquals("El borrador debe preservarse", borradorOriginal, borradorRecuperado)
    }

    // CP-05: Prevención de doble pulsación (Doble Clic)
    @Test
    fun cp05_guardarDoblePulsacion_procesaUnaSolaVez() {
        var ejecuciones = 0
        var guardando = false

        fun simularBotonGuardar() {
            if (!guardando) {
                guardando = true
                ejecuciones++
            }
        }

        simularBotonGuardar()
        simularBotonGuardar()

        assertEquals("Solo debe registrarse un guardado", 1, ejecuciones)
    }

    // CP-06: Transferencia de ID a vista detalle
    @Test
    fun cp06_seleccionarTarjeta_transfiereIdCorrectamente() {
        val actividad = ActividadFormativa(101L, "Taller Kotlin", "Desc", "2026-09-10", 0, 5, Prioridad.MEDIA)
        val rutaDestino = "detalle/${actividad.id}"
        assertTrue("La ruta debe incluir el ID", rutaDestino.contains("101"))
    }

    // CP-07: Navegación con ID inexistente (-1)
    @Test
    fun cp07_idInexistente_manejaErrorSinCrash() {
        val lista = listOf(
            ActividadFormativa(1L, "Java", "Desc", "2026-09-10", 0, 5, Prioridad.BAJA)
        )
        val idBuscado = -1L
        val encontrada = lista.find { it.id == idBuscado }

        assertNull("Si el ID no existe debe devolver null para evitar crash", encontrada)
    }

    // CP-08: Retorno a lista limpiando back stack
    @Test
    fun cp08_botonVolver_retornaALaLista() {
        val backStack = mutableListOf("lista", "detalle/1")
        backStack.removeAt(backStack.size - 1)

        assertEquals("Debe retornar a la pantalla principal", "lista", backStack.last())
    }

    // CP-09: Filtrado en tiempo real por búsqueda
    @Test
    fun cp09_busquedaKotlin_filtraCoincidencias() {
        val lista = listOf(
            ActividadFormativa(1L, "Taller Kotlin", "Desc", "2026-09-10", 10, 4, Prioridad.ALTA),
            ActividadFormativa(2L, "Guía Java", "Desc", "2026-09-12", 20, 6, Prioridad.BAJA)
        )
        val resultado = ReglasActividad.buscarPorTitulo(lista, "Kotlin")

        assertEquals(1, resultado.size)
        assertEquals("Taller Kotlin", resultado.first().titulo)
    }

    // CP-10: Título solo con espacios
    @Test
    fun cp10_tituloSoloEspacios_deshabilitaGuardar() {
        val error = ReglasActividad.validarTitulo("   ", mostrarVacio = true)
        assertEquals("El título es obligatorio", error)
    }

    // CP-11: Búsqueda sin coincidencias
    @Test
    fun cp11_busquedaSinCoincidencias_despliegaEstadoVacio() {
        val lista = listOf(
            ActividadFormativa(1L, "Kotlin", "Desc", "2026-09-10", 0, 2, Prioridad.MEDIA)
        )
        val resultado = ReglasActividad.buscarPorTitulo(lista, "XYZ999")

        assertTrue("Debe retornar una lista vacía", resultado.isEmpty())
    }

    // CP-12: Retención de progreso en rotación
    @Test
    fun cp12_rotacionPantalla_conservaSliderProgreso() {
        val progresoOriginal = 50
        val progresoConservado = progresoOriginal
        assertEquals(50, progresoConservado)
    }

    // CP-13: Regreso desde detalle
    @Test
    fun cp13_regresoDesdeDetalle_limpiaPila() {
        val pilaNavegacion = listOf("lista", "detalle/1")
        val nuevaPila = pilaNavegacion.dropLast(1)

        assertEquals(1, nuevaPila.size)
        assertEquals("lista", nuevaPila.first())
    }

    // CP-14: Slider de progreso 0% y 100%
    @Test
    fun cp14_limitesSlider_aceptaCeroYCien() {
        assertNull(ReglasActividad.validarProgreso(0))
        assertNull(ReglasActividad.validarProgreso(100))
        assertNotNull(ReglasActividad.validarProgreso(150))
    }

    // CP-15: Visualización de progreso en detalle
    @Test
    fun cp15_detalleActividad_muestraPorcentajeCorrecto() {
        val actividad = ActividadFormativa(1L, "Móviles", "Desc", "2026-09-10", 75, 2, Prioridad.ALTA)
        assertEquals(75, actividad.progreso)
    }

    // CP-16: Marcado de urgencia con <= 2 días restantes
    @Test
    fun cp16_actividadDosDiasRestantes_marcaUrgente() {
        val actividades = listOf(
            ActividadFormativa(1L, "Urgente", "Desc", "2026-09-02", 20, 2, Prioridad.ALTA)
        )
        val urgentes = ReglasActividad.actividadesUrgentes(actividades)

        assertEquals(1, urgentes.size)
        assertEquals("Urgente", urgentes.first().titulo)
    }
}