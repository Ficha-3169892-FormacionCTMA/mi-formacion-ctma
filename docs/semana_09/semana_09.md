# Proyecto Formatice CTMA: Capacidades del Dispositivo, Seguridad y Sincronización Offline-First con Supabase

**Programa:** Análisis y Desarrollo de Software (ADSO)  
**Institución:** Servicio Nacional de Aprendizaje (SENA)  
**Centro de Formación:** Centro de Tecnología de la Manufactura Avanzada (CTMA)  
**Módulo:** Desarrollo de Aplicaciones Móviles Android  
**Semana:** 09  
**Fecha:** Septiembre de 2026

---

## 1. Resumen Ejecutivo

El presente documento registra el incremento correspondiente a la Semana 9 de la aplicación móvil Mi Formación CTMA. En este incremento se incorporaron las capacidades del dispositivo necesarias para adjuntar evidencias fotográficas a las actividades formativas (cámara y galería), el manejo seguro de archivos y permisos en tiempo de ejecución, y la integración con un servicio remoto real (**Supabase**) para respaldar actividades y evidencias.

La aplicación mantiene la arquitectura **offline-first** establecida en semanas anteriores: la base de datos local Room continúa siendo la fuente única de verdad que observa la interfaz de usuario, y Supabase actúa como respaldo remoto. Todo cambio se guarda primero en el dispositivo y se sincroniza con el servidor cuando hay conexión, lo que permite restaurar la información completa (actividades y fotografías) tras una reinstalación limpia de la aplicación.

---

## 2. Descripción del Problema y Justificación

Hasta la Semana 8 la aplicación almacenaba la información únicamente en el dispositivo y el servicio REST configurado apuntaba a un servidor de ejemplo inexistente. Esto generaba las siguientes limitaciones:

1. Los aprendices no podían adjuntar evidencias fotográficas del desarrollo de sus actividades.
2. Al desinstalar la aplicación, cambiar de dispositivo o borrar los datos, toda la información se perdía.
3. La apertura de la cámara provocaba el cierre de la aplicación, debido a que el permiso `CAMERA` estaba declarado en el Manifest pero nunca se solicitaba al usuario (`SecurityException`).

Los objetivos técnicos del incremento fueron:

1. Capturar o seleccionar imágenes de forma segura mediante `FileProvider` y el selector de fotos del sistema.
2. Solicitar los permisos sensibles en tiempo de ejecución, sin cierres inesperados.
3. Respaldar actividades y evidencias en Supabase sin perder la capacidad de operar sin conexión.
4. Evolucionar el esquema de Room mediante migraciones explícitas, sin destruir los datos existentes.

---

## 3. Arquitectura del Sistema y Flujo de Datos

El flujo de datos sigue siendo unidireccional:

```
UI (Compose) -> ViewModel (StateFlow) -> Repositorio -> Room (fuente única de verdad)
                                              |
                                              +--> Supabase (REST + Storage) como respaldo remoto
```

### 3.1. Componentes del Flujo

* **Capa de Presentación (UI):** `PantallaDetalleActividad` gestiona los botones de cámara y galería, la solicitud de permisos y la visualización de la evidencia. Consume el estado mediante `collectAsStateWithLifecycle()`.
* **ViewModel:** `ActividadViewModel` delega las operaciones al repositorio dentro de `viewModelScope` y expone `OperacionUiState` (Inactiva, EnCurso, Exitosa, Fallida) durante la subida de evidencias.
* **Repositorios:**
    * `OfflineFirstActividadRepository`: guarda las actividades en Room, marca los cambios pendientes y los sincroniza con la tabla `actividades` de Supabase.
    * `EvidenciaRepository`: guarda la imagen en el almacenamiento interno y en Room, la sube a Supabase Storage, registra su metadata y descarga las evidencias faltantes.
* **Capa remota:** `SupabaseApiService` (Retrofit) con `SupabaseAuthInterceptor`, que agrega los encabezados `apikey` y `Authorization` a cada petición.

### 3.2. Estrategia de Sincronización

**Envío (teléfono -> Supabase):**
1. Toda actividad creada, editada o eliminada queda marcada como pendiente (`sincronizada = 0`).
2. Las eliminaciones son lógicas (`eliminada = 1`): la actividad se oculta de inmediato y se elimina físicamente cuando Supabase confirma el borrado. Esto permite eliminar sin conexión.
3. Las evidencias se guardan con estado `PENDIENTE`; si la subida falla quedan como `FALLIDA` y se reintentan en la siguiente sincronización.

**Descarga (Supabase -> teléfono):**
1. Se descargan las actividades que existen en Supabase y no en el dispositivo. Los cambios locales pendientes tienen prioridad sobre los remotos.
2. Se descargan las fotografías cuya actividad existe localmente y cuyo archivo no está en el dispositivo.
3. Las actualizaciones desde el servidor usan `@Upsert` en lugar de `REPLACE`, para evitar que la llave foránea con `ON DELETE CASCADE` borre las evidencias asociadas.

