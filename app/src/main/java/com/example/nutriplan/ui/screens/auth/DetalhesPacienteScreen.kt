package com.example.nutriplan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutriplan.data.database.PacienteEntity
import com.example.nutriplan.ui.viewmodel.PacienteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalhesPacienteScreen(
    pacienteId: String,
    viewModel: PacienteViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    var paciente by remember { mutableStateOf<PacienteEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Carrega os dados do paciente
    LaunchedEffect(pacienteId) {
        paciente = viewModel.buscarPorId(pacienteId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Paciente") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(pacienteId) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Deletar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (paciente == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card de informações básicas
                InfoCard(title = "Informações Básicas") {
                    InfoRow(label = "Nome", value = paciente!!.nome)
                    if (paciente!!.email.isNotEmpty()) {
                        InfoRow(label = "E-mail", value = paciente!!.email)
                    }
                    if (paciente!!.telefone.isNotEmpty()) {
                        InfoRow(label = "Telefone", value = paciente!!.telefone)
                    }
                    if (paciente!!.objetivo.isNotEmpty()) {
                        InfoRow(label = "Objetivo", value = paciente!!.objetivo)
                    }
                    InfoRow(label = "Status", value = paciente!!.status)
                }

                // Card de dados antropométricos
                if (paciente!!.pesoAtual > 0 || paciente!!.altura > 0 || paciente!!.idade > 0) {
                    InfoCard(title = "Dados Antropométricos") {
                        if (paciente!!.pesoAtual > 0) {
                            InfoRow(label = "Peso Atual", value = "${paciente!!.pesoAtual} kg")
                        }
                        if (paciente!!.pesoMeta > 0) {
                            InfoRow(label = "Peso Meta", value = "${paciente!!.pesoMeta} kg")
                        }
                        if (paciente!!.altura > 0) {
                            InfoRow(label = "Altura", value = "${paciente!!.altura} cm")
                        }
                        if (paciente!!.idade > 0) {
                            InfoRow(label = "Idade", value = "${paciente!!.idade} anos")
                        }

                        // Calcula IMC se tiver peso e altura
                        if (paciente!!.pesoAtual > 0 && paciente!!.altura > 0) {
                            val alturaMetros = paciente!!.altura / 100
                            val imc = paciente!!.pesoAtual / (alturaMetros * alturaMetros)
                            InfoRow(
                                label = "IMC",
                                value = String.format("%.2f", imc),
                                highlight = true
                            )
                        }
                    }
                }

                // Card de anamnese
                if (paciente!!.historicoDoencas.isNotEmpty() ||
                    paciente!!.alergiasAlimentares.isNotEmpty() ||
                    paciente!!.medicamentos.isNotEmpty() ||
                    paciente!!.rotinaExercicios.isNotEmpty()
                ) {
                    InfoCard(title = "Anamnese") {
                        if (paciente!!.historicoDoencas.isNotEmpty()) {
                            InfoRow(
                                label = "Histórico de Doenças",
                                value = paciente!!.historicoDoencas,
                                multiline = true
                            )
                        }
                        if (paciente!!.alergiasAlimentares.isNotEmpty()) {
                            InfoRow(
                                label = "Alergias Alimentares",
                                value = paciente!!.alergiasAlimentares,
                                multiline = true
                            )
                        }
                        if (paciente!!.medicamentos.isNotEmpty()) {
                            InfoRow(
                                label = "Medicamentos",
                                value = paciente!!.medicamentos,
                                multiline = true
                            )
                        }
                        if (paciente!!.rotinaExercicios.isNotEmpty()) {
                            InfoRow(
                                label = "Rotina de Exercícios",
                                value = paciente!!.rotinaExercicios,
                                multiline = true
                            )
                        }
                    }
                }

                // Card de datas
                InfoCard(title = "Informações Adicionais") {
                    InfoRow(
                        label = "Data de Cadastro",
                        value = formatDate(paciente!!.dataCriacao)
                    )
                    if (paciente!!.ultimaConsulta > 0) {
                        InfoRow(
                            label = "Última Consulta",
                            value = formatDate(paciente!!.ultimaConsulta)
                        )
                    }
                    if (paciente!!.proximaConsulta > 0) {
                        InfoRow(
                            label = "Próxima Consulta",
                            value = formatDate(paciente!!.proximaConsulta)
                        )
                    }
                }
            }
        }
    }

    // Dialog de confirmação de exclusão
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Tem certeza que deseja excluir este paciente? Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        paciente?.let {
                            viewModel.deletarPaciente(it)
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun InfoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    multiline: Boolean = false,
    highlight: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = if (highlight) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyLarge
            },
            color = if (highlight) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal
        )
        if (!multiline) {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}