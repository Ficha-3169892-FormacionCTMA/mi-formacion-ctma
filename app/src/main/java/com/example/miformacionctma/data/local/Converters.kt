package com.example.miformacionctma.data.local

import androidx.room3.ColumnTypeConverter
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
}
