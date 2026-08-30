package com.example.miformacionctma.model

object PruebasSemana2 {

    fun ejecutar() {

        // Escenario 1: título vacío
        val tituloVacio = ActividadFormativa(
            1, "   ", null, 50, 3, Prioridad.MEDIA
        )

        println("Escenario 1:")
        println(ReglasActividad.validarActividad(tituloVacio))

        // Escenario 2: progreso inválido
        val progresoInvalido = ActividadFormativa(
            2, "Kotlin", null, 120, 3, Prioridad.MEDIA
        )

        println("Escenario 2:")
        println(ReglasActividad.validarActividad(progresoInvalido))

        // Escenario 3: vencida
        val vencida = ActividadFormativa(
            3, "Entrega", null, 80, -1, Prioridad.ALTA
        )

        println("Escenario 3:")
        println(ReglasActividad.estadoActividad(vencida))

        // Escenario 4: completada
        val completa = ActividadFormativa(
            4, "Final", null, 100, -2, Prioridad.MEDIA
        )

        println("Escenario 4:")
        println(ReglasActividad.estadoActividad(completa))

        // Escenario 5: lista vacía
        println("Escenario 5:")
        println(ReglasActividad.promedioProgreso(emptyList()))

        // Escenario 6: búsqueda flexible
        val lista = listOf(
            ActividadFormativa(
                5, "Kotlin básico", null, 10, 5, Prioridad.BAJA
            )
        )

        println("Escenario 6:")
        println(ReglasActividad.buscarPorTitulo(lista, "  kotlin "))

        // Escenario 7: ordenamiento de actividades
        val listaParaOrdenar = listOf(
            ActividadFormativa(6, "Normal Baja", null, 0, 5, Prioridad.BAJA),
            ActividadFormativa(7, "Actividad Vencida", null, 20, -1, Prioridad.MEDIA),
            ActividadFormativa(8, "Normal Alta", null, 10, 2, Prioridad.ALTA)
        )

        println("Escenario 7 (Ordenadas):")
        val ordenadas = ReglasActividad.ordenarActividades(listaParaOrdenar)
        ordenadas.forEach { println("- ${it.titulo} (${ReglasActividad.estadoActividad(it)}, Prioridad: ${it.prioridad})") }
    }
}