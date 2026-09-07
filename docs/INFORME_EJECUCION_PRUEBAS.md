# Informe de Pruebas de Software y Plan de Validación: Proyecto MiFormaciónCTMA

**Autor:** Andrés Felipe Vargas Metrio  
**Institución:** Servicio Nacional de Aprendizaje (SENA) - CTMA  
**Programa:** Análisis y Desarrollo de Software (ADSO)  
**Fecha:** 4 de septiembre de 2026  
**Proyecto:** MiFormación CTMA

---

## 1. Introducción

El presente documento constituye el informe de pruebas de software y plan de validación para la aplicación móvil **Mi ormaciónCTMA**, desarrollada en el marco del programa de formación Análisis y Desarrollo de Software (ADSO) del SENA CTMA.

El objetivo fundamental de esta fase es verificar y validar la calidad técnica del software mediante la ejecución automatizada de pruebas unitarias y la verificación manual en dispositivos móviles reales (Android). A través de esta batería de pruebas se garantiza el cumplimiento estricto de las reglas de negocio, la robustez en el manejo de excepciones y la estabilidad de la interfaz de usuario ante eventos del ciclo de vida (como la rotación de pantalla y la gestión de la pila de navegación).

---

## 2. Contexto y Arquitectura del Sistema

La aplicación **MiFormaciónCTMA** está estructurada bajo los principios de la arquitectura recomendada para Android (Clean Architecture / MVVM simplificado), dividiendo las responsabilidades en capas claras:

1. **Capa de Modelo (`model`):**
   - `ActividadFormativa.kt`: Data class que define los atributos fundamentales de las actividades académicas (ID, título, descripción, fecha, porcentaje de progreso, días restantes y prioridad).
   - `Prioridad.kt`: Enum class que clasifica las prioridades en `BAJA`, `MEDIA` y `ALTA`.
   - `ReglasActividad.kt`: Objeto (`object`) de dominio que encapsula las funciones puras y la lógica de negocio para validaciones, cálculo de estados, filtrados y ordenamiento.

2. **Capa de Pruebas Unitarias (`test`):**
   - `ReglasActividadTest.kt`: Suite de pruebas base desarrollada para la verificación de funciones del dominio.
   - `PlanesDePruebaTest.kt`: Suite de pruebas completa que implementa la totalidad de los 16 Casos de Prueba (CP-01 a CP-16) definidos en el plan de pruebas del proyecto.

---

## 3. Plan de Pruebas y Casos de Uso (CP-01 a CP-16)

A continuación se detalla la especificación formal de cada uno de los 16 casos de prueba diseñados para evaluar los módulos de creación, listado, búsqueda y detalle de actividades.

### Tabla 1: Matriz de Especificación de Casos de Prueba

