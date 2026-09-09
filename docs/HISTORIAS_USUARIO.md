# Historias de Usuario — Mi Formación CTMA

### HU-01 - Consultar listado de actividades pendientes

**Como** aprendiz,  
**quiero** ver mis actividades pendientes,  
**para** organizar mi tiempo de estudio.

#### Criterios de Aceptación

* **CA-01.1:** Al seleccionar una tarjeta de la lista se transfiere su ID para navegar al detalle.
* **CA-01.2:** Al accionar el botón Volver desde la lista se limpia el historial de navegación.

#### Riesgos Relacionados

* R01
* R02

#### Casos de Prueba Relacionados

* CP-06
* CP-08

---

### HU-02 - Identificar actividades próximas a vencer

**Como** aprendiz,  
**quiero** identificar si una actividad está próxima a vencer,  
**para** priorizar su realización antes del límite.

#### Criterios de Aceptación

* **CA-02.1:** El sistema exige que las fechas ingresadas cumplan el formato estricto (AAAA-MM-DD).
* **CA-02.2:** Una actividad con 2 días o menos restantes y no completada se marca automáticamente
  como urgente.

#### Riesgos Relacionados

* R03
* R04

#### Casos de Prueba Relacionados

* CP-03
* CP-16

---

### HU-03 - Consultar progreso general y detalle para seguimiento

**Como** instructor,  
**quiero** consultar el estado de avance de una actividad,  
**para** realizar seguimiento del proceso formativo de los aprendices.

#### Criterios de Aceptación

* **CA-03.1:** La pantalla de detalle visualiza de forma precisa el porcentaje numérico de progreso
  registrado.
* **CA-03.2:** Si se intenta consultar un ID inexistente (-1), la app maneja la ausencia de datos
  sin cerrarse.

#### Riesgos Relacionados

* R05
* R08

#### Casos de Prueba Relacionados

* CP-07
* CP-15

---

### HU-04 - Registrar nueva actividad formativa mediante formulario

**Como** aprendiz,  
**quiero** ingresar una nueva actividad mediante un formulario interactivo,  
**para** agregar compromisos a mi lista de formación.

#### Criterios de Aceptación

* **CA-04.1:** Al abrir por primera vez la pantalla de creación con campos vacíos no se muestran
  mensajes de error inmediatos.
* **CA-04.2:** El título requiere un mínimo de 3 caracteres, mostrando un mensaje descriptivo si no
  lo cumple.
* **CA-04.3:** Pulsaciones simultáneas o rápidas en Guardar procesan la acción una única vez para
  evitar duplicados.
* **CA-04.4:** Si el título contiene únicamente espacios en blanco, la aplicación deshabilita la
  opción de guardar.

#### Riesgos Relacionados

* R02
* R04
* R11

#### Casos de Prueba Relacionados

* CP-01
* CP-02
* CP-05
* CP-10

---

### HU-05 - Conservar borrador por rotación de pantalla

**Como** aprendiz,  
**quiero** que la información ingresada en el formulario no se borre al cambiar la orientación del
dispositivo,  
**para** evitar diligenciar los datos nuevamente.

#### Criterios de Aceptación

* **CA-05.1:** El texto ingresado en los campos del formulario se conserva ante la recreación de la
  pantalla por rotación.
* **CA-05.2:** El porcentaje seleccionado en el control de progreso se mantiene intacto tras rotar
  el dispositivo.

#### Riesgos Relacionados

* R09

#### Casos de Prueba Relacionados

* CP-04
* CP-12

---

### HU-06 - Filtrar y buscar actividades en tiempo real

**Como** aprendiz,  
**quiero** buscar por texto para filtrar mis actividades por título,  
**para** encontrar rápidamente compromisos específicos.

#### Criterios de Aceptación

* **CA-06.1:** Al ingresar un texto de búsqueda (ej. "Kotlin"), la lista filtra las coincidencias
  ignorando mayúsculas/minúsculas.
* **CA-06.2:** Si la búsqueda no genera coincidencias, se despliega una vista visual de estado
  vacío.

#### Riesgos Relacionados

* R01
* R10

#### Casos de Prueba Relacionados

* CP-09
* CP-11

---

### HU-07 - Navegación segura entre pantallas y manejo de ID

**Como** aprendiz,  
**quiero** navegar entre la lista y el detalle sin errores,  
**para** recorrer la aplicación de forma fluida.

#### Criterios de Aceptación

* **CA-07.1:** El retorno desde la vista de detalle desapila correctamente la pantalla devolviendo
  el control al listado.

#### Riesgos Relacionados

* R08
* R12

#### Casos de Prueba Relacionados

* CP-13

---

### HU-08 - Modificar y actualizar progreso de evidencia

**Como** aprendiz e instructor,  
**quiero** ajustar el indicador de avance mediante una escala del 0% al 100%,  
**para** reflejar el grado de ejecución real del trabajo formativo.

#### Criterios de Aceptación

* **CA-08.1:** El control de progreso acepta valores límite (0% y 100%) como entradas válidas y
  rechaza valores fuera de rango.

#### Riesgos Relacionados

* R05
* R13

#### Casos de Prueba Relacionados

* CP-14
