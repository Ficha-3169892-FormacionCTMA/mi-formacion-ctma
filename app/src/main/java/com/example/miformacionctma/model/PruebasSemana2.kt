package com.example.miformacionctma.model

import java.time.Instant

object PruebasSemana2 {
    fun ejecutar() {
        // Escenario 1: título vacío (Inválido)
        val tituloVacio = ActividadFormativa(
            id = 1L,
            titulo = "   ",
            descripcion = "Intento de registro sin título",
            fecha = Instant.parse("2026-09-15T10:00:00Z"),
            progreso = 50,
            prioridad = Prioridad.MEDIA,
            completada = false
        )

        println("Escenario 1 (Título Vacío):")
        println(ReglasActividad.validarActividad(tituloVacio))

        // Escenario 2: progreso inválido (> 100)
        val progresoInvalido = ActividadFormativa(
            id = 2L,
            titulo = "Aprender Kotlin",
            descripcion = "Progreso fuera de rango",
            fecha = Instant.parse("2026-09-10T10:00:00Z"),
            progreso = 120,
            prioridad = Prioridad.MEDIA,
            completada = false
        )

        println("\nEscenario 2 (Progreso Inválido):")
        println(ReglasActividad.validarActividad(progresoInvalido))

        // Escenario 3: vencida (días restantes negativos / fecha pasada)
        val vencida = ActividadFormativa(
            id = 3L,
            titulo = "Entrega de Guía",
            descripcion = "Actividad con plazo expirado",
            fecha = Instant.parse("2026-08-01T10:00:00Z"),
            progreso = 80,
            prioridad = Prioridad.ALTA,
            completada = false
        )

        println("\nEscenario 3 (Vencida):")
        println(ReglasActividad.estadoActividad(vencida))

        // Escenario 4: completada (progreso 100%)
        val completa = ActividadFormativa(
            id = 4L,
            titulo = "Taller Final",
            descripcion = "Actividad entregada al 100%",
            fecha = Instant.parse("2026-08-20T10:00:00Z"),
            progreso = 100,
            prioridad = Prioridad.MEDIA,
            completada = true
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
                fecha = Instant.parse("2026-09-05T10:00:00Z"),
                progreso = 10,
                prioridad = Prioridad.BAJA,
                completada = false
            )
        )

        println("\nEscenario 6 (Búsqueda '  kotlin '):")
        println(ReglasActividad.buscarPorTitulo(lista, "  kotlin "))

        // Escenario 7: ordenamiento de actividades
        val listaParaOrdenar = listOf(
            ActividadFormativa(
                6L,
                "Normal Baja",
                "Prioridad baja",
                Instant.parse("2026-09-12T10:00:00Z"),
                0,
                Prioridad.BAJA,
                completada = false
            ),
            ActividadFormativa(
                7L,
                "Actividad Vencida",
                "Vencida",
                Instant.parse("2026-08-10T10:00:00Z"),
                20,
                Prioridad.MEDIA,
                completada = false
            ),
            ActividadFormativa(
                8L,
                "Normal Alta",
                "Prioridad alta",
                Instant.parse("2026-09-02T10:00:00Z"),
                10,
                Prioridad.ALTA,
                completada = false
            )
        )

        println("\nEscenario 7 (Ordenadas):")
        val ordenadas = ReglasActividad.ordenarActividades(listaParaOrdenar)
        ordenadas.forEach {
            println("- ${it.titulo} (Estado: ${ReglasActividad.estadoActividad(it)}, Prioridad: ${it.prioridad})")
        }
    }
}
