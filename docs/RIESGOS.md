# Gestión y Matriz de Riesgos - MiFormacionCTMA

## 1. Objetivo

Se realiza el análisis de riesgos para identificar situaciones que podrían afectar el cumplimiento
de las historias de usuario del alcance inicial y avanzado de **MiFormaciónCTMA**.
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
| R14 | Intentos de selección de archivos corruptos, imágenes sobredimensionadas o de formatos no permitidos. | CA-04                  |            3 |       4 | 12 — Alto    |
| R15 | Pérdida de conectividad o fallo del servidor durante la subida de evidencia fotográfica.              | CA-06                  |            4 |       3 | 12 — Alto    |
| R16 | Rechazo o revocación del permiso de notificaciones para recordatorios de actividades.                 | CA-07                  |            3 |       2 | 6 — Medio    |

---

## 4. Tratamiento de los riesgos

### R01 — Actividades pendientes no visibles

**Mitigación:** Realizar pruebas funcionales que comprueben que las actividades se muestran
correctamente en la pantalla principal.

### R02 — Clasificación incorrecta de actividades pendientes

**Mitigación:** Mantener centralizadas las reglas de negocio de estado en `ReglasActividadTest.kt`.

### R03 — Actividad próxima a vencer no identificada

**Mitigación:** Definir reglas claras de días restantes `<= 2` para determinar urgencia.

### R04 — Cálculo incorrecto de días restantes

**Mitigación:** Validar formato `AAAA-MM-DD` mediante `ReglasActividad`.

### R05 — Porcentaje de progreso incorrecto

**Mitigación:** Validar rangos de progreso entre 0 y 100%.

### R06 — Pérdida de información al reiniciar la app

**Mitigación:** Implementar persistencia en Room (`ActividadEntity`, `EvidenciaEntity`) con
migraciones probadas.

### R07 — Regresión durante el desarrollo

**Mitigación:** Suite automatizada de pruebas unitarias continuas.

### R08 — Identificador de actividad inexistente

**Mitigación:** Pantalla de estado de error visual sin cierres inesperados.

### R09 — Pérdida de borrador por rotación

**Mitigación:** Elevar el estado a ViewModel e implementar `rememberSaveable`.

### R10 — Búsqueda inconsistente

**Mitigación:** Persistir `textoBusqueda` en estado inmutable de UI.

### R11 — Títulos con espacios en blanco

**Mitigación:** Aplicar `.trim()` en validadores antes de guardar.

### R12 — Duplicación en pila de navegación

**Mitigación:** Configurar desapilamiento explícito en `NavHost`.

### R13 — Desincronización del slider

**Mitigación:** Flujo unidireccional de datos (UDF) para el progreso.

### R14 — Selección de archivos corruptos o sobredimensionados (>5MB)

**Mitigación:** Validación previa vía `ContentResolver` en `EvidenciaValidador.kt` rechazando el
archivo en la UI antes de guardarlo.

### R15 — Fallo de subida o pérdida de red al enviar evidencia

**Mitigación:** Conservar el archivo local en almacenamiento interno con estado `FALLIDA` en Room y
habilitar botón de reintento.

### R16 — Rechazo del permiso de notificaciones (`POST_NOTIFICATIONS`)

**Mitigación:** Solicitud contextual al activar el interruptor de recordatorios, manteniendo la app
totalmente funcional si el usuario rechaza.
