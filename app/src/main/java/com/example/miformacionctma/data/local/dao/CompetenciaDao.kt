package com.example.miformacionctma.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Embedded
import androidx.room3.Relation
import com.example.miformacionctma.data.local.entities.CompetenciaEntity
import com.example.miformacionctma.data.local.entities.ActividadEntity
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
}
