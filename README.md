# MiFormacionCTMA

Aplicación Android desarrollada con **Kotlin** y **Jetpack Compose** para organizar actividades,
compromisos y evidencias del proceso formativo CTMA, aplicando conceptos de **UI declarativa,
Material 3, accesibilidad y trabajo colaborativo con SCRUM**.

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

Se implementaron pruebas automatizadas con **JUnit** para verificar reglas de negocio, persistencia
y migración de datos.

Las pruebas incluyen:

* Reglas de negocio de `ReglasActividad`, mediante `ReglasActividadTest`.
* Casos de prueba funcionales definidos en `PlanesDePruebaTest`, incluyendo validaciones,
  navegación, búsqueda, progreso y manejo de actividades.
* Persistencia y operaciones del `ActividadDao`.
* Migración de la base de datos entre versiones.
* Validación de la estructura y contenido de las entidades persistidas.

Todas las pruebas ejecutadas finalizan correctamente.


---

## Organización del proyecto

- `app/` → código fuente Android.
- `docs/` → documentación organizada por semanas, con respuestas, análisis y evidencias de cada
  actividad.
- `README.md` → información general y guía de ejecución del proyecto.
- `.gitignore` → archivos y carpetas excluidos del control de versiones.

---

## Trabajo colaborativo y SCRUM

El proyecto fue desarrollado utilizando un **repositorio compartido en GitHub** y trabajo en **ramas
por integrante**, aplicando conceptos de:

- **SCRUM Master y Development Team**
- Incrementos funcionales por Sprint
- Componentes reutilizables
- Integración y prueba cruzada entre integrantes

---

## Estado actual

El proyecto cuenta con una **interfaz funcional, adaptable y accesible**, junto con persistencia
local mediante **Room**.

La información de actividades y competencias se almacena en una base de datos local y el acceso a
los datos se centraliza mediante la capa de repositorio, manteniendo una separación entre la
interfaz, la lógica de dominio y la persistencia.

También se implementaron y verificaron pruebas relacionadas con:

* Operaciones de los DAO.
* Migración de la base de datos.
* Persistencia de información.
* Validaciones y reglas de negocio.
* Funcionamiento de la interfaz y navegación.

El proyecto queda preparado para futuras iteraciones relacionadas con navegación, arquitectura y
nuevas funcionalidades de la aplicación.
