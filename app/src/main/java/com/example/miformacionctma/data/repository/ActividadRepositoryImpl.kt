package com.example.miformacionctma.data.repository

import com.example.miformacionctma.domain.ActividadesDemo
import com.example.miformacionctma.model.ActividadFormativa
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class ActividadRepositoryImpl : ActividadRepository {

    private val actividades =
        MutableStateFlow(ActividadesDemo.listaInicial)

    override fun observarActividades(): Flow<List<ActividadFormativa>> =
        actividades

    override fun buscar(
        texto: String
    ): Flow<List<ActividadFormativa>> =
        actividades.map { lista ->

            val textoNormalizado = texto.trim()

            if (textoNormalizado.isEmpty()) {
                lista
            } else {
                lista.filter { actividad ->
                    actividad.titulo.contains(
                        textoNormalizado,
                        ignoreCase = true
                    ) ||
                            actividad.descripcion.contains(
                                textoNormalizado,
                                ignoreCase = true
                            )
                }
            }
        }

    override suspend fun guardar(
        actividad: ActividadFormativa
    ) {
        actividades.update { lista ->
            lista + actividad
        }
    }

    override suspend fun eliminar(id: Long) {
        actividades.update { lista ->
            lista.filterNot { it.id == id }
        }
    }
}
