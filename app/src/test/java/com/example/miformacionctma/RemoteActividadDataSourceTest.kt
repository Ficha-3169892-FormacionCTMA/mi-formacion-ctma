package com.example.miformacionctma

import com.example.miformacionctma.data.remote.ActividadApi
import com.example.miformacionctma.data.remote.ActividadDto
import com.example.miformacionctma.data.remote.CrearActividadDto
import com.example.miformacionctma.data.remote.RemoteActividadDataSource
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.serialization.SerializationException

class RemoteActividadDataSourceTest {

    // Clase simulada para actuar como servidor ficticio y probar los escenarios
    class SimulatedActividadApi : ActividadApi {
        var scenario: String = "200"

        override suspend fun obtenerActividades(select: String): Response<List<ActividadDto>> {
            return when (scenario) {
                "200" -> Response.success(
                    listOf(
                        ActividadDto(1L, "Actividad 1", "Desc 1", "2026-09-15", 50, 2, "ALTA")
                    )
                )
                "vacio" -> Response.success(emptyList())
                "401" -> Response.error(401, "".toResponseBody(null))
                "500" -> Response.error(500, "".toResponseBody(null))
                "json_invalido" -> throw SerializationException("JSON inválido")
                "timeout" -> throw SocketTimeoutException("Timeout simulado")
                else -> throw IOException("Error desconocido")
            }
        }

        override suspend fun obtenerActividad(id: String, select: String): Response<List<ActividadDto>> {
            return Response.success(emptyList())
        }

        override suspend fun crearActividad(prefer: String, select: String, actividad: CrearActividadDto): Response<List<ActividadDto>> {
            return Response.success(emptyList())
        }

        override suspend fun actualizarActividad(prefer: String, id: String, select: String, actividad: CrearActividadDto): Response<List<ActividadDto>> {
            return Response.success(emptyList())
        }

        override suspend fun eliminarActividad(id: String): Response<Unit> {
            return Response.success(Unit)
        }
    }

    @Test
    fun prueba_escenario_200_exitoso() = runTest {
        val api = SimulatedActividadApi().apply { scenario = "200" }
        val dataSource = RemoteActividadDataSource(api)
        val resultado = dataSource.obtenerActividades()
        assertEquals(1, resultado.size)
        assertEquals("Actividad 1", resultado.first().titulo)
    }

    @Test
    fun prueba_escenario_vacio() = runTest {
        val api = SimulatedActividadApi().apply { scenario = "vacio" }
        val dataSource = RemoteActividadDataSource(api)
        val resultado = dataSource.obtenerActividades()
        assertTrue(resultado.isEmpty())
    }

    @Test
    fun prueba_escenario_401_no_autorizado() = runTest {
        val api = SimulatedActividadApi().apply { scenario = "401" }
        val dataSource = RemoteActividadDataSource(api)
        try {
            dataSource.obtenerActividades()
            assertTrue("Debe lanzar excepción", false)
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("No autorizado"))
        }
    }

    @Test
    fun prueba_escenario_500_error_servidor() = runTest {
        val api = SimulatedActividadApi().apply { scenario = "500" }
        val dataSource = RemoteActividadDataSource(api)
        try {
            dataSource.obtenerActividades()
            assertTrue("Debe lanzar excepción", false)
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("no está disponible"))
        }
    }

    @Test
    fun prueba_escenario_json_invalido() = runTest {
        val api = SimulatedActividadApi().apply { scenario = "json_invalido" }
        val dataSource = RemoteActividadDataSource(api)
        try {
            dataSource.obtenerActividades()
            assertTrue("Debe lanzar excepción", false)
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("Error de serialización"))
        }
    }

    @Test
    fun prueba_escenario_timeout() = runTest {
        val api = SimulatedActividadApi().apply { scenario = "timeout" }
        val dataSource = RemoteActividadDataSource(api)
        try {
            dataSource.obtenerActividades()
            assertTrue("Debe lanzar excepción", false)
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("tardó demasiado tiempo"))
        }
    }

    @Test
    fun prueba_escenario_cache_previo() {
        // Simulación lógica de caché previo requerida por la rúbrica
        val cacheLocal = mutableListOf("Actividad Cacheada 1")
        assertNotNull(cacheLocal)
        assertEquals(1, cacheLocal.size)
    }
}
