package com.example.miformacionctma.data.repository

import androidx.room3.Transactor
import androidx.room3.useWriterConnection
import com.example.miformacionctma.data.local.FormacionDatabase
import com.example.miformacionctma.data.local.dao.ActividadDao
import com.example.miformacionctma.data.local.dao.CompetenciaDao
import com.example.miformacionctma.data.local.dao.EvidenciaDao
import com.example.miformacionctma.data.remote.api.ActividadesApi
import com.example.miformacionctma.data.util.DataError
import com.example.miformacionctma.data.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ActividadRepositoryTest {

    private val api = mockk<ActividadesApi>(relaxed = true)
    private val dao = mockk<ActividadDao>(relaxed = true)
    private val competenciaDao = mockk<CompetenciaDao>(relaxed = true)
    private val evidenciaDao = mockk<EvidenciaDao>(relaxed = true)
    private val db = mockk<FormacionDatabase>(relaxed = true)
    private lateinit var repository: RoomActividadRepository

    @Before
    fun setUp() {
        repository = RoomActividadRepository(dao, competenciaDao, evidenciaDao, db, api)

        mockkStatic("androidx.room3.RoomDatabaseKt")
        coEvery { 
            db.useWriterConnection<Unit>(any()) 
        } answers {
            val block = arg<suspend (Transactor) -> Unit>(1)
            kotlinx.coroutines.runBlocking {
                block(mockk(relaxed = true))
            }
        }
    }

    @Test
    fun refresh_returnsSuccess_on200Ok() = runTest {
        coEvery { api.getActividades() } returns Response.success(emptyList())
        coEvery { api.getCompetencias() } returns Response.success(emptyList())
        coEvery { api.getEvidencias() } returns Response.success(emptyList())

        val result = repository.refresh()

        assertTrue(result is Result.Success)
        coVerify { dao.insertarTodas(any()) }
    }

    @Test
    fun refresh_returnsNoConnection_onIOException() = runTest {
        coEvery { api.getActividades() } throws IOException("Sin conexión")

        val result = repository.refresh()

        assertEquals(Result.Error(DataError.Network.NoConnection), result)
    }

    @Test
    fun refresh_returnsUnauthorized_on401() = runTest {
        coEvery { api.getActividades() } returns Response.error(401, "".toResponseBody(null))

        val result = repository.refresh()

        assertEquals(Result.Error(DataError.Network.Unauthorized), result)
    }

    @Test
    fun refresh_returnsServerError_on500() = runTest {
        coEvery { api.getActividades() } returns Response.error(500, "".toResponseBody(null))

        val result = repository.refresh()

        assertEquals(Result.Error(DataError.Network.Server), result)
    }

    @Test(expected = CancellationException::class)
    fun refresh_rethrowsCancellationException() = runTest {
        coEvery { api.getActividades() } throws CancellationException("Cancelado")

        repository.refresh()
    }
}
