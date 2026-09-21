package com.example.miformacionctma

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.miformacionctma.BuildConfig
import com.example.miformacionctma.data.local.database.AppDatabase
import com.example.miformacionctma.data.remote.NetworkModule
import com.example.miformacionctma.data.repository.OfflineFirstActividadRepository
import com.example.miformacionctma.ui.actividades.ActividadViewModel
import com.example.miformacionctma.ui.actividades.ActividadViewModelFactory
import com.example.miformacionctma.ui.navigation.MiFormacionAppNav
import com.example.miformacionctma.ui.theme.MiFormacionCTMATheme

class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }

    private val repository by lazy {
        OfflineFirstActividadRepository(
            api = NetworkModule.createActividadesApi(BuildConfig.BASE_URL),
            dao = database.actividadDao(),
            evidenciaDao = database.evidenciaDao()
        )
    }

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