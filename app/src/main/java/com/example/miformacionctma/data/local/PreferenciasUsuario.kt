package com.example.miformacionctma.data.local

data class PreferenciasUsuario(
    val filtroCompetencia: String? = null,
    val ordenDescendente: Boolean = true,
    val modoCuadricula: Boolean = false
)