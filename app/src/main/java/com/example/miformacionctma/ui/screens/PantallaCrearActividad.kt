package com.example.miformacionctma.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.miformacionctma.model.ReglasActividad

@Composable
fun PantallaCrearActividad(
    onGuardar: (String, String) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    var titulo by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var intentoGuardar by rememberSaveable { mutableStateOf(false) }

    val tituloError = ReglasActividad.validarTitulo(titulo, mostrarVacio = intentoGuardar)

    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Nueva Actividad Formativa",
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it.take(80) },
                label = { Text("Título de la actividad") },
                isError = tituloError != null,
                supportingText = {
                    if (tituloError != null) {
                        Text(tituloError, color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("${titulo.length}/80")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it.take(240) },
                label = { Text("Descripción (Opcional)") },
                supportingText = { Text("${descripcion.length}/240") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )

            Button(
                onClick = {
                    intentoGuardar = true
                    if (tituloError == null && titulo.isNotBlank()) {
                        onGuardar(titulo.trim(), descripcion.trim())
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Actividad")
            }

            OutlinedButton(
                onClick = onVolver, // <--- Conectado aquí
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }
}