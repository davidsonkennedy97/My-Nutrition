package com.example.nutriplan.ui.screens.dieta

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nutriplan.ui.theme.PrimaryGreen
import com.example.nutriplan.ui.viewmodel.DietaViewModel

@Composable
fun DietaTab(
    pacienteId: String,
    isDarkTheme: Boolean,
    dietaViewModel: DietaViewModel,
    onNavigateToDietaEditor: (String) -> Unit
) {
    val rotinas by dietaViewModel.getRotinas(pacienteId).collectAsState(initial = emptyList())
    val textColor = if (isDarkTheme) Color.White else Color.Black

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToDietaEditor(pacienteId) },
                containerColor = PrimaryGreen,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = if (rotinas.isEmpty()) Icons.Default.Add else Icons.Default.Restaurant,
                    contentDescription = if (rotinas.isEmpty()) "Criar Plano" else "Editar Plano"
                )
            }
        }
    ) { paddingValues ->
        if (rotinas.isEmpty()) {
            // Estado vazio - nenhum plano salvo
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = if (isDarkTheme) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "Nenhum plano alimentar cadastrado",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor
                    )
                    Text(
                        text = "Clique no botão + para criar um plano",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            // Lista de rotinas (plano alimentar salvo)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Plano Alimentar",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(rotinas, key = { it.id }) { rotina ->
                    RotinaCardSimples(
                        rotina = rotina,
                        isDarkTheme = isDarkTheme,
                        dietaViewModel = dietaViewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun RotinaCardSimples(
    rotina: com.example.nutriplan.data.dieta.RotinaEntity,
    isDarkTheme: Boolean,
    dietaViewModel: DietaViewModel
) {
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val alimentos by dietaViewModel
        .getAlimentosDaRotina(rotina.id)
        .collectAsState(initial = emptyList())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White  // ← MUDANÇA AQUI: Card sempre branco
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Nome e horário da rotina
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rotina.nome,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                    Text(
                        text = "Horário: ${rotina.horario}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black.copy(alpha = 0.7f)  // ← Texto sempre escuro no card branco
                    )
                }

                if (alimentos.isNotEmpty()) {
                    Surface(
                        color = PrimaryGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${alimentos.size} alimento(s)",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Lista de alimentos
            if (alimentos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = PrimaryGreen.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                alimentos.forEach { alimento ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "• ${alimento.nomeExibicao}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black,  // ← Texto sempre preto no card branco
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${String.format("%.1f", alimento.quantidade)} ${alimento.unidade}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black.copy(alpha = 0.6f)  // ← Texto sempre escuro no card branco
                        )
                    }
                }
            }

            // Observação
            if (rotina.observacao.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = PrimaryGreen.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Observação:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = rotina.observacao,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.8f)  // ← Texto sempre escuro no card branco
                )
            }
        }
    }
}