| ID Caso   | Nombre del Caso                        | Componente / Función Evaluada         | Criterio de Aceptación / Resultado Esperado                                                                                 |
|-----------|----------------------------------------|---------------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| **CP-01** | Estado inicial del formulario          | `ReglasActividad.validarTitulo`       | Al abrir la pantalla de creación con campos vacíos y `mostrarVacio = false`, no debe retornar mensajes de error inmediatos. |
| **CP-02** | Título menor a 3 caracteres            | `ReglasActividad.validarTitulo`       | Retorna el mensaje de error: `"Usa al menos 3 caracteres"`.                                                                 |
| **CP-03** | Formato de fecha inválido              | `ReglasActividad.validarFecha`        | Retorna el mensaje de error: `"Formato inválido (AAAA-MM-DD)"` al recibir formatos tipo `DD-MM-AAAA`.                       |
| **CP-04** | Preservación de borrador por rotación  | Estado de la UI / Formulario          | El texto ingresado en el formulario se preserva ante eventos de recreación de la Activity.                                  |
| **CP-05** | Prevención de doble pulsación          | Control de eventos de Guardar         | Pulsaciones simultáneas o rápidas procesan la acción una única vez para evitar registros duplicados.                        |
| **CP-06** | Transferencia de ID a Vista Detalle    | Navegación entre pantallas            | La ruta de navegación incluye correctamente el identificador único (`id`) de la actividad seleccionada.                     |
| **CP-07** | Manejo de ID inexistente (-1)          | Búsqueda por ID / Detalle             | La consulta devuelve `null` y la aplicación maneja el estado sin generar excepciones no controladas.                        |
| **CP-08** | Retorno a lista limpiando back stack   | Pila de Navegación                    | Al accionar el botón "Volver", se remueve la pantalla actual y se retorna a la vista principal.                             |
| **CP-09** | Búsqueda por palabra clave             | `ReglasActividad.buscarPorTitulo`     | La búsqueda por "Kotlin" filtra correctamente las coincidencias ignorando mayúsculas/minúsculas.                            |
| **CP-10** | Título conformado solo por espacios    | `ReglasActividad.validarTitulo`       | La cadena tratada con `.trim()` resulta vacía, devolviendo `"El título es obligatorio"`.                                    |
| **CP-11** | Búsqueda sin coincidencias             | `ReglasActividad.buscarPorTitulo`     | Retorna una lista vacía y despliega el componente de interfaz correspondiente a estado vacío.                               |
| **CP-12** | Retención de progreso en rotación      | Estado del Slider de Progreso         | El valor numérico del porcentaje de progreso asignado se mantiene tras la rotación de pantalla.                             |
| **CP-13** | Regreso desde pantalla de detalle      | Pila de Navegación                    | Desapila correctamente el detalle devolviendo el control a la lista principal.                                              |
| **CP-14** | Límites del slider de progreso         | `ReglasActividad.validarProgreso`     | Acepta valores limítrofes `0` y `100` como válidos (`null`), y rechaza valores fuera de rango.                              |
| **CP-15** | Visualización de porcentaje en detalle | `ActividadFormativa.progreso`         | Muestra de manera precisa el valor entero del porcentaje asignado.                                                          |
| **CP-16** | Marcado automático de urgencia         | `ReglasActividad.actividadesUrgentes` | Filtra actividades no completadas (`progreso < 100`) con `diasRestantes <= 2` marcándolas como urgentes.                    |

## 4. Resultados de la Ejecución de Pruebas Unitarias

La suite de pruebas fue ejecutada de manera automatizada utilizando la herramienta JUnit 4 integrada en el entorno Android Studio.

* **Total de pruebas ejecutadas:** 20 (4 pruebas en `ReglasActividadTest.kt` y 16 pruebas en `PlanesDePruebaTest.kt`).
* **Pruebas exitosas (Passed):** 20 (100%).
* **Pruebas fallidas (Failed):** 0 (0%).
* **Tiempo total de ejecución:** 364 ms.

```text
===============================================================================
RESULTADOS DE EJECUCIÓN - JUNIT 4 (ANDROID STUDIO)
===============================================================================
Executing tasks: [:app:testDebugUnitTest] in project C:\Users\dmk\AndroidStudioProjects\mi-formacion-ctma

Starting Gradle Daemon...
Gradle Daemon started in 5 s 87 ms
Calculating task graph as no cached configuration is available for tasks: :app:testDebugUnitTest

[✓] Test Results: 20 tests passed (20 total, 364 ms)
===============================================================================

```

### 4.1 Detalle de Verificación por Método de Prueba

1. *cp01_pantallaCrearAbierta_iniciaSinErrores*: Exitoso (Passed)
2. *cp02_tituloCorto_muestraMensajeError*: Exitoso (Passed)
3. *cp03_fechaInvalida_exigeFormatoCorrecto*: Exitoso (Passed)
4. *cp04_rotacionPantalla_conservaBorrador*: Exitoso (Passed)
5. *cp05_guardarDoblePulsacion_procesaUnaSolaVez*: Exitoso (Passed)
6. *cp06_seleccionarTarjeta_transfiereIdCorrectamente*: Exitoso (Passed)
7. *cp07_idInexistente_manejaErrorSinCrash*: Exitoso (Passed)
8. *cp08_botonVolver_retornaALaLista*: Exitoso (Passed)
9. *cp09_busquedaKotlin_filtraCoincidencias*: Exitoso (Passed)
10. *cp10_tituloSoloEspacios_deshabilitaGuardar*: Exitoso (Passed)
11. *cp11_busquedaSinCoincidencias_despliegaEstadoVacio*: Exitoso (Passed)
12. *cp12_rotacionPantalla_conservaSliderProgreso*: Exitoso (Passed)
13. *cp13_regresoDesdeDetalle_limpiaPila*: Exitoso (Passed)
14. *cp14_limitesSlider_aceptaCeroYCien*: Exitoso (Passed)
15. *cp15_detalleActividad_muestraPorcentajeCorrecto*: Exitoso (Passed)
16. *cp16_actividadDosDiasRestantes_marcaUrgente*: Exitoso (Passed)
17. *promedioProgreso_calculaCorrectamente*: Exitoso (Passed)
18. *actividadesUrgentes_filtraCorrectamente*: Exitoso (Passed)
19. *estadoActividad_devuelvePendiente*: Exitoso (Passed)
20. *validarTitulo_evaluaLimitesCorrectamente*: Exitoso (Passed)

