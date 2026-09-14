# Preguntas sobre Retrofit, REST, caché y arquitectura

## 1. Explique la diferencia entre código HTTP 200, 201, 401, 404 y 500.

* **200 OK:** La solicitud fue procesada correctamente. Por ejemplo, un `GET` que devuelve las actividades.
* **201 Created:** La solicitud fue exitosa y se creó un nuevo recurso. Por ejemplo, un `POST` que crea una actividad.
* **401 Unauthorized:** La solicitud no está autorizada, normalmente porque falta un token válido o las credenciales son incorrectas.
* **404 Not Found:** El recurso solicitado no existe. Por ejemplo, consultar una actividad cuyo ID no está registrado.
* **500 Internal Server Error:** Ocurrió un error interno en el servidor y este no pudo procesar correctamente la solicitud.

---

## 2. ¿Por qué `ActividadDto` no debe utilizarse como modelo de interfaz?

Porque `ActividadDto` pertenece a la **capa de datos remotos** y representa específicamente el formato de información que utiliza la API.

La interfaz debería trabajar con el modelo de dominio `ActividadFormativa`, que es independiente de cómo el servidor estructura sus datos. Esto permite cambiar la API sin tener que modificar toda la interfaz de la aplicación.

---

## 3. Ordene el flujo: Room, DTO, API, UI, mapper y Repository.

El flujo correcto es:

**API → DTO → Mapper → Repository → Room → UI**

Cuando se consulta el servidor, la API obtiene los datos, el DTO los representa, el mapper los convierte al modelo de dominio y el Repository decide cómo almacenarlos en Room. Finalmente, la UI recibe los datos mediante `Flow`.

---

## 4. ¿Qué debe ocurrir con el caché cuando refresh termina en timeout?

El caché **no debe eliminarse ni reemplazarse por una lista vacía**.

La aplicación debe conservar los datos que ya estaban almacenados en Room y mostrar un error indicando que no fue posible actualizar la información.

De esta manera, el usuario puede seguir utilizando los últimos datos disponibles aunque el servidor no esté temporalmente disponible.

---

## 5. ¿Por qué la conectividad validada es una señal y no una garantía permanente?

Porque comprobar que existe conexión a Internet solamente indica que había conectividad **en ese momento**.

La conexión puede perderse inmediatamente después, el servidor puede estar caído, puede existir un problema de DNS o la solicitud puede superar el tiempo límite.

Por eso, una comprobación de conectividad no garantiza que una petición HTTP vaya a funcionar correctamente.

---

## 6. Identifique dos riesgos de escribir un token real en el código o en el repositorio Git.

1. Si el repositorio es público o alguien obtiene acceso a él, puede **robar y utilizar el token** para acceder al servicio.

2. Aunque posteriormente se elimine el token del código, puede permanecer en el **historial de Git**, permitiendo que alguien lo recupere.

Por eso, las credenciales deben manejarse mediante variables de entorno, `local.properties`, secretos u otros mecanismos seguros de configuración.

---

## 7. ¿En qué capa debe configurarse el encabezado Authorization y por qué?

Debe configurarse en la **capa de comunicación HTTP**, normalmente mediante un **Interceptor de OkHttp**.

Esto permite agregar automáticamente el encabezado a las solicitudes:

```text
Authorization: Bearer <token>
```

sin tener que repetirlo en cada método de `ActividadApi`.

Además, mantiene separada la lógica de autenticación de los endpoints y facilita cambiar o renovar el token cuando sea necesario.

---

## 8. Explique cómo `CancellationException` debe tratarse al clasificar errores de red.

`CancellationException` **no debe tratarse como un error normal de red**. Debe volver a lanzarse mediante `throw` para permitir que las corrutinas se cancelen correctamente.

Si se captura como una excepción común y se convierte en un mensaje de error, se puede impedir la cancelación correcta de una operación y provocar comportamientos incorrectos en el `ViewModel` o en la interfaz.
