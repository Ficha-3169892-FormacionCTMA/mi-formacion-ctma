package com.example.miformacionctma.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.miformacionctma.data.repository.EvidenciaRepository
import com.example.miformacionctma.model.Evidencia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed interface EvidenciaUiState {
    object Cargando : EvidenciaUiState
    data class Contenido(val evidencias: List<Evidencia>) : EvidenciaUiState
    data class Error(val mensaje: String) : EvidenciaUiState
}

class EvidenciaViewModel(
    private val repository: EvidenciaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EvidenciaUiState>(EvidenciaUiState.Cargando)
    val uiState: StateFlow<EvidenciaUiState> = _uiState.asStateFlow()

    private val _errorEvent = MutableStateFlow<String?>(null)
    val errorEvent = _errorEvent.asStateFlow()

    fun cargarEvidencias(actividadId: Long, idsRelacionados: List<Long> = emptyList()) {
        viewModelScope.launch {
            val listaIds = (idsRelacionados + actividadId).distinct()
            listaIds.forEach { id ->
                try {
                    repository.refrescarDesdeServidor(id)
                } catch (e: Exception) {
                    // Silenciar
                }
            }

            // Unir todos los flujos de evidencias locales de las actividades relacionadas
            // O una solución más limpia: usar una lista mutable recolectando de todos.
            // Para mantenerlo reactivo y simple, podemos observar combinando o recolectando secuencialmente, 
            // pero dado que es un flujo, podemos recolectar las evidencias de todos los idsRelacionados.
            // O simplemente hacer un collect de cada uno o consultar el repositorio.
            // Vamos a recolectar de todas las actividades asignadas al mismo grupo/tarea.
            combineAndEmitEvidencias(listaIds)
        }
    }

    private fun combineAndEmitEvidencias(listaIds: List<Long>) {
        viewModelScope.launch {
            val flujos = listaIds.map { repository.observarEvidencias(it) }
            kotlinx.coroutines.flow.combine(flujos) { arrays ->
                arrays.flatMap { it }
            }.catch { e ->
                _uiState.value = EvidenciaUiState.Error(e.message ?: "Error desconocido")
            }.collect { evidencias ->
                _uiState.value = EvidenciaUiState.Contenido(evidencias)
            }
        }
    }

    fun guardarEvidencia(actividadId: Long, uri: Uri, finalidad: String?) {
        viewModelScope.launch {
            try {
                repository.guardarEvidenciaLocal(actividadId, uri, finalidad)
                // Después de guardar local, podríamos intentar sincronizar automáticamente
                // o dejar que el usuario lo haga manualmente. El prompt dice:
                // "seleccionar/capturar -> validar -> guardar localmente -> previsualizar -> confirmar -> sincronizar"
            } catch (e: Exception) {
                _errorEvent.value = e.message
            }
        }
    }

    fun sincronizar(evidenciaId: Long) {
        viewModelScope.launch {
            try {
                repository.sincronizarEvidencia(evidenciaId)
            } catch (e: Exception) {
                _errorEvent.value = "Error al sincronizar: ${e.message}"
            }
        }
    }

    fun eliminar(evidenciaId: Long) {
        viewModelScope.launch {
            try {
                repository.eliminarEvidencia(evidenciaId)
            } catch (e: Exception) {
                _errorEvent.value = "Error al eliminar: ${e.message}"
            }
        }
    }

    fun limpiarError() {
        _errorEvent.value = null
    }
}

class EvidenciaViewModelFactory(
    private val repository: EvidenciaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EvidenciaViewModel(repository) as T
    }
}
