# Historias de Usuario — MiFormacióoCTMA

### HU-01 - Consultar listado de actividades pendientes

**Como** aprendiz,  
**quiero** ver mis actividades pendientes,  
**para** organizar mi tiempo de estudio.

#### Criterios de Aceptación

* **CA-01.1:** Al abrir la aplicación se muestra un listado con las actividades registradas.
* **CA-01.2:** Cada elemento del listado presenta título, fecha límite, estado de prioridad y
  porcentaje de progreso.
* **CA-01.3:** Si no hay actividades registradas, se muestra una interfaz limpia con estado vacío.

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

* **CA-02.1:** Una actividad con pocos días restantes (1 a 3 días) se categoriza automáticamente
  como urgente.
* **CA-02.2:** La interfaz resalta visualmente la urgencia de la actividad en la tarjeta
  correspondiente.

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

* **CA-03.1:** El sistema muestra el porcentaje de progreso asociado a la actividad (0% a 100%).
* **CA-03.2:** Es posible abrir una vista detallada para inspeccionar la descripción completa de la
  evidencia.

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

* **CA-04.1:** El título es obligatorio y debe contener entre 3 y 80 caracteres.
* **CA-04.2:** La fecha límite debe validar el formato correcto (AAAA-MM-DD).
* **CA-04.3:** Los mensajes de error no aparecen de entrada hasta que el usuario interactúa con los
  campos.
* **CA-04.4:** Una doble pulsación rápida en Guardar no genera registros duplicados.

#### Riesgos Relacionados

* R02
* R04
* R11

#### Casos de Prueba Relacionados

* CP-01
* CP-02
* CP-03
* CP-05
* CP-10

---

### HU-05 - Conservar borrador por rotación de pantalla

**Como** aprendiz,  
**quiero** que la información ingresada en el formulario no se borre al cambiar la orientación del
dispositivo,  
**para** evitar diligenciar los datos nuevamente.

#### Criterios de Aceptación

* **CA-05.1:** Los datos parciales en los campos de texto se conservan al recrear la pantalla por
  rotación.
* **CA-05.2:** La selección de prioridad y progreso inicial se mantienen intactas tras el cambio de
  pantalla.

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

* **CA-06.1:** Al ingresar caracteres en la barra de búsqueda, la lista filtra coincidencias en
  tiempo real.
* **CA-06.2:** El texto ingresado en la búsqueda persiste ante giros o rotaciones de pantalla.
* **CA-06.3:** Si no hay coincidencias, se despliega una vista controlada de resultado no
  encontrado.

#### Riesgos Relacionados

* R01
* R10

#### Casos de Prueba Relacionados

* CP-09
* CP-11

---

### HU-07 - Navegación segura entre pantallas y manejo de ID

**Como** aprendiz,  
**quiero** navegar entre la lista, la creación y el detalle sin errores de aplicación,  
**para** recorrer la aplicación de forma fluida.

#### Criterios de Aceptación

* **CA-07.1:** La navegación al detalle transfiere únicamente el identificador primitivo ID.
* **CA-07.2:** Si se solicita un ID que no existe, la app muestra un estado de error controlado sin
  cerrarse abruptamente (crash).
* **CA-07.3:** El botón de regreso elimina la pantalla actual del back stack sin duplicar rutas.

#### Riesgos Relacionados

* R08
* R12

#### Casos de Prueba Relacionados

* CP-06
* CP-07
* CP-08
* CP-13

---

### HU-08 - Modificar y actualizar progreso de evidencia

**Como** aprendiz e instructor,  
**quiero** ajustar el indicador de avance mediante una escala del 0% al 100%,  
**para** reflejar el grado de ejecución real del trabajo formativo.

#### Criterios de Aceptación

* **CA-08.1:** El selector de progreso permite elegir valores continuos de 0 a 100.
* **CA-08.2:** El estado reflejado en el detalle se actualiza en coherencia con la entrada del
  usuario.

#### Riesgos Relacionados

* R05
* R13

#### Casos de Prueba Relacionados

* CP-14
* CP-15