---

## 5. Guía de Verificación Manual en Dispositivo Móvil Real

Para la sustentación presencial o virtual del proyecto, se establece el procedimiento secuencial para la comprobación funcional en el dispositivo físico:

1. **Instalación y Despliegue:**
   * Conectar el dispositivo Android vía cable USB asegurando que la opción *Depuración por USB* se encuentre activa.
   * Ejecutar la aplicación desde Android Studio seleccionando el terminal físico objetivo.

2. **Prueba de Formularios y Validaciones (CP-02, CP-03, CP-10, CP-14):**
   * Acceder al módulo de creación de actividad.
   * Ingresar un título con menos de tres caracteres (por ejemplo, "AB") y presionar el botón *Guardar*; validar la presentación de la alerta: *"Usa al menos 3 caracteres"*.
   * Ingresar una fecha en formato no válido (por ejemplo, "15/09/2026") y confirmar el despliegue del mensaje: *"Formato inválido (AAAA-MM-DD)"*.

3. **Prueba de Ciclo de Vida y Estado de Interfaz (CP-04, CP-12):**
   * Diligenciar parcialmente el formulario sin guardar (ingresando título, descripción y ajustando el control deslizante de progreso al 50%).
   * Rotar el dispositivo horizontalmente para activar la vista apaisada y verificar que el borrador mantenga los datos ingresados.

4. **Prueba de Filtro y Búsqueda de Información (CP-09, CP-11):**
   * Ingresar el término "Kotlin" en el campo de búsqueda de la lista principal y confirmar la actualización inmediata de la vista.
   * Digitar una cadena sin coincidencias (por ejemplo, "XYZ999") y validar la renderización del componente visual de lista vacía.

5. **Prueba de Navegación y Control de Pila (CP-06, CP-08, CP-13):**
   * Seleccionar una tarjeta de actividad para navegar hacia la vista de detalle y comprobar la coincidencia del identificador transmitido.
   * Accionar el control *Volver* o ejecutar el gesto de retroceso del sistema operativo para validar el retorno ordenado a la vista principal sin cierres inesperados.

---

## 6. Conclusiones

1. La implementación de las pruebas automatizadas mediante JUnit demostró una cobertura completa de las reglas de negocio declaradas en `ReglasActividad.kt`, asegurando un comportamiento determinista de la aplicación ante datos válidos e inválidos.
2. La arquitectura adoptada facilitó la independencia entre la lógica de dominio y los componentes visuales, lo que permitió simular y validar estados de interfaz sin incurrir en ejecuciones lentas de emuladores.
3. El proyecto **MiFormaciónCTMA** cumple satisfactoriamente con la totalidad de criterios de aceptación exigidos en el Plan de Pruebas, garantizando estabilidad para su presentación funcional.

---

## 7. Referencias

* Beck, K. (2003). *Test-driven development: By example*. Addison-Wesley Professional.
* Google Developers. (2026). *Guide to app architecture: Android Developers*. https://developer.android.com/topic/architecture
* JUnit.org. (2026). *JUnit 4 Documentation*. https://junit.org/junit4/
* Servicio Nacional de Aprendizaje (SENA). (2026). *Diseño de pruebas unitarias y de integración para software móvil*. Centro de Tecnología de la Manufactura Avanzada (CTMA).