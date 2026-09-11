package com.example.miformacionctma

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.miformacionctma.data.local.FormacionDatabase
import com.example.miformacionctma.data.repository.ActividadRepository
import com.example.miformacionctma.ui.navigation.MiFormacionAppNav
import com.example.miformacionctma.ui.state.ActividadViewModelFactory
import com.example.miformacionctma.ui.theme.MiFormacionCTMATheme

class MainActivity : ComponentActivity() {

    // Inicialización perezosa de la base de datos y el repositorio
    private val database by lazy { FormacionDatabase.obtenerBaseDatos(applicationContext) }
    private val repository by lazy { ActividadRepository(database.actividadDao()) }
    private val viewModelFactory by lazy { ActividadViewModelFactory(repository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MiFormacionCTMATheme {
                // Pasamos la factory al NavHost/AppNav
                MiFormacionAppNav(factory = viewModelFactory)
            }
        }
    }
}