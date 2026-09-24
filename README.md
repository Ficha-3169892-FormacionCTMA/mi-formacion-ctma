# MiFormacionCTMA

Aplicación Android desarrollada con Kotlin y Jetpack Compose para la gestión de actividades formativas del proceso CTMA. El proyecto implementa arquitectura Clean Architecture / MVVM, persistencia local Offline-First con Room, sincronización remota con Supabase, gestión segura de evidencias fotográficas, permisos contextuales y soporte para múltiples variantes de compilación (*Build Variants*).

---

## Requisitos y Ejecución

- **Android Studio** Ladybug (o superior) y Android SDK 34+.
- Dispositivo físico o emulador con depuración activa.
- Archivo `secrets.properties` configurado en la raíz del proyecto basándose en `secrets.properties.example`.

Para ejecutar la aplicación, seleccione la variante deseada (`devDebug`, `stageDebug` o `prodRelease`) en el panel **Build Variants** de Android Studio y presione el botón **Run**.

---

## Variantes de Compilación (Build Variants)

El proyecto cuenta con 3 sabores de producto (*Product Flavors*) configurados en Gradle:

- **`dev`**: Entorno de desarrollo local/pruebas rápidas (`applicationIdSuffix = ".dev"`). Permite inspección de red detallada.
- **`stage`**: Entorno de homologación y pruebas de integración (`applicationIdSuffix = ".stage"`). Tráfico HTTPS forzado.
- **`prod`**: Entorno de producción final. Tráfico HTTPS estricto y redacción total de logs de red (`HttpLoggingInterceptor.Level.NONE`).

---

## Arquitectura y Módulos

El proyecto sigue el patrón **Offline-First** y **Unidirectional Data Flow (UDF)**:

1. **Capa de Interfaz de Usuario (UI)**: Diseñada con Jetpack Compose y Material 3.
   - Componentes clave: `SeccionEvidencia` (gestión de cámara/galería) y `SeccionRecordatorios` (notificaciones contextuales).
2. **Capa de Presentación (ViewModel)**: Manejo del estado reactivo mediante `StateFlow` y recolección limpia de eventos.
3. **Capa de Datos y Persistencia (Room & Retrofit)**:
   - **Room Database (v3)**: Almacena entidades `ActividadEntity`, `CompetenciaEntity` y `EvidenciaEntity` (solo metadatos y URIs locales, nunca Base64 ni bytes de imágenes).
   - **Retrofit & OkHttp**: Sincronización multipart con cabeceras `Idempotency-Key` y redacción de encabezados sensibles (`Authorization`, `apikey`).
   - **DataStore**: Almacenamiento de preferencias del usuario (orden de lista y activación de recordatorios).

---

## Gestión de Evidencias Fotográficas y Seguridad

- **Mínimo Privilegio**: Selección de imágenes mediante **Photo Picker** (`ActivityResultContracts.PickVisualMedia()`) sin solicitar permisos ampliados a la galería.
- **Captura Segura**: Integración con **FileProvider** restringida al subdirectorio interno `filesDir/evidencias/` entregan únicamente `content URI`.
- **Validación Vía ContentResolver**: Filtrado estricto de tipos MIME (`image/jpeg`, `image/png`, `image/webp`), límite de tamaño (5 MB) y prueba de apertura de *stream*.
- **Redacción de Logs**: Supresión de tokens y cabeceras sensibles en Logcat.
- **Seguridad de Red**: Configuración `network-security-config.xml` deshabilitando tráfico en texto claro (`cleartextTrafficPermitted="false"`) para stage y prod.

---

## Permisos Contextuales (Notificaciones)

- La solicitud del permiso `POST_NOTIFICATIONS` (Android 13+) se realiza **exclusivamente de forma contextual** cuando el usuario intenta activar voluntariamente el interruptor de recordatorios.
- Si el usuario rechaza el permiso, la app conserva la preferencia desactivada sin repetir diálogos molestos y continúa funcionando normalmente sin bloqueos.
- **No se solicitan permisos de ubicación**.

---

## Configuración de Supabase

1. Cree las tablas `actividades`, `competencias` y el bucket de almacenamiento `evidencias` en Supabase.
2. Configure las políticas de acceso según su entorno:
   - **Desarrollo / Demo**:
     ```sql
     create policy "Acceso total actividades" on actividades for all using (true);
     create policy "Acceso total competencias" on competencias for all using (true);
     ```
   - **Producción**: Restrinja las políticas RLS mediante `auth.uid() = user_id` o permisos por rol.
3. Configure `secrets.properties` con `SUPABASE_URL` y `SUPABASE_PUBLISHABLE_KEY`.

---

## Calidad y Pruebas

- Suite de pruebas unitarias sobre reglas de negocio (`ReglasActividadTest.kt`).
- Pruebas de migración y Room DAOs (`MigrationTest.kt`).
- Cobertura de los 9 Casos de Aceptación (CA 01 al CA 09) utilizando material de prueba sintético.