**Momento de la sincronización:** `MainActivity` registra un `ConnectivityManager.NetworkCallback` mientras la aplicación está visible. La sincronización se ejecuta al abrir la aplicación con conexión, cada vez que la red vuelve con la aplicación abierta y después de cada operación de creación, edición o eliminación. El orden es primero actividades y luego evidencias, porque una evidencia requiere que su actividad exista en Room.

**Estados y errores:** el ViewModel expone `SincronizacionUiState` (Inactiva, EnCurso, Fallida). Los fallos se muestran en un Snackbar con la acción "Reintentar", y los errores de red se traducen a mensajes comprensibles (sin conexión, tiempo de espera agotado, datos inválidos, error del servidor). La pantalla de error del listado incluye un botón "Reintentar" que vuelve a suscribirse a Room.

### 3.3. Preferencias con DataStore

El filtro **"Solo completadas"** de la lista se guarda con Preferences DataStore (`PreferenciasRepository`) y se lee de forma reactiva en el ViewModel mediante `combine` junto con la búsqueda y los datos de Room. El valor se conserva al cerrar y abrir la aplicación. El ViewModel depende de la interfaz `PreferenciasUsuario`, lo que permite sustituirla por un doble en las pruebas.

### 3.4. Capa de Red

* Timeouts explícitos en OkHttp: conexión 10 s, lectura y escritura 20 s, llamada completa 30 s.
* Clasificación de errores en `classifyNetworkCall`: `SocketTimeoutException` -> `Timeout`, `IOException` -> `NoConnection`, `SerializationException` -> `InvalidPayload`, cualquier otra excepción -> `Unknown`. `CancellationException` se relanza para respetar la cancelación estructurada.

---

## 4. Capacidades del Dispositivo y Seguridad

### 4.1. Captura y Selección de Imágenes

| Origen | Mecanismo | Permiso requerido |
| :--- | :--- | :--- |
| Galería | `ActivityResultContracts.PickVisualMedia` (selector de fotos del sistema) | Ninguno |
| Cámara | `ActivityResultContracts.TakePicture` con URI de `FileProvider` | `CAMERA`, solicitado en tiempo de ejecución |

Antes de subir la imagen, la aplicación la reduce a un lado máximo de 1600 px, corrige su orientación según los metadatos EXIF y la comprime en formato JPEG (calidad 85). De esta forma, las fotografías de alta resolución de la cámara no superan el límite de 5 MB establecido.

### 4.2. Medidas de Seguridad

* **Credenciales fuera del repositorio:** la URL y la clave pública de Supabase se leen desde `local.properties` (excluido por `.gitignore`) y se inyectan en `BuildConfig` durante la compilación.
* **`FileProvider` no exportado y restringido:** `android:exported="false"` y `android:grantUriPermissions="true"`, de modo que solo la aplicación de cámara recibe un permiso temporal sobre la URI del archivo. `file_paths.xml` comparte únicamente la carpeta `cacheDir/images/` donde se guarda la foto temporal.
* **Separación de capas:** la interfaz de usuario recibe `EvidenciaUi`, un modelo propio de presentación, y no la entidad de Room.
* **Almacenamiento interno:** las evidencias se guardan en `filesDir/evidencias`, inaccesible para otras aplicaciones.
* **Permisos en tiempo de ejecución:** `CAMERA` y `POST_NOTIFICATIONS` (Android 13 o superior) se solicitan en el momento de uso; si el usuario los niega, se muestra un mensaje en lugar de cerrar la aplicación.
* **Borrado seguro de archivos:** la aplicación solo elimina archivos ubicados dentro de su propia carpeta de evidencias.

---

## 5. Especificación Técnica de la Base de Datos

### 5.1. Esquema Local (Room, versión 7)

**Tabla 1**  
*Estructura de la tabla `actividades`*

| Campo | Tipo SQLite | Restricciones | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | INTEGER | Primary Key | Identificador de la actividad (compartido con Supabase). |
| `titulo` | TEXT | NOT NULL | Nombre de la actividad. |
| `descripcion` | TEXT | NOT NULL | Detalle de la actividad. |
| `fecha` | TEXT | NOT NULL | Fecha límite en formato AAAA-MM-DD. |
| `prioridad` | TEXT | NOT NULL | BAJA, MEDIA o ALTA. |
| `progreso` | INTEGER | NOT NULL | Porcentaje de avance (0 a 100). |
| `completada` | INTEGER | NOT NULL | Indicador booleano de finalización. |
| `sincronizada` | INTEGER | NOT NULL, Default: 0 | 0 = cambios pendientes de enviar a Supabase. |
| `eliminada` | INTEGER | NOT NULL, Default: 0 | 1 = borrado lógico pendiente de confirmar. |

