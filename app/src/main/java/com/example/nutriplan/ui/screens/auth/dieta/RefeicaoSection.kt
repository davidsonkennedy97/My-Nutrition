package com.example.nutriplan.ui.screens.dieta

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.nutriplan.data.dieta.DietaRefeicaoEntity
import com.example.nutriplan.ui.theme.DarkBackground
import com.example.nutriplan.ui.theme.PrimaryGreen

@Composable
fun RefeicaoSection(
    planoId: Int,
    viewModel: DietaViewModel
) {
    val refeicoes by viewModel.getRefeicoes(planoId)
        .collectAsState(initial = emptyList())

    var mostrarDialogo by remember { mutableStateOf(false) }
    var nomeRefeicao by remember { mutableStateOf("") }
    var horarioRefeicao by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Refeições",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            IconButton(onClick = { mostrarDialogo = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar refeição", tint = PrimaryGreen)
            }
        }

        Spacer(Modifier.height(8.dp))

        if (refeicoes.isEmpty()) {
            Text(
                "Nenhuma refeição. Toque em + para adicionar.",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            refeicoes.forEach { refeicao ->
                RefeicaoCard(
                    refeicao = refeicao,
                    viewModel = viewModel,
                    onDeletar = { viewModel.deletarRefeicao(refeicao) }
                )
            }
        }
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            containerColor = Color(0xFF1A1A1A),
            title = { Text("Nova Refeição", color = Color.White) },
            text = {
                Column {
                    OutlinedTextField(
                        value = nomeRefeicao,
                        onValueChange = { nomeRefeicao = it },
                        label = { Text("Nome (ex: Café da manhã)", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = PrimaryGreen
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = horarioRefeicao,
                        onValueChange = { horarioRefeicao = it },
                        label = { Text("Horário (ex: 07:00)", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = PrimaryGreen
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (nomeRefeicao.isNotBlank()) {
                        viewModel.criarRefeicao(
                            planoId = planoId,
                            nome = nomeRefeicao.trim(),
                            horario = horarioRefeicao.trim()
                        ) {}
                        nomeRefeicao = ""
                        horarioRefeicao = ""
                        mostrarDialogo = false
                    }
                }) {
                    Text("Criar", color = PrimaryGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }
}
