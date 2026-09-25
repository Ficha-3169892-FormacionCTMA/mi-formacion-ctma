package com.example.miformacionctma.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.example.miformacionctma.data.local.entities.EvidenciaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenciaDao {

    @Query("SELECT * FROM evidencias WHERE actividadId = :actividadId")
    fun observeEvidenciasForActividad(actividadId: String): Flow<List<EvidenciaEntity>>

    @Query("SELECT * FROM evidencias WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): EvidenciaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(evidencia: EvidenciaEntity)

    @Query("UPDATE evidencias SET estado = :nuevoEstado WHERE id = :id")
    suspend fun updateStatus(id: String, nuevoEstado: String)

    @Query("DELETE FROM evidencias WHERE id = :id")
    suspend fun deleteById(id: String)
}