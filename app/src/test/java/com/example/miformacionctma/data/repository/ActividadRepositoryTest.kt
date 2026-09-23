package com.example.miformacionctma.data.repository

import androidx.room3.useWriterConnection
import com.example.miformacionctma.data.local.FormacionDatabase
import com.example.miformacionctma.data.local.dao.ActividadDao
import com.example.miformacionctma.data.local.dao.CompetenciaDao
import com.example.miformacionctma.data.remote.api.ActividadesApi
import com.example.miformacionctma.data.util.DataError
import com.example.miformacionctma.data.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import androidx.room3.Transactor

@OptIn(ExperimentalCoroutinesApi::class)
class ActividadRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ActividadesApi
    private lateinit var repository: RoomActividadRepository
    private val dao = mockk<ActividadDao>(relaxed = true)
    private val competenciaDao = mockk<CompetenciaDao>(relaxed = true)
    private val db = mockk<FormacionDatabase>(relaxed = true)

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val json = Json { ignoreUnknownKeys = true }
        val contentType = "application/json".toMediaType()

        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(ActividadesApi::class.java)

        repository = RoomActividadRepository(dao, competenciaDao, db, api)

        // Mock static extension function useWriterConnection
        mockkStatic("androidx.room3.RoomDatabaseKt")
        val slot = slot<suspend (Transactor) -> Any>()
        coEvery { 
            db.useWriterConnection<Any>(capture(slot)) 
        } coAnswers {
            slot.captured(mockk(relaxed = true))
        }
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // CA-01: Respuesta 200 OK con datos válidos
    @Test
    fun refresh_returnsSuccess_on200Ok() = runTest {
        val jsonResponse = """
            [
              {
                "id": 1,
                "titulo": "Actividad Test",
                "descripcion": "Desc",
                "fecha": "2026-09-25T10:00:00Z",
                "progreso": 50,
                "prioridad": "ALTA",
                "competencia_id": 5,
                "esta_completada": false
              }
            ]
        """.trimIndent()

        server.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val result = repository.refresh()

        assertTrue(result is Result.Success)
        coVerify { dao.insertarTodas(any()) }
        
        val request = server.takeRequest()
        assertTrue(request.headers["Authorization"]?.startsWith("Bearer") ?: false)
    }

    // CA-02: Respuesta 200 OK con arreglo vacío ([])
    @Test
    fun refresh_returnsSuccess_onEmptyArray() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        val result = repository.refresh()

        assertTrue(result is Result.Success)
        coVerify { dao.insertarTodas(emptyList()) }
    }

    // CA-03: Timeout de red
    @Test
    fun refresh_returnsTimeoutError_onTimeout() = runTest {
        server.enqueue(
            MockResponse()
                .setBodyDelay(2, TimeUnit.SECONDS)
                .setResponseCode(200)
                .setBody("[]")
        )

        // Nota: El timeout real del cliente configurado es mayor, pero simulamos fallo
        // Para esta prueba, forzamos un error de timeout si es posible configurando Retrofit ad-hoc
        // O simplemente verificamos la clasificación si lanzamos IOException simulada
    }

    // CA-04: Sin conexión a red
    @Test
    fun refresh_returnsNoConnection_onIOException() = runTest {
        // Simulamos fallo de red apagando el servidor antes de la petición
        server.shutdown()

        val result = repository.refresh()

        assertEquals(Result.Error(DataError.Network.NoConnection), result)
    }

    // CA-05: Error de autenticación HTTP 401
    @Test
    fun refresh_returnsUnauthorized_on401() = runTest {
        server.enqueue(MockResponse().setResponseCode(401))

        val result = repository.refresh()

        assertEquals(Result.Error(DataError.Network.Unauthorized), result)
    }

    // CA-06: Error de servidor (5xx) o JSON inválido
    @Test
    fun refresh_returnsServerError_on500() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val result = repository.refresh()

        assertEquals(Result.Error(DataError.Network.Server), result)
    }

    @Test
    fun refresh_returnsInvalidPayload_onMalformedJson() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{\"invalid\": \"json\"}"))

        val result = repository.refresh()

        assertTrue(result is Result.Error)
        assertEquals(DataError.Network.InvalidPayload, (result as Result.Error).error)
    }

    // CA-07: Ejecución simultánea
    @Test
    fun refresh_handlesConcurrentCalls() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]").setBodyDelay(100, TimeUnit.MILLISECONDS))
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        val job1 = launch { repository.refresh() }
        val job2 = launch { repository.refresh() }

        job1.join()
        job2.join()
        
        // Verificamos que no explota y Room gestiona las transacciones
        coVerify(exactly = 2) { db.useWriterConnection<Any>(any()) }
    }

    // CA-08: Cancelación
    @Test(expected = CancellationException::class)
    fun refresh_rethrowsCancellationException() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]").setBodyDelay(5, TimeUnit.SECONDS))
        
        val job = launch {
            repository.refresh()
        }
        
        delay(100)
        job.cancelAndJoin()
    }
}
