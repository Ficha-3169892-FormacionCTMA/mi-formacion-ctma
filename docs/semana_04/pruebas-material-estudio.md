# Pruebas material de estudio

## **Evidencia punto 12.4 sobre la conservación de estado al rotar pantalla**

### **1. Procedimiento Realizado**

1. Se abrió la aplicación en el emulador y se navegó a la pantalla **Nueva Actividad Formativa**
   mediante el botón de agregar (`+`).

2. Se ingresó texto en los campos de entrada:
    * **Título:** `"Estudio de Jetpack Compose"`
    * **Descripción:** `"Prueba persistencia borrador"`

3. Sin guardar la actividad, se provocó una recreación de la `Activity` rotando el dispositivo de
   orientación **Vertical (Portrait)** a **Horizontal (Landscape)**.

---

### **2. Resultado Observado**

* El texto ingresado en ambos campos permaneció intacto tras la recomposición de la interfaz en modo
  horizontal.
* Las validaciones mantuvieron el estado de interacción previo sin mensajes
  de error.

---

### **3. Justificación Técnica**

El comportamiento se logró gracias a la implementación de `rememberSaveable` en el composable
`PantallaCrearActividad`:

```kotlin
var titulo by rememberSaveable { mutableStateOf("") }
var descripcion by rememberSaveable { mutableStateOf("") }
```

---

## **Evidencia punto 12.5 sobre el manejo de estado de error y recuperación accessible**

### **1. Procedimiento Realizado**

1. Se forzó una navegación hacia la ruta de detalle con un identificador que no existe dentro de la
   lista de datos (`listaActividades`):
    ```kotlin
    navController.navigate(Destino.Detalle.crearRuta(999L))
    ```

2. El sistema evaluó la búsqueda en el modelo de datos dentro de `PantallaDetalleActividad`:
    ```kotlin
    val actividad = actividades.find { it.id == actividadId }
    ```

---

## **2. Resultado Observado y Estructura de Interfaz**

Al resultar en `null`, la interfaz no genera un fallo, sino que activa una vista del error claro y
accesible:

```kotlin
if (actividad != null) {
    // Muestra información de la actividad
} else {
    // Vista de recuperación accesible
    Text(
        text = "Actividad no encontrada (ID: $actividadId)",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.error
    )
    Text(
        text = "La actividad solicitada no existe o fue eliminada.",
        style = MaterialTheme.typography.bodyMedium
    )
    Button(onClick = onVolver) {
        Text("Volver a la lista")
    }
}
```

---

## **3. Justificación Técnica**

* **Inmutabilidad y Null Safety:** El uso de la función `find` retorna un tipo
  `ActividadFormativa?` opcional, obligando al sistema a manejar explícitamente la ausencia de datos
  antes de mostrar los textos.
* **Recuperación navegable:** Se provee una acción clara (`onVolver`) vinculada a
  `navController.popBackStack()`, devolviendo al usuario de forma segura al destino `Lista` en lugar
  de dejar la pantalla bloqueada o vacía.

## **Evidencia punto 12.6 sobre la pila del Back Stack de Navegación**

1. **Estado Inicial (Base de la Pila):** El destino inicial configurado en el `NavHost` es
   `Destino.Lista.ruta`. Esta pantalla siempre permanece en la base de la pila como la vista raíz
   de la aplicación.

2. **Comportamiento en Navegación Positiva (`navigate`):** Cada llamada a
   `navController.navigate(...)` apila un nuevo destino (`Crear` o `Detalle(id)`) sobre `Lista`,
   pausando la ejecución de la pantalla anterior.

3. **Comportamiento en Navegación Trasera (`popBackStack`):** Al presionar "Volver" o "Cancelar",
   se destruye el composable en la cima de la pila y se restaura el estado de `Lista`.

4. **Estado Final en la Acción 6:** La pila contiene dos elementos: `[Lista, Detalle(id=2)]`. Si
   el usuario presiona el botón físico de atrás del sistema Android, se removerá `Detalle(id=2)`
   y regresará a `Lista`.

## **Evidencia punto 12.7 sobre la modificación de la regla de negocio**

1. **Cambio aplicado:** Se modificó la regla `validarTitulo` en `ReglasActividad.kt`, elevando la
   longitud mínima del título de 3 a 5 caracteres.

2. **Demostración de estabilidad:** Se ejecutaron las pruebas unitarias (`ReglasActividadTest`).  

**Resultado:**
1. La prueba ajustada para `validarTitulo` pasó exitosamente con la nueva restricción.

2. Las pruebas preexistentes (`promedioProgreso_calculaCorrectamente`,
          `actividadesUrgentes_filtraCorrectamente` y `estadoActividad_devuelvePendiente`) continuaron
          pasando en verde (**PASSED**).
