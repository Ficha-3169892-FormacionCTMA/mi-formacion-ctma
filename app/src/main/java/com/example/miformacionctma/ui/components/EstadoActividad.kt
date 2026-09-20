package com.example.miformacionctma.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun EstadoActividad(
    texto: String,
    modifier: Modifier = Modifier
) {
    val colorBg = when(texto.uppercase()) {
        "COMPLETADA" -> Color(0xFFE8F5E9)
        "EN PROCESO" -> Color(0xFFFFF3E0)
        else -> Color(0xFFF5F5F5)
    }
    
    val colorText = when(texto.uppercase()) {
        "COMPLETADA" -> Color(0xFF2E7D32)
        "EN PROCESO" -> Color(0xFFE65100)
        else -> Color(0xFF616161)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = colorBg
    ) {
        Text(
            text = texto.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = colorText,
            fontWeight = FontWeight.Bold
        )
    }
}
