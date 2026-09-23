package com.example.miformacionctma.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.miformacionctma.data.local.entity.ActividadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActividadDao {

    @Query("SELECT * FROM actividades WHERE eliminada = 0 ORDER BY id DESC")
    fun obtenerTodasLasActividades(): Flow<List<ActividadEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM actividades WHERE id = :id AND eliminada = 0)")
    suspend fun existeActividad(id: Int): Boolean

    @Query("SELECT * FROM actividades WHERE id = :id")
    suspend fun obtenerPorId(id: Int): ActividadEntity?

    // Actividades con cambios locales (creadas, editadas o borradas) pendientes de enviar a Supabase
    @Query("SELECT * FROM actividades WHERE sincronizada = 0")
    suspend fun obtenerPendientes(): List<ActividadEntity>

    @Query("UPDATE actividades SET sincronizada = 1 WHERE id = :id")
    suspend fun marcarSincronizada(id: Int)

    @Query("DELETE FROM actividades WHERE id = :id")
    suspend fun eliminarPorId(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarActividad(actividad: ActividadEntity)

    // Upsert actualiza sin borrar la fila, así no se borran en cascada las evidencias (a diferencia de REPLACE)
    @Upsert
    suspend fun upsertActividad(actividad: ActividadEntity)

    @Update
    suspend fun actualizarActividad(actividad: ActividadEntity)

    @Delete
    suspend fun eliminarActividad(actividad: ActividadEntity)
}
