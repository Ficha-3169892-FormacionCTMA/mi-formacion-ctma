package com.example.miformacionctma.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miformacionctma.data.local.dao.CompetenciaDao
import com.example.miformacionctma.data.local.entities.CompetenciaEntity
import com.example.miformacionctma.data.repository.PreferenciasRepository
import com.example.miformacionctma.domain.repository.ActividadRepository
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.ui.state.FormularioActividadUiState
import com.example.miformacionctma.ui.state.ListadoUiState
import com.example.miformacionctma.ui.state.OperacionUiState
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class EvidenciaSupabaseDto(
    val id: String,
    val actividad_id: String,
    val aprendiz_id: String,
    val foto_url: String,
    val estado: String = "pendiente"
)

@Serializable
data class ProfileDto(
    val id: String,
    val nombre: String,
    val rol: String
)

@Serializable
data class ActividadSupabaseDto(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val fecha: String,
    val progreso: Int,
    val prioridad: String,
    val competencia_id: Long,
    val instructor_id: String,
    val aprendiz_id: String? = null
)

class ActividadesViewModel(
    private val repository: ActividadRepository,
    private val preferenciasRepository: PreferenciasRepository,
    private val competenciaDao: CompetenciaDao
) : ViewModel() {

    private val supabase = createSupabaseClient(
        supabaseUrl = "https://vdivunauypivdejqkkcn.supabase.co",
        supabaseKey = "sb_publishable_Omiradihv8GXIQJihbYx9w_JfKhtCd0"
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }

    private val _esInstructor = MutableStateFlow(true)
    val esInstructor: StateFlow<Boolean> = _esInstructor.asStateFlow()

    private val _aprendices = MutableStateFlow<List<ProfileDto>>(emptyList())
    val aprendices: StateFlow<List<ProfileDto>> = _aprendices.asStateFlow()

    private val _textoBusqueda = MutableStateFlow("")
    val textoBusqueda = _textoBusqueda.asStateFlow()

    private val _operacion = MutableStateFlow<OperacionUiState>(OperacionUiState.Inactiva)
    val operacion: StateFlow<OperacionUiState> = _operacion.asStateFlow()

    private val _formularioState = MutableStateFlow(FormularioActividadUiState())
    val formularioState: StateFlow<FormularioActividadUiState> = _formularioState.asStateFlow()

    val competencias: StateFlow<List<CompetenciaEntity>> = competenciaDao.observarConActividades()
        .map { listaConActividades -> listaConActividades.map { it.competencia } }
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ListadoUiState> = combine(
        _textoBusqueda
            .map { it.trim() }
            .distinctUntilChanged()
            .flatMapLatest { texto ->
                if (texto.isEmpty()) repository.observarTodos()
                else repository.buscar(texto)
            },
        preferenciasRepository.preferencias,
        _esInstructor
    ) { actividades, pref, esInstructor ->

        val currentUser = supabase.auth.currentUserOrNull()
        val currentUserId = currentUser?.id

        // 👈 Filtrado inteligente según el rol del usuario logueado
        val filtradasPorRol = if (esInstructor) {
            actividades
        } else {
            actividades.filter { it.aprendizId == currentUserId }
        }

        val filtradas = filtradasPorRol.filter {
            pref.filtroCompetencia == null || it.competenciaId.toString() == pref.filtroCompetencia
        }

        if (filtradas.isEmpty()) {
            ListadoUiState.Vacio
        } else {
            val ordenadas = if (pref.ordenDescendente) {
                filtradas.sortedByDescending { prv -> prv.titulo }
            } else {
                filtradas.sortedBy { prv -> prv.titulo }
            }
            ListadoUiState.Contenido(ordenadas)
        }
    }
        .catch { error ->
            if (error is CancellationException) throw error
            emit(ListadoUiState.Error(error.message ?: "Error desconocido de persistencia"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ListadoUiState.Cargando
        )

    init {
        viewModelScope.launch {
            verificarRolUsuario()
        }
    }

    fun iniciarSesion(email: String, pass: String, onResultado: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                supabase.auth.signInWith(io.github.jan.supabase.auth.providers.builtin.Email) {
                    this.email = email
                    this.password = pass
                }
                verificarRolUsuario()
                onResultado(true, null)
            } catch (e: Exception) {
                onResultado(false, e.localizedMessage ?: "Error al iniciar sesión")
            }
        }
    }

    fun cerrarSesion(onCerrar: () -> Unit) {
        viewModelScope.launch {
            try {
                supabase.auth.signOut()
                onCerrar()
            } catch (e: Exception) {
                onCerrar()
            }
        }
    }

    private fun verificarRolUsuario() {
        viewModelScope.launch {
            try {
                val currentUser = supabase.auth.currentUserOrNull()
                if (currentUser != null) {
                    val profile = supabase.from("profiles")
                        .select {
                            filter { eq("id", currentUser.id) }
                        }
                        .decodeSingle<ProfileDto>()

                    _esInstructor.value = (profile.rol == "instructor")
                    if (profile.rol == "instructor") {
                        cargarAprendices()
                    }

                    // 👈 Sincronizamos las actividades de Supabase al verificar la sesión
                    sincronizarActividadesRemotas()
                }
            } catch (e: Exception) {
                // Fallback seguro
            }
        }
    }

    private fun sincronizarActividadesRemotas() {
        viewModelScope.launch {
            try {
                val listaRemota = supabase.from("actividades")
                    .select()
                    .decodeList<ActividadSupabaseDto>()

                for (dto in listaRemota) {
                    val actividadFormativa = ActividadFormativa(
                        id = dto.id,
                        titulo = dto.titulo,
                        descripcion = dto.descripcion,
                        fecha = dto.fecha,
                        progreso = dto.progreso,
                        prioridad = try { Prioridad.valueOf(dto.prioridad.uppercase()) } catch (_: Exception) { Prioridad.MEDIA },
                        competenciaId = dto.competencia_id,
                        diasRestantes = 0, // 👈 Parámetro requerido agregado correctamente
                        aprendizId = dto.aprendiz_id
                    )
                    repository.guardar(actividadFormativa)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun cargarAprendices() {
        viewModelScope.launch {
            try {
                val lista = supabase.from("profiles")
                    .select {
                        filter { eq("rol", "aprendiz") }
                    }
                    .decodeList<ProfileDto>()
                _aprendices.value = lista
            } catch (e: Exception) {
                // Silencioso
            }
        }
    }

    fun cambiarBusqueda(texto: String) {
        _textoBusqueda.value = texto
    }

    fun actualizarTitulo(titulo: String) {
        val error = if (titulo.isBlank()) "El título es obligatorio" else if (titulo.length > 80) "Máximo 80 caracteres" else null
        _formularioState.value = _formularioState.value.copy(titulo = titulo, tituloTocado = true, tituloError = error)
    }

    fun actualizarDescripcion(descripcion: String) {
        val error = if (descripcion.length > 240) "Máximo 240 caracteres" else null
        _formularioState.value = _formularioState.value.copy(descripcion = descripcion, descripcionTocado = true, descripcionError = error)
    }

    fun actualizarFecha(fecha: String) {
        val error = if (fecha.isBlank()) "La fecha es obligatoria" else null
        _formularioState.value = _formularioState.value.copy(fecha = fecha, fechaTocado = true, fechaError = error)
    }

    fun actualizarPrioridad(prioridad: Prioridad) {
        _formularioState.value = _formularioState.value.copy(prioridad = prioridad)
    }

    fun actualizarProgreso(progreso: Int) {
        _formularioState.value = _formularioState.value.copy(progreso = progreso)
    }

    fun actualizarCompetenciaSeleccionada(competenciaId: Long) {
        _formularioState.value = _formularioState.value.copy(competenciaId = competenciaId)
    }

    fun actualizarAprendizSeleccionado(aprendizId: String?) {
        _formularioState.value = _formularioState.value.copy(aprendizId = aprendizId)
    }

    fun guardar(actividad: ActividadFormativa) {
        if (!_esInstructor.value) {
            _operacion.value = OperacionUiState.Fallida("Los aprendices no pueden crear actividades")
            return
        }

        viewModelScope.launch {
            _operacion.value = OperacionUiState.EnCurso
            try {
                repository.guardar(actividad)

                val currentUser = supabase.auth.currentUserOrNull()
                if (currentUser != null) {
                    val dto = ActividadSupabaseDto(
                        id = actividad.id,
                        titulo = actividad.titulo,
                        descripcion = actividad.descripcion,
                        fecha = actividad.fecha,
                        progreso = actividad.progreso,
                        prioridad = actividad.prioridad.name,
                        competencia_id = actividad.competenciaId,
                        instructor_id = currentUser.id,
                        aprendiz_id = _formularioState.value.aprendizId
                    )
                    supabase.from("actividades").insert(dto)
                }

                _operacion.value = OperacionUiState.Exitosa
                _formularioState.value = FormularioActividadUiState()
            } catch (cancelada: CancellationException) {
                throw cancelada
            } catch (error: Exception) {
                _operacion.value = OperacionUiState.Fallida(error.message ?: "Fallo al guardar la actividad")
            }
        }
    }

    fun subirEvidencia(actividadId: String, bytesFoto: ByteArray, nombreArchivo: String) {
        viewModelScope.launch {
            _operacion.value = OperacionUiState.EnCurso
            try {
                val currentUser = supabase.auth.currentUserOrNull()
                val aprendizId = currentUser?.id ?: "usuario-local-test"

                val filePath = "$actividadId/$nombreArchivo"

                supabase.storage["evidencias"].upload(filePath, bytesFoto) {
                    upsert = true
                }

                val publicUrl = supabase.storage["evidencias"].publicUrl(filePath)

                val nuevaEvidencia = EvidenciaSupabaseDto(
                    id = System.currentTimeMillis().toString(),
                    actividad_id = actividadId,
                    aprendiz_id = aprendizId,
                    foto_url = publicUrl
                )
                supabase.from("evidencias").insert(nuevaEvidencia)

                _operacion.value = OperacionUiState.Exitosa
            } catch (e: Exception) {
                e.printStackTrace()
                _operacion.value = OperacionUiState.Fallida("Error al subir evidencia: ${e.message}")
            }
        }
    }

    fun eliminar(id: String) {
        if (!_esInstructor.value) {
            _operacion.value = OperacionUiState.Fallida("Los aprendices no pueden eliminar actividades")
            return
        }

        viewModelScope.launch {
            _operacion.value = OperacionUiState.EnCurso
            try {
                repository.eliminar(id)

                val currentUser = supabase.auth.currentUserOrNull()
                if (currentUser != null) {
                    supabase.from("actividades").delete {
                        filter { eq("id", id) }
                    }
                }

                _operacion.value = OperacionUiState.Exitosa
            } catch (cancelada: CancellationException) {
                throw cancelada
            } catch (error: Exception) {
                _operacion.value = OperacionUiState.Fallida(error.message ?: "Fallo al eliminar la actividad")
            }
        }
    }

    fun cambiarModoCuadricula(activo: Boolean) {
        viewModelScope.launch {
            preferenciasRepository.guardarModoCuadricula(activo)
        }
    }

    fun cambiarFiltroCompetencia(valor: String?) {
        viewModelScope.launch {
            preferenciasRepository.guardarFiltro(valor)
        }
    }

    fun reiniciarOperacion() {
        _operacion.value = OperacionUiState.Inactiva
    }
}