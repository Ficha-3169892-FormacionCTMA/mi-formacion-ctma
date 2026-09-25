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

## 🔑 Credenciales de Acceso para Pruebas

La aplicación cuenta con usuarios preconfigurados en Supabase para validar los diferentes roles (Instructor y Aprendices):

### 1. Rol Instructor
* **Correo:** `instructor@sena.edu.co`
* **Contraseña:** `user123`
* **UUID:** `df3b0ab1-7129-414d-a457-1694057f88fd`

*(Alternativa Instructor)*
* **Correo:** `instructor1@sena.edu.co`
* **Contraseña:** `instructor123`
* **UUID:** `79d59938-c884-4c12-bec0-8c06e4b18ea1`

### 2. Rol Aprendiz
* **Correo:** `aprendiz@sena.edu.co`
* **Contraseña:** `aprendiz123`
* **UUID:** `9fd64873-8c88-49bc-959e-871f338d232a`

*(Cuenta de Aprendiz Principal - Sebas)*
* **Correo:** `sebas@sena.edu.co`
* **Contraseña:** `sebas123`
* **UUID:** `af6ef7e4-5689-45b9-8dc8-71046edbba0c`

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

## Trabajo colaborativo y SCRUM

El proyecto fue desarrollado utilizando un **repositorio compartido en GitHub** y trabajo en **ramas por integrante**, aplicando conceptos de:

- **SCRUM Master y Development Team**
- Incrementos funcionales por Sprint
- Componentes reutilizables
- Integración y prueba cruzada entre integrantes

---

## Persistencia Local y Sincronización Remota

Se incorporó persistencia local estructurada con **Room** y sincronización en la nube con **Supabase (Backend-as-a-Service)** bajo un enfoque **Offline-First**.

### Recorrido del Dato (Arquitectura)
1. **Lectura / Observación:** `Room Database` → `Flow<List<Entity>>` → `Repository contrato` → `ViewModel (combina flows y roles)` → `UiState` → `Jetpack Compose UI` (recomposición automática).
2. **Escritura / Sincronización:** `Compose UI` → `ViewModel` → Guardado local en `Room` + Inserción remota en `Supabase` con su respectivo `aprendiz_id`.

---

## Estado actual

El proyecto cuenta con una **interfaz funcional, adaptable y accesible**, autenticación por roles (Instructor / Aprendiz), asignación de tareas dirigidas y sincronización en tiempo real con Supabase.