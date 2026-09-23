package com.example.miformacionctma

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.miformacionctma.data.local.database.AppDatabase
import com.example.miformacionctma.data.remote.NetworkModule
import com.example.miformacionctma.data.repository.EvidenciaRepository
import com.example.miformacionctma.data.repository.OfflineFirstActividadRepository
import com.example.miformacionctma.ui.actividades.ActividadViewModel
import com.example.miformacionctma.ui.actividades.ActividadViewModelFactory
import com.example.miformacionctma.ui.navigation.MiFormacionAppNav
import com.example.miformacionctma.ui.theme.MiFormacionCTMATheme
import java.io.File

class MainActivity : ComponentActivity() {

    // Instancia perezosa y segura de la base de datos Room
    private val database by lazy { AppDatabase.getDatabase(this) }

    // Repositorio offline-first que coordina API remota y persistencia local (Room)
    private val repository by lazy {
        val supabaseService = NetworkModule.createSupabaseApiService()

        val evidenciaRepo = EvidenciaRepository(
            evidenciaDao = database.evidenciaDao(),
            actividadDao = database.actividadDao(),
            supabaseApiService = supabaseService,
            // Almacenamiento interno: las fotos no se borran al cerrar la app
            directorioEvidencias = File(filesDir, "evidencias")
        )

        OfflineFirstActividadRepository(
            supabaseApiService = supabaseService,
            dao = database.actividadDao(),
            evidenciaRepository = evidenciaRepo
        )
    }

    // ViewModel inyectado mediante su Factory
    private val actividadViewModel: ActividadViewModel by viewModels {
        ActividadViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilita diseño de pantalla completa (edge-to-edge)
        enableEdgeToEdge()

        setContent {
            MiFormacionCTMATheme {
                // Entrada principal de navegación pasando el ViewModel singleton de la actividad
                MiFormacionAppNav(viewModel = actividadViewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Cada vez que la app vuelve a primer plano se sincronizan actividades y fotos con Supabase
        actividadViewModel.sincronizarConServidor()
    }
}