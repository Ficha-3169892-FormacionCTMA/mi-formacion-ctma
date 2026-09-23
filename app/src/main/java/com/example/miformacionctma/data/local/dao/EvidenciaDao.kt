package com.example.miformacionctma.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.miformacionctma.data.local.entity.EvidenciaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenciaDao {

    @Query("SELECT * FROM evidencias WHERE actividadId = :actividadId")
    fun observarEvidenciaPorActividad(actividadId: Int): Flow<EvidenciaEntity?>

    @Query("SELECT * FROM evidencias WHERE actividadId = :actividadId")
    suspend fun obtenerEvidenciaPorActividad(actividadId: Int): EvidenciaEntity?

    @Query("SELECT * FROM evidencias")
    suspend fun obtenerTodas(): List<EvidenciaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEvidencia(evidencia: EvidenciaEntity)

    @Query("DELETE FROM evidencias WHERE actividadId = :actividadId")
    suspend fun eliminarEvidenciaPorActividad(actividadId: Int)
}
