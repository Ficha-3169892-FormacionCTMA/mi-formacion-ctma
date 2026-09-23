# MiFormacionCTMA

Aplicación Android desarrollada con Kotlin y Jetpack Compose para la gestion de actividades
formativas del proceso CTMA. El proyecto implementa conceptos de UI declarativa, Material 3,
accesibilidad y una arquitectura robusta basada en flujos reactivos y concurrencia avanzada.

---

## Requisitos y Ejecución

- Android Studio y SDK de Android actualizados.
- Dispositivo físico con depuración inalámbrica o emulador compatible.
- Archivo secrets.properties configurado en la raíz del proyecto.

Para ejecutar la aplicación, vincule su dispositivo desde el Device Manager y presione el botón Run.

---

## Arquitectura del Proyecto

Se utiliza una Arquitectura en Capas (Clean Architecture) con flujo unidireccional:

1. Capa de Interfaz de Usuario (UI): Diseñada con el patron Route-Screen, utilizando Compose y
   recolección de estados conscientes del ciclo de vida.
2. Capa de Presentacion (ViewModel): Gestiona el estado reactivo mediante StateFlow y coordina la
   lógica de negocio.
3. Capa de Datos (Offline-First):
    - Room Database: Fuente local canónica de verdad con transacciones atómicas.
    - Supabase: Sincronización remota mediante Retrofit y OkHttp.
    - DataStore: Almacenamiento de preferencias del usuario.

---

## Concurrencia y Seguridad

- Política Main-Safety: Operaciones pesadas en hilos de background nativos.
- Cancelación Cooperativa: Uso de flatMapLatest para optimizar consultas de búsqueda.
- Optimización de Recursos: Uso de WhileSubscribed(5000) en flujos compartidos.
- Seguridad de Datos: Inyección dinámica de tokens y redacción automática de claves en logs.

---

## Configuración de Supabase

Para habilitar la persistencia en la nube, siga estos pasos:

1. Cree las tablas actividades y competencias en Supabase con los campos correspondientes.
2. Ejecute el siguiente script para configurar las políticas de acceso:
   ```sql
   create policy "Acceso total" on actividades for all using (true);
   create policy "Acceso total" on competencias for all using (true);
   ```
3. Configure su archivo secrets.properties con las variables SUPABASE_URL y SUPABASE_PUBLISHABLE_KEY
   basándose en el archivo `.example`.

---

## Pruebas y Calidad

El proyecto incluye una suite de 24 pruebas automatizadas con JUnit, MockWebServer y MockK:

- Pruebas de Repositorio y Red: Verificación de escenarios exitosos, fallos de conexión, timeouts y
  errores de servidor.
- Pruebas de ViewModel: Validación de transiciones de estado y flujos reactivos.
- Persistencia Local: Verificación de DAOs y migraciones de base de datos.

---

## Estado Actual

La aplicación es un sistema CRUD completo y resiliente. Permite crear, editar, eliminar y visualizar
actividades con sincronización automática. La arquitectura garantiza el funcionamiento offline
utilizando los últimos datos guardados localmente ante la falta de conexión.
