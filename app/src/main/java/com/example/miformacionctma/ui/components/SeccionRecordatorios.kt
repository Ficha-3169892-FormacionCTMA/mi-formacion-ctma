package com.example.miformacionctma.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

/**
 * Componente de UI en Compose para gestionar la activación contextual voluntaria
 * de los recordatorios de actividades.
 * Solicita el permiso POST_NOTIFICATIONS (Android 13+) ÚNICAMENTE cuando el usuario intenta activarlos.
 */
@Composable
fun SeccionRecordatorios(
    recordatoriosActivados: Boolean,
    onCambiarRecordatorios: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mensajeDenegado by remember { mutableStateOf<String?>(null) }

    // Launcher contextual para solicitar POST_NOTIFICATIONS
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            mensajeDenegado = null
            onCambiarRecordatorios(true)
        } else {
            // Si el usuario lo rechaza: se apaga la preferencia, no se repite el diálogo
            // y la aplicación continúa funcionando normalmente.
            mensajeDenegado =
                "Permiso de notificaciones denegado. La aplicación continuará funcionando normalmente sin enviar recordatorios."
            onCambiarRecordatorios(false)
        }
    }

    fun intentarActivar() {
        mensajeDenegado = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val estadoPermiso = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (estadoPermiso == PackageManager.PERMISSION_GRANTED) {
                onCambiarRecordatorios(true)
            } else {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Android 12 o inferior no requiere permiso de tiempo de ejecución para notificaciones
            onCambiarRecordatorios(true)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Recordatorios de Actividades",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Recibe notificaciones locales sobre las fechas límite de tus actividades formativas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = recordatoriosActivados,
                    onCheckedChange = { nuevoEstado ->
                        if (nuevoEstado) {
                            intentarActivar()
                        } else {
                            mensajeDenegado = null
                            onCambiarRecordatorios(false)
                        }
                    }
                )
            }

            if (mensajeDenegado != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = mensajeDenegado!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
