# Gestión y Matriz de Riesgos - MiFormacionCTMA

## 1. Objetivo

Se realiza el análisis de riesgos para identificar situaciones que podrían afectar el cumplimiento
de las historias de usuario del alcance inicial de **MiFormaciónCTMA**.
Para cada riesgo se establece su probabilidad, impacto, nivel de riesgo, estrategia de mitigación y,
cuando corresponde, el caso de prueba utilizado como evidencia de control.

---

## 2. Escala de valoración

### 2.1 Probabilidad

La probabilidad representa qué tan posible es que ocurra el riesgo durante el desarrollo o
utilización de la aplicación.

| Valor | Nivel    | Descripción                                    |
|-------|----------|------------------------------------------------|
| 1     | Muy baja | Es poco probable que ocurra.                   |
| 2     | Baja     | Puede ocurrir ocasionalmente.                  |
| 3     | Media    | Existe una posibilidad moderada de que ocurra. |
| 4     | Alta     | Es probable que ocurra.                        |
| 5     | Muy alta | Es muy probable que ocurra.                    |

### 2.2 Impacto

El impacto representa el efecto que tendría el riesgo sobre la aplicación o sobre el cumplimiento de
las historias de usuario.

| Valor | Nivel    | Descripción                                                      |
|-------|----------|------------------------------------------------------------------|
| 1     | Muy bajo | La afectación sobre el proyecto sería mínima.                    |
| 2     | Bajo     | La afectación sería limitada y fácilmente corregible.            |
| 3     | Medio    | Puede afectar parcialmente una funcionalidad.                    |
| 4     | Alto     | Puede afectar significativamente una funcionalidad o al usuario. |
| 5     | Muy alto | Puede impedir el cumplimiento de una funcionalidad importante.   |

### 2.3 Nivel de riesgo

El nivel de riesgo se obtiene mediante la siguiente fórmula:

> **Nivel de riesgo = Probabilidad × Impacto**

| Resultado | Nivel   |
|-----------|---------|
| 1–4       | Bajo    |
| 5–9       | Medio   |
| 10–14     | Alto    |
| 15–25     | Crítico |

---

## 3. Registro de riesgos

| ID  | Riesgo                                                                                                | Historias relacionadas | Probabilidad | Impacto | Nivel        |
|-----|-------------------------------------------------------------------------------------------------------|------------------------|-------------:|--------:|--------------|
| R01 | Las actividades pendientes no se muestran correctamente al abrir la aplicación.                       | HU-01, HU-06           |            2 |       4 | 8 — Medio    |
| R02 | Una actividad puede clasificarse incorrectamente y no aparecer como pendiente.                        | HU-01, HU-04           |            3 |       4 | 12 — Alto    |
| R03 | Una actividad próxima a vencer puede no ser identificada como urgente.                                | HU-02                  |            3 |       4 | 12 — Alto    |
| R04 | El cálculo de los días restantes puede presentar errores debido al manejo de fechas.                  | HU-02, HU-04           |            3 |       4 | 12 — Alto    |
| R05 | El porcentaje de progreso mostrado puede no corresponder con el avance de la actividad.               | HU-03, HU-08           |            2 |       4 | 8 — Medio    |
| R06 | La información de las actividades y su progreso puede perderse al cerrar o reiniciar la aplicación.   | HU-01, HU-03           |            4 |       4 | 16 — Crítico |
| R07 | Los cambios realizados durante el desarrollo pueden introducir errores en funcionalidades existentes. | HU-01, HU-04           |            3 |       4 | 12 — Alto    |
| R08 | Un identificador de actividad inexistente puede provocar un fallo durante la navegación al detalle.   | HU-03, HU-07           |            2 |       5 | 10 — Alto    |
| R09 | Pérdida de borrador del formulario al rotar la pantalla o cambiar la configuración.                   | HU-04, HU-05           |            4 |       3 | 12 — Alto    |
| R10 | La barra de búsqueda no filtra correctamente en tiempo real o se limpia al rotar.                     | HU-06                  |            3 |       3 | 9 — Medio    |
| R11 | Registro de actividades con títulos con puros espacios en blanco.                                     | HU-04                  |            3 |       3 | 9 — Medio    |
| R12 | Duplicación de instancias de pantallas en la pila de navegación (*back stack*).                       | HU-07                  |            2 |       4 | 8 — Medio    |
| R13 | Desincronización entre el control slider y la variable de progreso.                                   | HU-08                  |            2 |       3 | 6 — Medio    |

---

## 4. Tratamiento de los riesgos

### R01 — Actividades pendientes no visibles

**Descripción:**  
Existe el riesgo de que la aplicación no muestre correctamente las actividades pendientes al abrir la pantalla principal.

**Historias relacionadas:** HU-01, HU-06.

