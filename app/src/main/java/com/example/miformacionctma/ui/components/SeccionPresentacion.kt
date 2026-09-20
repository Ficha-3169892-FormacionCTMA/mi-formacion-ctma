package com.example.miformacionctma.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miformacionctma.R

@Composable
fun SeccionPresentacion(
    resumen: String,
    modifier: Modifier = Modifier,
    onResumenClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "MI FORMACIÓN CTMA",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "CENTRO DE TECNOLOGÍA MANUFACTURA AVANZADA",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }

        Image(
            painter = painterResource(id = R.drawable.ilustracion_formacion),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        )

        Card(
            onClick = { onResumenClick() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ESTADO DEL SPRINT",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = resumen.trimEnd(),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }
        }
    }
}


@Composable
fun SeccionAgile(
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "FUNDAMENTOS ÁGILES",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )

        Card(
            onClick = { onCardClick() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Principios Ágiles", style = MaterialTheme.typography.titleMedium)

                PrincipioItem("1. Satisfacer al cliente", "Entregar valor útil desde etapas tempranas y de forma continua.")
                PrincipioItem("2. Aceptar cambios", "Los cambios en los requisitos pueden mejorar el producto final.")
                PrincipioItem("3. Entregas frecuentes", "Mostrar funcionalidades funcionando en periodos cortos.")
                PrincipioItem("4. Trabajo conjunto", "Desarrolladores y usuarios deben colaborar constantemente.")
                PrincipioItem("5. Equipos motivados", "Las personas motivadas producen mejores resultados.")
                PrincipioItem("6. Comunicación directa", "Hablar directamente reduce errores y acelera decisiones.")
                PrincipioItem("7. Software funcional", "El progreso real se mide por funcionalidades que funcionan.")
                PrincipioItem("8. Ritmo sostenible", "El equipo debe mantener una carga de trabajo equilibrada.")
                PrincipioItem("9. Excelencia técnica", "El buen diseño y el código limpio facilitan la evolución del sistema.")
                PrincipioItem("10. Simplicidad", "Hacer solo lo necesario evita trabajo innecesario.")
                PrincipioItem("11. Autoorganización", "Los equipos organizan su propio trabajo y toman decisiones técnicas.")
                PrincipioItem("12. Mejora continua", "El equipo revisa su trabajo y busca mejorar en cada Sprint.")
            }
        }
    }
}

@Composable
private fun AgileBullet(text: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = "• ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
    }
}

@Composable
private fun PrincipioItem(titulo: String, descripcion: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(titulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text(descripcion, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
    }
}
