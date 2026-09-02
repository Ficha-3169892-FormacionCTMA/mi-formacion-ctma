# Casos de Prueba — MiFormacionCTMA

| ID        | HU / CA         | Técnica        | Precondición           | Pasos                                            | Esperado                                                   |  Estado  |
|:----------|:----------------|:---------------|:-----------------------|:-------------------------------------------------|:-----------------------------------------------------------|:--------:|
| **CP-01** | HU-04 / CA-04.3 | Caso de uso    | Pantalla Crear abierta | Navegar por primera vez a la pantalla            | Campos inician vacíos sin errores agresivos                | **PASÓ** |
| **CP-02** | HU-04 / CA-04.1 | Equivalencia   | Formulario abierto     | Escribir `"AB"` en el título y cambiar de campo  | Muestra `"Usa al menos 3 caracteres"` y bloquea botón      | **PASÓ** |
| **CP-03** | HU-04 / CA-04.2 | Negativa       | Formulario abierto     | Ingresar fecha `"15-09-2026"`                    | Muestra error exigiendo formato `"AAAA-MM-DD"`             | **PASÓ** |
| **CP-04** | HU-05 / CA-05.1 | Recreación     | Borrador en progreso   | Girar el dispositivo horizontalmente             | Conserva el borrador escrito en los campos                 | **PASÓ** |
| **CP-05** | HU-04 / CA-04.4 | Error guessing | Formulario válido      | Presionar rápidamente el botón Guardar dos veces | Se procesa una sola vez y no duplica la entidad            | **PASÓ** |
| **CP-06** | HU-07 / CA-07.1 | Caso de uso    | Lista con ítems        | Seleccionar una tarjeta de la lista              | Transfiere el ID y carga la vista de Detalle               | **PASÓ** |
| **CP-07** | HU-07 / CA-07.2 | Negativa       | Navegación activa      | Abrir ruta con ID inexistente (`-1`)             | Muestra pantalla de error controlado sin crash             | **PASÓ** |
| **CP-08** | HU-07 / CA-07.3 | Caso de uso    | Vista Detalle/Crear    | Presionar la flecha superior para volver         | Retorna a la lista principal limpiando el back stack       | **PASÓ** |
| **CP-09** | HU-06 / CA-06.1 | Equivalencia   | Lista con ítems        | Escribir `"Kotlin"` en la barra de búsqueda      | La lista se filtra en tiempo real mostrando coincidencias  | **PASÓ** |
| **CP-10** | HU-04 / CA-04.1 | Negativa       | Formulario abierto     | Escribir `"   "` (solo espacios) en título       | Muestra `"El título es obligatorio"` y deshabilita guardar | **PASÓ** |
| **CP-11** | HU-06 / CA-06.3 | Limites        | Lista cargada          | Escribir `"XYZ999"` sin coincidencias            | Presenta vista de resultado vacío de forma limpia          | **PASÓ** |
| **CP-12** | HU-05 / CA-05.2 | Recreación     | Formulario abierto     | Asignar progreso al 50% y rotar la pantalla      | El indicador del slider conserva el valor 50%              | **PASÓ** |
| **CP-13** | HU-07 / CA-07.3 | Caso de uso    | Pantalla Detalle       | Presionar el botón Volver                        | Retorna al listado sin duplicar vistas en la pila          | **PASÓ** |
| **CP-14** | HU-08 / CA-08.1 | Limites        | Formulario abierto     | Deslizar control de progreso a 0% y 100%         | Ambos valores son aceptados y procesados                   | **PASÓ** |
| **CP-15** | HU-03 / CA-03.1 | Caso de uso    | Lista disponible       | Abrir detalle de una actividad con 75% avance    | La pantalla muestra correctamente la cifra 75%             | **PASÓ** |
| **CP-16** | HU-02 / CA-02.1 | Negocio        | Actividades en lista   | Cargar ítem con 1 día restante                   | Se despliega resaltado con el estado de urgente            | **PASÓ** |