**Tabla 2**  
*Estructura de la tabla `evidencias`*

| Campo | Tipo SQLite | Restricciones | Descripción |
| :--- | :--- | :--- | :--- |
| `actividadId` | INTEGER | Primary Key, Foreign Key (CASCADE) | Actividad a la que pertenece la evidencia. |
| `uri` | TEXT | NOT NULL | Ruta del archivo en el almacenamiento interno. |
| `tipo` | TEXT | NOT NULL | Tipo MIME de la imagen. |
| `tamano` | INTEGER | NOT NULL | Tamaño en bytes. |
| `estado` | TEXT | NOT NULL | PENDIENTE, SINCRONIZADA o FALLIDA. |
| `urlRemota` | TEXT | Nullable | URL pública en Supabase Storage. |

### 5.2. Migraciones de Esquema

| Migración | Cambio | Objetivo |
| :--- | :--- | :--- |
| 4 -> 5 | Crea la tabla `evidencias` | Corregida para coincidir exactamente con `EvidenciaEntity` (antes creaba una columna `id` inexistente en la entidad, lo que provocaba el cierre de la aplicación). |
| 5 -> 6 | `ALTER TABLE evidencias ADD COLUMN urlRemota TEXT` | Vincular la evidencia local con su archivo remoto. |
| 6 -> 7 | `ADD COLUMN sincronizada` y `ADD COLUMN eliminada` | Control de sincronización y borrado offline de actividades. |

Se retiró `fallbackToDestructiveMigration()`, de manera que ninguna actualización de la aplicación borra los datos del usuario.

### 5.3. Esquema Remoto (Supabase)

* **Tabla `actividades`:** mismos campos que la tabla local (sin los de control de sincronización), más `actualizado_en`.
* **Tabla `evidencias`:** `id`, `actividad_id`, `url_remota`, `tipo_mime`, `tamano_bytes` y `fecha_creacion`.
* **Bucket `evidencias_bucket`:** público, con políticas de lectura, subida, reemplazo y borrado.

Los scripts de creación se encuentran en `docs/supabase/tabla_actividades.sql`, `docs/supabase/tabla_evidencias.sql` y `docs/supabase/politicas_storage.sql`.

---

## 6. Otros Ajustes Incluidos en el Incremento

* **Fecha en formato DD/MM/AAAA:** el campo inserta las barras automáticamente mientras el usuario escribe; internamente la fecha se guarda como AAAA-MM-DD. Al editar, la fecha se muestra de nuevo en formato DD/MM/AAAA.
* **Edición de actividades:** el botón Guardar volvía a estar siempre deshabilitado porque `puedeGuardar` había dejado de calcularse a partir de los errores de validación. Se restableció el cálculo.
* **Días restantes:** se calculan a partir de la fecha límite (antes siempre mostraban 0). Se habilitó *core library desugaring* para que `java.time` funcione en Android 7 (API 24-25).
* **Búsqueda:** el campo de búsqueda de la lista no recibía el texto ni el callback desde la navegación; se conectó al ViewModel y ahora filtra con `debounce(300)`.
* **Tests instrumentados:** se agregó `android.injected.androidTest.leaveApksInstalledAfterRun=true` en `gradle.properties`, porque Gradle desinstalaba la aplicación del teléfono al finalizar las pruebas.

---

## 7. Verificación y Pruebas

### 7.1. Pruebas Automatizadas

| Suite | Tipo | Resultado |
| :--- | :--- | :--- |
| `PlanesDePruebaTest` y `ReglasActividadTest` | Unitarias (JVM) | 28 / 28 aprobadas |
| `ActividadViewModelTest` | Unitarias con `runTest`, repositorio y preferencias falsos | 10 / 10 aprobadas |
| `SupabaseApiServiceTest` | Unitarias con MockWebServer | 5 / 5 aprobadas |
| `ActividadDaoTest` | Instrumentadas (Room en memoria) | 17 / 17 aprobadas |
| `ExampleInstrumentedTest` | Instrumentada | 1 / 1 aprobada |
| `ComponentesUiTest` | Instrumentadas (Compose) | 10 / 10 aprobadas |

`ActividadViewModelTest` cubre: transición Cargando -> Vacio, búsqueda con debounce, filtro persistido en preferencias, filtros sin resultados, lista completa para detalle y edición aunque haya filtros activos, error de Room con reintento, error de sincronización sin internet, orden actividades -> evidencias, normalización de fecha al crear y mapeo a `EvidenciaUi`. `SupabaseApiServiceTest` cubre: encabezados de autenticación, conversión de JSON, upsert con `merge-duplicates`, `actividad_id` como texto o número, `InvalidPayload` y `NoConnection`.

