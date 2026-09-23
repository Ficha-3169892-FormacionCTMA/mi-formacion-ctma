package com.example.miformacionctma.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CompetenciaDto(
    @SerialName("id")
    val id: Long,
    @SerialName("nombre")
    val nombre: String
)
