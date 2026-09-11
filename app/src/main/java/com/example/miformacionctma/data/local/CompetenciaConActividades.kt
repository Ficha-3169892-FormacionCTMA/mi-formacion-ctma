package com.example.miformacionctma.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class CompetenciaConActividades(
    @Embedded val competencia: CompetenciaEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "competenciaId"
    )
    val actividades: List<ActividadEntity>
)