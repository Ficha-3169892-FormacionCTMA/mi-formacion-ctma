package com.example.miformacionctma.domain

import com.example.miformacionctma.data.local.entity.CompetenciaEntity

object CompetenciasDemo {
    val listaInicial = listOf(
        CompetenciaEntity(
            id = 1L,
            nombre = "Desarrollo de software"
        ),
        CompetenciaEntity(
            id = 2L,
            nombre = "Diseño y accesibilidad"
        ),
        CompetenciaEntity(
            id = 3L,
            nombre = "Gestión de proyectos"
        )
    )
}
