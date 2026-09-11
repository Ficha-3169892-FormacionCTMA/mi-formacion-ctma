package com.example.miformacionctma.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CompetenciaDao {

    @Transaction
    @Query("SELECT * FROM competencias ORDER BY nombre ASC")
    fun observarConActividades(): Flow<List<CompetenciaConActividades>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(competencia: CompetenciaEntity): Long
}