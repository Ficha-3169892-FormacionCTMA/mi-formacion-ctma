package com.example.miformacionctma.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.miformacionctma.R
import com.example.miformacionctma.domain.ActividadesDemo
import com.example.miformacionctma.model.ActividadFormativa
import com.example.miformacionctma.model.ReglasActividad
import com.example.miformacionctma.ui.theme.MiFormacionCTMATheme

@Composable
fun SeccionPresentacion(
    resumen: String,
    modifier: Modifier = Modifier,
    onResumenClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Mi Formación CTMA",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Hola, Aprendices",
            style = MaterialTheme.typography.bodyLarge
        )

        Image(
            painter = painterResource(id = R.drawable.ilustracion_formacion),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = MaterialTheme.shapes.medium
                )
        )

        Card(
            onClick = { onResumenClick() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Resumen del Sprint",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = resumen.trimEnd(),
                    style = MaterialTheme.typography.bodyMedium
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Fundamentos Ágiles y SCRUM",
            style = MaterialTheme.typography.headlineSmall
        )

        Card(
            onClick = { onCardClick() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Valores Ágiles", style = MaterialTheme.typography.titleMedium)
                Text("• Individuos e interacciones", style = MaterialTheme.typography.bodyMedium)
                Text("• Software funcionando", style = MaterialTheme.typography.bodyMedium)
                Text("• Colaboración con el cliente", style = MaterialTheme.typography.bodyMedium)
                Text("• Respuesta al cambio", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Card(
            onClick = { onCardClick() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
private fun PrincipioItem(titulo: String, descripcion: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(titulo, style = MaterialTheme.typography.bodyMedium)
        Text(descripcion, style = MaterialTheme.typography.bodySmall)
    }
}

fun construirTextoResumen(actividades: List<ActividadFormativa>): String {
    val urgentes = ReglasActividad.actividadesUrgentes(actividades).size
    val promedio = ReglasActividad.promedioProgreso(actividades)
    val completadas = actividades.count { it.progreso >= 100 }
    val total = actividades.size

    return """
        Urgentes: $urgentes
        Promedio: ${"%.1f".format(promedio)}%
        Completadas: $completadas
        Total actividades: $total
    """.trimIndent()
}

@Preview(showBackground = true)
@Composable
private fun SeccionPresentacionPreview() {
    MiFormacionCTMATheme {
        SeccionPresentacion(
            resumen = construirTextoResumen(ActividadesDemo.listaInicial)
        )
    }
}
