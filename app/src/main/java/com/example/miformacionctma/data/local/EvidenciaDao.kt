package com.example.miformacionctma.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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