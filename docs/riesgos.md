# Gestión y Matriz de Riesgos - MiFormaciónCTMA

## 1. Objetivo
Se realiza el análisis de riesgos para identificar situaciones que podrían afectar el cumplimiento de las historias de usuario del alcance inicial de **MiFormaciónCTMA**.
Para cada riesgo se establece su probabilidad, impacto, nivel de riesgo, estrategia de mitigación y, cuando corresponde, el caso de prueba utilizado como evidencia de control.

---

## 2. Escala de valoración

### 2.1 Probabilidad

La probabilidad representa qué tan posible es que ocurra el riesgo durante el desarrollo o utilización de la aplicación.

| Valor | Nivel | Descripción |
|---|---|---|
| 1 | Muy baja | Es poco probable que ocurra. |
| 2 | Baja | Puede ocurrir ocasionalmente. |
| 3 | Media | Existe una posibilidad moderada de que ocurra. |
| 4 | Alta | Es probable que ocurra. |
| 5 | Muy alta | Es muy probable que ocurra. |

### 2.2 Impacto

El impacto representa el efecto que tendría el riesgo sobre la aplicación o sobre el cumplimiento de las historias de usuario.

| Valor | Nivel | Descripción |
|---|---|---|
| 1 | Muy bajo | La afectación sobre el proyecto sería mínima. |
| 2 | Bajo | La afectación sería limitada y fácilmente corregible. |
| 3 | Medio | Puede afectar parcialmente una funcionalidad. |
| 4 | Alto | Puede afectar significativamente una funcionalidad o al usuario. |
| 5 | Muy alto | Puede impedir el cumplimiento de una funcionalidad importante. |

### 2.3 Nivel de riesgo

El nivel de riesgo se obtiene mediante la siguiente fórmula:

> **Nivel de riesgo = Probabilidad × Impacto**

| Resultado | Nivel |
|---|---|
| 1–4 | Bajo |
| 5–9 | Medio |
| 10–14 | Alto |
| 15–25 | Crítico |

---

## 3. Registro de riesgos

| ID | Riesgo | Historias relacionadas | Probabilidad | Impacto | Nivel |
|---|---|---|---:|---:|---|
| R01 | Las actividades pendientes no se muestran correctamente al abrir la aplicación. | HU-01 | 2 | 4 | 8 — Medio |
| R02 | Una actividad puede clasificarse incorrectamente y no aparecer como pendiente. | HU-01 | 3 | 4 | 12 — Alto |
| R03 | Una actividad próxima a vencer puede no ser identificada como urgente. | HU-02 | 3 | 4 | 12 — Alto |
| R04 | El cálculo de los días restantes puede presentar errores debido al manejo de fechas. | HU-02 | 3 | 4 | 12 — Alto |
| R05 | El porcentaje de progreso mostrado puede no corresponder con el avance de la actividad. | HU-03 | 2 | 4 | 8 — Medio |
| R06 | La información de las actividades y su progreso puede perderse al cerrar o reiniciar la aplicación. | HU-01, HU-03 | 4 | 4 | 16 — Crítico |
| R07 | Los cambios realizados durante el desarrollo pueden introducir errores en funcionalidades existentes. | HU-01, HU-02, HU-03 | 3 | 4 | 12 — Alto |
| R08 | Un identificador de actividad inexistente puede provocar un fallo durante la navegación al detalle. | HU-03 | 2 | 5 | 10 — Alto |

---

## 4. Tratamiento de los riesgos

### R01 — Actividades pendientes no visibles

**Descripción:**  
Existe el riesgo de que la aplicación no muestre correctamente las actividades pendientes al abrir la pantalla principal.

**Historia relacionada:** HU-01.

**Consecuencia:**  
El aprendiz podría no identificar las actividades que debe realizar y tendría dificultades para organizar su tiempo de estudio.

**Mitigación:**  
Realizar pruebas funcionales que comprueben que las actividades se muestran correctamente en la pantalla principal y que la navegación hacia ellas funciona de manera adecuada.

**Evidencia relacionada:** CP-06 y CP-08.

**Estado:** Controlado parcialmente mediante pruebas de navegación y retorno.

---

### R02 — Clasificación incorrecta de actividades pendientes

**Descripción:**  
Existe el riesgo de que una actividad sea clasificada incorrectamente y no sea identificada como pendiente cuando debería serlo.

**Historia relacionada:** HU-01.

