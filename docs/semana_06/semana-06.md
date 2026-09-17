# Semana 06 - Persistencia local y fuente única de verdad

## 1. Objetivo

Implementar persistencia local para la aplicación **MiFormacionCTMA**, reemplazando el
almacenamiento temporal en memoria por una fuente de datos persistente y centralizada.

La implementación utiliza **Room** para almacenar las actividades y competencias, manteniendo el
acceso a los datos a través de la capa de repositorio.

También se incorporaron mecanismos de migración y pruebas de persistencia para verificar la
integridad de la información almacenada.

---

## 2. Persistencia local

La aplicación utiliza una base de datos local mediante Room.

La información de las actividades se almacena en la tabla correspondiente a `ActividadEntity`,
mientras que las competencias utilizan su propia entidad y DAO.

La estructura permite conservar los datos registrados incluso después de cerrar y volver a ejecutar
la aplicación.

### Componentes principales

- `ActividadEntity`: entidad utilizada para persistir las actividades.
- `CompetenciaEntity`: entidad utilizada para persistir las competencias.
- `ActividadConCompetencia`: estructura para representar la relación entre actividades y competencias.
- `ActividadDao`: operaciones de acceso a las actividades.
- `CompetenciaDao`: operaciones de acceso a las competencias.
- `FormacionDatabase`: configuración de la base de datos Room.
- `DatabaseMigrations`: migraciones utilizadas para actualizar el esquema de la base de datos.
- `ActividadMappers`: conversión entre `ActividadEntity` y `ActividadFormativa`.
- `ActividadRepository`: abstracción para el acceso a las actividades.
- `RoomActividadRepository`: implementación del repositorio utilizando Room.
- `PreferenciasRepository`: acceso centralizado a las preferencias locales.

---

## 3. Fuente única de verdad

La interfaz no accede directamente a los DAO ni a la base de datos.

El acceso a los datos se concentra en el repositorio, que funciona como intermediario entre la
interfaz y la fuente de persistencia.

El flujo general queda establecido como:

```text
UI
 ↓
ViewModel
 ↓
ActividadRepository
 ↓
RoomActividadRepository
 ↓
ActividadDao / CompetenciaDao
 ↓
FormacionDatabase
 ↓
SQLite
```

Para devolver información al dominio se utilizan mapeadores:

```text
ActividadEntity
      ↓
  toDomain()
      ↓
ActividadFormativa
```

Y para almacenar información:

```text
ActividadFormativa
      ↓
  toEntity()
      ↓
ActividadEntity
```

Esto permite mantener separadas las estructuras utilizadas por la base de datos de los modelos
utilizados por la lógica de la aplicación.

---

## 4. Cálculo de días restantes

Los días restantes se calculan a partir de la fecha límite de la actividad.

El valor se obtiene automáticamente a partir de la fecha almacenada, en lugar de depender de un
número introducido manualmente por el usuario.

El cálculo utiliza el formato `yyyy-MM-dd` y considera la fecha actual como referencia.

De esta manera, una actividad puede determinar dinámicamente si se encuentra próxima a vencer o si
ya está vencida.

---

## 5. Validación de fechas

La fecha ingresada por el usuario se valida mediante `SimpleDateFormat` con el formato:

```text
yyyy-MM-dd
```

Además, se desactiva el comportamiento permisivo, esto permite rechazar fechas inexistentes como:

```text
2020-20-20
```

y aceptar únicamente fechas válidas.

---

## 6. Migración de la base de datos

Se implementó la migración necesaria para actualizar el esquema de la base de datos sin perder la
información almacenada.

La migración fue verificada mediante una prueba específica de migración.

---

## 7. Pruebas de persistencia

Se realizaron pruebas automatizadas para comprobar el funcionamiento de la capa de persistencia.

### DAO

La prueba del DAO verifica las operaciones realizadas directamente sobre la fuente de datos.

### Migración

La prueba de migración verifica que el cambio de esquema pueda realizarse correctamente.

### Evidencias

![1. pruebas-actividades-dao.png](evidencias/1.%20pruebas-actividades-dao.png)

![2. pruebas-migracion-v2.png](evidencias/2.%20pruebas-migracion-v2.png)

---

## 8. Verificación de la base de datos

La estructura persistida fue inspeccionada mediante el visor de base de datos de Android Studio.

Se verificó la existencia y contenido de las tablas correspondientes a:

* `actividades`
* `competencias`

### Evidencias

![3. tabla-actividades-database-inspector.png](evidencias/3.%20tabla-actividades-database-inspector.png)

![4. tabla-competencias-database-inspector.png](evidencias/4.%20tabla-competencias-database-inspector.png)

---

## 9. Resultado

La aplicación cuenta actualmente con persistencia local mediante Room y una separación entre:

* modelos de dominio;
* entidades de persistencia;
* DAO;
* repositorio;
* interfaz de usuario.

Las pruebas realizadas finalizaron correctamente y la información almacenada puede verificarse
directamente en la base de datos local.

La implementación deja preparada la aplicación para continuar con futuras mejoras relacionadas con
persistencia, navegación y evolución de la arquitectura.
