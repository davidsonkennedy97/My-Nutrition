package com.example.nutriplan.ui.screens.dieta

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.nutriplan.data.dieta.RotinaEntity
import com.example.nutriplan.ui.theme.PrimaryGreen
import com.example.nutriplan.ui.viewmodel.DietaViewModel

@Composable
fun DietaTab(
    pacienteId: String,
    isDarkTheme: Boolean,
    dietaViewModel: DietaViewModel,
    onNavigateToDietaEditor: (String) -> Unit
) {
    val todasRotinas by dietaViewModel
        .getRotinas(pacienteId)
        .collectAsState(initial = emptyList())

    var nomesPlanos by remember { mutableStateOf(listOf("Plano 1")) }
    var planoSelecionadoIdx by remember { mutableIntStateOf(0) }
    var mostrarDialogRenomear by remember { mutableStateOf(false) }
    var nomeTemp by remember { mutableStateOf("") }
    var rotinaParaExcluir by remember { mutableStateOf<RotinaEntity?>(null) }
    var rotinaParaEditar by remember { mutableStateOf<RotinaEntity?>(null) }

    val totalPlanos = nomesPlanos.size
    val rotinasPorPlano = if (totalPlanos == 0 || todasRotinas.isEmpty()) {
        emptyList()
    } else {
        todasRotinas.chunked(maxOf(1, (todasRotinas.size + totalPlanos - 1) / totalPlanos))
    }
    val rotinasDoPLanoAtual: List<RotinaEntity> =
        rotinasPorPlano.getOrNull(planoSelecionadoIdx) ?: emptyList()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToDietaEditor(pacienteId) },
                containerColor = PrimaryGreen,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Criar Novo Plano"
                )
            }
        }
    ) { paddingValues ->

        if (todasRotinas.isEmpty()) {
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
                        tint = if (isDarkTheme) Color.White.copy(alpha = 0.3f)
                        else Color.Black.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "Nenhum plano alimentar cadastrado",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isDarkTheme) Color.White else Color.Black
                    )
                    Text(
                        text = "Clique no botão + para criar um plano",
                        style = MaterialTheme.typography.bodyMedium,
                        color = (if (isDarkTheme) Color.White else Color.Black).copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Plano Alimentar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (nomesPlanos.size > 1) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(nomesPlanos.indices.toList()) { idx ->
                            val selecionado = idx == planoSelecionadoIdx
                            FilterChip(
                                selected = selecionado,
                                onClick = { planoSelecionadoIdx = idx },
                                label = {
                                    Text(
                                        text = nomesPlanos[idx],
                                        fontWeight = if (selecionado) FontWeight.Bold
                                        else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryGreen,
                                    selectedLabelColor = Color.White,
                                    containerColor = PrimaryGreen.copy(alpha = 0.1f),
                                    labelColor = PrimaryGreen
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selecionado,
                                    borderColor = PrimaryGreen.copy(alpha = 0.4f),
                                    selectedBorderColor = PrimaryGreen
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                nomeTemp = nomesPlanos[planoSelecionadoIdx]
                                mostrarDialogRenomear = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Renomear plano",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Renomear plano",
                                color = PrimaryGreen,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(rotinasDoPLanoAtual, key = { it.id }) { rotina ->
                        RotinaCardAccordion(
                            rotina = rotina,
                            isDarkTheme = isDarkTheme,
                            dietaViewModel = dietaViewModel,
                            onEditar = { rotinaParaEditar = rotina },
                            onExcluir = { rotinaParaExcluir = rotina }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (mostrarDialogRenomear) {
        AlertDialog(
            onDismissRequest = { mostrarDialogRenomear = false },
            title = {
                Text(
                    text = "Renomear Plano",
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color.Black
                )
            },
            text = {
                OutlinedTextField(
                    value = nomeTemp,
                    onValueChange = { nomeTemp = it },
                    label = { Text("Nome do plano") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = PrimaryGreen.copy(alpha = 0.5f),
                        focusedLabelColor = PrimaryGreen
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (nomeTemp.isNotBlank()) {
                            nomesPlanos = nomesPlanos.toMutableList().also {
                                it[planoSelecionadoIdx] = nomeTemp.trim()
                            }
                        }
                        mostrarDialogRenomear = false
                    }
                ) {
                    Text("Salvar", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogRenomear = false }) {
                    Text("Cancelar", color = PrimaryGreen)
                }
            }
        )
    }

    rotinaParaExcluir?.let { rotina ->
        AlertDialog(
            onDismissRequest = { rotinaParaExcluir = null },
            title = {
                Text(
                    text = "Excluir refeição?",
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color.Black
                )
            },
            text = {
                Text(
                    text = "\"${rotina.nome}\" será removida permanentemente.",
                    color = if (isDarkTheme) Color.White else Color.Black
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        dietaViewModel.deleteRotina(rotina)
                        rotinaParaExcluir = null
                    }
                ) {
                    Text("Excluir", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { rotinaParaExcluir = null }) {
                    Text("Cancelar", color = PrimaryGreen)
                }
            }
        )
    }

    rotinaParaEditar?.let { rotina ->
        EditarRotinaDialog(
            rotina = rotina,
            isDarkTheme = isDarkTheme,
            onDismiss = { rotinaParaEditar = null },
            onConfirm = { novoNome, novoHorario ->
                dietaViewModel.updateRotina(rotina.copy(nome = novoNome, horario = novoHorario))
                rotinaParaEditar = null
            }
        )
    }
}
@Composable
private fun RotinaCardAccordion(
    rotina: RotinaEntity,
    isDarkTheme: Boolean,
    dietaViewModel: DietaViewModel,
    onEditar: () -> Unit,
    onExcluir: () -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    val alimentos by dietaViewModel
        .getAlimentosDaRotina(rotina.id)
        .collectAsState(initial = emptyList())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandido = !expandido },
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
                        color = Color.Black.copy(alpha = 0.7f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
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
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Icon(
                        imageVector = if (expandido) Icons.Default.ExpandLess
                        else Icons.Default.ExpandMore,
                        contentDescription = if (expandido) "Recolher" else "Expandir",
                        tint = PrimaryGreen
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEditar,
                    border = BorderStroke(1.dp, PrimaryGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Editar",
                        color = PrimaryGreen,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onExcluir,
                    border = BorderStroke(1.dp, Color.Red),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = Color.Red,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Excluir",
                        color = Color.Red,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedVisibility(
                visible = expandido,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = PrimaryGreen.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    if (alimentos.isNotEmpty()) {
                        alimentos.forEach { alimento ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• ${alimento.nomeExibicao}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Black,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${String.format("%.1f", alimento.quantidade)} ${alimento.unidade}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Black.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Nenhum alimento cadastrado nesta refeição.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black.copy(alpha = 0.5f)
                        )
                    }

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
                            color = Color.Black.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditarRotinaDialog(
    rotina: RotinaEntity,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (nome: String, horario: String) -> Unit
) {
    var nome by remember { mutableStateOf(rotina.nome) }
    var horario by remember { mutableStateOf(rotina.horario) }

    val textColor = if (isDarkTheme) Color.White else Color.Black
    val bgColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = bgColor,
            border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.4f)),
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Editar Refeição",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome da refeição", color = textColor.copy(alpha = 0.7f)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = PrimaryGreen.copy(alpha = 0.5f),
                        focusedLabelColor = PrimaryGreen,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = horario,
                    onValueChange = { horario = it },
                    label = { Text("Horário (ex: 08:00)", color = textColor.copy(alpha = 0.7f)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = PrimaryGreen.copy(alpha = 0.5f),
                        focusedLabelColor = PrimaryGreen,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, PrimaryGreen)
                    ) {
                        Text("Cancelar", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val n = nome.trim()
                            val h = horario.trim()
                            if (n.isNotEmpty() && h.isNotEmpty()) {
                                onConfirm(n, h)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Salvar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
