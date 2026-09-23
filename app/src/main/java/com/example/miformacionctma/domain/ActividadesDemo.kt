package com.example.miformacionctma.domain

import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.Prioridad
import java.time.Instant

object ActividadesDemo {
    val listaInicial = listOf(
        ActividadFormativa(
            id = 1L,
            titulo = "Construir pantalla Compose accesible",
            descripcion = "Crear la interfaz principal usando Jetpack Compose y Material 3.",
            fecha = Instant.parse("2026-09-02T10:00:00Z"),
            progreso = 65,
            prioridad = Prioridad.ALTA
        ),
        ActividadFormativa(
            id = 2L,
            titulo = "Diseñar tarjeta reutilizable de actividades",
            descripcion = "Implementar TarjetaActividad como composable reutilizable y desacoplado.",
            fecha = Instant.parse("2026-09-04T10:00:00Z"),
            progreso = 40,
            prioridad = Prioridad.ALTA
        ),
        ActividadFormativa(
            id = 3L,
            titulo = "Validar títulos largos y adaptación visual en diferentes tamaños de pantalla",
            descripcion = "Comprobar que el diseño no se recorte cuando el texto sea demasiado extenso.",
            fecha = Instant.parse("2026-09-06T10:00:00Z"),
            progreso = 20,
            prioridad = Prioridad.MEDIA
        ),
        ActividadFormativa(
            id = 4L,
            titulo = "Preparar estado vacío de la pantalla",
            descripcion = "Mostrar un mensaje comprensible cuando no existan actividades registradas.",
            fecha = Instant.parse("2026-08-29T10:00:00Z"),
            progreso = 100,
            prioridad = Prioridad.MEDIA
        ),
        ActividadFormativa(
            id = 5L,
            titulo = "Aplicar tema Material 3 y tipografía consistente",
            descripcion = "Centralizar colores, tamaños y estilos del proyecto para evitar duplicación.",
            fecha = Instant.parse("2026-09-01T10:00:00Z"),
            progreso = 55,
            prioridad = Prioridad.ALTA
        ),
        ActividadFormativa(
            id = 6L,
            titulo = "Revisar accesibilidad con fuente grande",
            descripcion = "Probar escalado de texto, contraste y tamaños táctiles adecuados para distintos usuarios.",
            fecha = Instant.parse("2026-09-05T10:00:00Z"),
            progreso = 15,
            prioridad = Prioridad.MEDIA
        ),
        ActividadFormativa(
            id = 7L,
            titulo = "Implementar LazyColumn con claves estables",
            descripcion = "Usar key = { it.id } para mejorar la recomposición y mantener el estado de los elementos.",
            fecha = Instant.parse("2026-08-30T10:00:00Z"),
            progreso = 80,
            prioridad = Prioridad.ALTA
        ),
        ActividadFormativa(
            id = 8L,
            titulo = "Agregar adaptación para pantallas anchas con BoxWithConstraints",
            descripcion = "Cambiar entre lista y cuadrícula según el ancho disponible del dispositivo.",
            fecha = Instant.parse("2026-09-03T10:00:00Z"),
            progreso = 30,
            prioridad = Prioridad.MEDIA
        ),
        ActividadFormativa(
            id = 9L,
            titulo = "Documentar decisiones de UX y accesibilidad",
            descripcion = "Registrar hallazgos encontrados durante las pruebas y las correcciones realizadas.",
            fecha = Instant.parse("2026-09-07T10:00:00Z"),
            progreso = 10,
            prioridad = Prioridad.BAJA
        ),
        ActividadFormativa(
            id = 10L,
            titulo = "Preparar demostración final y revisión cruzada del Sprint de UI Compose",
            descripcion = "Verificar funcionamiento, capturas, commits y explicación técnica del incremento desarrollado.",
            fecha = Instant.parse("2026-09-09T10:00:00Z"),
            progreso = 0,
            prioridad = Prioridad.MEDIA
        )
    )
}
