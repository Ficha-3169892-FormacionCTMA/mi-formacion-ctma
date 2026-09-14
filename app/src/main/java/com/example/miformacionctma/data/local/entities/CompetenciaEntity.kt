package com.example.miformacionctma.data.local.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "competencias")
data class CompetenciaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String
)
