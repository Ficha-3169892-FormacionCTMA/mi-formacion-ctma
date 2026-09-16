package com.example.miformacionctma

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.miformacionctma.data.local.database.AppDatabase
import com.example.miformacionctma.data.repository.ActividadRepository
import com.example.miformacionctma.ui.navigation.MiFormacionAppNav
import com.example.miformacionctma.ui.theme.MiFormacionCTMATheme
import com.example.miformacionctma.ui.actividades.ActividadViewModel
import com.example.miformacionctma.ui.actividades.ActividadViewModelFactory

class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { ActividadRepository(database.actividadDao()) }

    private val actividadViewModel: ActividadViewModel by viewModels {
        ActividadViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MiFormacionCTMATheme {
                MiFormacionAppNav(viewModel = actividadViewModel)
            }
        }
    }
}