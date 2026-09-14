package com.example.miformacionctma.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.miformacionctma.MainActivity
import org.junit.Rule
import org.junit.Test

class NavegacionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun navegarACrearActividad_yVolver() {
        // CP-01 / HU-04: Ir a pantalla de creación
        // Intentar buscar el botón FAB con "+" o el botón de "Crear actividad" si está vacío
        val nodoCrear = try {
            composeTestRule.onNodeWithText("+")
        } catch (e: Exception) {
            composeTestRule.onNodeWithText("Crear actividad")
        }

        nodoCrear.performClick()

        // Verificar que estamos en la pantalla de formulario
        composeTestRule.onNodeWithText("Nueva Actividad").assertIsDisplayed()
        composeTestRule.onNodeWithText("Título").assertIsDisplayed()

        // HU-07: Probar el botón Volver
        composeTestRule.onNodeWithText("Volver").performClick()

        // Verificar que regresamos a la lista
        composeTestRule.onNodeWithText("Actividades Formativas").assertIsDisplayed()
    }
}
