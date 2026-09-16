# Mi Formación CTMA - Semana 8: Servicios Web, Caché y Resiliencia

Este modulo implementa la integración con servicios REST mediante Retrofit, estableciendo una arquitectura robusta de tipo Offline-First para la aplicacion Mi Formacion CTMA.

## 1. Arquitectura y Decisiones de Diseño

La aplicacion sigue estrictamente la regla de fuente unica de verdad recomendada por Android Developers:
- Flujo de datos unidireccional: API (Retrofit) a DTO (Transporte) a Mappers a Entity (Room) a Flow a UI (Compose y ViewModel).
- Room como fuente canónica: Las capas superiores de la interfaz de usuario observan exclusivamente la base de datos local a traves de flujos reactivos (Flow), garantizando que la UI nunca interactue directamente con objetos de red (DTO).
- Resiliencia ante fallos de red: Un error de conexión o un fallo remoto (como timeout o codigos 5xx) nunca sobrescribe ni vacia el cache local de Room, preservando la informacion disponible en el dispositivo.

Referencias:
Android Developers. (s.f.). Arquitectura offline-first. Recuperado de https://developer.android.com/topic/architecture/data-layer/offline-first

## 2. Contrato de Red y Timeouts

La comunicación con el backend se realiza bajo un contrato REST configurado en Retrofit con OkHttpClient:
- Base URL: Configurada dinámicamente desde el entorno de compilación.
- Timeouts explícitos:
    - connectTimeout: 10 segundos.
    - readTimeout: 20 segundos.
    - callTimeout: 30 segundos.
- Endpoints soportados:
    - GET /v1/actividades: Consulta general del listado de actividades formativas.
    - GET /v1/actividades/{id}: Detalle de una actividad especifica.
    - POST /v1/actividades: Creación de un nuevo recurso.
    - PUT /v1/actividades/{id}: Actualización completa del recurso.

Referencias:
Square. (s.f.). OkHttp. Recuperado de https://square.github.io/okhttp/

## 3. seguridad y Autenticación por Token

- Cero secretos embebidos: Se prohibe el uso de tokens reales de produccion almacenados en codigo fuente, archivos de recursos (strings.xml) o historiales de Git.
- Interceptor Bearer: La inserción del encabezado de autorización (Authorization: Bearer <token>) se gestiona en tiempo de ejecucion a traves de un TokenProvider seguro integrado en el cliente HTTP mediante un interceptor de OkHttp.
- Redacción de logs: Se excluyen los cuerpos sensibles y los encabezados de autorizacion de los registros de depuracion.

Referencias:
Android Developers. (s.f.). Riesgos de secretos embebidos. Recuperado de https://developer.android.com/privacy-and-security/risks/hardcoded-cryptographic-secrets

## 4. Manejo y Clasificacion de Errores (DataError)

El repositorio traduce las excepciones técnicas en un dominio de errores controlado para la interfaz de usuario:
- IOException se traduce a DataError.NoConnection (Modo sin conexión o datos cacheados visibles).
- SocketTimeoutException se traduce a DataError.Timeout (Actualización demorada).
- HTTP 401 se traduce a DataError.Unauthorized (Sesion vencida o invalida).
- HTTP 404 se traduce a DataError.NotFound (Recurso no disponible).
- HTTP 500-599 se traduce a DataError.Server (Falla temporal del servicio).
- SerializationException se traduce a DataError.InvalidPayload (Estructura JSON invalida).
- CancellationException se propaga correctamente para respetar el ciclo de vida de las corrutinas en los ViewModels.

## 5. Pruebas y Validación en Dispositivo

- Validación Offline-First: Comprobado en dispositivos físicos sin conectividad Wi-Fi ni tarjeta SIM. La persistencia local en Room permite la consulta, creacion, edicion y eliminacion fluida de registros de manera completamente autonoma.
- Gestion de Estados: Separacion limpia entre el estado del contenido local y el estado breve de la operacion de actualizacion remota (refresh), permitiendo mostrar avisos de sincronizacion no invasivos sin bloquear la visualizacion de datos previos.
- Pruebas Unitarias: Arquitectura preparada para pruebas deterministas con servidores simulados (MockWebServer) y cobertura de los casos de aceptacion obligatorios (CA-01 a CA-08).

Referencias:
Android Developers. (s.f.). Pruebas de la capa de datos. Recuperado de https://developer.android.com/training/testing/fundamentals

## 6. Uso de Inteligencia Artificial Validado

Desarrollo y depuración asistida mediante flujos interactivos de control de calidad, garantizando la correcta adaptacion de tipos de Long a Int, el cumplimiento de normas de codificacion en Kotlin y la estabilidad de la arquitectura orientada a la entrega del proyecto de la Semana 8.