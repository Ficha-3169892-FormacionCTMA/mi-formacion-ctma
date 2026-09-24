package com.example.miformacionctma.data.local.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import androidx.room3.Upsert
import com.example.miformacionctma.data.local.entity.ActividadConCompetencia
import com.example.miformacionctma.data.local.entity.ActividadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActividadDao {

    @Upsert
    suspend fun insertar(actividad: ActividadEntity)

    @Upsert
    suspend fun insertarTodas(actividades: List<ActividadEntity>)

    @Update
    suspend fun actualizar(actividad: ActividadEntity)

    @Delete
    suspend fun eliminar(actividad: ActividadEntity)

    @Query("SELECT * FROM actividades ORDER BY id ASC")
    fun observarTodas(): Flow<List<ActividadEntity>>

    @Query("SELECT * FROM actividades WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): ActividadEntity?

    @Query(
        """
        SELECT * FROM actividades
        WHERE titulo LIKE '%' || :texto || '%'
        COLLATE NOCASE
        ORDER BY id ASC
    """
    )
    fun buscarPorTitulo(texto: String): Flow<List<ActividadEntity>>

    @Query("SELECT COUNT(*) FROM actividades")
    suspend fun contarActividades(): Int

    @Transaction
    @Query("SELECT * FROM actividades WHERE id = :id LIMIT 1")
    suspend fun obtenerConCompetencia(id: Long): ActividadConCompetencia?

    @Query("DELETE FROM actividades WHERE id NOT IN (:serverIds)")
    suspend fun eliminarActividadesNoPresentes(serverIds: List<Long>)

    @Query("DELETE FROM actividades")
    suspend fun eliminarTodas()
}
