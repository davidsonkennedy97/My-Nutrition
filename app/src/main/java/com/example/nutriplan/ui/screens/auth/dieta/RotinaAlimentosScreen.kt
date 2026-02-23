package com.example.nutriplan.ui.screens.dieta

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nutriplan.data.dieta.AlimentoEntity
import com.example.nutriplan.ui.theme.PrimaryGreen
import com.example.nutriplan.ui.viewmodel.DietaViewModel
import java.util.Locale
import kotlin.math.abs

private val ORIGENS = listOf("Todas", "IBGE", "Taco", "tucunduva")

@Composable
fun RotinaAlimentosScreen(
    rotinaId: Long,
    rotinaNome: String,
    isDarkTheme: Boolean,
    dietaViewModel: DietaViewModel,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val titulo    = rotinaNome.trim().ifEmpty { "Rotina" }
    val textColor = if (isDarkTheme) Color.White else Color.Black

    var query             by remember { mutableStateOf("") }
    var origemSelecionada by remember { mutableStateOf("Todas") }

    val selecionados = remember { mutableStateOf(listOf<AlimentoEntity>()) }

    val importReady by dietaViewModel.importReady.collectAsState()

    val resultados by produceState(
        initialValue = emptyList<AlimentoEntity>(),
        key1 = query,
        key2 = importReady,
        key3 = origemSelecionada
    ) {
        if (!importReady || query.trim().length < 2) {
            value = emptyList()
        } else {
            dietaViewModel.searchAlimentos(query).collect { lista ->
                value = if (origemSelecionada == "Todas") lista
                else lista.filter { it.origem == origemSelecionada }
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(color = PrimaryGreen, contentColor = Color.White) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.CenterStart).size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = titulo,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 56.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )

                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BadgedBox(
                            badge = {
                                if (selecionados.value.isNotEmpty()) {
                                    Badge(containerColor = PrimaryGreen) {
                                        Text(
                                            text = "${selecionados.value.size}",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            TextButton(onClick = {
                                selecionados.value.forEach { alimento ->
                                    dietaViewModel.addAlimentoNaRotina(rotinaId, alimento)
                                }
                                onSave()
                            }) {
                                Text("Salvar", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (!importReady) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    CircularProgressIndicator(color = PrimaryGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Carregando tabelas de alimentos...",
                        color = textColor.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Buscar alimento (mín. 2 letras)",
                            color = textColor.copy(alpha = 0.6f)
                        )
                    },
                    singleLine = true,
                    textStyle = TextStyle(color = textColor)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ORIGENS.forEach { origem ->
                        val chipSelecionado = origem == origemSelecionada
                        FilterChip(
                            selected = chipSelecionado,
                            onClick = { origemSelecionada = origem },
                            label = {
                                Text(
                                    text = when (origem) {
                                        "IBGE"      -> "IBGE"
                                        "Taco"      -> "TACO"
                                        "tucunduva" -> "Tucunduva"
                                        else        -> "Todas"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (chipSelecionado) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryGreen,
                                selectedLabelColor = Color.White,
                                containerColor = if (isDarkTheme) Color.DarkGray else Color.LightGray,
                                labelColor = textColor
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = chipSelecionado,
                                selectedBorderColor = PrimaryGreen,
                                borderColor = Color.Gray
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    if (selecionados.value.isNotEmpty()) {
                        items(selecionados.value, key = { "sel_${it.id}" }) { item ->
                            AlimentoCard(
                                alimento = item,
                                isDarkTheme = isDarkTheme,
                                selecionado = true,
                                onClick = {
                                    selecionados.value = selecionados.value.filter { it.id != item.id }
                                }
                            )
                        }

                        item {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = Color.Gray.copy(alpha = 0.3f)
                            )
                        }
                    }

                    if (query.trim().length >= 2) {
                        val naoSelecionados = resultados.filter { r ->
                            selecionados.value.none { it.id == r.id }
                        }

                        if (naoSelecionados.isEmpty()) {
                            item {
                                Text(
                                    text = if (resultados.isEmpty()) "Nenhum alimento encontrado."
                                    else "Todos os resultados já selecionados.",
                                    color = textColor.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        } else {
                            items(naoSelecionados, key = { "res_${it.id}" }) { item ->
                                AlimentoCard(
                                    alimento = item,
                                    isDarkTheme = isDarkTheme,
                                    selecionado = false,
                                    onClick = {
                                        selecionados.value = selecionados.value + item
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlimentoCard(
    alimento: AlimentoEntity,
    isDarkTheme: Boolean,
    selecionado: Boolean,
    onClick: () -> Unit
) {
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val subColor  = textColor.copy(alpha = 0.75f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selecionado) 1.dp else 0.5.dp,
            color = if (selecionado) PrimaryGreen else Color.Gray.copy(alpha = 0.2f)
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) { Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alimento.alimento,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${alimento.origem}  •  ${formatQtd(alimento.quantidadeBase)}${alimento.unidadeBase}",
                    color = subColor,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "P: ${format1(alimento.proteina)}g  G: ${format1(alimento.lipidios)}g  C: ${format1(alimento.carboidratos)}g  •  ${format1(alimento.calorias)} kcal",
                    color = if (selecionado) PrimaryGreen else textColor,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (selecionado) FontWeight.SemiBold else FontWeight.Normal
                )
            }

            Icon(
                imageVector = if (selecionado) Icons.Default.Close else Icons.Default.Check,
                contentDescription = if (selecionado) "Remover" else "Selecionar",
                tint = if (selecionado) PrimaryGreen else Color.Transparent,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun format1(v: Double): String =
    String.format(Locale.US, "%.1f", v).replace(".", ",")

private fun formatQtd(v: Double): String =
    if (abs(v - v.toInt()) <= 0.00001) v.toInt().toString() else format1(v)
