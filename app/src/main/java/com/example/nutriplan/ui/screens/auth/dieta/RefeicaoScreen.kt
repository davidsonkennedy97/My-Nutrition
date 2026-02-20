package com.example.nutriplan.ui.screens.dieta

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutriplan.data.dieta.DietaItemEntity
import com.example.nutriplan.ui.theme.PrimaryGreen
import com.example.nutriplan.ui.viewmodel.DietaViewModel

@Composable
fun RefeicaoScreen(
    refeicaoId: String,
    nomeRefeicao: String,
    isDarkTheme: Boolean,
    dietaViewModel: DietaViewModel,
    onNavigateBack: () -> Unit,
    onBuscarAlimento: (refeicaoId: String) -> Unit
) {
    val bg = if (isDarkTheme) Color(0xFF000000) else Color(0xFFF8F8F8)
    val textPrimary = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
    val textSub = if (isDarkTheme) Color.White.copy(0.5f) else Color(0xFF888888)
    val cardBg = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White

    val itens by dietaViewModel.listarItens(refeicaoId)
        .collectAsState(initial = emptyList())

    // ── Totais calculados automaticamente ────────────────────
    val totalProteina = itens.sumOf { it.proteina.toDouble() }.toFloat()
    val totalLipidios = itens.sumOf { it.lipidios.toDouble() }.toFloat()
    val totalCarbo = itens.sumOf { it.carboidrato.toDouble() }.toFloat()
    val totalCalorias = itens.sumOf { it.calorias.toDouble() }.toFloat()

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                color = PrimaryGreen
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                    Text(
                        nomeRefeicao,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Botão adicionar alimento ──────────────────────
            item {
                Button(
                    onClick = { onBuscarAlimento(refeicaoId) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Adicionar alimento", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            // ── Lista de alimentos ────────────────────────────
            if (itens.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.SetMeal,
                                null,
                                tint = PrimaryGreen.copy(alpha = 0.4f),
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                "Nenhum alimento adicionado",
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Text(
                                "Toque em \"Adicionar alimento\" para buscar.",
                                color = textSub,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                items(itens, key = { it.id }) { item ->
                    AlimentoCard(
                        item = item,
                        isDarkTheme = isDarkTheme,
                        onDelete = { dietaViewModel.deletarItem(item) }
                    )
                }

                // ── Card resumo da refeição ───────────────────
                item {
                    Spacer(Modifier.height(4.dp))
                    ResumoRefeicaoCard(
                        totalProteina = totalProteina,
                        totalLipidios = totalLipidios,
                        totalCarbo = totalCarbo,
                        totalCalorias = totalCalorias,
                        cardBg = cardBg,
                        textPrimary = textPrimary
                    )
                }
            }
        }
    }
}

// ── Card de cada alimento ─────────────────────────────────────
@Composable
private fun AlimentoCard(
    item: DietaItemEntity,
    isDarkTheme: Boolean,
    onDelete: () -> Unit
) {
    val cardBg = if (isDarkTheme) Color(0xFF1A1A1A) else Color.White
    val textPrimary = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
    val textSub = if (isDarkTheme) Color.White.copy(0.5f) else Color(0xFF888888)
    var showConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Nome + origem + deletar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.alimentoNome,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        fontSize = 15.sp
                    )
                    Text(
                        "Origem: ${item.origem}  •  ${item.quantidade}${item.unidade}",
                        fontSize = 12.sp,
                        color = textSub
                    )
                }
                IconButton(onClick = { showConfirm = true }) {
                    Icon(Icons.Default.Delete, null, tint = Color(0xFFCC0000))
                }
            }

            HorizontalDivider(color = if (isDarkTheme) Color.White.copy(0.08f) else Color(0xFFEEEEEE))

            // Macros em linha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroInfo("Prot.", item.proteina, Color(0xFFE53935))
                MacroInfo("Lip.", item.lipidios, Color(0xFFFFA000))
                MacroInfo("Carbo", item.carboidrato, Color(0xFF1E88E5))
                MacroInfo("Kcal", item.calorias, PrimaryGreen)
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Remover alimento?") },
            text = { Text("\"${item.alimentoNome}\" será removido desta refeição.") },
            confirmButton = {
                TextButton(onClick = { showConfirm = false; onDelete() }) {
                    Text("Remover", color = Color(0xFFCC0000), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Cancelar") }
            }
        )
    }
}

// ── Macro individual ──────────────────────────────────────────
@Composable
private fun MacroInfo(label: String, valor: Float, cor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = cor, fontWeight = FontWeight.SemiBold)
        Text("%.1fg".format(valor), fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

// ── Resumo total da refeição ──────────────────────────────────
@Composable
private fun ResumoRefeicaoCard(
    totalProteina: Float,
    totalLipidios: Float,
    totalCarbo: Float,
    totalCalorias: Float,
    cardBg: Color,
    textPrimary: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Análise da refeição",
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                fontSize = 15.sp
            )

            // Totais
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroInfo("Proteínas", totalProteina, Color(0xFFE53935))
                MacroInfo("Lipídios", totalLipidios, Color(0xFFFFA000))
                MacroInfo("Carboidratos", totalCarbo, Color(0xFF1E88E5))
                MacroInfo("Calorias", totalCalorias, PrimaryGreen)
            }

            HorizontalDivider()

            // Gráfico de rosca simples
            GraficoRosca(
                proteina = totalProteina,
                lipidios = totalLipidios,
                carbo = totalCarbo
            )
        }
    }
}
