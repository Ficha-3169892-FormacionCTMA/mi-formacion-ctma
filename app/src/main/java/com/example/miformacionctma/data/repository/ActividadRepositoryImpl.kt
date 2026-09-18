package com.example.miformacionctma.data.repository

import com.example.miformacionctma.data.local.ActividadDao
import com.example.miformacionctma.data.local.toDomain
import com.example.miformacionctma.data.local.toEntity
import com.example.miformacionctma.data.remote.CrearActividadDto
import com.example.miformacionctma.data.remote.RemoteActividadDataSource
import com.example.miformacionctma.data.remote.toDomain
import com.example.miformacionctma.model.ActividadFormativa
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ActividadRepositoryImpl(
    private val dao: ActividadDao,
    private val remote: RemoteActividadDataSource
) : ActividadRepository {

    override fun observarActividades(): Flow<List<ActividadFormativa>> {
        return dao.observarTodas()
            .map { lista ->
                lista.map { entity ->
                    entity.toDomain()
                }
            }
    }

    override fun buscar(texto: String): Flow<List<ActividadFormativa>> {
        return dao.buscar(texto.trim())
            .map { lista ->
                lista.map { entity ->
                    entity.toDomain()
                }
            }
    }

    override suspend fun guardar(
        actividad: ActividadFormativa
    ) {
        dao.guardar(
            actividad.toEntity()
        )
    }

    override suspend fun eliminar(
        id: Long
    ) {
        dao.eliminar(id)
    }

    override suspend fun refrescarDesdeServidor() {
        val actividadesRemotas = remote.obtenerActividades()
        android.util.Log.d("ActividadRepository", "Recibidas ${actividadesRemotas.size} actividades del servidor")
        val entities = actividadesRemotas.map { it.toDomain().toEntity() }
        dao.refrescarTodo(entities)
    }

    override suspend fun obtenerDesdeServidor(
        id: Long
    ): ActividadFormativa {

        val dto =
            remote.obtenerActividad(id)

        val actividad =
            dto.toDomain()

        dao.guardar(
            actividad.toEntity()
        )

        return actividad
    }

    override suspend fun crearEnServidor(
        actividad: ActividadFormativa
    ): ActividadFormativa {

        val datos = CrearActividadDto(
            titulo = actividad.titulo,
            descripcion = actividad.descripcion,
            fecha = actividad.fecha,
            progreso = actividad.progreso,
            diasRestantes = actividad.diasRestantes,
            prioridad = actividad.prioridad.name
        )

        val dto =
            remote.crearActividad(datos)

        val actividadCreada =
            dto.toDomain()

        dao.guardar(
            actividadCreada.toEntity()
        )

        return actividadCreada
    }

    override suspend fun actualizarEnServidor(
        actividad: ActividadFormativa
    ): ActividadFormativa {

        val datos = CrearActividadDto(
            titulo = actividad.titulo,
            descripcion = actividad.descripcion,
            fecha = actividad.fecha,
            progreso = actividad.progreso,
            diasRestantes = actividad.diasRestantes,
            prioridad = actividad.prioridad.name
        )

        val dto =
            remote.actualizarActividad(
                id = actividad.id,
                actividad = datos
            )

        val actividadActualizada =
            dto.toDomain()

        dao.guardar(
            actividadActualizada.toEntity()
        )

        return actividadActualizada
    }

    override suspend fun eliminarDelServidor(
        id: Long
    ) {

        remote.eliminarActividad(id)

        dao.eliminar(id)
    }
}