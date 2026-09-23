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

## Configuración de Supabase

La app respalda actividades y fotos en **Supabase**. Las credenciales **no se suben al repositorio**; cada integrante debe agregarlas a su archivo `local.properties` (en la raíz del proyecto):

```properties
SUPABASE_URL=https://<id-del-proyecto>.supabase.co
SUPABASE_ANON_KEY=<clave-publica>
```

Para un proyecto de Supabase nuevo, ejecutar en *SQL Editor* los scripts de `docs/supabase/` en este orden: `tabla_actividades.sql`, `tabla_evidencias.sql` y `politicas_storage.sql`.

Sin estas credenciales la app funciona sin conexión (Room), pero no sincroniza.

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

| Tipo | Comando | Cobertura |
| :--- | :--- | :--- |
| Unitarias (43) | `./gradlew testDevDebugUnitTest` | Reglas de negocio, `ActividadViewModel` con `runTest` y repositorio falso, API de Supabase con MockWebServer |
| Instrumentadas (28) | `./gradlew connectedDevDebugAndroidTest` | DAOs de Room, sincronización local y componentes de UI en Compose |

Las pruebas instrumentadas requieren el teléfono desbloqueado. En teléfonos Xiaomi (MIUI) hay que activar además el permiso *"Mostrar ventanas emergentes mientras se ejecuta en segundo plano"* para la app `.dev`.

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

Incremento de la **semana 9** completado: persistencia local con Room y DataStore, arquitectura MVVM con `StateFlow`, evidencias fotográficas con cámara y galería, y sincronización **offline-first** con Supabase, incluida la restauración de datos y fotos tras reinstalar la app. El detalle de cada semana está en `docs/semana_XX/`.
