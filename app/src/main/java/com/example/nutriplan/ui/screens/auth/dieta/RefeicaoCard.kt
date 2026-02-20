package com.example.nutriplan.ui.screens.dieta

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.nutriplan.data.dieta.DietaItemEntity
import com.example.nutriplan.data.dieta.DietaRefeicaoEntity
import com.example.nutriplan.ui.theme.DarkSurface
import com.example.nutriplan.ui.theme.PrimaryGreen

@Composable
fun RefeicaoCard(
    refeicao: DietaRefeicaoEntity,
    viewModel: DietaViewModel,
    onDeletar: () -> Unit
) {
    var expandido by remember { mutableStateOf(false) }
    var mostrarBusca by remember { mutableStateOf(false) }

    val itens by viewModel.getItens(refeicao.id)
        .collectAsState(initial = emptyList())

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // Cabeçalho
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        refeicao.nome,
                        color = PrimaryGreen,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        refeicao.horario,
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                IconButton(onClick = { expandido = !expandido }) {
                    Icon(
                        if (expandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                IconButton(onClick = onDeletar) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                }
            }

            // Macros totais
            val totalKcal = itens.sumOf { it.kcal.toDouble() }.toFloat()
            val totalProt = itens.sumOf { it.proteina.toDouble() }.toFloat()
            val totalCarb = itens.sumOf { it.carbo.toDouble() }.toFloat()
            val totalGord = itens.sumOf { it.gordura.toDouble() }.toFloat()

            if (itens.isNotEmpty()) {
                Text(
                    "%.0f kcal | P: %.1fg | C: %.1fg | G: %.1fg".format(
                        totalKcal, totalProt, totalCarb, totalGord
                    ),
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Lista de itens expandida
            AnimatedVisibility(visible = expandido) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = Color.DarkGray)
                    Spacer(Modifier.height(4.dp))

                    itens.forEach { item ->
                        ItemAlimentoRow(item = item, onDeletar = { viewModel.deletarItem(item) })
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { mostrarBusca = true },
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(PrimaryGreen)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryGreen)
                        Spacer(Modifier.width(4.dp))
                        Text("Adicionar alimento", color = PrimaryGreen)
                    }
                }
            }
        }
    }

    if (mostrarBusca) {
        BuscaAlimentoSheet(
            refeicaoId = refeicao.id,
            onAdicionarItem = { viewModel.inserirItem(it) },
            onDismiss = { mostrarBusca = false }
        )
    }
}

@Composable
fun ItemAlimentoRow(item: DietaItemEntity, onDeletar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${item.nomeAlimento} — ${item.quantidade.toInt()}${item.unidade}",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "%.0f kcal | P:%.1f C:%.1f G:%.1f".format(
                    item.kcal, item.proteina, item.carbo, item.gordura
                ),
                color = Color.Gray,
                style = MaterialTheme.typography.labelSmall
            )
        }
        IconButton(onClick = onDeletar) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
        }
    }
}
