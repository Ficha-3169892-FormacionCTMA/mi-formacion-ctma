package com.example.miformacionctma.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.example.miformacionctma.data.local.entity.CompetenciaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompetenciaDao {

    @Insert
    suspend fun insertar(competencia: CompetenciaEntity): Long

    @Query("SELECT * FROM competencias ORDER BY nombre ASC")
    fun observarTodas(): Flow<List<CompetenciaEntity>>

    @Query("SELECT * FROM competencias WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): CompetenciaEntity?

    @Insert
    suspend fun insertarTodas(competencias: List<CompetenciaEntity>)

    @Query("SELECT COUNT(*) FROM competencias")
    suspend fun contar(): Int
}
