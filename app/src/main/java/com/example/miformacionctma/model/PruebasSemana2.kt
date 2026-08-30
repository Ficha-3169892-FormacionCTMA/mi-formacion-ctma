package com.example.miformacionctma.model

object PruebasSemana2 {
    fun ejecutar() {
        // Escenario 1: título vacío (Inválido)
        val tituloVacio = ActividadFormativa(
            id = 1L,
            titulo = "   ",
            descripcion = "Intento de registro sin título",
            fecha = "2026-09-15",
            progreso = 50,
            diasRestantes = 3,
            prioridad = Prioridad.MEDIA
        )

        println("Escenario 1 (Título Vacío):")
        println(ReglasActividad.validarActividad(tituloVacio))

        // Escenario 2: progreso inválido (> 100)
        val progresoInvalido = ActividadFormativa(
            id = 2L,
            titulo = "Aprender Kotlin",
            descripcion = "Progreso fuera de rango",
            fecha = "2026-09-10",
            progreso = 120,
            diasRestantes = 3,
            prioridad = Prioridad.MEDIA
        )

        println("\nEscenario 2 (Progreso Inválido):")
        println(ReglasActividad.validarActividad(progresoInvalido))

        // Escenario 3: vencida (días restantes negativos / fecha pasada)
        val vencida = ActividadFormativa(
            id = 3L,
            titulo = "Entrega de Guía",
            descripcion = "Actividad con plazo expirado",
            fecha = "2026-08-01",
            progreso = 80,
            diasRestantes = -1,
            prioridad = Prioridad.ALTA
        )

        println("\nEscenario 3 (Vencida):")
        println(ReglasActividad.estadoActividad(vencida))

        // Escenario 4: completada (progreso 100%)
        val completa = ActividadFormativa(
            id = 4L,
            titulo = "Taller Final",
            descripcion = "Actividad entregada al 100%",
            fecha = "2026-08-20",
            progreso = 100,
            diasRestantes = -2,
            prioridad = Prioridad.MEDIA
        )

        println("\nEscenario 4 (Completada):")
        println(ReglasActividad.estadoActividad(completa))

        // Escenario 5: lista vacía
        println("\nEscenario 5 (Promedio de Lista Vacía):")
        println(ReglasActividad.promedioProgreso(emptyList()))

        // Escenario 6: búsqueda flexible por título
        val lista = listOf(
            ActividadFormativa(
                id = 5L,
                titulo = "Kotlin básico",
                descripcion = "Introducción al lenguaje",
                fecha = "2026-09-05",
                progreso = 10,
                diasRestantes = 5,
                prioridad = Prioridad.BAJA
            )
        )

        println("\nEscenario 6 (Búsqueda '  kotlin '):")
        println(ReglasActividad.buscarPorTitulo(lista, "  kotlin "))

        // Escenario 7: ordenamiento de actividades
        val listaParaOrdenar = listOf(
            ActividadFormativa(6L, "Normal Baja", "Prioridad baja", "2026-09-12", 0, 5, Prioridad.BAJA),
            ActividadFormativa(7L, "Actividad Vencida", "Vencida", "2026-08-10", 20, -1, Prioridad.MEDIA),
            ActividadFormativa(8L, "Normal Alta", "Prioridad alta", "2026-09-02", 10, 2, Prioridad.ALTA)
        )

        println("\nEscenario 7 (Ordenadas):")
        val ordenadas = ReglasActividad.ordenarActividades(listaParaOrdenar)
        ordenadas.forEach {
            println("- ${it.titulo} (Estado: ${ReglasActividad.estadoActividad(it)}, Prioridad: ${it.prioridad})")
        }
    }
}