package com.example.miformacionctma.data.repository

import com.example.miformacionctma.data.local.ActividadDao
import com.example.miformacionctma.data.local.ActividadEntity
import com.example.miformacionctma.data.remote.ActividadesApi
import com.example.miformacionctma.data.remote.DataError
import com.example.miformacionctma.data.remote.NetworkFailure
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Fake DAO para simular las operaciones de Room en memoria durante las pruebas.
 */
class FakeActividadDao : ActividadDao {
    private val db = MutableStateFlow<List<ActividadEntity>>(emptyList())

    override fun observeAll(): Flow<List<ActividadEntity>> = db

    override suspend fun insertAll(actividades: List<ActividadEntity>) {
        db.value = actividades
    }

    override suspend fun deleteAll() {
        db.value = emptyList()
    }
}

/**
 * Pruebas unitarias para el repositorio utilizando MockWebServer.
 */
class ActividadRepositoryTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var repository: OfflineFirstActividadRepository
    private lateinit var fakeDao: FakeActividadDao

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val json = Json { ignoreUnknownKeys = true }
        val contentType = "application/json".toMediaType()

        val client = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(1, TimeUnit.SECONDS)
            .build()

        val api = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(ActividadesApi::class.java)

        fakeDao = FakeActividadDao()
        repository = OfflineFirstActividadRepository(api, fakeDao)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun refresh200OKGuardaActividadesEnRoom() = runTest {
        val jsonResponse = """
            [
                {
                    "id": "ACT-101",
                    "titulo": "Prueba de red",
                    "descripcion": "Verificar MockWebServer",
                    "progreso": 50,
                    "prioridad": "ALTA",
                    "competencia_id": "CP-01",
                    "fecha_limite": "2026-09-20",
                    "completada": false,
                    "actualizado_en": "2026-09-20T10:00:00Z"
                }
            ]
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(jsonResponse)
        )

        val result = repository.refresh()

        assertTrue(result.isSuccess)
        val request = mockWebServer.takeRequest()
        assertEquals("/v1/actividades", request.path)
    }

    @Test
    fun refresh401RetornaFalloDeNoAutorizado() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        val result = repository.refresh()

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as? NetworkFailure
        assertEquals(DataError.Unauthorized, exception?.error)
    }

    @Test
    fun refresh500RetornaFalloDeServidor() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        val result = repository.refresh()

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as? NetworkFailure
        assertTrue(exception?.error is DataError.Server)
    }
}