package com.example.miformacionctma.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.miformacionctma.data.local.entity.EvidenciaEntity
import com.example.miformacionctma.data.repository.ActividadRepository
import com.example.miformacionctma.data.repository.AuthRepository
import com.example.miformacionctma.data.repository.PreferenciasRepository
import com.example.miformacionctma.data.util.Result
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Competencia
import com.example.miformacionctma.model.ReglasActividad
import com.example.miformacionctma.model.RolUsuario
import com.example.miformacionctma.model.Usuario
import com.example.miformacionctma.ui.state.ListadoUiState
import com.example.miformacionctma.ui.state.OperacionUiState
import com.example.miformacionctma.ui.state.RefreshUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ActividadesViewModel(
    private val repository: ActividadRepository,
    private val preferenciasRepository: PreferenciasRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val usuarioSesion: StateFlow<Usuario?> = authRepository.usuarioActual
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    private val _errorLogin = MutableStateFlow<String?>(null)
    val errorLogin: StateFlow<String?> = _errorLogin.asStateFlow()

    fun limpiarErrorLogin() {
        _errorLogin.value = null
    }

    fun iniciarSesion(email: String, password: String, nombre: String, rol: RolUsuario) {
        viewModelScope.launch {
            val resultado = authRepository.iniciarSesion(email, password, nombre, rol)
            if (resultado.isFailure) {
                _errorLogin.value = resultado.exceptionOrNull()?.message ?: "Error al iniciar sesión. Comprueba tus datos."
            } else {
                _errorLogin.value = null
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            authRepository.cerrarSesion()
        }
    }

    private val _textoBusqueda = MutableStateFlow("")
    val textoBusqueda: StateFlow<String> = _textoBusqueda.asStateFlow()

    private val _operacionUiState = MutableStateFlow<OperacionUiState>(OperacionUiState.Inactiva)
    val operacionUiState: StateFlow<OperacionUiState> = _operacionUiState.asStateFlow()

    private val _syncState = MutableStateFlow<RefreshUiState>(RefreshUiState.Idle)
    val syncState: StateFlow<RefreshUiState> = _syncState.asStateFlow()

    fun actualizarBusqueda(texto: String) {
        _textoBusqueda.value = texto
    }

    fun reiniciarOperacion() {
        _operacionUiState.value = OperacionUiState.Inactiva
    }

    val competencias: StateFlow<List<Competencia>> =
        repository.observarCompetencias()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val ordenarPorPrioridad: StateFlow<Boolean> =
        preferenciasRepository.ordenarPorPrioridad
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false
            )

    val recordatoriosActivados: StateFlow<Boolean> =
        preferenciasRepository.recordatoriosActivados
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false
            )

    val uiState: StateFlow<ListadoUiState> = _textoBusqueda
        .flatMapLatest { texto ->
            if (texto.isBlank()) {
                repository.observarActividades()
            } else {
                repository.observarPorTitulo(texto)
            }
        }
        .combine(ordenarPorPrioridad) { lista, porPrioridad ->
            if (porPrioridad) {
                ReglasActividad.ordenarActividades(lista)
            } else {
                lista
            }
        }
        .map { lista ->
            if (lista.isEmpty()) {
                ListadoUiState.Vacio
            } else {
                ListadoUiState.Contenido(lista)
            }
        }
        .catch { e ->
            if (e is CancellationException) throw e
            emit(ListadoUiState.Error("Error al cargar las actividades: ${e.message}"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ListadoUiState.Cargando
        )

    fun insertar(actividad: ActividadFormativa) {
        viewModelScope.launch {
            _operacionUiState.value = OperacionUiState.EnCurso
            try {
                repository.insertar(actividad)
                _operacionUiState.value = OperacionUiState.Exitosa
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _operacionUiState.value = OperacionUiState.Fallida("Error al insertar: ${e.message}")
            }
        }
    }

    fun actualizar(actividad: ActividadFormativa) {
        viewModelScope.launch {
            _operacionUiState.value = OperacionUiState.EnCurso
            try {
                repository.actualizar(actividad)
                _operacionUiState.value = OperacionUiState.Exitosa
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _operacionUiState.value = OperacionUiState.Fallida("Error al actualizar: ${e.message}")
            }
        }
    }

    fun eliminar(actividad: ActividadFormativa) {
        viewModelScope.launch {
            _operacionUiState.value = OperacionUiState.EnCurso
            try {
                repository.eliminar(actividad)
                _operacionUiState.value = OperacionUiState.Exitosa
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _operacionUiState.value = OperacionUiState.Fallida("Error al eliminar: ${e.message}")
            }
        }
    }

    suspend fun obtenerPorId(id: Long): ActividadFormativa? {
        return repository.obtenerPorId(id)
    }

    fun guardarOrdenPorPrioridad(valor: Boolean) {
        viewModelScope.launch {
            preferenciasRepository.guardarOrdenPorPrioridad(valor)
        }
    }

    fun guardarRecordatoriosActivados(valor: Boolean) {
        viewModelScope.launch {
            preferenciasRepository.guardarRecordatoriosActivados(valor)
        }
    }

    suspend fun obtenerConCompetencia(
        id: Long
    ): Pair<ActividadFormativa, String?>? {
        return repository.obtenerConCompetencia(id)
    }

    fun observarEvidencias(actividadId: Long, usuarioId: String): Flow<List<EvidenciaEntity>> {
        return repository.observarEvidencias(actividadId, usuarioId)
    }

    fun observarEvidenciasSegunRol(
        actividadId: Long,
        usuarioId: String,
        esInstructor: Boolean
    ): Flow<List<EvidenciaEntity>> {
        return if (esInstructor) {
            repository.observarEvidenciasInstructor(actividadId)
        } else {
            repository.observarEvidencias(actividadId, usuarioId)
        }
    }

    fun guardarEvidenciaYSubir(
        context: Context,
        actividadId: Long,
        localUri: Uri,
        mimeType: String,
        tamanoBytes: Long,
        usuarioId: String
    ) {
        viewModelScope.launch {
            try {
                val evidencia = repository.guardarEvidenciaLocal(
                    actividadId = actividadId,
                    localUri = localUri.toString(),
                    mimeType = mimeType,
                    tamano = tamanoBytes,
                    usuarioId = usuarioId
                )
                repository.subirEvidencia(context, evidencia.id)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
    }

    fun reintentarSubirEvidencia(context: Context, evidenciaId: Long) {
        viewModelScope.launch {
            repository.subirEvidencia(context, evidenciaId)
        }
    }

    fun eliminarEvidencia(context: Context, evidenciaId: Long) {
        viewModelScope.launch {
            repository.eliminarEvidencia(context, evidenciaId)
        }
    }

    /**
     * Sincroniza las actividades desde el servidor.
     */
    fun refreshActividades() {
        viewModelScope.launch {
            _syncState.value = RefreshUiState.Running
            val result = repository.refresh()
            _syncState.value = when (result) {
                is Result.Success -> RefreshUiState.Success
                is Result.Error -> RefreshUiState.Failed(result.error)
            }
        }
    }

    init {
        viewModelScope.launch {
            try {
                repository.inicializarDatos()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
        refreshActividades()
    }
}

class ActividadesViewModelFactory(
    private val repository: ActividadRepository,
    private val preferenciasRepository: PreferenciasRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActividadesViewModel::class.java)) {
            return ActividadesViewModel(
                repository,
                preferenciasRepository,
                authRepository
            ) as T
        }

        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
