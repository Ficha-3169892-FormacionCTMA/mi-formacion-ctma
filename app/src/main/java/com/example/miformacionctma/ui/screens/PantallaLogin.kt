package com.example.miformacionctma.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.miformacionctma.model.RolUsuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLogin(
    errorLogin: String? = null,
    onLimpiarError: () -> Unit = {},
    onIniciarSesionClick: (email: String, password: String, nombre: String, rol: RolUsuario) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(RolUsuario.APRENDIZ) }
    var errorValidacionLocal by remember { mutableStateOf<String?>(null) }

    fun validarEIngresar() {
        if (email.isBlank() || !email.contains("@")) {
            errorValidacionLocal = "Ingresa un correo electrónico válido"
            return
        }
        if (password.isBlank() || password.length < 4) {
            errorValidacionLocal = "Ingresa una contraseña de al menos 4 caracteres"
            return
        }
        errorValidacionLocal = null
        onLimpiarError()
        onIniciarSesionClick(email.trim(), password.trim(), nombre.trim(), rolSeleccionado)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Acceso Seguro a la Plataforma") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "MiFormación CTMA",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Selecciona tu rol e ingresa tus credenciales (correo y contraseña).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "1. Selecciona tu Rol",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = rolSeleccionado == RolUsuario.APRENDIZ,
                            onClick = { rolSeleccionado = RolUsuario.APRENDIZ },
                            label = { Text("Aprendiz") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = rolSeleccionado == RolUsuario.INSTRUCTOR,
                            onClick = { rolSeleccionado = RolUsuario.INSTRUCTOR },
                            label = { Text("Instructor") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "2. Credenciales de Acceso",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorValidacionLocal = null
                            onLimpiarError()
                        },
                        label = { Text("Correo institucional *") },
                        placeholder = { Text("ejemplo@sena.edu.co") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorValidacionLocal = null
                            onLimpiarError()
                        },
                        label = { Text("Contraseña *") },
                        placeholder = { Text("********") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre Completo (Opcional)") },
                        placeholder = { Text("Ej: Juan Pérez") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val mensajeMostrado = errorValidacionLocal ?: errorLogin
                    if (mensajeMostrado != null) {
                        Text(
                            text = mensajeMostrado,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = { validarEIngresar() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Iniciar Sesión / Registrarse")
                    }
                }
            }

            // accesos rápidos de prueba
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Accesos Rápidos de Prueba (Contraseña predefinida: 123456):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = false,
                            onClick = {
                                email = "instructor@sena.edu.co"
                                password = "123456"
                                nombre = "Instructor Wilson"
                                rolSeleccionado = RolUsuario.INSTRUCTOR
                                errorValidacionLocal = null
                                onLimpiarError()
                            },
                            label = { Text("Instructor") }
                        )

                        FilterChip(
                            selected = false,
                            onClick = {
                                email = "aprendiz1@sena.edu.co"
                                password = "123456"
                                nombre = "Aprendiz Sergio"
                                rolSeleccionado = RolUsuario.APRENDIZ
                                errorValidacionLocal = null
                                onLimpiarError()
                            },
                            label = { Text("Aprendiz 1") }
                        )

                        FilterChip(
                            selected = false,
                            onClick = {
                                email = "aprendiz2@sena.edu.co"
                                password = "123456"
                                nombre = "Aprendiz Joel"
                                rolSeleccionado = RolUsuario.APRENDIZ
                            },
                            label = { Text("Aprendiz 2") }
                        )
                    }
                }
            }
        }
    }
}
