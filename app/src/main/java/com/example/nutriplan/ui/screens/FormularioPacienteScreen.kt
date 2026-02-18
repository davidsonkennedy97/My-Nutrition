package com.example.nutriplan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutriplan.data.database.PacienteEntity
import com.example.nutriplan.ui.viewmodel.PacienteViewModel
import com.example.nutriplan.ui.theme.PrimaryGreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

fun formatarTelefone(texto: String): String {
    val apenasDigitos = texto.filter { it.isDigit() }

    return when {
        apenasDigitos.length <= 2 -> apenasDigitos
        apenasDigitos.length <= 3 -> "(${apenasDigitos.substring(0, 2)}) ${apenasDigitos.substring(2)}"
        apenasDigitos.length <= 7 -> "(${apenasDigitos.substring(0, 2)}) ${apenasDigitos.substring(2, 3)} ${apenasDigitos.substring(3)}"
        apenasDigitos.length <= 11 -> "(${apenasDigitos.substring(0, 2)}) ${apenasDigitos.substring(2, 3)} ${apenasDigitos.substring(3, 7)}-${apenasDigitos.substring(7)}"
        else -> "(${apenasDigitos.substring(0, 2)}) ${apenasDigitos.substring(2, 3)} ${apenasDigitos.substring(3, 7)}-${apenasDigitos.substring(7, 11)}"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioPacienteScreen(
    pacienteId: String? = null,
    viewModel: PacienteViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    isDarkTheme: Boolean = false,
    currentLanguage: String = "pt",
    onLanguageChange: () -> Unit = {},
    onThemeChange: () -> Unit = {}
) {
    var nome by remember { mutableStateOf("") }
    var apelido by remember { mutableStateOf("") }
    var sexo by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf(TextFieldValue("")) }
    var dataNascimento by remember { mutableStateOf(TextFieldValue("")) }
    var objetivo by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Ativo") }
    var dataCadastroExistente by remember { mutableStateOf<String?>(null) }

    var expandedSexo by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }
    val sexoOpcoes = listOf("Masculino", "Feminino")
    val statusOpcoes = listOf("Ativo", "Inativo")

    val textColor = if (isDarkTheme) Color.White else Color.Black
    val scope = rememberCoroutineScope()

    LaunchedEffect(pacienteId) {
        pacienteId?.let { id ->
            scope.launch {
                viewModel.buscarPorId(id)?.let { paciente ->
                    nome = paciente.nome
                    apelido = paciente.apelido
                    sexo = paciente.sexo
                    email = paciente.email

                    val telefoneFormatado = formatarTelefone(paciente.telefone)
                    telefone = TextFieldValue(
                        text = telefoneFormatado,
                        selection = TextRange(telefoneFormatado.length)
                    )

                    dataNascimento = TextFieldValue(
                        text = paciente.dataNascimento,
                        selection = TextRange(paciente.dataNascimento.length)
                    )
                    objetivo = paciente.objetivo
                    status = paciente.status
                    dataCadastroExistente = paciente.dataCadastro
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                color = PrimaryGreen
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterStart),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Voltar",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Text(
                            text = if (pacienteId == null) "Novo Paciente" else "Editar Paciente",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = 8.dp)
                    ) {
                        Text(
                            text = "My Nutrition",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        TextButton(
                            onClick = onLanguageChange,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(
                                text = if (currentLanguage == "pt") "EN" else "PT",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = onThemeChange,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle theme",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Dados Básicos",
                style = MaterialTheme.typography.titleMedium,
                color = if (isDarkTheme) Color.White else PrimaryGreen,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome Completo *", color = textColor.copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = textColor
                )
            )

            OutlinedTextField(
                value = apelido,
                onValueChange = { apelido = it },
                label = { Text("Apelido", color = textColor.copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = textColor
                )
            )

            ExposedDropdownMenuBox(
                expanded = expandedSexo,
                onExpandedChange = { expandedSexo = !expandedSexo }
            ) {
                OutlinedTextField(
                    value = sexo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sexo", color = textColor.copy(alpha = 0.7f)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = expandedSexo
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        disabledTextColor = textColor
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedSexo,
                    onDismissRequest = { expandedSexo = false },
                    modifier = Modifier
                        .exposedDropdownSize()
                        .background(Color(0xFF303233))
                ) {
                    sexoOpcoes.forEach { opcao ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = opcao,
                                    color = Color.White
                                )
                            },
                            onClick = {
                                sexo = opcao
                                expandedSexo = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = Color.White
                            )
                        )
                    }
                }
            }

            OutlinedTextField(
                value = dataNascimento,
                onValueChange = { newValue ->
                    val digitos = newValue.text.filter { it.isDigit() }

                    if (digitos.length <= 8) {
                        val formatado = buildString {
                            digitos.forEachIndexed { index, char ->
                                if (index == 2 || index == 4) append("/")
                                append(char)
                            }
                        }

                        dataNascimento = TextFieldValue(
                            text = formatado,
                            selection = TextRange(formatado.length)
                        )
                    }
                },
                label = { Text("Data de Nascimento", color = textColor.copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                placeholder = { Text("DD/MM/AAAA", color = textColor.copy(alpha = 0.5f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = textColor
                )
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail", color = textColor.copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = textColor
                )
            )

            OutlinedTextField(
                value = telefone,
                onValueChange = { newValue ->
                    val digitos = newValue.text.filter { it.isDigit() }

                    if (digitos.length <= 11) {
                        val formatado = formatarTelefone(digitos)

                        telefone = TextFieldValue(
                            text = formatado,
                            selection = TextRange(formatado.length)
                        )
                    }
                },
                label = { Text("Telefone", color = textColor.copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                placeholder = { Text("(00) 0 0000-0000", color = textColor.copy(alpha = 0.5f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = textColor
                )
            )

            OutlinedTextField(
                value = objetivo,
                onValueChange = { objetivo = it },
                label = { Text("Objetivo", color = textColor.copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ex: Perder peso, ganhar massa muscular...", color = textColor.copy(alpha = 0.5f)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = textColor
                )
            )

            ExposedDropdownMenuBox(
                expanded = expandedStatus,
                onExpandedChange = { expandedStatus = !expandedStatus }
            ) {
                OutlinedTextField(
                    value = status,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status", color = textColor.copy(alpha = 0.7f)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = expandedStatus
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        disabledTextColor = textColor
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedStatus,
                    onDismissRequest = { expandedStatus = false },
                    modifier = Modifier
                        .exposedDropdownSize()
                        .background(Color(0xFF303233))
                ) {
                    statusOpcoes.forEach { opcao ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = opcao,
                                    color = Color.White
                                )
                            },
                            onClick = {
                                status = opcao
                                expandedStatus = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (nome.isNotBlank()) {
                        val telefoneApenasDigitos = telefone.text.filter { it.isDigit() }
                        val dataCadastro = dataCadastroExistente ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

                        val paciente = PacienteEntity(
                            id = pacienteId ?: "",
                            nome = nome,
                            apelido = apelido,
                            sexo = sexo,
                            email = email,
                            telefone = telefoneApenasDigitos,
                            dataNascimento = dataNascimento.text,
                            objetivo = objetivo,
                            status = status,
                            dataCadastro = dataCadastro,
                            pesoAtual = 0f,
                            pesoMeta = 0f,
                            altura = 0f,
                            idade = 0,
                            historicoDoencas = "",
                            alergiasAlimentares = "",
                            medicamentos = "",
                            rotinaExercicios = ""
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
                enabled = nome.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.White,
                    disabledContainerColor = PrimaryGreen.copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = if (pacienteId == null) "Adicionar Paciente" else "Salvar Alterações",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}