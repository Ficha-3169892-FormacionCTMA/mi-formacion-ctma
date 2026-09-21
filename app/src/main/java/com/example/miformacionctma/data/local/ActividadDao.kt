package com.example.miformacionctma.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de acceso a datos para la tabla de actividades en Room.
 */
@Dao
interface ActividadDao {

    /**
     * Observa todas las actividades guardadas en la base de datos local.
     * Al devolver un Flow, la UI se actualiza automáticamente cuando hay cambios.
     */
    @Query("SELECT * FROM actividades")
    fun observeAll(): Flow<List<ActividadEntity>>

    /**
     * Inserta o reemplaza una lista de actividades en la base de datos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(actividades: List<ActividadEntity>)

    /**
     * Elimina todas las actividades de la tabla local.
     */
    @Query("DELETE FROM actividades")
    suspend fun deleteAll()
}