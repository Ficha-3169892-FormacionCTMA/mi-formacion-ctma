# Proyecto Formatice CTMA: Implementación de Persistencia Local y Fuente Única de Verdad

**Programa:** Análisis y Desarrollo de Software (ADSO)  
**Institución:** Servicio Nacional de Aprendizaje (SENA)  
**Centro de Formación:** Centro de Tecnología de la Manufactura Avanzada (CTMA)  
**Módulo:** Desarrollo de Aplicaciones Móviles Android  
**Semana:** 06  
**Fecha:** Septiembre de 2026

---

## 1. Resumen Ejecutivo

El presente documento registra la arquitectura, decisiones de diseño e implementación del módulo de persistencia local para la aplicación móvil Mi Formación CTMA. En el marco del patrón de arquitectura limpia (Clean Architecture) recomendado para la plataforma Android, se reemplazó el almacenamiento temporal en memoria por una base de datos relacional local gestionada mediante la biblioteca Room. Asimismo, se incorporó la biblioteca Preferences DataStore para la gestión de configuraciones de usuario. La capa de repositorio actúa como la fuente única de verdad (Single Source of Truth), asegurando que los componentes de la interfaz de usuario no interactúen de forma directa con los mecanismos de almacenamiento ni ejecuten consultas a la base de datos.

---

## 2. Descripción del Problema y Justificación

En las versiones previas de la aplicación, las actividades formativas y las preferencias de visualización se gestionaban mediante estructuras de datos volátiles alojadas en la memoria RAM del dispositivo. Como consecuencia, la información ingresada por los aprendices se perdía al finalizar el proceso de la aplicación o al reiniciar el dispositivo.

La integración de la capa de persistencia local resuelve esta limitación mediante los siguientes objetivos técnicos:

1. Garantizar la disponibilidad de la información en entornos sin conectividad a red.
2. Mantener la consistencia del estado de los datos a través de los diferentes componentes visuales desarrollados en Jetpack Compose.
3. Permitir la evolución controlada de la estructura de la base de datos mediante migraciones de esquema explícitas, evitando la pérdida de información de los usuarios.

---

## 3. Arquitectura del Sistema y Flujo de Datos

El diseño del sistema sigue las pautas oficiales de la arquitectura de guía de desarrollo en Android. La estructura de capas se distribuye de la siguiente manera:

### 3.1. Componentes del Flujo

* **Capa de Presentación (UI):** Desarrollada mediante Jetpack Compose. Consume el estado expuesto por el ViewModel a través de un StateFlow de lectura.
* **Capa de Dominio / Estado (ViewModel):** Gestiona la lógica de presentación y delega las operaciones de lectura y escritura a la capa de datos mediante el uso de corrutinas en el contexto de viewModelScope[cite: 1].
* **Capa de Datos (Repository Pattern):** Actúa como mediador y fuente única de verdad. El repositorio coordina las fuentes de datos locales (Room y DataStore) y expone flujos de datos reactivos (Flow)[cite: 1].
* **Capa de Persistencia (Room Database & DataStore):** Room gestiona las operaciones relacionales sobre la base de datos SQLite subyacente mediante Data Access Objects (DAO)[cite: 1]. Preferences DataStore gestiona pares clave-valor asíncronos para la configuración[cite: 1].

---

## 4. Especificación Técnica de la Base de Datos

### 4.1. Esquema de la Entidad Principal

La entidad `ActividadEntity` define la estructura de la tabla `actividades` dentro del motor SQLite[cite: 1].

**Tabla 1**  
*Estructura de la tabla actividades (Versión 2 de esquema)*

| Campo | Tipo de Dato SQLite | Restricciones | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | INTEGER | Primary Key, AutoIncrement | Identificador único de la actividad formativa. |
| `titulo` | TEXT | NOT NULL | Nombre asignado a la evidencia o guía. |
| `descripcion` | TEXT | NOT NULL | Detalle y alcance de la actividad. |
| `fecha` | TEXT | NOT NULL | Fecha límite o de registro formateada. |
| `completada` | INTEGER | NOT NULL, Default: 0 | Indicador booleano de finalización. |

---

## 5. Estrategia de Migración de Esquema

Para garantizar la evolución continua del sistema sin recurrir a la destrucción de los datos del usuario, se implementó una migración explícita de la versión 1 a la versión 2[cite: 1].

### 5.1. Definición de la Migración

La migración agrega la columna `completada` a la tabla preexistente mediante la ejecución de una instrucción SQL nativa[cite: 1].

sql
```ALTER TABLE actividades ADD COLUMN completada INTEGER NOT NULL DEFAULT 0```

Esta modificación asegura que las versiones instaladas que contenían datos en el esquema 1 preserven sus registros intactos al actualizar a la versión 2, asignando el valor predeterminado 0 (false) a los registros previos[cite: 1].

## 6. Verificación y Pruebas Instrumentadas
Con el propósito de validar la correcta ejecución de las operaciones de lectura y escritura en la base de datos, se construyó una suite de pruebas instrumentadas dirigidas al DAO[cite: 1].

### 6.1. Entorno de Pruebas
* Framework de Pruebas: AndroidX Test con Runner JUnit4.

Estrategia de Aislamiento: Creación de base de datos en memoria mediante Room.inMemoryDatabaseBuilder.

Ejecución: Pruebas ejecutadas directamente en dispositivo físico Android.

### 6.2. Resultados de Aceptación
1. Persistencia (PA-01): La información registrada por el usuario permanece almacenada tras el cierre definitivo del proceso de la aplicación[cite: 1].

2. Reactividad (PA-02): La modificación de un registro en la base de datos invalida de forma automática la consulta previa, notificando al flujo observable (Flow) y actualizando la interfaz de usuario sin requerir recargas manuales[cite: 1].

3. Prueba de DAO (PA-07): La prueba unitaria instrumentada insertarYObtenerActividad completó su ciclo de ejecución con estado satisfactorio (1/1 pruebas aprobadas)[cite: 1].

## 7. Requisitos de Instalación y Despliegue
### 7.1. Requisitos del Sistema
* Entorno de Desarrollo: Android Studio Ladybug o superior[cite: 1].

Lenguaje: Kotlin 2.0 o superior.

Procesador de Anotaciones: KSP (Kotlin Symbol Processing).

SDK Mínimo: API 23 (Android 6.0 Marshmallow)[cite: 1].

SDK Objetivo: API 34 o superior.

### 7.2. Pasos de Compilación
1. Clonar el repositorio de código fuente en la estación de trabajo.

2. Abrir el proyecto desde Android Studio.

3. Ejecutar la sincronización del sistema de construcción Gradle para descargar las dependencias declaradas.

4. Conectar un dispositivo físico mediante depuración USB o iniciar un emulador con API 23 o superior[cite: 1].

5. Seleccionar la configuración de ejecución app y compilar el proyecto.

## 8. Referencias
* Android Developers. (2026). Save data in a local database using Room. Google Developer Documentation. https://developer.android.com/training/data-storage/room[cite: 1]

* Android Developers. (2026). Data layer and offline-first architecture. Google Developer Documentation. https://developer.android.com/topic/architecture/data-layer[cite: 1]

* Servicio Nacional de Aprendizaje (SENA). (2026). Guía de aprendizaje semana 06: Persistencia local y fuente única de verdad (Mi Formación CTMA). Centro de Tecnología de la Manufactura Avanzada[cite: 1].