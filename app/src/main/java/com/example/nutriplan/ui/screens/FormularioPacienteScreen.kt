package com.example.nutriplan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutriplan.data.database.PacienteEntity
import com.example.nutriplan.ui.viewmodel.PacienteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioPacienteScreen(
    pacienteId: String? = null,
    viewModel: PacienteViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var objetivo by remember { mutableStateOf("") }
    var pesoAtual by remember { mutableStateOf("") }
    var pesoMeta by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var idade by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Ativo") }
    var historicoDoencas by remember { mutableStateOf("") }
    var alergiasAlimentares by remember { mutableStateOf("") }
    var medicamentos by remember { mutableStateOf("") }
    var rotinaExercicios by remember { mutableStateOf("") }

    var expandedStatus by remember { mutableStateOf(false) }
    val statusOpcoes = listOf("Ativo", "Inativo", "Em tratamento")

    // Carrega os dados do paciente se estiver editando
    LaunchedEffect(pacienteId) {
        pacienteId?.let { id ->
            viewModel.buscarPorId(id)?.let { paciente ->
                nome = paciente.nome
                email = paciente.email
                telefone = paciente.telefone
                objetivo = paciente.objetivo
                pesoAtual = if (paciente.pesoAtual > 0) paciente.pesoAtual.toString() else ""
                pesoMeta = if (paciente.pesoMeta > 0) paciente.pesoMeta.toString() else ""
                altura = if (paciente.altura > 0) paciente.altura.toString() else ""
                idade = if (paciente.idade > 0) paciente.idade.toString() else ""
                status = paciente.status
                historicoDoencas = paciente.historicoDoencas
                alergiasAlimentares = paciente.alergiasAlimentares
                medicamentos = paciente.medicamentos
                rotinaExercicios = paciente.rotinaExercicios
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (pacienteId == null) "Novo Paciente" else "Editar Paciente") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Seção: Dados Básicos
            Text(
                text = "Dados Básicos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome Completo *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            OutlinedTextField(
                value = telefone,
                onValueChange = { telefone = it },
                label = { Text("Telefone") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )

            OutlinedTextField(
                value = objetivo,
                onValueChange = { objetivo = it },
                label = { Text("Objetivo") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ex: Perder peso, ganhar massa muscular...") },
                singleLine = true
            )

            // Status dropdown
            ExposedDropdownMenuBox(
                expanded = expandedStatus,
                onExpandedChange = { expandedStatus = !expandedStatus }
            ) {
                OutlinedTextField(
                    value = status,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedStatus,
                    onDismissRequest = { expandedStatus = false }
                ) {
                    statusOpcoes.forEach { opcao ->
                        DropdownMenuItem(
                            text = { Text(opcao) },
                            onClick = {
                                status = opcao
                                expandedStatus = false
                            }
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Seção: Dados Antropométricos
            Text(
                text = "Dados Antropométricos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = pesoAtual,
                    onValueChange = { pesoAtual = it },
                    label = { Text("Peso Atual (kg)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                OutlinedTextField(
                    value = pesoMeta,
                    onValueChange = { pesoMeta = it },
                    label = { Text("Peso Meta (kg)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = altura,
                    onValueChange = { altura = it },
                    label = { Text("Altura (cm)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                OutlinedTextField(
                    value = idade,
                    onValueChange = { idade = it },
                    label = { Text("Idade") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Seção: Anamnese
            Text(
                text = "Anamnese",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = historicoDoencas,
                onValueChange = { historicoDoencas = it },
                label = { Text("Histórico de Doenças") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4
            )

            OutlinedTextField(
                value = alergiasAlimentares,
                onValueChange = { alergiasAlimentares = it },
                label = { Text("Alergias Alimentares") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4
            )

            OutlinedTextField(
                value = medicamentos,
                onValueChange = { medicamentos = it },
                label = { Text("Medicamentos em Uso") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4
            )

            OutlinedTextField(
                value = rotinaExercicios,
                onValueChange = { rotinaExercicios = it },
                label = { Text("Rotina de Exercícios") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botão Salvar
            Button(
                onClick = {
                    if (nome.isNotBlank()) {
                        val paciente = PacienteEntity(
                            id = pacienteId ?: "",
                            nome = nome,
                            email = email,
                            telefone = telefone,
                            objetivo = objetivo,
                            pesoAtual = pesoAtual.toFloatOrNull() ?: 0f,
                            pesoMeta = pesoMeta.toFloatOrNull() ?: 0f,
                            altura = altura.toFloatOrNull() ?: 0f,
                            idade = idade.toIntOrNull() ?: 0,
                            status = status,
                            historicoDoencas = historicoDoencas,
                            alergiasAlimentares = alergiasAlimentares,
                            medicamentos = medicamentos,
                            rotinaExercicios = rotinaExercicios
                        )

                        if (pacienteId == null) {
                            viewModel.adicionarPaciente(paciente)
                        } else {
                            viewModel.atualizarPaciente(paciente)
                        }

                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = nome.isNotBlank()
            ) {
                Text(
                    text = if (pacienteId == null) "Adicionar Paciente" else "Salvar Alterações",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}