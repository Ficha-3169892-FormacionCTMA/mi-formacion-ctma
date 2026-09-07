package com.example.miformacionctma.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad

@Entity(tableName = "actividades")
data class ActividadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val titulo: String,
    val descripcion: String,
    val fecha: String,
    val progreso: Int,
    val diasRestantes: Int,
    val prioridad: String
)

fun ActividadEntity.toDomain(): ActividadFormativa {
    return ActividadFormativa(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        fecha = fecha,
        progreso = progreso,
        diasRestantes = diasRestantes,
        prioridad = Prioridad.valueOf(prioridad)
    )
}

fun ActividadFormativa.toEntity(): ActividadEntity {
    return ActividadEntity(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        fecha = fecha,
        progreso = progreso,
        diasRestantes = diasRestantes,
        prioridad = prioridad.name
    )
}
