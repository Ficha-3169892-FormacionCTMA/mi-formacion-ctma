package com.example.miformacionctma.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Entity(tableName = "actividades")
data class ActividadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val descripcion: String,
    val fecha: String,
    val prioridad: Prioridad = Prioridad.MEDIA, // Guardar prioridad en Room
    val progreso: Int = 0,
    val completada: Boolean = false,
    // false = hay cambios locales que aún no se han enviado a Supabase
    @ColumnInfo(defaultValue = "0") val sincronizada: Boolean = false,
    // true = el usuario la borró; se oculta y se elimina de verdad cuando Supabase confirma el borrado
    @ColumnInfo(defaultValue = "0") val eliminada: Boolean = false
)

fun ActividadEntity.toActividadFormativa(): ActividadFormativa {
    // Manejo seguro de fecha para evitar cierres si el formato viene invertido (yyyy-MM-dd / dd/MM/yyyy)
    val diasCalculados = try {
        val formatter = if (this.fecha.contains("-")) {
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
        } else {
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
        }
        val fechaLimite = LocalDate.parse(this.fecha, formatter)
        ChronoUnit.DAYS.between(LocalDate.now(), fechaLimite).toInt()
    } catch (e: Exception) {
        0
    }

    return ActividadFormativa(
        id = this.id.toLong(),
        titulo = this.titulo,
        descripcion = this.descripcion,
        fecha = this.fecha,
        diasRestantes = diasCalculados,
        prioridad = this.prioridad, // Pasar la prioridad real a la UI
        progreso = this.progreso
    )
}