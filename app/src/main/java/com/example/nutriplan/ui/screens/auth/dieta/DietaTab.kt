package com.example.nutriplan.ui.screens.dieta

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutriplan.data.dieta.DietaPlanoEntity
import com.example.nutriplan.ui.theme.DarkBackground
import com.example.nutriplan.ui.theme.DarkSurface
import com.example.nutriplan.ui.theme.PrimaryGreen

@Composable
fun DietaTab(pacienteId: Int) {

    val viewModel: DietaViewModel = viewModel()

    LaunchedEffect(pacienteId) {
        viewModel.setPacienteId(pacienteId)
    }

    val planos by viewModel.planos.collectAsState()

    var planoSelecionado by remember { mutableStateOf<DietaPlanoEntity?>(null) }
    var mostrarDialogoPlano by remember { mutableStateOf(false) }
    var nomePlano by remember { mutableStateOf("") }

    // Se o plano selecionado foi deletado, limpa a seleção
    LaunchedEffect(planos) {
        if (planoSelecionado != null && planos.none { it.id == planoSelecionado!!.id }) {
            planoSelecionado = null
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // ---- CABEÇALHO PLANOS ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Planos de Dieta",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                IconButton(onClick = { mostrarDialogoPlano = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Novo plano", tint = PrimaryGreen)
                }
            }

            Spacer(Modifier.height(8.dp))

            // ---- LISTA DE PLANOS ----
            if (planos.isEmpty()) {
                Text(
                    "Nenhum plano criado. Toque em + para começar.",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                planos.forEach { plano ->
                    val selecionado = planoSelecionado?.id == plano.id
                    Card(
                        onClick = {
                            planoSelecionado = if (selecionado) null else plano
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selecionado) PrimaryGreen else DarkSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                plano.nome,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.deletarPlano(plano) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = if (selecionado) Color.White else Color.Red
                                )
                            }
                        }
                    }
                }
            }

            // ---- REFEIÇÕES DO PLANO SELECIONADO ----
            planoSelecionado?.let { plano ->
                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = Color.DarkGray)
                Spacer(Modifier.height(12.dp))

                Text(
                    "Plano: ${plano.nome}",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryGreen
                )

                Spacer(Modifier.height(8.dp))

                RefeicaoSection(
                    planoId = plano.id,
                    viewModel = viewModel
                )
            }
        }
    }

    // ---- DIÁLOGO NOVO PLANO ----
    if (mostrarDialogoPlano) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPlano = false },
            containerColor = Color(0xFF1A1A1A),
            title = { Text("Novo Plano", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = nomePlano,
                    onValueChange = { nomePlano = it },
                    label = { Text("Nome do plano", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = PrimaryGreen
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (nomePlano.isNotBlank()) {
                        viewModel.criarPlano(
                            pacienteId = pacienteId,
                            nome = nomePlano.trim()
                        ) {}
                        nomePlano = ""
                        mostrarDialogoPlano = false
                    }
                }) {
                    Text("Criar", color = PrimaryGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoPlano = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }
}
