package com.example.nutriplan.ui.screens.dieta

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.example.nutriplan.ui.theme.PrimaryGreen

private val CorProteina = Color(0xFF2196F3)  // Azul
private val CorLipidios = Color(0xFFFF9800)  // Laranja
private val CorCarbo = Color(0xFFF44336)     // Vermelho

@Composable
fun GraficoRosca(
    proteina: Float,
    lipidios: Float,
    carbo: Float
) {
    val total = proteina + lipidios + carbo

    // Se não tiver dados ainda, não mostra nada
    if (total == 0f) {
        Box(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Adicione alimentos para ver o gráfico",
                color = Color.Gray,
                fontSize = 13.sp
            )
        }
        return
    }

    val pctProteina = proteina / total * 100
    val pctLipidios = lipidios / total * 100
    val pctCarbo = carbo / total * 100

    val sweepProteina = proteina / total * 360f
    val sweepLipidios = lipidios / total * 360f
    val sweepCarbo = carbo / total * 360f

    // Centralizar o Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center  // Centralizado
    ) {
        // ── Rosca ──────────────────────────────────────────────
        Canvas(
            modifier = Modifier.size(130.dp)
        ) {
            val stroke = Stroke(width = 38.dp.toPx())
            var startAngle = -90f

            // Proteína (Azul)
            drawArc(
                color = CorProteina,
                startAngle = startAngle,
                sweepAngle = sweepProteina,
                useCenter = false,
                style = stroke
            )
            startAngle += sweepProteina

            // Lipídios (Laranja)
            drawArc(
                color = CorLipidios,
                startAngle = startAngle,
                sweepAngle = sweepLipidios,
                useCenter = false,
                style = stroke
            )
            startAngle += sweepLipidios

            // Carboidratos (Vermelho)
            drawArc(
                color = CorCarbo,
                startAngle = startAngle,
                sweepAngle = sweepCarbo,
                useCenter = false,
                style = stroke
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        // ── Legenda ─────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LegendaItem(
                cor = CorProteina,
                label = "Proteínas",
                valor = proteina,
                pct = pctProteina
            )
            LegendaItem(
                cor = CorLipidios,
                label = "Lipídios",
                valor = lipidios,
                pct = pctLipidios
            )
            LegendaItem(
                cor = CorCarbo,
                label = "Carboidratos",
                valor = carbo,
                pct = pctCarbo
            )
        }
    }
}

// ── Item da legenda ─────────────────────────────────────────────
@Composable
private fun LegendaItem(
    cor: Color,
    label: String,
    valor: Float,
    pct: Float
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .then(
                    Modifier.padding(0.dp)
                ),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = cor)
            }
        }
        Column {
            Text(
                label,
                fontSize = 12.sp,
                color = cor,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "%.1fg (%.1f%%)".format(valor, pct),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen  // MUDANÇA AQUI: verde em vez de preto/branco
            )
        }
    }
}
