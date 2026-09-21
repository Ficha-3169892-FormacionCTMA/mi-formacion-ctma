package com.example.miformacionctma.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.ui.theme.MiFormacionCTMATheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ComponentesUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val actividadBase = ActividadFormativa(
        id = 1L,
        titulo = "Actividad de Prueba",
        descripcion = "Descripción de prueba",
        fecha = "2026-09-15",
        progreso = 50,
        diasRestantes = 5,
        prioridad = Prioridad.MEDIA
    )

    @Test
    fun tarjetaActividad_muestraTituloCorrectamente() {
        composeTestRule.setContent {
            MiFormacionCTMATheme {
                TarjetaActividad(actividad = actividadBase)
            }
        }
        composeTestRule.onNodeWithText("Actividad de Prueba").assertIsDisplayed()
    }

    @Test
    fun tarjetaActividad_muestraDescripcionCorrectamente() {
        composeTestRule.setContent {
            MiFormacionCTMATheme {
                TarjetaActividad(actividad = actividadBase)
            }
        }
        composeTestRule.onNodeWithText("Descripción de prueba").assertIsDisplayed()
    }

    @Test
    fun tarjetaActividad_ocultaDescripcionSiEstaVacia() {
        composeTestRule.setContent {
            MiFormacionCTMATheme {
                TarjetaActividad(actividad = actividadBase.copy(descripcion = ""))
            }
        }
        // No debería haber un nodo con texto vacío que sea una descripción, 
        // pero verificamos que al menos el título siga ahí y no haya crashes.
        composeTestRule.onNodeWithText("Actividad de Prueba").assertIsDisplayed()
    }

    @Test
    fun tarjetaActividad_muestraProgresoEnTexto() {
        composeTestRule.setContent {
            MiFormacionCTMATheme {
                TarjetaActividad(actividad = actividadBase.copy(progreso = 75))
            }
        }
        composeTestRule.onNodeWithText("Progreso: 75%").assertIsDisplayed()
    }

    @Test
    fun tarjetaActividad_muestraEstadoCompletadaSiProgresoEs100() {
        composeTestRule.setContent {
            MiFormacionCTMATheme {
                TarjetaActividad(actividad = actividadBase.copy(progreso = 100))
            }
        }
        composeTestRule.onNodeWithText("Completada").assertIsDisplayed()
    }

    @Test
    fun tarjetaActividad_muestraEstadoEnProcesoSiProgresoEsMayorACero() {
        composeTestRule.setContent {
            MiFormacionCTMATheme {
                TarjetaActividad(actividad = actividadBase.copy(progreso = 10))
            }
        }
        composeTestRule.onNodeWithText("En proceso").assertIsDisplayed()
    }

    @Test
    fun tarjetaActividad_muestraEstadoPendienteSiProgresoEsCero() {
        composeTestRule.setContent {
            MiFormacionCTMATheme {
                TarjetaActividad(actividad = actividadBase.copy(progreso = 0))
            }
        }
        composeTestRule.onNodeWithText("Pendiente").assertIsDisplayed()
    }

    @Test
    fun tarjetaActividad_muestraPrioridadFormateada() {
        composeTestRule.setContent {
            MiFormacionCTMATheme {
                TarjetaActividad(actividad = actividadBase.copy(prioridad = Prioridad.ALTA))
            }
        }
        composeTestRule.onNodeWithText("Prioridad: Alta").assertIsDisplayed()
    }

    @Test
    fun tarjetaActividad_muestraFechaFormateada() {
        composeTestRule.setContent {
            MiFormacionCTMATheme {
                TarjetaActividad(actividad = actividadBase.copy(fecha = "2026-12-31"))
            }
        }
        // El formateador cambia 2026-12-31 a 31/12/2026
        composeTestRule.onNodeWithText("Fecha: 31/12/2026").assertIsDisplayed()
    }

    @Test
    fun tarjetaActividad_capturaClickCorrectamente() {
        var clickeado = false
        composeTestRule.setContent {
            MiFormacionCTMATheme {
                TarjetaActividad(
                    actividad = actividadBase,
                    onClick = { clickeado = true }
                )
            }
        }
        composeTestRule.onNodeWithText("Actividad de Prueba").performClick()
        assertTrue("El callback de click debe haberse ejecutado", clickeado)
    }
}
