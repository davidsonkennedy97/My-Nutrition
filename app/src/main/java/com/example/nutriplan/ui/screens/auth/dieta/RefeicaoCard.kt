package com.example.nutriplan.ui.screens.dieta

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
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
import com.example.nutriplan.ui.viewmodel.DietaViewModel
import com.example.nutriplan.ui.viewmodel.DietaRefeicao
import com.example.nutriplan.ui.viewmodel.DietaItem
import com.example.nutriplan.ui.theme.PrimaryGreen  // adicione este import se PrimaryGreen estiver em theme

@Composable
fun RefeicaoCard(
    refeicao: DietaRefeicao,
    viewModel: DietaViewModel,
    onDeletar: () -> Unit
) {
    var expandido by remember { mutableStateOf(false) }
    var mostrarBusca by remember { mutableStateOf(false) }

    val itens by remember { mutableStateOf(refeicao.itens) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(refeicao.nome, color = PrimaryGreen, style = MaterialTheme.typography.titleSmall)
                    Text(refeicao.horario, color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                }

                IconButton(onClick = { expandido = !expandido }) {
                    Icon(if (expandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = Color.White)
                }

                IconButton(onClick = onDeletar) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red)
                }
            }

            val totalKcal = itens.sumOf { it.calorias }.toFloat()

            if (itens.isNotEmpty()) {
                Text(
                    "%.0f kcal".format(totalKcal),
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            AnimatedVisibility(visible = expandido) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = Color.DarkGray)
                    Spacer(Modifier.height(4.dp))

                    itens.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${item.nome} — ${item.quantidade}", color = Color.White, style = MaterialTheme.typography.bodySmall)
                                Text("%.0f kcal".format(item.calorias), color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                            }
                            IconButton(onClick = { viewModel.deletarItem(refeicao.id, item.id) }) {
                                Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { mostrarBusca = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, null, tint = PrimaryGreen)
                        Spacer(Modifier.width(4.dp))
                        Text("Adicionar alimento", color = PrimaryGreen)
                    }
                }
            }
        }
    }

    if (mostrarBusca) {
        mostrarBusca = false  // temporário
    }
}