# Informe de Implementación Técnica: Concurrencia, Flow y Estado Reactivo
**Programa de Formación:** Análisis y Desarrollo de Software (ADSO)  
**Proyecto:** Mi Formación CTMA  
**Módulo:** Desarrollo de Aplicaciones Móviles Android — Semana 7

---

## 1. Introducción

El presente documento detalla la arquitectura asíncrona y reactiva desarrollada durante la Semana 7 para la aplicación móvil Mi Formación CTMA. El objetivo principal de este incremento radica en la integración de Jetpack Room, Preferences DataStore, ViewModel y Jetpack Compose mediante el uso de Corrutinas de Kotlin, `Flow` y `StateFlow`, garantizando la ejecución de operaciones no bloqueantes, la recolección consciente del ciclo de vida y la gestión determinista de estados de la interfaz de usuario (Android Developers, 2024a).

---

## 2. Arquitectura de Datos y Concurrencia

La solución sigue el patrón de diseño de Arquitectura Limpia recomendada por Google (Android Developers, 2024b), estructurada en capas con flujo de datos unidireccional (UDF):

Fuente de Datos (Room / DataStore) -> Repositorio (Main-Safe) -> ViewModel (StateFlow) -> UI (Jetpack Compose)

### 2.1. Capa de Persistencia y Repositorio
Las fuentes de datos locales expuestas a través de Interfaces de Acceso a Datos (DAO) y repositorios de preferencias emiten flujos continuos mediante `Flow`. El repositorio actúa como la fuente única de verdad, exponiendo funciones suspendidas que garantizan la seguridad en el hilo principal (*main-safety*) al delegar las operaciones de lectura y escritura intensivas al despachador `Dispatchers.IO` (Kotlin, 2023).

### 2.2. Búsqueda Reactiva y Cancelación de Corrutinas
Para optimizar las consultas a la base de datos y evitar el procesamiento de solicitudes obsoletas ante la entrada continua de texto por parte del usuario, se implementó una estrategia de transformación de flujos dentro de `ActividadesViewModel`.

Mediante la aplicación del operador `.debounce(300L)`, se introduce una retención temporal de 300 milisegundos. Posteriormente, se utiliza el operador `.flatMapLatest`, el cual cancela automáticamente cualquier trabajo de recolección anterior en respuesta a la emisión de una nueva cadena de búsqueda, ejecutando únicamente la consulta más reciente (Android Developers, 2024c).

---

## 3. Modelado de Estados de la Interfaz (UiState)

Con el fin de evitar la representación de estados imposibles en la interfaz de usuario, la aplicación segrega los estados de consulta de los estados de ejecución de operaciones (Android Developers, 2024d).

### 3.1. Estado de Consulta de Listado (`ListadoUiState`)
Se modela mediante una jerarquía de clases selladas (*sealed interface*) que representa los cuatro estados fundamentales de la pantalla principal:
1. `Cargando`: Indica la inicialización o procesamiento en curso de la consulta.
2. `Contenido`: Almacena y expone la lista de actividades leídas desde la base de datos.
3. `Vacio`: Define la ausencia de registros o de coincidencias bajo un criterio de búsqueda.
4. `Error`: Captura y expone mensajes de excepción para su visualización.

### 3.2. Estado de Operación (`OperacionUiState`)
Maneja el ciclo de vida de las operaciones de escritura (inserción, actualización y eliminación):
* `Inactiva`
* `EnCurso`
* `Exitosa`
* `Fallida`

Las excepciones ocurridas durante la ejecución dentro del alcance del ViewModel (`viewModelScope`) se gestionan mediante bloques de control de excepciones, garantizando el relanzamiento explícito de `CancellationException` para preservar la propagación de la cancelación estructural de las corrutinas (Kotlin, 2023).

El estado se expone hacia la capa de presentación mediante `StateFlow`, utilizando la función de transformación `stateIn` configurada con la estrategia `SharingStarted.WhileSubscribed(5_000)` para optimizar los recursos del sistema durante transiciones o pause del ciclo de vida.

---

## 4. Recolección Consciente del Ciclo de Vida en la UI

En la capa de presentación, desarrollada en Jetpack Compose, se utiliza la función `collectAsStateWithLifecycle()` proporcionada por la librería `androidx.lifecycle.runtime.compose`. Esta estrategia asegura que la recolección de los flujos `StateFlow` se detenga automáticamente cuando la interfaz de usuario no se encuentre en un estado visible en pantalla (mínimo `STARTED`), previniendo fugas de memoria y consumo innecesario de recursos de procesamiento en segundo plano (Android Developers, 2024a).

Las notificaciones de éxito o fallo resultantes de `OperacionUiState` son procesadas en la función navegacional `MiFormacionAppNav` mediante un bloque `LaunchedEffect`, el cual interactúa de forma asíncrona con el componente `SnackbarHostState`.

---

## 5. Matriz de Verificación de Casos de Aceptación

| Código | Descripción del Caso | Resultado de la Verificación |
| :--- | :--- | :--- |
| CA-01 | Inicialización sin actividades en la base de datos. Transición de estado de Cargando a Vacio sin emitir valores nulos. | Cumplido |
| CA-02 | Inserción o modificación de registros desde el formulario. Actualización automática de la lista en la interfaz sin requerir recarga manual. | Cumplido |
| CA-03 | Persistencia de filtros y ordenamiento en DataStore al reiniciar la aplicación. | Cumplido |
| CA-04 | Cancelación de consultas anteriores durante búsquedas continuas mediante el uso de flatMapLatest. | Cumplido |
| CA-05 | Gestión de fallos simulados en la capa de datos mostrando pantalla de Error con opción de reintento sin cierre de la aplicación. | Cumplido |
| CA-06 | Destrucción del alcance de ejecución (viewModelScope) al salir de la pantalla, cancelando los trabajos asíncronos activos. | Cumplido |
| CA-07 | Preservación del estado de la interfaz de usuario ante eventos de cambio de configuración (rotación de dispositivo). | Cumplido |
| CA-08 | Ejecución de pruebas unitarias deterministas utilizando corrutinas de prueba (runTest) y repositorios simulados sin uso de retardos por hilo (Thread.sleep). | Cumplido |

---

## 6. Referencias Bibliográficas

* Android Developers. (2024a). *Corrutinas de Kotlin con componentes conscientes del ciclo de vida*. Google Developers. https://developer.android.com/topic/libraries/architecture/coroutines
* Android Developers. (2024b). *Guía de arquitectura de aplicaciones*. Google Developers. https://developer.android.com/topic/architecture
* Android Developers. (2024c). *StateFlow y SharedFlow*. Google Developers. https://developer.android.com/kotlin/flow/stateflow-and-sharedflow
* Android Developers. (2024d). *Producción del estado de la UI*. Google Developers. https://developer.android.com/topic/architecture/ui-layer/state-production
* Kotlin. (2023). *Coroutines guide: Cancellation and exceptions*. Kotlin Language Documentation. https://kotlinlang.org/docs/exception-handling.html