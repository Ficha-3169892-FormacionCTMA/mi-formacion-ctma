package com.example.miformacionctma

import com.example.miformacionctma.data.remote.ActividadRemotaDTO
import com.example.miformacionctma.data.remote.DataError
import com.example.miformacionctma.data.remote.NetworkFailure
import com.example.miformacionctma.data.remote.NetworkModule
import com.example.miformacionctma.data.remote.SupabaseApiService
import com.example.miformacionctma.data.remote.classifyNetworkCall
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SupabaseApiServiceTest {

    private lateinit var servidor: MockWebServer
    private lateinit var api: SupabaseApiService

    @Before
    fun setup() {
        servidor = MockWebServer()
        servidor.start()
        api = NetworkModule.createSupabaseApiService(servidor.url("/").toString())
    }

    @After
    fun teardown() {
        servidor.shutdown()
    }

    @Test
    fun listarActividades_enviaCredencialesYConvierteElJson() = runTest {
        servidor.enqueue(
            MockResponse().setBody(
                """[{"id":-830113613,"titulo":"APIREST","descripcion":"","fecha":"2026-09-30",
                   "prioridad":"ALTA","progreso":75,"completada":false,"actualizado_en":"2026-09-23"}]"""
            )
        )

        val respuesta = api.listActividades()
        val peticion = servidor.takeRequest()

        assertEquals(-830113613L, respuesta.body()!!.single().id)
        assertEquals(75, respuesta.body()!!.single().progreso)
        assertTrue(peticion.path!!.startsWith("/rest/v1/actividades?select="))
        assertEquals(BuildConfig.SUPABASE_ANON_KEY, peticion.getHeader("apikey"))
        assertEquals("Bearer ${BuildConfig.SUPABASE_ANON_KEY}", peticion.getHeader("Authorization"))
    }

    @Test
    fun upsertActividad_usaMergeDuplicatesParaNoDuplicar() = runTest {
        servidor.enqueue(MockResponse().setResponseCode(201))

        api.upsertActividad(ActividadRemotaDTO(1, "Taller", "", "2026-09-30", "MEDIA", 10))
        val peticion = servidor.takeRequest()

        assertEquals("POST", peticion.method)
        assertTrue(peticion.getHeader("Prefer")!!.contains("resolution=merge-duplicates"))
        assertTrue(peticion.body.readUtf8().contains("\"titulo\":\"Taller\""))
    }

    @Test
    fun listarEvidencias_aceptaActividadIdComoTextoONumero() = runTest {
        servidor.enqueue(
            MockResponse().setBody(
                """[{"actividad_id":"7","url_remota":"https://x/a.jpg","tipo_mime":"image/jpeg","tamano_bytes":10},
                    {"actividad_id":8,"url_remota":"https://x/b.jpg","tipo_mime":"image/jpeg","tamano_bytes":20}]"""
            )
        )

        val filas = api.listEvidenciaData().body()!!

        assertEquals(listOf("7", "8"), filas.map { it.actividadId.content })
    }

    @Test
    fun jsonInvalido_seClasificaComoInvalidPayload() = runTest {
        servidor.enqueue(MockResponse().setBody("""[{"id":"no-es-un-numero"}]"""))

        val resultado = classifyNetworkCall { api.listActividades() }

        assertEquals(DataError.InvalidPayload, (resultado.exceptionOrNull() as NetworkFailure).error)
    }

    @Test
    fun servidorCaido_seClasificaComoSinConexion() = runTest {
        servidor.shutdown()

        val resultado = classifyNetworkCall { api.listActividades() }

        assertEquals(DataError.NoConnection, (resultado.exceptionOrNull() as NetworkFailure).error)
    }
}
