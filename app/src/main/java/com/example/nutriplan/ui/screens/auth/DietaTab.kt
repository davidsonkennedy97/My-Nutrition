package com.example.nutriplan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nutriplan.ui.dieta.DietaPlano
import com.example.nutriplan.ui.theme.PrimaryGreen
import com.example.nutriplan.ui.viewmodel.DietaViewModel

@Composable
fun DietaTab(
    pacienteId: String,
    isDarkTheme: Boolean,
    dietaViewModel: DietaViewModel,
    onNavigateToDietEditor: (String) -> Unit
) {
    val planosMap by dietaViewModel.planosPorPaciente.collectAsState()
    val planos = planosMap[pacienteId].orEmpty()

    val bg = if (isDarkTheme) Color(0xFF000000) else Color.White
    val textPrimary = if (isDarkTheme) Color.White else Color.Black
    val textSecondary = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { onNavigateToDietEditor(pacienteId) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryGreen,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "+ Criar dieta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (planos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Nenhuma dieta cadastrada",
                        style = MaterialTheme.typography.titleMedium,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Crie uma dieta e visualize refeições e totais (P/C/G).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(planos, key = { it.id }) { plano ->
                    DietaPlanoCard(
                        plano = plano,
                        onDelete = { dietaViewModel.deletarPlano(pacienteId, plano.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DietaPlanoCard(
    plano: DietaPlano,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PrimaryGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plano.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "P ${"%.1f".format(plano.total.proteina)}g • C ${"%.1f".format(plano.total.carboidrato)}g • G ${"%.1f".format(plano.total.gordura)}g",
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expandir",
                        tint = Color.White
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = Color.White
                    )
                }
            }

            if (expanded) {
                Divider(color = Color.White.copy(alpha = 0.25f))

                if (plano.refeicoes.isEmpty()) {
                    Text("Sem refeições.", color = Color.White.copy(alpha = 0.9f))
                } else {
                    plano.refeicoes.forEach { ref ->
                        Text(
                            text = ref.titulo,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        if (ref.itens.isEmpty()) {
                            Text("— sem itens", color = Color.White.copy(alpha = 0.85f))
                        } else {
                            ref.itens.forEach { item ->
                                Text(
                                    text = "• ${item.foodNome} — ${item.gramas}g (P ${"%.1f".format(item.macros.proteina)} / C ${"%.1f".format(item.macros.carboidrato)} / G ${"%.1f".format(item.macros.gordura)})",
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}