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

Se implementaron **pruebas unitarias con JUnit** para verificar:

- `promedioProgreso()`
- `actividadesUrgentes()`
- `estadoActividad()`

Todas las pruebas finalizan correctamente.

---

## Organización del proyecto

- `app/` → código fuente Android.
- `docs/` → documentación organizada por semanas, con respuestas, análisis y evidencias de cada actividad.
- `README.md` → información general y guía de ejecución del proyecto.
- `.gitignore` → archivos y carpetas excluidos del control de versiones.

---

## Laboratorio Incremental — Semana 9: Dispositivo y Seguridad

### 1. Sistema de Evidencias Fotográficas
Se ha implementado un sistema robusto para asociar evidencias fotográficas a las actividades formativas:
- **Flujo:** Selección/Captura -> Validación -> Guardado Local (Privado) -> Previsualización -> Confirmación -> Sincronización Remota.
- **Relación:** 1:N entre `Actividad` y `Evidencias`.
- **Persistencia:** Room (Metadatos y URI local) + Almacenamiento Interno (Archivos físicos) + Supabase Storage (Remoto).
- **Estados de Sincronización:** `LOCAL`, `SUBIENDO`, `SINCRONIZADA`, `FALLIDA`.

### 2. Seguridad y Privacidad
- **Almacenamiento Seguro:** Las imágenes se copian a un subdirectorio privado (`files/evidencias/`) inaccesible para otras aplicaciones.
- **FileProvider:** Uso estricto de `content://` mediante `FileProvider` para la captura con cámara, evitando `file://` y fugas de seguridad.
- **Redacción de Logs:** Configuración de `OkHttp` para redactar cabeceras `Authorization` y `apikey` en Logcat.
- **HTTPS:** Comunicación obligatoria cifrada con Supabase.
- **Permisos:** Gestión reactiva de `POST_NOTIFICATIONS` en Android 13+ y uso de `PickVisualMedia` para evitar permisos de galería innecesarios.

### 3. Validaciones de Archivos
- **MIME Type:** Solo se aceptan imágenes (`image/*`).
- **Tamaño Máximo:** Límite de 5MB por archivo.
- **Integridad:** Validación de URIs legibles y generación de nombres únicos (`EVI_...`).

### 4. Matriz de Riesgos
| Riesgo | Impacto | Mitigación |
| :--- | :--- | :--- |
| Pérdida de red durante subida | Alto | Estado `FALLIDA` y opción de `Reintentar`. |
| Agotamiento de almacenamiento local | Medio | Validación de tamaño y limpieza de archivos al eliminar. |
| Acceso no autorizado a imágenes | Alto | Uso de almacenamiento privado (`filesDir`). |
| Fuga de tokens en logs | Crítico | Redacción quirúrgica en `HttpLoggingInterceptor`. |
| Imágenes corruptas o inválidas | Medio | Validación de lectura y MIME antes de persistir. |
| Incompatibilidad de cámara externa | Bajo | Implementación estricta con `FileProvider` y `TakePicture`. |
| Denegación de permisos de notificación | Bajo | Flujo funcional degradado (app sigue usable). |
| Desbordamiento de base de datos | Bajo | No se guardan bytes, solo metadatos y rutas. |

### 5. Verificación (Casos de Prueba CA01-CA09)
Se implementó una suite de pruebas que garantiza el cumplimiento de los 9 casos requeridos:
- **CA01-CA03:** Éxito en captura, selección y uso de FileProvider.
- **CA04:** Rechazo de archivos inválidos o pesados.
- **CA05-CA06:** Resiliencia ante reinicios y fallos de sincronización.
- **CA07-CA08:** Manejo de permisos y eliminación limpia (lógica y física).
- **CA09:** Auditoría de seguridad en configuración y logs.

---

## Trabajo colaborativo y SCRUM

El proyecto fue desarrollado utilizando un **repositorio compartido en GitHub** y trabajo en **ramas por integrante**, aplicando conceptos de:

- **SCRUM Master y Development Team**
- Incrementos funcionales por Sprint
- Componentes reutilizables
- Integración y prueba cruzada entre integrantes

---

## Estado actual

El proyecto cuenta con una **interfaz funcional, adaptable y accesible**, persistencia de datos offline-first y sincronización en la nube con Supabase.

---

## Arquitectura de Sincronización, Red y Caché Local

