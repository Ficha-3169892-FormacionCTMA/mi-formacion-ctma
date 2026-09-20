package com.example.miformacionctma.data.remote

import com.example.miformacionctma.model.AuthResponse
import com.example.miformacionctma.model.Usuario
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class SignupRequest(
    val email: String,
    val password: String,
    @SerialName("data") val data: Map<String, JsonElement>
)

interface AuthApi {

    @POST("auth/v1/signup")
    suspend fun signup(
        @Body request: SignupRequest
    ): Response<AuthResponse>

    @POST("auth/v1/token?grant_type=password")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @GET("rest/v1/perfiles")
    suspend fun obtenerPerfil(
        @Query("id") id: String,
        @Query("select") select: String = "*"
    ): Response<List<Usuario>>

    @POST("rest/v1/perfiles")
    suspend fun crearPerfilManual(
        @Body perfil: Usuario
    ): Response<Unit>

    @GET("rest/v1/perfiles")
    suspend fun obtenerEstudiantes(
        @Query("rol") rol: String = "eq.ESTUDIANTE",
        @Query("select") select: String = "id,email,nombre_completo,rol"
    ): Response<List<Usuario>>
}
