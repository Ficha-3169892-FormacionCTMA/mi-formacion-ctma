package com.example.miformacionctma.data.repository

import com.example.miformacionctma.data.local.ActividadDao
import com.example.miformacionctma.data.remote.ActividadApiService
import com.example.miformacionctma.data.remote.NetworkException
import com.example.miformacionctma.data.remote.RemoteActividadDataSource
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class ActividadRepositoryRefreshTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ActividadApiService
    private lateinit var dao: ActividadDao
    private lateinit var repository: ActividadRepositoryImpl

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        
        val json = Json { ignoreUnknownKeys = true }
        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ActividadApiService::class.java)

        dao = mock(ActividadDao::class.java)
        val remoteDataSource = RemoteActividadDataSource(apiService)
        repository = ActividadRepositoryImpl(dao, remoteDataSource)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `refresh with success 200 updates dao`() = runTest {
        // Arrange
        val jsonResponse = """
            [
                {
                    "id": 1,
                    "titulo": "Actividad 1",
                    "descripcion": "Desc 1",
                    "fecha": "2026-09-11",
                    "progreso": 10,
                    "diasRestantes": 7,
                    "prioridad": "ALTA"
                }
            ]
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(jsonResponse).setResponseCode(200))

        // Act
        repository.refresh()

        // Assert
        verify(dao).refrescarActividades(any())
    }

    @Test
    fun `refresh with empty list 200 updates dao with empty`() = runTest {
        // Arrange
        mockWebServer.enqueue(MockResponse().setBody("[]").setResponseCode(200))

        // Act
        repository.refresh()

        // Assert
        verify(dao).refrescarActividades(emptyList())
    }

    @Test
    fun `refresh with 401 throws NoAutorizado`() = runTest {
        // Arrange
        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        // Act & Assert
        try {
            repository.refresh()
            org.junit.Assert.fail("Debería haber lanzado NoAutorizado")
        } catch (e: NetworkException.NoAutorizado) {
            // Éxito
        }
    }

    @Test
    fun `refresh with 500 throws ErrorServidor`() = runTest {
        // Arrange
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        // Act & Assert
        try {
            repository.refresh()
            org.junit.Assert.fail("Debería haber lanzado ErrorServidor")
        } catch (e: NetworkException.ErrorServidor) {
            // Éxito
        }
    }

    @Test
    fun `refresh with invalid json throws FormatoInvalido`() = runTest {
        // Arrange
        mockWebServer.enqueue(MockResponse().setBody("{ invalid }").setResponseCode(200))

        // Act & Assert
        try {
            repository.refresh()
            org.junit.Assert.fail("Debería haber lanzado FormatoInvalido")
        } catch (e: NetworkException.FormatoInvalido) {
            // Éxito
        }
    }
}
