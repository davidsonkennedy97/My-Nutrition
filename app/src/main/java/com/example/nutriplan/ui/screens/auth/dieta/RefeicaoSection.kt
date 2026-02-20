package com.example.nutriplan.ui.screens.dieta

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.nutriplan.ui.theme.PrimaryGreen
import com.example.nutriplan.ui.viewmodel.DietaViewModel
import com.example.nutriplan.ui.viewmodel.DietaRefeicao

@Composable
fun RefeicaoSection(
    planoId: String,
    viewModel: DietaViewModel
) {
    val refeicoes by viewModel.refeicoes

    var mostrarDialogo by remember { mutableStateOf(false) }
    var nomeRefeicao by remember { mutableStateOf("") }
    var horarioRefeicao by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Refeições", style = MaterialTheme.typography.titleMedium, color = Color.White)
            IconButton(onClick = { mostrarDialogo = true }) {
                Icon(Icons.Default.Add, null, tint = PrimaryGreen)
            }
        }

        Spacer(Modifier.height(8.dp))

        if (refeicoes.isEmpty()) {
            Text("Nenhuma refeição. Toque em + para adicionar.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        } else {
            refeicoes.filter { it.planoId == planoId }.forEach { refeicao ->
                RefeicaoCard(
                    refeicao = refeicao,
                    viewModel = viewModel,
                    onDeletar = { viewModel.deletarRefeicao(refeicao.id) }
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
                        label = { Text("Nome", color = Color.LightGray) },
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
                        label = { Text("Horário", color = Color.LightGray) },
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
                        viewModel.criarRefeicao(planoId, nomeRefeicao.trim(), horarioRefeicao.trim())
                        nomeRefeicao = ""
                        horarioRefeicao = ""
                        mostrarDialogo = false
                    }
                }) { Text("Criar", color = PrimaryGreen) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar", color = Color.Gray) }
            }
        )
    }
}