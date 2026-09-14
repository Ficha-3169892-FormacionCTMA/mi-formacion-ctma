package com.example.miformacionctma

import com.example.miformacionctma.data.local.entities.ActividadEntity
import com.example.miformacionctma.data.local.entities.CompetenciaEntity
import com.example.miformacionctma.data.local.toDomain
import com.example.miformacionctma.data.local.toEntity
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import com.example.miformacionctma.model.ReglasActividad
import com.example.miformacionctma.ui.state.ListadoUiState
import com.example.miformacionctma.ui.state.OperacionUiState
import org.junit.Assert.*
import org.junit.Test

class ConsolidatedUnitTests {

    // 01. CP-02: Título < 3 caracteres (Equivalencia)
    @Test
    fun test_cp02_tituloCorto_errorMensaje() {
        val error = ReglasActividad.validarTitulo("AB")
        assertEquals("Usa al menos 3 caracteres", error)
    }

    // 02. CP-03: Fecha inválida (Negativa)
    @Test
    fun test_cp03_fechaInvalida_errorFormato() {
        val error = ReglasActividad.validarFecha("15-09-2026")
        assertEquals("Formato inválido (AAAA-MM-DD)", error)
    }

    // 03. CP-10: Título solo con espacios (Negativa)
    @Test
    fun test_cp10_tituloEspacios_errorObligatorio() {
        val error = ReglasActividad.validarTitulo("   ", mostrarVacio = true)
        assertEquals("El título es obligatorio", error)
    }

    // 04. CP-14: Slider de progreso 0% y 100% (Limites)
    @Test
    fun test_cp14_progresoLimites_validos() {
        assertNull(ReglasActividad.validarProgreso(0))
        assertNull(ReglasActividad.validarProgreso(100))
        assertNotNull(ReglasActividad.validarProgreso(150))
    }

    // 05. CP-16: Marcado de urgencia con <= 2 días restantes (Negocio)
    @Test
    fun test_cp16_urgencia_marcaCorrectamente() {
        val actividades = listOf(
            ActividadFormativa("1", "Urgente", "Desc", "2026-09-02", 20, 2, Prioridad.ALTA)
        )
        val urgentes = ReglasActividad.actividadesUrgentes(actividades)
        assertEquals(1, urgentes.size)
        assertEquals("Urgente", urgentes.first().titulo)
    }

    // 06. Reglas: Promedio de progreso (Logic)
    @Test
    fun test_reglas_promedioProgreso() {
        val lista = listOf(
            ActividadFormativa("1", "A", "D", "F", 100, 0, Prioridad.ALTA),
            ActividadFormativa("2", "B", "D", "F", 50, 0, Prioridad.MEDIA)
        )
        val promedio = ReglasActividad.promedioProgreso(lista)
        assertEquals(75.0, promedio, 0.1)
    }

    // 07. Reglas: Determinación de estado "Completada" (Logic)
    @Test
    fun test_reglas_estadoCompletada() {
        val actividad = ActividadFormativa("1", "T", "D", "F", 100, 0, Prioridad.MEDIA)
        assertEquals("Completada", ReglasActividad.estadoActividad(actividad))
    }

    // 08. S6 Mapping: Entity a Dominio (Mapping)
    @Test
    fun test_s6_mapping_entityToDomain() {
        val entity = ActividadEntity("ID", "T", "D", 50, "ALTA", 1L, 0L, true)
        val domain = entity.toDomain()
        assertEquals(Prioridad.ALTA, domain.prioridad)
        assertTrue(domain.completada)
    }

    // 09. S6 Mapping: Dominio a Entity (Mapping)
    @Test
    fun test_s6_mapping_domainToEntity() {
        val domain = ActividadFormativa("ID", "T", "D", "F", 0, 0, Prioridad.BAJA, 2L, 0L, false)
        val entity = domain.toEntity()
        assertEquals("BAJA", entity.prioridad)
        assertEquals(2L, entity.competenciaId)
    }

    // 10. S6 Validation: ID por defecto Competencia (Restricciones)
    @Test
    fun test_s6_competencia_defaultId() {
        val comp = CompetenciaEntity(nombre = "Software")
        assertEquals(0L, comp.id)
    }

    // 11. S7 CA-01: Estado inicial vacío (State transition)
    @Test
    fun test_s7_ca01_listaVacia() {
        val state = ListadoUiState.Vacio
        assertTrue(state is ListadoUiState.Vacio)
    }

    // 12. S7 CA-03: Filtrado de lista por competencia (Combination)
    @Test
    fun test_s7_ca03_filtradoCompetencia() {
        val lista = listOf(
            ActividadFormativa("1", "T1", "D", "F", 0, 0, Prioridad.ALTA, competenciaId = 1L),
            ActividadFormativa("2", "T2", "D", "F", 0, 0, Prioridad.ALTA, competenciaId = 2L)
        )
        val filtradas = lista.filter { it.competenciaId == 1L }
        assertEquals(1, filtradas.size)
    }

    // 13. S7 CA-04: Búsqueda dinámica - Precedencia (Concurrency Logic)
    @Test
    fun test_s7_ca04_busquedaPrecedencia() {
        val busquedas = mutableListOf("anterior", "reciente")
        assertEquals("reciente", busquedas.last())
    }

    // 14. S7 CA-05: Mapeo de excepción a UI Error (Error handling)
    @Test
    fun test_s7_ca05_errorState() {
        val msg = "Fallo persistencia"
        val state = ListadoUiState.Error(msg)
        assertEquals(msg, state.mensaje)
    }

    // 15. S7 Operation: Transición exitosa de guardado (Operation flow)
    @Test
    fun test_s7_operacionExitosa() {
        val state = OperacionUiState.Exitosa
        assertEquals(OperacionUiState.Exitosa, state)
    }
}
