# MiFormacionCTMA

Aplicación Android desarrollada con **Kotlin** y **Jetpack Compose** para organizar actividades,
compromisos y evidencias del proceso formativo CTMA, aplicando conceptos de **UI declarativa,
Material 3, accesibilidad, concurrencia avanzada con Corrutinas y flujos reactivos (Flow), y trabajo
colaborativo con SCRUM**.

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

## Arquitectura del Proyecto

La aplicación implementa una **Arquitectura en Capas Guiada por el Dominio (Clean Architecture)**
adaptando las pautas recomendadas de flujo unidireccional (UVI):

1. **Capa de Interfaz de Usuario (UI Layer):**
    - Diseñada bajo el patrón **Route - Screen**. `PantallaActividadesRoute` es el componente
      acoplado a la infraestructura que maneja el ciclo de vida del ViewModel, mientras que
      `PantallaActividadesScreen` es una función Composable sin estado (*stateless*), lo que
      incrementa su testabilidad y facilita las vistas previas (*Previews*).
    - Recolección de estados de forma eficiente mediante el operador consciente del ciclo de vida *
      *`collectAsStateWithLifecycle()`**.
2. **Capa del Presentador Lógico (ViewModel):**
    - Centraliza las interacciones y funciona como el cerebro reactivo de la aplicación.
    - Combina flujos reactivos fríos transformándolos en un único estado inmutable expuesto mediante
      un `StateFlow`.
3. **Capa de Datos (Data Layer - Repositorios/Fuentes de Datos):**
    - **Room Database:** Fuente local canónica de verdad. Almacena de forma persistente las entidades exponiendo flujos asíncronos continuos a través de DAOs. Utiliza transacciones atómicas (`useWriterConnection`) para garantizar la integridad durante la sincronización.
    - **Retrofit & OkHttp:** Capa de red para la sincronización remota. Configurada con *timeouts* explícitos y un sistema de autenticación basado en `Interceptor` para inyectar tokens Bearer de forma dinámica.
    - **DataStore Preferences:** Guarda configuraciones efímeras del usuario de manera transaccional (ej. criterios de ordenamiento).
    - **Offline-First Policy:** La UI nunca consume datos directamente de la red (DTOs). El Repositorio coordina la descarga, el mapeo y la persistencia en Room, desde donde la UI observa los cambios.

---

## Concurrencia, Red y Seguridad

El proyecto sigue una estricta política de **Main-Safety** y seguridad de datos:

- **Autenticación Segura:** Los tokens no están *hardcoded* ni se guardan en el `BuildConfig`. Se inyectan en tiempo de ejecución mediante un `TokenProvider` y se redactan automáticamente en los logs de red para evitar fugas.
- **Gestión de Estados de Sincronización:** Se manejan dos dimensiones de estado: el contenido (datos locales siempre disponibles) y el estado de refresco (`RefreshUiState`: Idle, Running, Success, Failed).
- **Resiliencia ante Fallos:** Si la red falla, la caché local se mantiene intacta. Se clasifican los errores (401, 404, 5xx, No conexión, Timeout) para ofrecer una respuesta visual acorde y accionable (Reintentar).
- **Asincronía Reactiva Continuada (`Flow`):** Room y DataStore exponen streams asíncronos continuos
  que operan de forma segura nativa en hilos de background independientes. Por este motivo, se evitó
  la inyección innecesaria o redundante de `withContext(Dispatchers.IO)` en la capa del ViewModel,
  manteniendo el código limpio y ágil (*main-safe by default*).
- **Cancelación Cooperativa con `flatMapLatest`:** Implementado en la barra de búsqueda. Cuando el
  usuario escribe rápidamente, el operador cancela de forma automática la corrutina de la consulta
  SQLite obsoleta anterior, enviando a la base de datos únicamente la petición más reciente.
- **Optimización de Recursos con `WhileSubscribed(5_000)`:** El flujo compartido caliente (
  `stateIn`) retiene la información en memoria durante interrupciones breves de la UI (como la
  rotación del dispositivo), pero suspende las consultas a la base de datos si la aplicación pasa a
  segundo plano más de 5 segundos, optimizando la batería y la RAM.
