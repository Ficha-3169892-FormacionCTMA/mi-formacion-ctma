# Plan de Pruebas Consolidado — MiFormacionCTMA

Este documento detalla la suite de pruebas automatizadas del proyecto, integrada por 30 casos de prueba divididos equitativamente entre pruebas unitarias e instrumentadas, cumpliendo con los requerimientos de las Semanas 2 a 7.

## Resumen de Ejecución
- **Total de Pruebas:** 30
- **Pruebas Unitarias:** 15 (Pasaron exitosamente)
- **Pruebas Instrumentadas:** 15 (Pasaron exitosamente)

---

## 1. Pruebas Unitarias (Lógica y Estado)
Ubicación: `ConsolidatedUnitTests.kt`

| ID | Caso de Prueba | Técnica | Esperado | Estado |
|:---|:---|:---|:---|:---:|
| **CP-02** | Título < 3 caracteres | Equivalencia | Mensaje "Usa al menos 3 caracteres" | **PASÓ** |
| **CP-03** | Fecha inválida | Negativa | Mensaje "Formato inválido (AAAA-MM-DD)" | **PASÓ** |
| **CP-10** | Título solo espacios | Negativa | Mensaje "El título es obligatorio" | **PASÓ** |
| **CP-14** | Límites de progreso (0% y 100%) | Límites | Valores permitidos y validados | **PASÓ** |
| **CP-16** | Marcado de urgencia (<= 2 días) | Negocio | La actividad se marca como urgente | **PASÓ** |
| **UNIT-06**| Cálculo de promedio de progreso | Lógica | Resultado matemático exacto (ej. 75%) | **PASÓ** |
| **UNIT-07**| Determinación estado "Completada" | Lógica | Estado cambia a "Completada" al 100% | **PASÓ** |
| **UNIT-08**| Mapeo Room: Entidad a Dominio | Persistencia | Conversión íntegra de tipos de datos | **PASÓ** |
| **UNIT-09**| Mapeo Room: Dominio a Entidad | Persistencia | Preservación de estructura relacional | **PASÓ** |
| **UNIT-10**| Restricción: ID competencia defecto | Restricciones| El ID inicial de competencia es 0L | **PASÓ** |
| **UNIT-11**| CA-01: Estado inicial de lista | Transición | Inicia en estado "Vacio" si no hay datos | **PASÓ** |
| **UNIT-12**| CA-03: Filtrado por competencia | Combinación | La lista se reduce según el filtro | **PASÓ** |
| **UNIT-13**| CA-04: Precedencia de búsqueda | Concurrencia | Prevalece la consulta más reciente | **PASÓ** |
| **UNIT-14**| CA-05: Mapeo de errores de flujo | Errores | La excepción se traduce a UiState.Error | **PASÓ** |
| **UNIT-15**| Operación: Flujo de guardado exitoso | Operación | Transición a OperacionUiState.Exitosa | **PASÓ** |

---

## 2. Pruebas Instrumentadas (UI y Persistencia)
Ubicación: `ConsolidatedInstrumentedTests.kt`

| ID | Caso de Prueba | Componente | Esperado | Estado |
|:---|:---|:---|:---|:---:|
| **CP-16** | Persistencia de item urgente | Room DAO | El item ALTA prioridad persiste en DB | **PASÓ** |
| **INST-02**| Inserción y consulta por ID | Room DAO | Recuperación íntegra de la actividad | **PASÓ** |
| **INST-03**| Eliminación de actividad | Room DAO | El registro desaparece de la base | **PASÓ** |
| **INST-04**| Búsqueda de actividades en DB | Room DAO | Retorna coincidencias por texto | **PASÓ** |
| **CP-05** | Prevención de doble pulsación | UI Behavior | El proceso se controla mediante estados | **PASÓ** |
| **CP-01** | Campos inician vacíos | UI State | El formulario carga limpio sin errores | **PASÓ** |
| **CP-04** | Conservación borrador (Giro) | Lifecycle | El texto escrito sobrevive a rotación | **PASÓ** |
| **CP-06** | Selección y transferencia de ID | Navegación | Se carga la ruta de detalle correcta | **PASÓ** |
| **CP-07** | ID inexistente manejo sin crash | Error Handling| Muestra pantalla de error controlada | **PASÓ** |
| **CP-08** | Flecha superior para volver | Nav Flow | Retorna a lista limpiando backstack | **PASÓ** |
| **CP-09** | Filtrado en tiempo real (Búsqueda) | UI Logic | Lista se actualiza al escribir | **PASÓ** |
| **CP-11** | Búsqueda sin coincidencias | UI Logic | Presenta vista de resultado vacío | **PASÓ** |
| **CP-12** | Slider conserva valor (Giro) | State | Progreso se mantiene tras rotación | **PASÓ** |
| **CP-13** | Botón Volver desde detalle | Nav Flow | Regresa al listado sin duplicación | **PASÓ** |
| **CP-15** | Visualización de porcentaje (75%) | Data Display | El detalle muestra la cifra correcta | **PASÓ** |
