package com.example.miformacionctma.data.local

import androidx.room3.ColumnTypeConverter
import com.example.miformacionctma.data.local.entity.EstadoSincronizacion
import java.time.Instant

class Converters {
    @ColumnTypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @ColumnTypeConverter
    fun dateToTimestamp(date: Instant?): Long? {
        return date?.toEpochMilli()
    }

    @ColumnTypeConverter
    fun fromEstadoSincronizacion(value: EstadoSincronizacion?): String? {
        return value?.name
    }

    @ColumnTypeConverter
    fun toEstadoSincronizacion(value: String?): EstadoSincronizacion? {
        return value?.let { 
            try {
                EstadoSincronizacion.valueOf(it)
            } catch (e: IllegalArgumentException) {
                EstadoSincronizacion.LOCAL
            }
        }
    }
}
