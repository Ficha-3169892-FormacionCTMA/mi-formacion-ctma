package com.example.miformacionctma.data.repository

import com.example.miformacionctma.data.remote.api.ActividadesApi
import com.example.miformacionctma.data.remote.dto.PerfilDto
import com.example.miformacionctma.model.RolUsuario
import com.example.miformacionctma.model.Usuario
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AuthRepository(
    private val api: ActividadesApi,
    private val preferenciasRepository: PreferenciasRepository
) {
    val usuarioActual: Flow<Usuario?> = preferenciasRepository.usuarioSesion

    suspend fun iniciarSesion(
        email: String,
        password: String,
        nombre: String,
        rolSeleccionado: RolUsuario
    ): Result<Usuario> {
        val emailLimpio = email.trim().lowercase()
        val passwordLimpio = password.trim()
        val nombreLimpio = nombre.trim().ifBlank { emailLimpio.substringBefore('@') }

        return try {
            val response = api.getPerfilPorEmail("eq.$emailLimpio")
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val perfilDto = response.body()!!.first()
                if (!perfilDto.password.isNullOrEmpty() && perfilDto.password != passwordLimpio) {
                    return Result.failure(IllegalArgumentException("Contraseña incorrecta."))
                }
                val rol = try {
                    RolUsuario.valueOf(perfilDto.rol)
                } catch (e: Exception) {
                    rolSeleccionado
                }
                val usuario = Usuario(
                    id = perfilDto.id ?: UUID.randomUUID().toString(),
                    email = perfilDto.email,
                    nombre = perfilDto.nombre,
                    rol = rol
                )
                preferenciasRepository.guardarSesionUsuario(usuario)
                Result.success(usuario)
            } else {
                val nuevoId = UUID.randomUUID().toString()
                val nuevoPerfil = PerfilDto(
                    id = nuevoId,
                    email = emailLimpio,
                    nombre = nombreLimpio,
                    rol = rolSeleccionado.name,
                    password = passwordLimpio
                )
                api.crearPerfil(nuevoPerfil)
                val usuario = Usuario(
                    id = nuevoId,
                    email = emailLimpio,
                    nombre = nombreLimpio,
                    rol = rolSeleccionado
                )
                preferenciasRepository.guardarSesionUsuario(usuario)
                Result.success(usuario)
            }
        } catch (e: Exception) {
            val usuarioOffline = Usuario(
                id = UUID.randomUUID().toString(),
                email = emailLimpio,
                nombre = nombreLimpio,
                rol = rolSeleccionado
            )
            preferenciasRepository.guardarSesionUsuario(usuarioOffline)
            Result.success(usuarioOffline)
        }
    }

    suspend fun cerrarSesion() {
        preferenciasRepository.cerrarSesionUsuario()
    }
}
