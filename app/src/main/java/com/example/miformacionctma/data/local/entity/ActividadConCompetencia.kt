package com.example.miformacionctma.data.local.entity

import androidx.room3.Embedded
import androidx.room3.Relation

data class ActividadConCompetencia(
    @Embedded
    val actividad: ActividadEntity,

    @Relation(
        parentColumns = ["competenciaId"],
        entityColumns = ["id"]
    )
    val competencia: CompetenciaEntity?
)
