# MiFormacionCTMA

Aplicación Android desarrollada con **Kotlin** y **Jetpack Compose** para organizar actividades, compromisos y evidencias del proceso formativo CTMA, aplicando conceptos de **UI declarativa, Material 3, accesibilidad y trabajo colaborativo con SCRUM**.

---

## Requisitos

- Android Studio
- JDK instalado
- SDK de Android instalado
- Dispositivo físico con **depuración inalámbrica** habilitada
- Conexión a la misma red local entre el dispositivo y el computador

### Alternativa

También puede ejecutarse mediante un **AVD Android** compatible.

---

## Semana 8: Servicios Web, Caché y Resiliencia

Se ha implementado una arquitectura **offline-first** que integra servicios remotos y persistencia local.

### Arquitectura de Datos
- **Retrofit + OkHttp**: Consumo de API REST con timeouts configurados y logs securizados.
- **Room**: Fuente única de verdad (SSOT) que observa la UI mediante `Flow`.
- **Offline-First**: Las respuestas del servidor se persisten en Room de forma atómica.
- **Resiliencia**: Clasificación de errores (401, 404, 5xx, Sin Red, Timeout) y manejo seguro de `CancellationException`.

### Autenticación
- **TokenProvider**: Abstracción para el manejo de credenciales.
- **AuthInterceptor**: Inyección automática del token `Bearer` en las cabeceras de red.

### Pruebas de Integración
- **MockWebServer**: Simulación de diversos escenarios de red (éxito, errores HTTP, JSON inválido) para garantizar la robustez del repositorio.

---

## Ejecución

### Opción recomendada: dispositivo físico (inalámbrico)

1. Abrir el proyecto en Android Studio.
2. Esperar la sincronización de Gradle.
3. Activar **Depuración inalámbrica** en el dispositivo Android.
4. Vincular el dispositivo desde **Device Manager**.
5. Seleccionar el dispositivo conectado.
6. Ejecutar con **Run ▶**.

### Opción alternativa: emulador

1. Abrir **Device Manager**.
2. Seleccionar un AVD compatible (por ejemplo **Pixel 4 - Android 11**).
3. Iniciar el emulador.
4. Ejecutar la aplicación con **Run ▶**.

---

## Pruebas realizadas

Se han implementado pruebas automáticas en varios niveles:

1. **Pruebas Unitarias (JUnit 4)**:
    - Lógica de negocio: `promedioProgreso()`, `actividadesUrgentes()`, etc.
    - Mapeo de datos: Transformación correcta entre DTOs y Entidades.

2. **Pruebas de Integración (MockWebServer + Mockito)**:
    - Escenarios de éxito (200 OK) con actualización de caché.
    - Manejo de errores 401 (No autorizado) y 500 (Error servidor).
    - Resiliencia ante JSON malformado.
    - Verificación de la atomicidad de las transacciones en Room durante el refresh.

Todas las pruebas finalizan correctamente.

---

## Organización del proyecto

- `app/` → código fuente Android.
- `docs/` → documentación organizada por semanas, con respuestas, análisis y evidencias de cada actividad.
- `README.md` → información general y guía de ejecución del proyecto.
- `.gitignore` → archivos y carpetas excluidos del control de versiones.

---

## Trabajo colaborativo y SCRUM

El proyecto fue desarrollado utilizando un **repositorio compartido en GitHub** y trabajo en **ramas por integrante**, aplicando conceptos de:

- **SCRUM Master y Development Team**
- Incrementos funcionales por Sprint
- Componentes reutilizables
- Integración y prueba cruzada entre integrantes

---

## Estado actual

El proyecto cuenta con una **interfaz funcional, adaptable y accesible**, lista para continuar con futuras iteraciones relacionadas con **persistencia de datos, navegación y arquitectura avanzada en Compose**.
