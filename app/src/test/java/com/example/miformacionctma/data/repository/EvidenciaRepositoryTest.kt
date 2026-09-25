package com.example.miformacionctma.data.repository

import com.example.miformacionctma.data.local.dao.EvidenciaDao
import com.example.miformacionctma.data.local.entities.EvidenciaEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Fake DAO para simular operaciones de evidencias en memoria durante las pruebas.
 */
class FakeEvidenciaDao : EvidenciaDao {
    private val db = MutableStateFlow<List<EvidenciaEntity>>(emptyList())

    override fun observeEvidenciasForActividad(actividadId: String): Flow<List<EvidenciaEntity>> = db

    override suspend fun findById(id: String): EvidenciaEntity? {
        return db.value.find { it.id == id }
    }

    override suspend fun insertOrUpdate(evidencia: EvidenciaEntity) {
        val current = db.value.toMutableList()
        current.removeAll { it.id == evidencia.id }
        current.add(evidencia)
        db.value = current
    }

    override suspend fun updateStatus(id: String, nuevoEstado: String) {
        val item = findById(id) ?: return
        insertOrUpdate(item.copy(estado = nuevoEstado))
    }

    override suspend fun deleteById(id: String) {
        val current = db.value.toMutableList()
        current.removeAll { it.id == id }
        db.value = current
    }
}

/**
 * Pruebas unitarias para verificar la gestión de evidencias.
 */
class EvidenciaRepositoryTest {

    private lateinit var fakeDao: FakeEvidenciaDao

    @Before
    fun setup() {
        fakeDao = FakeEvidenciaDao()
    }

    @Test
    fun cambioDeEstadoASincronizada() = runTest {
        val evidencia = EvidenciaEntity(
            id = "EVI-1",
            actividadId = "ACT-101",
            localUri = "content://media/external/images/media/1",
            mimeType = "image/jpeg",
            sizeBytes = 1024L,
            estado = "LOCAL",
            creadaEnEpochMillis = System.currentTimeMillis()
        )
        fakeDao.insertOrUpdate(evidencia)

        fakeDao.updateStatus("EVI-1", "SINCRONIZADA")

        val recuperada = fakeDao.findById("EVI-1")
        assertEquals("SINCRONIZADA", recuperada?.estado)
    }

    @Test
    fun eliminacionDeEvidenciaLocal() = runTest {
        val evidencia = EvidenciaEntity(
            id = "EVI-2",
            actividadId = "ACT-101",
            localUri = "content://media/external/images/media/2",
            mimeType = "image/png",
            sizeBytes = 2048L,
            estado = "LOCAL",
            creadaEnEpochMillis = System.currentTimeMillis()
        )
        fakeDao.insertOrUpdate(evidencia)

        fakeDao.deleteById("EVI-2")

        val recuperada = fakeDao.findById("EVI-2")
        assertEquals(null, recuperada)
    }
}