package com.example.miformacionctma.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Insert
import androidx.room3.Update
import androidx.room3.OnConflictStrategy
import com.example.miformacionctma.data.local.entities.ActividadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActividadDao {
    @Query("SELECT * FROM actividades ORDER BY fechaLimiteEpochMillis DESC")
    fun observarTodos(): Flow<List<ActividadEntity>>

    @Query("SELECT * FROM actividades WHERE id = :id")
    fun observarPorId(id: String): Flow<ActividadEntity?>

    @Query(
        "SELECT * FROM actividades " +
                "WHERE LOWER(titulo) LIKE '%' || LOWER(:texto) || '%' " +
                "ORDER BY fechaLimiteEpochMillis DESC"
    )
    fun buscar(texto: String): Flow<List<ActividadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(actividad: ActividadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(actividades: List<ActividadEntity>) // <-- Añadido para el refresh

    @Update
    suspend fun actualizar(actividad: ActividadEntity)

    @Query("DELETE FROM actividades WHERE id = :id")
    suspend fun eliminarPorId(id: String): Int
}