package com.example.miformacionctma.data.local.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.example.miformacionctma.data.local.entity.EstadoSincronizacion
import com.example.miformacionctma.data.local.entity.EvidenciaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenciaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(evidencia: EvidenciaEntity): Long

    @Update
    suspend fun actualizar(evidencia: EvidenciaEntity)

    @Delete
    suspend fun eliminar(evidencia: EvidenciaEntity)

    @Query("SELECT * FROM evidencias WHERE actividadId = :actividadId LIMIT 1")
    fun observarPorActividadId(actividadId: Long): Flow<EvidenciaEntity?>

    @Query("SELECT * FROM evidencias WHERE actividadId = :actividadId LIMIT 1")
    suspend fun obtenerPorActividadId(actividadId: Long): EvidenciaEntity?

    @Query("UPDATE evidencias SET estado = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: Long, estado: EstadoSincronizacion)

    @Query("DELETE FROM evidencias WHERE actividadId = :actividadId")
    suspend fun eliminarPorActividadId(actividadId: Long)

    @Query("DELETE FROM evidencias")
    suspend fun eliminarTodas()
}
