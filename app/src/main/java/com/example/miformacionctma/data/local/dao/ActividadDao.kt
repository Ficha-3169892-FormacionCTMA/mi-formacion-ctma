package com.example.miformacionctma.data.local.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import com.example.miformacionctma.data.local.entity.ActividadConCompetencia
import com.example.miformacionctma.data.local.entity.ActividadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActividadDao {

    @Insert
    suspend fun insertar(actividad: ActividadEntity)

    @Update
    suspend fun actualizar(actividad: ActividadEntity)

    @Delete
    suspend fun eliminar(actividad: ActividadEntity)

    @Query("SELECT * FROM actividades ORDER BY id ASC")
    fun observarTodas(): Flow<List<ActividadEntity>>

    @Query("SELECT * FROM actividades WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): ActividadEntity?

    @Query("""
        SELECT * FROM actividades
        WHERE titulo LIKE '%' || :texto || '%'
        COLLATE NOCASE
        ORDER BY id ASC
    """)
    fun buscarPorTitulo(texto: String): Flow<List<ActividadEntity>>

    @Query("SELECT COUNT(*) FROM actividades")
    suspend fun contarActividades(): Int

    @Transaction
    @Query("SELECT * FROM actividades ORDER BY id ASC")
    fun observarActividadesConCompetencia(): Flow<List<ActividadConCompetencia>>

    @Transaction
    @Query("SELECT * FROM actividades WHERE id = :id LIMIT 1")
    suspend fun obtenerConCompetencia(id: Long): ActividadConCompetencia?
}
