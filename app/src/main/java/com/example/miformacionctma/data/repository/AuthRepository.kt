package com.example.miformacionctma.data.repository

import com.example.miformacionctma.data.remote.AuthApi
import com.example.miformacionctma.data.remote.LoginRequest
import com.example.miformacionctma.data.remote.RetrofitInstance
import com.example.miformacionctma.data.remote.SignupRequest
import com.example.miformacionctma.model.AuthResponse
import com.example.miformacionctma.model.Rol
import com.example.miformacionctma.model.Usuario
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonPrimitive
import retrofit2.Response

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Usuario>
    suspend fun signup(email: String, password: String, nombre: String, rol: Rol): Result<Usuario>
    suspend fun logout()
    suspend fun estaAutenticado(): Boolean
    suspend fun obtenerUsuarioActual(): Usuario?
    suspend fun obtenerListaEstudiantes(): List<Usuario>
}

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val prefs: PreferenciasRepository
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Usuario> {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val auth = response.body()!!
                
                // Actualizamos el token en el interceptor para obtener el perfil
                RetrofitInstance.actualizarToken(auth.accessToken)
                
                // Reintento de lectura de perfil (por si el trigger de Supabase tarda)
                var usuario: Usuario? = null
                for (i in 1..5) { // Aumentamos a 5 reintentos
                    val perfilResponse = authApi.obtenerPerfil("eq.${auth.user.id}")
                    if (perfilResponse.isSuccessful && perfilResponse.body()?.isNotEmpty() == true) {
                        usuario = perfilResponse.body()!![0]
                        break
                    }
                    kotlinx.coroutines.delay(1000) // Esperar 1 segundo entre reintentos
                }

                if (usuario != null) {
                    // Guardamos sesión
                    prefs.guardarSesion(auth.accessToken, usuario.id, usuario.rol.name)
                    Result.success(usuario)
                } else {
                    // EMERGENCIA: El trigger falló, intentamos crear el perfil manualmente
                    val nuevoUsuario = Usuario(
                        id = auth.user.id,
                        email = auth.user.email,
                        nombreCompleto = auth.user.userMetadata?.get("nombre_completo")?.toString()?.removeSurrounding("\"") ?: "Usuario",
                        rol = try {
                            Rol.valueOf(auth.user.userMetadata?.get("rol")?.toString()?.removeSurrounding("\"") ?: "ESTUDIANTE")
                        } catch (e: Exception) { Rol.ESTUDIANTE }
                    )
                    
                    val createResponse = authApi.crearPerfilManual(nuevoUsuario)
                    if (createResponse.isSuccessful) {
                        prefs.guardarSesion(auth.accessToken, nuevoUsuario.id, nuevoUsuario.rol.name)
                        Result.success(nuevoUsuario)
                    } else {
                        Result.failure(Exception("Sincronización fallida: ${createResponse.errorBody()?.string()}"))
                    }
                }
            } else {
                Result.failure(Exception("Credenciales incorrectas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signup(email: String, password: String, nombre: String, rol: Rol): Result<Usuario> {
        return try {
            val metadata = mapOf(
                "nombre_completo" to JsonPrimitive(nombre),
                "rol" to JsonPrimitive(rol.name)
            )
            val response = authApi.signup(SignupRequest(email, password, metadata))
            if (response.isSuccessful && response.body() != null) {
                // Supabase Auth crea el usuario. El trigger en la DB creará el perfil.
                // Logueamos automáticamente tras el registro
                login(email, password)
            } else {
                Result.failure(Exception("Error al registrar: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        prefs.borrarSesion()
        RetrofitInstance.actualizarToken(null)
    }

    override suspend fun estaAutenticado(): Boolean {
        val token = prefs.accessToken.first()
        if (token != null) {
            RetrofitInstance.actualizarToken(token)
            return true
        }
        return false
    }

    override suspend fun obtenerUsuarioActual(): Usuario? {
        val userId = prefs.userId.first() ?: return null
        return try {
            val response = authApi.obtenerPerfil("eq.$userId")
            if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                response.body()!![0]
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun obtenerListaEstudiantes(): List<Usuario> {
        return try {
            val response = authApi.obtenerEstudiantes()
            if (response.isSuccessful) response.body() ?: emptyList()
            else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