### 1. Contrato de la API (Supabase / PostgREST)
Las operaciones de red consumen la API de Supabase en base a los siguientes endpoints y parámetros:
- `GET /actividades`: Obtiene el listado completo de actividades.
- `GET /actividades?id=eq.{id}`: Obtiene el detalle de una actividad en particular usando filtros de igualdad de PostgREST.
- `POST /actividades`: Crea una actividad. Incluye la cabecera `Prefer: return=representation` para forzar al servidor a devolver el JSON del objeto creado.
- `PATCH /actividades?id=eq.{id}`: Actualiza parcialmente una actividad. Incluye la cabecera `Prefer: return=representation`.
- `DELETE /actividades?id=eq.{id}`: Elimina un registro de forma permanente.

### 2. Decisiones de Caché (Estrategia Offline-First)
- **Persistencia Atómica:** La sincronización desde el servidor se realiza usando una operación `@Transaction` en Room (`refrescarTodo`). Esto garantiza que la base de datos local limpie los datos antiguos e inserte los nuevos registros en un único bloque atómico. Si la operación de red o guardado falla, la base de datos no queda en un estado corrupto o inconsistente.
- **Flujo de Datos (SSOT):** La interfaz de usuario nunca muestra los datos directamente desde la red; en su lugar, se suscribe a un `Flow` continuo proveniente de Room. De esta forma, cualquier actualización o refresco en segundo plano impacta automáticamente la UI sin recargar pantallas.

### 3. Clasificación y Manejo de Errores
El componente `RemoteActividadDataSource` clasifica exhaustivamente las fallas para entregar retroalimentación clara a la UI:
- **Sin conexión o Fallos de Red (`IOException`):** Transforma la excepción en un mensaje descriptivo indicando falta de conexión con el servidor.
- **Tiempos de espera agotados (`SocketTimeoutException`):** Captura retardos excesivos e informa al usuario.
- **Errores de Autenticación (HTTP 401):** Detecta credenciales incorrectas o expiradas.
- **Recursos no encontrados (HTTP 404):** Identifica cuando un recurso solicitado ya no existe.
- **Errores del Servidor (HTTP 5xx):** Informa indisponibilidad temporal del backend.
- **Errores de Serialización (`SerializationException`):** Maneja respuestas malformadas o inesperadas de la API protegiendo la estabilidad del aplicativo.
- **Excepciones de Cancelación (`CancellationException`):** Se relanzan explícitamente para mantener intacto el ciclo de vida de los Coroutine Scopes de Kotlin.

### 4. Seguridad e Inyección de Tokens (Sesión Segura)
- Se abstiene de emplear literales fijos o quemados en código.
- Los tokens son inyectados dinámicamente mediante una abstracción `SessionTokenProvider`.
- Se configuró la política de logs en `HttpLoggingInterceptor` aplicando `redactHeader("apikey")` y `redactHeader("Authorization")` para prohibir la exposición de claves y secretos en la consola (Logcat).

### 5. Limitaciones del Sistema
- La sincronización actual realiza un reemplazo masivo (`Clear and Insert`) en la tabla durante el refresh. En bases de datos muy masivas, se recomienda evolucionar a un esquema de sincronización diferencial basado en marcas de tiempo (`updated_at`).
- La edición asume que el dispositivo cuenta con red en el momento del envío; no se encolan mutaciones offline complejas con políticas de reintento persistentes.

### 6. Pruebas de Servidor Simulado
Se diseñó e implementó la suite completa `RemoteActividadDataSourceTest` que valida la robustez del datasource simulando los 7 escenarios requeridos:
- **Respuesta 200 con éxito:** Retorno y mapeo correcto.
- **Respuesta Vacía:** Comportamiento seguro ante colecciones nulas o sin elementos.
- **Error 401:** Lógica de No Autorizado.
- **Error 500:** Respuesta controlada ante caídas de servidor.
- **JSON Inválido:** Captura idónea de fallos de parseo/serialización.
- **Timeout:** Validación del comportamiento por SocketTimeout.
- **Caché Previo:** Verificación conceptual de resiliencia local.

### 7. Uso de IA Validado
El uso del asistente de Inteligencia Artificial para esta iteración ha sido estrictamente validado y supervisado por el equipo de desarrollo para garantizar código limpio y apego a las buenas prácticas arquitectónicas:
- Diagnóstico preciso del error de parseo `EOF` mediante el entendimiento de las cabeceras `Prefer` de PostgREST.
- Refactorización de Retrofit con parámetros nombrados y tipados robustos.
- Corrección quirúrgica del flujo MVI/MVVM asegurando la reactividad continua de la UI.

