# Evidencias semana 04

## **1. Matriz de continuidad (Semana 3 --> Semana 4)**

| Elemento / Componente             | Estado en Semana 3                          | Evolución en Semana 4                                                          | Justificación de Arquitectura                                                    |
|-----------------------------------|---------------------------------------------|--------------------------------------------------------------------------------|----------------------------------------------------------------------------------|
| **`ActividadFormativa.kt`**       | Modelo de datos inmutable.                  | Se mantiene sin cambios estructurales.                                         | Fuente de verdad de la entidad de negocio.                                       |
| **`ReglasActividad.kt`**          | Validaciones y cálculos de negocio.         | Se reutilizan las reglas para validar campos del formulario y resúmenes.       | Desacoplamiento de la lógica de negocio respecto a la UI.                        |
| **`TarjetaActividad.kt`**         | Componente visual aislado.                  | Se conecta con el callback `onActividadClick(id)`.                             | Permite la navegación hacia el detalle mediante propagación de eventos.          |
| **`PantallaActividades.kt`**      | Pantalla estática con lista de actividades. | Se integra la barra de búsqueda persistente, estado vacío y el FAB para crear. | Presentación de datos con soporte de acciones globales de la app.                |
| **`PantallaCrearActividad.kt`**   | No existía.                                 | Nueva pantalla *stateless* para el formulario.                                 | Cumplimiento del patrón Unidirectional Data Flow (UDF).                          |
| **`PantallaDetalleActividad.kt`** | No existía.                                 | Nueva pantalla para visualizar la información de una actividad según su ID.    | Manejo de navegación con argumentos y tratamiento de errores si el ID no existe. |
| **`MiFormacionAppNav.kt`**        | No existía.                                 | Contenedor principal de rutas (`NavHost`) y propietario del estado global.     | Gestión centralizada de back stack, estado y navegación.                         |

---

## **2. Registro de evidencia de los 8 casos de prueba**

| ID        | Caso de Prueba                        | Entrada / Acción                                                             | Resultado Esperado                                                                          | Resultado Obtenido                                                        | Estado   |
|-----------|---------------------------------------|------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------|---------------------------------------------------------------------------|----------|
| **CP-01** | **Formulario Limpio al Iniciar**      | Navegar a la pantalla Crear Actividad.                                       | Los campos inician vacíos sin mostrar mensajes de error "agresivos" de entrada.             | Los errores permanecen ocultos hasta que el usuario interactúa.           | **PASÓ** |
| **CP-02** | **Validación de Título Mínimo**       | Escribir `"AB"` en el título y tocar otro campo.                             | Muestra mensaje de error indicando mínimo 3 caracteres y deshabilita el botón guardar.      | Muestra `"Usa al menos 3 caracteres"` y el botón se deshabilita.          | **PASÓ** |
| **CP-03** | **Validación de Fecha Formato**       | Escribir `"15-09-2026"` en la fecha.                                         | Muestra error de formato exigiendo `AAAA-MM-DD`.                                            | Muestra `"Ingresa la fecha en formato AAAA-MM-DD"`.                       | **PASÓ** |
| **CP-04** | **Restauración por Rotación**         | Escribir borrador parcial y rotar pantalla del dispositivo.                  | Conserva el borrador escrito sin perder datos.                                              | `rememberSaveable` retiene la información durante la recreación.          | **PASÓ** |
| **CP-05** | **Protección contra Doble Toque**     | Llenar formulario válido y presionar rápidamente el botón Guardar dos veces. | Procesa el guardado una sola vez y evita duplicar la actividad en la lista.                 | La bandera deshabilita la acción inmediatamente tras el primer click.     | **PASÓ** |
| **CP-06** | **Navegación con Argumento Válido**   | Tocar una tarjeta de la lista principal.                                     | Transfiere el `actividadId` y despliega la pantalla de Detalle con la información correcta. | Muestra la pantalla de Detalle con la actividad correspondiente.          | **PASÓ** |
| **CP-07** | **Manejo de Argumento Inexistente**   | Intentar abrir un ID no registrado (ej. `-1`).                               | Muestra un estado de error controlado sin provocar el cierre de la app (`crash`).           | Presenta la interfaz de "Actividad no encontrada" y el botón para volver. | **PASÓ** |
| **CP-08** | **Retorno y Limpieza del Back Stack** | Presionar la flecha de volver desde la pantalla Detalle o Crear.             | Regresa a la pantalla principal sin duplicar instancias en la pila de navegación.           | `popBackStack()` remueve el destino actual y retorna a la lista.          | **PASÓ** |

---

## **3. Diagrama UDF y Mapa de Navegación**

### Flujo de Datos Unidireccional (UDF)

![Diagrama Estado Formulario](evidencias/1.%20diagrama-estado-formulario.png)

---

### Mapa de Destinos y Navegación

![Mapa de Destinos](evidencias/2.%20mapa-destinos.png)

---

#### Descripción del Mapa de Navegación

* **Ruta `lista`:** Pantalla principal. Muestra la lista adaptable de actividades, la barra de
  búsqueda persistente y el botón flotante (FAB).

* **Ruta `crear`:** Formulario stateless. Recibe `FormularioActividadUiState` y emite eventos para
  actualizar el borrador. Al guardar con éxito o cancelar, ejecuta `popBackStack()` para volver a la
  lista.

* **Ruta `detalle/{actividadId}`:** Recibe únicamente el parámetro primitivo `actividadId` (`Long`).
  Busca la actividad en la fuente de verdad o presenta un estado controlado de error si no existe.