**Consecuencia:**  
El aprendiz podría no identificar las actividades que debe realizar y tendría dificultades para organizar su tiempo de estudio.

**Mitigación:**  
Realizar pruebas funcionales que comprueben que las actividades se muestran correctamente en la pantalla principal y que la navegación hacia ellas funciona de manera adecuada.

**Evidencia relacionada:** CP-06, CP-08, CP-09 y CP-11.

**Estado:** Controlado mediante pruebas de navegación, filtrado y retorno.

---

### R02 — Clasificación incorrecta de actividades pendientes

**Descripción:**  
Existe el riesgo de que una actividad sea clasificada incorrectamente y no sea identificada como pendiente cuando debería serlo.

**Historias relacionadas:** HU-01, HU-04.

**Consecuencia:**  
El aprendiz podría interpretar incorrectamente su carga de trabajo y dejar de atender una actividad que requiere su atención.

**Mitigación:**  
Mantener centralizadas las reglas de negocio encargadas de determinar el estado de las actividades y realizar pruebas con diferentes valores de progreso y fechas.

**Evidencia relacionada:** Reglas de negocio de la aplicación (`ReglasActividadTest.kt`).

**Estado:** Controlado mediante pruebas unitarias de reglas de negocio.

---

### R03 — Actividad próxima a vencer no identificada

**Descripción:**  
Existe el riesgo de que una actividad con pocos días restantes no sea marcada correctamente como urgente.

**Historia relacionada:** HU-02.

**Consecuencia:**  
El aprendiz podría no priorizar una actividad próxima a vencer y aumentar el riesgo de incumplirla.

**Mitigación:**  
Definir una regla clara para determinar cuándo una actividad debe considerarse urgente y realizar pruebas con diferentes cantidades de días restantes.

**Evidencia relacionada:** CP-16.

**Estado:** Controlado mediante prueba funcional específica sobre el indicador de urgencia.

---

### R04 — Cálculo incorrecto de días restantes

**Descripción:**  
Existe el riesgo de que el cálculo de los días restantes de una actividad presente errores relacionados con las fechas.

**Historias relacionadas:** HU-02, HU-04.

**Consecuencia:**  
Una actividad podría ser marcada como urgente cuando no corresponde o podría no ser marcada como urgente cuando debería serlo.

**Mitigación:**  
Validar el formato de las fechas y realizar pruebas con actividades vencidas, próximas a vencer y con fechas futuras.

**Evidencia relacionada:** CP-03 valida el formato de fecha `AAAA-MM-DD`.

**Estado:** Controlado mediante validación de entrada en el formulario y reglas de negocio.

---

### R05 — Porcentaje de progreso incorrecto

**Descripción:**  
Existe el riesgo de que el porcentaje de progreso mostrado en una actividad no corresponda con el avance registrado.

**Historias relacionadas:** HU-03, HU-08.

**Consecuencia:**  
El instructor podría realizar un seguimiento incorrecto del proceso formativo debido a información de progreso inexacta.

**Mitigación:**  
Validar los valores de progreso y realizar pruebas utilizando diferentes porcentajes, incluyendo 0 %, valores intermedios y 100 %.

**Evidencia relacionada:** CP-14 y CP-15.

**Estado:** Controlado mediante pruebas de validación de entradas continuas del slider y despliegue en detalle.

---

### R06 — Pérdida de información

**Descripción:**  
Existe el riesgo de que las actividades registradas y su información de progreso se pierdan cuando la aplicación se cierra o se reinicia.

**Historias relacionadas:** HU-01 y HU-03.

**Consecuencia:**  
El aprendiz podría perder información necesaria para organizar sus actividades y el instructor podría dejar de disponer de información relacionada con el avance formativo.

**Mitigación:**  
Implementar un mecanismo de persistencia de datos y realizar pruebas que comprueben que la información permanece disponible después de cerrar y volver a abrir la aplicación.

**Evidencia relacionada:**  
No existe actualmente un caso de prueba específico que compruebe la persistencia tras matar el proceso completo de la app.

**Estado:** Pendiente.

**Prioridad:** Crítica.

---

### R07 — Regresión durante el desarrollo

**Descripción:**  
Los cambios realizados durante el desarrollo pueden introducir errores en funcionalidades que anteriormente funcionaban correctamente.

**Historias relacionadas:** HU-01, HU-04.

**Consecuencia:**  
Una modificación podría afectar la lista de actividades, la identificación de actividades urgentes o la visualización del progreso.

**Mitigación:**  
Ejecutar las pruebas funcionales después de cambios importantes y mantener pruebas automatizadas para las reglas principales de negocio.

**Evidencia relacionada:** CP-01 a CP-16 y suite unitaria en `ReglasActividadTest.kt`.

**Estado:** Controlado mediante ejecución continua de casos de prueba.

---

### R08 — Identificador de actividad inexistente