Pruebas agregadas en este incremento: formato de fecha DD/MM/AAAA, precarga de fecha al editar, habilitación del botón Guardar, persistencia de la URL remota, verificación de existencia de actividades, `@Upsert` sin borrado en cascada de evidencias, borrado lógico y control de pendientes de sincronización.

### 7.2. Pruebas Manuales en Dispositivo Físico

| Código | Caso de Aceptación | Resultado |
| :--- | :--- | :--- |
| CA-01 | Al pulsar Cámara, la aplicación solicita el permiso y no se cierra. | Cumplido |
| CA-02 | La fotografía se muestra en el detalle de la actividad y persiste al cerrar y abrir la aplicación. | Cumplido |
| CA-03 | Sin conexión, la evidencia queda guardada en el dispositivo con estado "pendiente de subir". | Cumplido |
| CA-04 | Con conexión, la imagen se sube a `evidencias_bucket` y su URL pública queda registrada en la tabla `evidencias` y en Room. | Cumplido |
| CA-05 | La actividad se respalda en la tabla `actividades` de Supabase. | Cumplido |
| CA-06 | Tras desinstalar y reinstalar la aplicación, se restauran automáticamente la actividad y su fotografía desde Supabase. | Cumplido |
| CA-07 | La actualización desde versiones anteriores de la base de datos conserva los registros existentes. | Cumplido |
| CA-08 | El filtro "Solo completadas" se conserva al cerrar y abrir la aplicación (DataStore). | Cumplido |
| CA-09 | La búsqueda filtra la lista y muestra un mensaje cuando no hay coincidencias. | Cumplido |

---

## 8. Limitaciones y Trabajo Pendiente

1. **Tests de UI en MIUI:** en teléfonos Xiaomi, `ComponentesUiTest` requiere que la aplicación `.dev` tenga activado el permiso "Mostrar ventanas emergentes mientras se ejecuta en segundo plano" (Ajustes > Aplicaciones > MiFormacionCTMA > Otros permisos). MIUI lo desactiva tras cada reinstalación y, sin él, bloquea la actividad de prueba (`Abort background activity starts`). Con el permiso activo, la suite completa (28 pruebas instrumentadas) se ejecutó con éxito.
2. **Sincronización en segundo plano:** ocurre mientras la aplicación está visible; si la conexión vuelve con la aplicación cerrada, se sincroniza al abrirla. Una mejora futura sería WorkManager.
3. **Políticas de Supabase:** permiten acceso completo con la clave pública, lo cual es adecuado para el entorno formativo pero no para producción (se requeriría autenticación de usuarios).
4. **Borrados remotos:** si una actividad se elimina directamente desde el panel de Supabase, no se elimina del dispositivo (decisión de diseño para evitar pérdidas de datos por errores de permisos).

---

## 9. Requisitos de Instalación y Configuración

### 9.1. Configuración de Supabase

1. Crear un proyecto en Supabase.
2. En *SQL Editor*, ejecutar en este orden: `docs/supabase/tabla_actividades.sql`, `docs/supabase/tabla_evidencias.sql` y `docs/supabase/politicas_storage.sql`.
3. Agregar las credenciales al archivo `local.properties` en la raíz del proyecto (este archivo no se sube al repositorio):

```properties
SUPABASE_URL=https://<id-del-proyecto>.supabase.co
SUPABASE_ANON_KEY=<clave-publica>
```

### 9.2. Compilación y Ejecución

1. Sincronizar Gradle en Android Studio.
2. Seleccionar la variante `devDebug`.
3. Ejecutar en un dispositivo físico o emulador con API 24 o superior.
4. Para las pruebas instrumentadas, mantener el dispositivo desbloqueado durante la ejecución. En teléfonos Xiaomi (MIUI), activar además el permiso descrito en la limitación 1.

---

## 10. Referencias

* Android Developers. (2026). *Photo picker*. Google Developer Documentation. https://developer.android.com/training/data-storage/shared/photopicker
* Android Developers. (2026). *Request runtime permissions*. Google Developer Documentation. https://developer.android.com/training/permissions/requesting
* Android Developers. (2026). *FileProvider*. Google Developer Documentation. https://developer.android.com/reference/androidx/core/content/FileProvider
* Android Developers. (2026). *Build an offline-first app*. Google Developer Documentation. https://developer.android.com/topic/architecture/data-layer/offline-first
* Android Developers. (2026). *Migrate your Room database*. Google Developer Documentation. https://developer.android.com/training/data-storage/room/migrating-db-versions
* Supabase. (2026). *Storage access control*. Supabase Docs. https://supabase.com/docs/guides/storage/security/access-control
* Supabase. (2026). *REST API (PostgREST)*. Supabase Docs. https://supabase.com/docs/guides/api
