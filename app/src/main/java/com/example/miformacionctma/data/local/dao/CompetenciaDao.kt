package com.example.miformacionctma.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.example.miformacionctma.data.local.entity.CompetenciaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompetenciaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(competencia: CompetenciaEntity): Long

    @Query("SELECT * FROM competencias ORDER BY nombre ASC")
    fun observarTodas(): Flow<List<CompetenciaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(competencias: List<CompetenciaEntity>)

    @Query("SELECT COUNT(*) FROM competencias")
    suspend fun contar(): Int

    @Query("DELETE FROM competencias")
    suspend fun eliminarTodas()
}
