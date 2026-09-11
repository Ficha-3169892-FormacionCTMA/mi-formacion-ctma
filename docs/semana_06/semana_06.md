#  Mi Formación CTMA - Módulo de Persistencia (Semana 6)

Este módulo implementa la capa de persistencia local para la aplicación **Mi Formación CTMA**, siguiendo los lineamientos de arquitectura recomendados por Android y las especificaciones del SENA CTMA.

---

## Arquitectura de la Capa de Datos

Se aplica el patrón de **Fuente Única de Verdad (SSOT)**[cite: 1]:
* **Room Database**: Origen canónico para los datos estructurados relacionales (`competencias` y `actividades`)[cite: 1].
* **Preferences DataStore**: Origen canónico para configuraciones ligeras de usuario (filtros de competencia y modo de vista)[cite: 1].
* **StateFlow / UI State**: Estado temporal de interacción en pantalla (diálogos, campos de texto sin enviar)[cite: 1].

###  Recorrido del Dato
`Compose Event` ➔ `ViewModel` ➔ `Repository` ➔ `DAO / DataStore` ➔ `Room DB` ➔ `Flow Emission` ➔ `UiState` ➔ `Compose UI`[cite: 1]

> **Regla de Consistencia**: Toda modificación se escribe directamente en la base de datos[cite: 1]. La interfaz de usuario no mantiene listas paralelas en memoria; únicamente observa las emisiones de los `Flow` expuestos por el Repository[cite: 1].

---

##  Modelo Relacional y Migración

### 1. Entidades y Relación (1:N)
* **`CompetenciaEntity`**: Representa el módulo formativo[cite: 1].
* **`ActividadEntity`**: Representa las evidencias o tareas asociadas a una competencia[cite: 1].
    * Clave foránea `competenciaId` con restricción `ForeignKey.RESTRICT` para prevenir huérfanos[cite: 1].
    * Índices en `competenciaId` y `titulo` para optimizar el rendimiento de las consultas[cite: 1].

### 2. Control de Versiones de la Base de Datos (v1 ➔ v2)
* **Versión 1**: Esquema inicial de competencias y actividades[cite: 1].
* **Versión 2**: Incorporación de la propiedad `completada: Boolean` en la tabla `actividades`[cite: 1].
* **Estrategia de Migración (`MIGRATION_1_2`)**: Uso de SQL explícito (`ALTER TABLE actividades ADD COLUMN completada INTEGER NOT NULL DEFAULT 0`) evitando el uso de `fallbackToDestructiveMigration` para garantizar la preservación de los datos del usuario[cite: 1].

---

##  Pruebas e Inspección

1. **Pruebas de DAO (`ActividadDaoTest`)**:
    * Ejecutadas sobre una base de datos SQLite en memoria (`inMemoryDatabaseBuilder`)[cite: 1].
    * Verificación de inserciones, consultas reactivas con `Flow`, restricciones de clave foránea y ordenamiento[cite: 1].

2. **Pruebas de Migración (`MigrationTest`)**:
    * Validadas mediante `MigrationTestHelper`[cite: 1].
    * Creación del esquema v1, inserción de datos de prueba en SQL literal, ejecución de `MIGRATION_1_2` y comprobación de integridad en v2[cite: 1].

3. **Diagnóstico**:
    * Inspección del esquema, claves foráneas, índices y consultas en tiempo real utilizando **Database Inspector**[cite: 1].

---

##  Prospectiva: Sincronización API (Semana 8)

Cuando se integre la API REST en la Semana 8:
* La API actualizará únicamente la base de datos local de Room[cite: 1].
* La UI continuará leyendo Room como fuente canónica sin acoplarse a las peticiones de red[cite: 1].