**Descripción:**  
Existe el riesgo de que la navegación hacia el detalle reciba un identificador de actividad que no exista en los datos registrados.

**Historias relacionadas:** HU-03, HU-07.

**Consecuencia:**  
La aplicación podría presentar un error, mostrar información incorrecta o impedir que el usuario continúe con la navegación.

**Mitigación:**  
Validar que el identificador recibido corresponda a una actividad existente y manejar adecuadamente los casos en los que no se encuentre información asociada.

**Evidencia relacionada:** CP-07.

**Estado:** Controlado mediante pantalla de error visual y botón de retorno sin cierre abrupto (*crash*).

---

### R09 — Pérdida de borrador en el formulario por rotación

**Descripción:**  
Existe el riesgo de que el texto y las opciones seleccionadas en el formulario de creación se reinicien al cambiar la orientación del dispositivo.

**Historias relacionadas:** HU-04, HU-05.

**Consecuencia:**  
El aprendiz perdería la información ingresada parcialmente y tendría que volver a diligenciar el formulario completo.

**Mitigación:**  
Elevar el estado del formulario e implementar `rememberSaveable` para retener la información durante la recreación de la Activity.

**Evidencia relacionada:** CP-04 y CP-12.

**Estado:** Controlado mediante `rememberSaveable`.

---

### R10 — Búsqueda inconsistente o limpia tras rotación

**Descripción:**  
Existe el riesgo de que la barra de búsqueda pierda el filtro aplicado o no actualice la lista inmediatamente al ingresar caracteres.

**Historia relacionada:** HU-06.

**Consecuencia:**  
El aprendiz tendría que escribir nuevamente el criterio de búsqueda al rotar la pantalla o vería datos no filtrados.

**Mitigación:**  
Persistir la variable `textoBusqueda` mediante `rememberSaveable` y conectar el filtrado directamente al estado inmutable.

**Evidencia relacionada:** CP-09 y CP-11.

**Estado:** Controlado mediante persistencia de consulta en UI state.

---

### R11 — Títulos formados únicamente por espacios en blanco

**Descripción:**  
Existe el riesgo de que un usuario ingrese múltiples espacios vacíos en el campo de título y el sistema lo acepte como un texto válido.

**Historia relacionada:** HU-04.

**Consecuencia:**  
Se registrarían actividades invisibles o vacías en la lista principal, afectando la usabilidad.

**Mitigación:**  
Aplicar la función `.trim()` a las entradas de texto antes de verificar la longitud y la presencia de caracteres.

**Evidencia relacionada:** CP-10 y prueba unitaria en `validarTitulo_evaluaLimitesCorrectamente`.

**Estado:** Controlado en la función de validación de `ReglasActividad`.

---

### R12 — Duplicación de instancias en la pila de navegación (*back stack*)

**Descripción:**  
Existe el riesgo de que al presionar repetidamente el botón de retorno o guardar se apilen múltiples instancias de la pantalla de lista.

**Historia relacionada:** HU-07.

**Consecuencia:**  
El usuario tendría que presionar el botón de atrás múltiples veces para salir de la aplicación o navegar entre pantallas.

**Mitigación:**  
Utilizar la función `popBackStack()` para remover el destino actual de la pila antes de retornar.

**Evidencia relacionada:** CP-08 y CP-13.

**Estado:** Controlado mediante la configuración centralizada de `NavHost`.

---

### R13 — Desincronización del control slider de progreso

**Descripción:**  
Existe el riesgo de que el valor visual del slider de progreso no coincida con el entero retenido en el estado del formulario.

**Historia relacionada:** HU-08.

**Consecuencia:**  
Se guardaría una actividad con un porcentaje de avance distinto al que el usuario creyó haber seleccionado.

**Mitigación:**  
Manejar el valor del slider como un estado numérico entero (0 a 100) actualizado de forma unidireccional (UDF).

**Evidencia relacionada:** CP-14.

**Estado:** Controlado en la implementación de `PantallaCrearActividad`.

---

## 5. Conclusiones

El análisis permite identificar los principales riesgos asociados a las funcionalidades definidas para **Mi Formación CTMA**.

Los riesgos con mayor prioridad están relacionados principalmente con:

- La persistencia de la información.
- La correcta clasificación de las actividades.
- La identificación de actividades próximas a vencer.
- El cálculo de fechas.
- La prevención de regresiones durante el desarrollo.
- La validación de identificadores de actividades.
- La conservación de datos en formulario ante eventos del ciclo de vida (rotación).

Se recomienda priorizar la implementación y validación de la **persistencia de datos en base de datos local**, así como mantener ejecutada la suite completa de casos de prueba (`CP-01` al `CP-16`).

El seguimiento periódico de esta matriz permitirá actualizar la probabilidad, impacto y estado de cada riesgo a medida que avance el desarrollo del proyecto.