**Consecuencia:**  
El aprendiz podría interpretar incorrectamente su carga de trabajo y dejar de atender una actividad que requiere su atención.

**Mitigación:**  
Mantener centralizadas las reglas de negocio encargadas de determinar el estado de las actividades y realizar pruebas con diferentes valores de progreso y fechas.

**Evidencia relacionada:** Reglas de negocio de la aplicación.

**Estado:** Parcialmente controlado. Se recomienda ampliar las pruebas específicas sobre los diferentes estados de una actividad.

---

### R03 — Actividad próxima a vencer no identificada

**Descripción:**  
Existe el riesgo de que una actividad con pocos días restantes no sea marcada correctamente como urgente.

**Historia relacionada:** HU-02.

**Consecuencia:**  
El aprendiz podría no priorizar una actividad próxima a vencer y aumentar el riesgo de incumplirla.

**Mitigación:**  
Definir una regla clara para determinar cuándo una actividad debe considerarse urgente y realizar pruebas con diferentes cantidades de días restantes.

**Evidencia relacionada:** Reglas de negocio de urgencia.

**Estado:** Pendiente de una prueba funcional específica de identificación de actividades urgentes.

---

### R04 — Cálculo incorrecto de días restantes

**Descripción:**  
Existe el riesgo de que el cálculo de los días restantes de una actividad presente errores relacionados con las fechas.

**Historia relacionada:** HU-02.

**Consecuencia:**  
Una actividad podría ser marcada como urgente cuando no corresponde o podría no ser marcada como urgente cuando debería serlo.

**Mitigación:**  
Validar el formato de las fechas y realizar pruebas con actividades vencidas, próximas a vencer y con fechas futuras.

**Evidencia relacionada:** CP-03 valida el formato de fecha.

**Estado:** Controlado parcialmente. CP-03 comprueba el formato de entrada, pero no cubre por sí solo el cálculo de días restantes.

---

### R05 — Porcentaje de progreso incorrecto

**Descripción:**  
Existe el riesgo de que el porcentaje de progreso mostrado en una actividad no corresponda con el avance registrado.

**Historia relacionada:** HU-03.

**Consecuencia:**  
El instructor podría realizar un seguimiento incorrecto del proceso formativo debido a información de progreso inexacta.

**Mitigación:**  
Validar los valores de progreso y realizar pruebas utilizando diferentes porcentajes, incluyendo 0 %, valores intermedios y 100 %.

**Evidencia relacionada:** Reglas de negocio y visualización del detalle.

**Estado:** Parcialmente controlado. Se recomienda incorporar una prueba específica para validar diferentes valores de progreso.

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
No existe actualmente un caso de prueba específico que compruebe la persistencia después de reiniciar la aplicación.

**Estado:** Pendiente.

**Prioridad:** Crítica.

---

### R07 — Regresión durante el desarrollo

**Descripción:**  
Los cambios realizados durante el desarrollo pueden introducir errores en funcionalidades que anteriormente funcionaban correctamente.

**Historias relacionadas:** HU-01, HU-02 y HU-03.

**Consecuencia:**  
Una modificación podría afectar la lista de actividades, la identificación de actividades urgentes o la visualización del progreso.

**Mitigación:**  
Ejecutar las pruebas funcionales después de cambios importantes y mantener pruebas automatizadas para las reglas principales de negocio.

**Evidencia relacionada:** CP-01 a CP-08.

**Estado:** Controlado mediante la ejecución de los casos de prueba registrados en Semana 04.

---

### R08 — Identificador de actividad inexistente

**Descripción:**  
Existe el riesgo de que la navegación hacia el detalle reciba un identificador de actividad que no exista en los datos registrados.

**Historia relacionada:** HU-03.

**Consecuencia:**  
La aplicación podría presentar un error, mostrar información incorrecta o impedir que el usuario continúe con la navegación.

**Mitigación:**  
Validar que el identificador recibido corresponda a una actividad existente y manejar adecuadamente los casos en los que no se encuentre información asociada.

**Evidencia relacionada:** Pruebas de navegación y validación de actividades.

**Estado:** Pendiente de validar mediante un caso de prueba específico para identificadores inexistentes.

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

Se recomienda priorizar la implementación y validación de la **persistencia de datos**, así como ampliar los casos de prueba relacionados con estados de actividades, fechas, progreso e identificadores inexistentes.

El seguimiento periódico de esta matriz permitirá actualizar la probabilidad, impacto y estado de cada riesgo a medida que avance el desarrollo del proyecto.
