package com.example.miformacionctma.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Embedded
import androidx.room3.Relation
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import com.example.miformacionctma.data.local.entities.ActividadEntity
import com.example.miformacionctma.data.local.entities.CompetenciaEntity
import kotlinx.coroutines.flow.Flow

data class CompetenciaConActividades(
    @Embedded val competencia: CompetenciaEntity,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["competenciaId"]
    )
    val actividades: List<ActividadEntity>
)

@Dao
interface CompetenciaDao {
    @Transaction
    @Query("SELECT * FROM competencias ORDER BY nombre")
    fun observarConActividades(): Flow<List<CompetenciaConActividades>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(competencia: CompetenciaEntity): Long
}
