package com.example.miformacionctma.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActividadDao {

    @Query("SELECT * FROM actividades ORDER BY id ASC")
    fun observarTodas(): Flow<List<ActividadEntity>>

    @Query("""
        SELECT * FROM actividades
        WHERE titulo LIKE '%' || :texto || '%'
        OR descripcion LIKE '%' || :texto || '%'
        ORDER BY id ASC
    """)
    fun buscar(texto: String): Flow<List<ActividadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(actividad: ActividadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodas(actividades: List<ActividadEntity>)

    @Query("DELETE FROM actividades")
    suspend fun borrarTodas()

    @androidx.room.Transaction
    suspend fun refrescarTodo(actividades: List<ActividadEntity>) {
        borrarTodas()
        guardarTodas(actividades)
    }

    @Query("DELETE FROM actividades WHERE id = :id")
    suspend fun eliminar(id: Long)
}
