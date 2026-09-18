package com.example.miformacionctma.data.local

import androidx.room.TypeConverter
import com.example.miformacionctma.model.EstadoEvidencia

class Converters {
    @TypeConverter
    fun fromEstadoEvidencia(estado: EstadoEvidencia): String {
        return estado.name
    }

    @TypeConverter
    fun toEstadoEvidencia(value: String): EstadoEvidencia {
        return try {
            EstadoEvidencia.valueOf(value)
        } catch (e: Exception) {
            EstadoEvidencia.LOCAL
        }
    }
}
