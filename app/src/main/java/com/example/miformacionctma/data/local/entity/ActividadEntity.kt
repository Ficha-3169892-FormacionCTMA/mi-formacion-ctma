package com.example.miformacionctma.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad

@Entity(tableName = "actividades")
data class ActividadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val descripcion: String,
    val fecha: String,
    val prioridad: Prioridad = Prioridad.MEDIA, // Guardar prioridad en Room
    val progreso: Int = 0,
    val completada: Boolean = false
)

fun ActividadEntity.toActividadFormativa(): ActividadFormativa {
    return ActividadFormativa(
        id = this.id.toLong(),
        titulo = this.titulo,
        descripcion = this.descripcion,
        fecha = this.fecha,
        diasRestantes = 0,
        prioridad = this.prioridad, // Pasar la prioridad real a la UI
        progreso = this.progreso
    )
}