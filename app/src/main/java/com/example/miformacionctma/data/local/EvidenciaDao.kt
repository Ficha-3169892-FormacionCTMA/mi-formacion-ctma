package com.example.miformacionctma.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenciaDao {

    @Query("SELECT * FROM evidencias WHERE actividadId = :actividadId")
    fun observarPorActividad(actividadId: Long): Flow<List<EvidenciaEntity>>

    @Query("SELECT * FROM evidencias WHERE id = :id")
    suspend fun obtenerPorId(id: Long): EvidenciaEntity?

    @Query("SELECT * FROM evidencias WHERE actividadId = :actividadId")
    suspend fun obtenerPorActividad(actividadId: Long): List<EvidenciaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(evidencia: EvidenciaEntity): Long

    @Delete
    suspend fun eliminar(evidencia: EvidenciaEntity)

    @Query("DELETE FROM evidencias WHERE actividadId = :actividadId")
    suspend fun eliminarPorActividad(actividadId: Long)

    @Query("UPDATE evidencias SET estado = :nuevoEstado WHERE id = :id")
    suspend fun actualizarEstado(id: Long, nuevoEstado: String)
}
