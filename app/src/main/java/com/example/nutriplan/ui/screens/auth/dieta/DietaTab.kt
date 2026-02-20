package com.example.nutriplan.ui.screens.dieta

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nutriplan.ui.theme.PrimaryGreen
import com.example.nutriplan.ui.viewmodel.DietaViewModel

@Composable
fun DietaTab(
    pacienteId: String,
    isDarkTheme: Boolean = false,
    dietaViewModel: DietaViewModel,
    onNavigateToDietEditor: (String) -> Unit = {}
) {
    val planos by dietaViewModel.planos.collectAsState()
    val refeicoes by dietaViewModel.refeicoes.collectAsState()
    val backgroundColor = if (isDarkTheme) Color(0xFF121212) else Color(0xFFF5F5F5)
    val surfaceColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black

    var showCreatePlanoDialog by remember { mutableStateOf(false) }
    var selectedPlanoId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Text(
            text = "Planos de Dieta",
            style = MaterialTheme.typography.headlineSmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { showCreatePlanoDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Text("Criar Novo Plano", color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(planos.filter { it.pacienteId == pacienteId }) { plano ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            selectedPlanoId = if (selectedPlanoId == plano.id) null else plano.id
                        },
                    colors = CardDefaults.cardColors(containerColor = surfaceColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(plano.nome, color = textColor, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { dietaViewModel.deletarPlano(plano.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Deletar Plano", tint = Color.Red)
                        }
                    }
                }
                if (selectedPlanoId == plano.id) {
                    RefeicaoSection(
                        planoId = plano.id,
                        refeicoes = refeicoes.filter { it.planoId == plano.id },
                        dietaViewModel = dietaViewModel,
                        isDarkTheme = isDarkTheme,
                        textColor = textColor,
                        surfaceColor = surfaceColor
                    )
                }
            }
        }
    }

    if (showCreatePlanoDialog) {
        var nomePlano by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreatePlanoDialog = false },
            title = { Text("Criar Novo Plano", color = textColor) },
            text = {
                OutlinedTextField(
                    value = nomePlano,
                    onValueChange = { nomePlano = it },
                    label = { Text("Nome do Plano", color = textColor) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = textColor
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nomePlano.isNotBlank()) {
                            dietaViewModel.criarPlano(pacienteId, nomePlano)
                            nomePlano = ""
                            showCreatePlanoDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Criar", color = Color.White)
                }
            },
            dismissButton = {
                Button(onClick = { showCreatePlanoDialog = false }) {
                    Text("Cancelar", color = Color.Black)
                }
            },
            containerColor = surfaceColor
        )
    }
}

@Composable
fun RefeicaoSection(
    planoId: String,
    refeicoes: List<com.example.nutriplan.ui.viewmodel.DietaRefeicao>,
    dietaViewModel: DietaViewModel,
    isDarkTheme: Boolean,
    textColor: Color,
    surfaceColor: Color
) {
    var showCreateRefeicaoDialog by remember { mutableStateOf(false) }
    var selectedRefeicaoId by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp)) {
        Button(
            onClick = { showCreateRefeicaoDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Text("Adicionar Refeição", color = Color.White)
        }
        Spacer(modifier = Modifier.height(8.dp))
        refeicoes.forEach { refeicao ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        selectedRefeicaoId = if (selectedRefeicaoId == refeicao.id) null else refeicao.id
                    },
                colors = CardDefaults.cardColors(containerColor = surfaceColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${refeicao.nome} - ${refeicao.horario}",
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { dietaViewModel.deletarRefeicao(refeicao.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Deletar Refeição", tint = Color.Red)
                        }
                    }
                    Text(
                        "Total Calorias: ${"%.1f".format(refeicao.itens.sumOf { it.calorias })} kcal",
                        color = textColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (selectedRefeicaoId == refeicao.id) {
                        Spacer(modifier = Modifier.height(8.dp))
                        refeicao.itens.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${item.nome} - ${item.quantidade} - ${"%.1f".format(item.calorias)} kcal",
                                    color = textColor,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { dietaViewModel.deletarItem(refeicao.id, item.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Deletar Item", tint = Color.Red)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        var showAddItemDialog by remember { mutableStateOf(false) }
                        Button(
                            onClick = { showAddItemDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Text("Adicionar Item", color = Color.White)
                        }
                        if (showAddItemDialog) {
                            var nomeItem by remember { mutableStateOf("") }
                            var quantidadeItem by remember { mutableStateOf("") }
                            var caloriasItem by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showAddItemDialog = false },
                                title = { Text("Adicionar Item", color = textColor) },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = nomeItem,
                                            onValueChange = { nomeItem = it },
                                            label = { Text("Nome do alimento", color = textColor) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = textColor,
                                                unfocusedTextColor = textColor,
                                                focusedBorderColor = PrimaryGreen,
                                                unfocusedBorderColor = textColor
                                            )
                                        )
                                        OutlinedTextField(
                                            value = quantidadeItem,
                                            onValueChange = { quantidadeItem = it },
                                            label = { Text("Quantidade (ex: 100g)", color = textColor) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = textColor,
                                                unfocusedTextColor = textColor,
                                                focusedBorderColor = PrimaryGreen,
                                                unfocusedBorderColor = textColor
                                            )
                                        )
                                        OutlinedTextField(
                                            value = caloriasItem,
                                            onValueChange = { caloriasItem = it },
                                            label = { Text("Calorias (kcal)", color = textColor) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = textColor,
                                                unfocusedTextColor = textColor,
                                                focusedBorderColor = PrimaryGreen,
                                                unfocusedBorderColor = textColor
                                            )
                                        )
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            val cal = caloriasItem.toDoubleOrNull() ?: 0.0
                                            if (nomeItem.isNotBlank() && quantidadeItem.isNotBlank()) {
                                                dietaViewModel.adicionarItem(refeicao.id, nomeItem, quantidadeItem, cal)
                                                nomeItem = ""
                                                quantidadeItem = ""
                                                caloriasItem = ""
                                                showAddItemDialog = false
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                                    ) {
                                        Text("Adicionar", color = Color.White)
                                    }
                                },
                                dismissButton = {
                                    Button(onClick = { showAddItemDialog = false }) {
                                        Text("Cancelar", color = Color.Black)
                                    }
                                },
                                containerColor = surfaceColor
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateRefeicaoDialog) {
        var nomeRefeicao by remember { mutableStateOf("") }
        var horarioRefeicao by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateRefeicaoDialog = false },
            title = { Text("Criar Refeição", color = textColor) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nomeRefeicao,
                        onValueChange = { nomeRefeicao = it },
                        label = { Text("Nome da Refeição", color = textColor) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = textColor
                        )
                    )
                    OutlinedTextField(
                        value = horarioRefeicao,
                        onValueChange = { horarioRefeicao = it },
                        label = { Text("Horário (ex: 08:00)", color = textColor) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = textColor
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nomeRefeicao.isNotBlank() && horarioRefeicao.isNotBlank()) {
                            dietaViewModel.criarRefeicao(planoId, nomeRefeicao, horarioRefeicao)
                            nomeRefeicao = ""
                            horarioRefeicao = ""
                            showCreateRefeicaoDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Criar", color = Color.White)
                }
            },
            dismissButton = {
                Button(onClick = { showCreateRefeicaoDialog = false }) {
                    Text("Cancelar", color = Color.Black)
                }
            },
            containerColor = surfaceColor
        )
    }
}