- **Manejo Correcto de `CancellationException`:** Todas las corrutinas de mutación de datos (
  `insertar`, `actualizar`, `eliminar`) dentro del `viewModelScope` capturan excepciones controladas
  en bloques `try/catch` pero relanzan explícitamente cualquier `CancellationException` para no
  interferir con la maquinaria interna de cancelación cooperativa de Kotlin.

---

## Pruebas Realizadas y Testing Determinista

Se implementaron pruebas automatizadas con **JUnit** para verificar reglas de negocio, persistencia,
flujos de estado y migración de datos. La suite incluye un total de **24 pruebas automatizadas** que
finalizan correctamente en verde:

* **Pruebas de Repositorio y Red (`ActividadRepositoryTest`):**
    - Simulación de servidor real mediante **`MockWebServer`**, permitiendo pruebas deterministas sin dependencia de internet.
    - Verificación de 8 escenarios críticos: éxito (200), lista vacía, fallos de autenticación (401), errores de servidor (500), JSON inválido, *timeouts*, concurrencia y cancelación.
    - Uso de **`MockK`** para interceptar y validar transacciones atómicas en la base de datos.
* **Pruebas de ViewModel y Flujos de Estado (`ActividadesViewModelTest`):**
    - Implementadas bajo entornos de tiempo virtual con **`runTest`** y `StandardTestDispatcher`
      eliminando por completo retardos físicos o bloqueos como `Thread.sleep()`.
    - Simulación aislada de la capa de datos mediante dobles de prueba (**`FakeActividadRepository`
      ** y **`FakePreferenciasRepository`**).
    - Prueba de flujos calientes mediante recolección explícita dentro del **`backgroundScope`** y
      avance controlado de reloj con `advanceUntilIdle()`.
    - Verificación exitosa de la máquina de estados: transiciones predecibles de
      `Cargando` $\rightarrow$ `Vacio` $\rightarrow$ `Contenido`.
* **Reglas de negocio funcionales (`PlanesDePruebaTest` y `ReglasActividadTest`):**
    - Casos de prueba funcionales incluyendo validaciones de entrada, navegación entre pantallas con
      paso de argumentos, búsquedas, control del slider de progreso (límites 0% y 100%) y marcado de
      alertas de urgencia (fechas <= 2 días restantes).
* **Persistencia e Infraestructura Local:**
    - Operaciones, conteos y consultas relacionales en `ActividadDaoTest`.
    - Migración de la base de datos entre versiones y validación estructural en `MigrationTest`.

---

## Organización del Proyecto

- `app/` → código fuente Android organizado por capas (data, domain, model, ui).
- `docs/` → documentación organizada por semanas, con respuestas, análisis y evidencias de cada
  actividad.
- `README.md` → información general, justificación de arquitectura y guía de ejecución del proyecto.
- `.gitignore` → archivos y carpetas excluidos del control de versiones.

---

## Trabajo Colaborativo y SCRUM

El proyecto fue desarrollado utilizando un **repositorio compartido en GitHub** y trabajo en **ramas
por integrante**, aplicando conceptos de:

- **SCRUM Master y Development Team**
- Incrementos funcionales por Sprint
- Componentes reutilizables
- Integración y prueba cruzada entre integrantes

---

## Estado Actual

El proyecto cuenta con una **arquitectura totalmente reactiva, funcional, adaptable y accesible**,
implementando persistencia local robusta mediante **Room** y almacenamiento de preferencias con *
*DataStore**.

Toda la gestión de datos se maneja bajo flujos asíncronos asumiendo la responsabilidad de la
seguridad de hilos (*main-safety*), aislando por completo las entidades SQLite de la capa visual. La
interfaz responde en tiempo real a las consultas, permitiendo búsquedas de cancelación optimizada
por hardware y ordenamiento dinámico preservado ante rotaciones accidentales. La estabilidad
completa está garantizada por una amplia suite de 24 pruebas unitarias e instrumentadas en verde.
