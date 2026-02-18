package com.example.nutriplan.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutriplan.ui.theme.PrimaryGreen
import com.example.nutriplan.ui.viewmodel.PacienteViewModel
import com.example.nutriplan.utils.CalculosMedidas
import com.example.nutriplan.utils.calcularIdade
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

fun formatarTelefoneParaExibicao(telefone: String): String {
    val digitos = telefone.filter { it.isDigit() }
    return when (digitos.length) {
        11 -> "(${digitos.substring(0, 2)}) ${digitos.substring(2, 3)} ${digitos.substring(3, 7)}-${digitos.substring(7, 11)}"
        10 -> "(${digitos.substring(0, 2)}) ${digitos.substring(2, 6)}-${digitos.substring(6, 10)}"
        else -> telefone
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalhesPacienteScreen(
    pacienteId: String,
    initialTabIndex: Int = 0,
    viewModel: PacienteViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToFormularioMedida: (String, String?) -> Unit = { _, _ -> },
    isDarkTheme: Boolean = false,
    currentLanguage: String = "pt",
    onLanguageChange: () -> Unit = {},
    onThemeChange: () -> Unit = {}
) {
    var paciente by remember { mutableStateOf<com.example.nutriplan.data.database.PacienteEntity?>(null) }
    var selectedTabIndex by remember { mutableStateOf(initialTabIndex) }
    val tabs = listOf("Dados", "Medidas", "Dieta", "Evolução")
    val scope = rememberCoroutineScope()

    LaunchedEffect(pacienteId) {
        scope.launch {
            paciente = viewModel.buscarPorId(pacienteId)
        }
    }

    if (paciente == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val pacienteData = paciente!!

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
                            text = "Detalhes do Paciente",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = PrimaryGreen,
                contentColor = Color.White
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> DadosTab(pacienteData, onNavigateToEdit)
                1 -> MedidasTab(
                    pacienteId = pacienteId,
                    paciente = pacienteData,
                    onNavigateToFormulario = { onNavigateToFormularioMedida(pacienteId, null) },
                    onNavigateToEdit = { medidaId -> onNavigateToFormularioMedida(pacienteId, medidaId) },
                    isDarkTheme = isDarkTheme
                )
                2 -> DietaTab()
                3 -> EvolucaoTab(
                    pacienteId = pacienteId,
                    paciente = pacienteData
                )
            }
        }
    }
}

@Composable
fun DadosTab(
    paciente: com.example.nutriplan.data.database.PacienteEntity,
    onNavigateToEdit: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryGreen)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val borderColor = when (paciente.status) {
                "Ativo" -> Color(0xFF4CAF50)
                "Inativo" -> Color(0xFFF44336)
                else -> Color.Gray
            }

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .border(
                        width = 3.dp,
                        color = borderColor,
                        shape = CircleShape
                    )
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF424242)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = paciente.nome.take(2).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = paciente.nome,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (paciente.email.isNotEmpty()) {
                Text(
                    text = paciente.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(
                title = "Informações Pessoais",
                icon = Icons.Default.Person
            ) {
                InfoRow("Nome", paciente.nome)

                if (paciente.apelido.isNotEmpty()) {
                    InfoRow("Apelido", paciente.apelido)
                }

                if (paciente.sexo.isNotEmpty()) {
                    InfoRow("Sexo", paciente.sexo)
                }

                if (paciente.dataNascimento.isNotEmpty()) {
                    InfoRow("Data de Nascimento", paciente.dataNascimento)
                }

                if (paciente.telefone.isNotEmpty()) {
                    InfoRow("Telefone", formatarTelefoneParaExibicao(paciente.telefone))
                }

                if (paciente.email.isNotEmpty()) {
                    InfoRow("E-mail", paciente.email)
                }
            }

            if (paciente.objetivo.isNotEmpty()) {
                InfoCard(
                    title = "Objetivo",
                    icon = Icons.Default.Flag
                ) {
                    Text(
                        text = paciente.objetivo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            InfoCard(
                title = "Status",
                icon = Icons.Default.Info
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status do Paciente",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Surface(
                        color = when (paciente.status) {
                            "Ativo" -> Color(0xFF4CAF50)
                            "Inativo" -> Color(0xFFF44336)
                            else -> Color.Gray
                        },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = paciente.status,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (paciente.dataCadastro.isNotEmpty()) {
                InfoCard(
                    title = "Data de Cadastro",
                    icon = Icons.Default.CalendarToday
                ) {
                    Text(
                        text = paciente.dataCadastro,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onNavigateToEdit(paciente.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Editar Paciente",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MedidasTab(
    pacienteId: String,
    paciente: com.example.nutriplan.data.database.PacienteEntity,
    onNavigateToFormulario: () -> Unit,
    onNavigateToEdit: (String) -> Unit = {},
    isDarkTheme: Boolean = false
) {
    val medidaViewModel: com.example.nutriplan.ui.viewmodel.MedidaViewModel = viewModel()
    val medidas by medidaViewModel.medidas.collectAsState()
    val isLoading by medidaViewModel.isLoading.collectAsState()
    val scope = rememberCoroutineScope()

    var showDetalhesDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var medidaSelecionada by remember { mutableStateOf<com.example.nutriplan.data.database.MedidaEntity?>(null) }

    LaunchedEffect(pacienteId) {
        medidaViewModel.carregarMedidasDoPaciente(pacienteId)
    }

    // ORDENAR MEDIDAS (MAIS ANTIGA PRIMEIRO, MAIS NOVA POR ÚLTIMO)
    val medidasOrdenadas = remember(medidas) {
        medidas.sortedBy { medida ->
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                sdf.parse(medida.dataMedicao)?.time ?: 0L
            } catch (e: Exception) {
                0L
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onNavigateToFormulario,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryGreen,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Adicionar",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Adicionar Medida",
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (medidasOrdenadas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = if (isDarkTheme) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Nenhuma medida cadastrada",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isDarkTheme) Color.White else Color.Black
                    )
                    Text(
                        text = "Clique no botão acima para adicionar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(medidasOrdenadas, key = { it.id }) { medida ->
                    MedidaCard(
                        medida = medida,
                        isDarkTheme = isDarkTheme,
                        onCardClick = {
                            medidaSelecionada = medida
                            showDetalhesDialog = true
                        },
                        onEditClick = { medidaId ->
                            onNavigateToEdit(medidaId)
                        },
                        onDeleteClick = {
                            medidaSelecionada = medida
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showDetalhesDialog && medidaSelecionada != null) {
        DetalheMedidaDialog(
            medida = medidaSelecionada!!,
            paciente = paciente,
            isDarkTheme = isDarkTheme,
            onDismiss = { showDetalhesDialog = false }
        )
    }

    if (showDeleteDialog && medidaSelecionada != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = if (isDarkTheme) Color(0xFF1C1C1E) else Color(0xFFF5F5F5),
            title = {
                Text(
                    "Excluir Medida",
                    color = if (isDarkTheme) Color.White else Color.Black
                )
            },
            text = {
                Text(
                    "Deseja realmente excluir a medida do dia ${medidaSelecionada!!.dataMedicao}?",
                    color = if (isDarkTheme) Color.White else Color.Black
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            medidaViewModel.deletarMedida(medidaSelecionada!!)
                            showDeleteDialog = false
                            medidaSelecionada = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = if (isDarkTheme) Color.White else Color.Black)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedidaCard(
    medida: com.example.nutriplan.data.database.MedidaEntity,
    isDarkTheme: Boolean,
    onCardClick: (String) -> Unit = {},
    onEditClick: (String) -> Unit = {},
    onDeleteClick: (String) -> Unit = {}
) {
    Card(
        onClick = { onCardClick(medida.id) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryGreen
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = medida.dataMedicao,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Peso",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "%.1f kg".format(medida.peso),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column {
                    Text(
                        text = "Altura",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "%.0f cm".format(medida.altura),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column {
                    Text(
                        text = "IMC",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "%.1f".format(medida.imc),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            if (medida.pgcValor != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "% Gordura",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "%.1f%%".format(medida.pgcValor),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    if (medida.mlgKg != null) {
                        Column {
                            Text(
                                text = "MLG",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "%.1f kg".format(medida.mlgKg),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GET",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "%.0f kcal".format(medida.getValor),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TMB",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = medida.tmbMetodo,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onEditClick(medida.id) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { onDeleteClick(medida.id) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DetalheMedidaDialog(
    medida: com.example.nutriplan.data.database.MedidaEntity,
    paciente: com.example.nutriplan.data.database.PacienteEntity,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit
) {
    val idade = calcularIdade(paciente.dataNascimento)

    val massaMuscular = CalculosMedidas.calcularMassaMuscular(medida.peso, medida.altura, idade)
    val massaOssea = CalculosMedidas.calcularMassaOssea(medida.altura)
    val massaResidual = CalculosMedidas.calcularMassaResidual(medida.peso)

    val massaGordura = medida.pgcValor?.let {
        CalculosMedidas.calcularMassaGordura(medida.peso, it)
    }

    val massaLivreGordura = medida.pgcValor?.let {
        CalculosMedidas.calcularMLG(medida.peso, it)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDarkTheme) Color(0xFF1C1C1E) else Color(0xFFF5F5F5),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Detalhes - ${medida.dataMedicao}",
                    color = if (isDarkTheme) Color.White else Color.Black
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionTitle("Dados Básicos", isDarkTheme)
                InfoRowDialog("Idade", "$idade anos", isDarkTheme)
                InfoRowDialog("Altura", "%.0f cm".format(medida.altura), isDarkTheme)
                InfoRowDialog("Peso", "%.1f kg".format(medida.peso), isDarkTheme)
                InfoRowDialog("IMC", "%.2f".format(medida.imc), isDarkTheme)
                InfoRowDialog("Classificação IMC", CalculosMedidas.classificarIMC(medida.imc), isDarkTheme)
                InfoRowDialog("Faixa de Peso Ideal", "%.1f - %.1f kg".format(medida.pesoIdealMin, medida.pesoIdealMax), isDarkTheme)

                Divider(color = if (isDarkTheme) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.2f))
                SectionTitle("Composição Corporal Completa", isDarkTheme)

                medida.pgcValor?.let {
                    InfoRowDialog("% Gordura (${medida.pgcMetodo})", "%.2f%%".format(it), isDarkTheme)
                    medida.pgcClassificacao?.let { classificacao ->
                        InfoRowDialog("Classificação PGC", classificacao, isDarkTheme)
                    }
                }

                massaGordura?.let {
                    InfoRowDialog("Massa de Gordura", "%.2f kg".format(it), isDarkTheme)
                }

                massaLivreGordura?.let {
                    InfoRowDialog("Massa Livre de Gordura (MLG)", "%.2f kg".format(it), isDarkTheme)
                }

                InfoRowDialog("Massa Muscular", "%.2f kg".format(massaMuscular), isDarkTheme)
                InfoRowDialog("Massa Óssea", "%.2f kg".format(massaOssea), isDarkTheme)
                InfoRowDialog("Massa Residual", "%.2f kg".format(massaResidual), isDarkTheme)

                Divider(color = if (isDarkTheme) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.2f))
                SectionTitle("Gasto Energético", isDarkTheme)
                InfoRowDialog("TMB (${medida.tmbMetodo})", "%.0f kcal/dia".format(medida.tmbValor), isDarkTheme)
                InfoRowDialog("Fator de Atividade", "${medida.faNivel} (${medida.faValor})", isDarkTheme)
                InfoRowDialog("GET (Gasto Total)", "%.0f kcal/dia".format(medida.getValor), isDarkTheme)

                if (medida.rcqValor != null) {
                    Divider(color = if (isDarkTheme) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.2f))
                    SectionTitle("RCQ (Relação Cintura-Quadril)", isDarkTheme)
                    InfoRowDialog("RCQ", "%.2f".format(medida.rcqValor), isDarkTheme)
                    medida.rcqClassificacao?.let {
                        InfoRowDialog("Risco", it, isDarkTheme)
                    }
                }

                val temCircunferencias = listOfNotNull(
                    medida.circPescoco, medida.circOmbro, medida.circTorax,
                    medida.circCintura, medida.circAbdomen, medida.circQuadril,
                    medida.circCoxaMedial, medida.circPanturrilha,
                    medida.circBracoRelaxado, medida.circBracoContraido,
                    medida.circAntebraco, medida.circPunho
                ).isNotEmpty()

                if (temCircunferencias) {
                    Divider(color = if (isDarkTheme) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.2f))
                    SectionTitle("Circunferências (cm)", isDarkTheme)

                    medida.circPescoco?.let { InfoRowDialog("Pescoço", "%.1f cm".format(it), isDarkTheme) }
                    medida.circOmbro?.let { InfoRowDialog("Ombro", "%.1f cm".format(it), isDarkTheme) }
                    medida.circTorax?.let { InfoRowDialog("Tórax", "%.1f cm".format(it), isDarkTheme) }
                    medida.circCintura?.let { InfoRowDialog("Cintura", "%.1f cm".format(it), isDarkTheme) }
                    medida.circAbdomen?.let { InfoRowDialog("Abdômen", "%.1f cm".format(it), isDarkTheme) }
                    medida.circQuadril?.let { InfoRowDialog("Quadril", "%.1f cm".format(it), isDarkTheme) }
                    medida.circCoxaMedial?.let { InfoRowDialog("Coxa Medial", "%.1f cm".format(it), isDarkTheme) }
                    medida.circPanturrilha?.let { InfoRowDialog("Panturrilha", "%.1f cm".format(it), isDarkTheme) }
                    medida.circBracoRelaxado?.let { InfoRowDialog("Braço Relaxado", "%.1f cm".format(it), isDarkTheme) }
                    medida.circBracoContraido?.let { InfoRowDialog("Braço Contraído", "%.1f cm".format(it), isDarkTheme) }
                    medida.circAntebraco?.let { InfoRowDialog("Antebraço", "%.1f cm".format(it), isDarkTheme) }
                    medida.circPunho?.let { InfoRowDialog("Punho", "%.1f cm".format(it), isDarkTheme) }
                }

                val temPregas = listOfNotNull(
                    medida.pregaBiceps, medida.pregaTriceps, medida.pregaPeitoral,
                    medida.pregaAxilarMedia, medida.pregaSubescapular, medida.pregaAbdomen,
                    medida.pregaSuprailiaca, medida.pregaCoxa
                ).isNotEmpty()

                if (temPregas) {
                    Divider(color = if (isDarkTheme) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.2f))
                    SectionTitle("Pregas Subcutâneas (mm)", isDarkTheme)

                    medida.pregaBiceps?.let { InfoRowDialog("Bíceps", "%.1f mm".format(it), isDarkTheme) }
                    medida.pregaTriceps?.let { InfoRowDialog("Tríceps", "%.1f mm".format(it), isDarkTheme) }
                    medida.pregaPeitoral?.let { InfoRowDialog("Peitoral", "%.1f mm".format(it), isDarkTheme) }
                    medida.pregaAxilarMedia?.let { InfoRowDialog("Axilar Média", "%.1f mm".format(it), isDarkTheme) }
                    medida.pregaSubescapular?.let { InfoRowDialog("Subescapular", "%.1f mm".format(it), isDarkTheme) }
                    medida.pregaAbdomen?.let { InfoRowDialog("Abdômen", "%.1f mm".format(it), isDarkTheme) }
                    medida.pregaSuprailiaca?.let { InfoRowDialog("Supra-ilíaca", "%.1f mm".format(it), isDarkTheme) }
                    medida.pregaCoxa?.let { InfoRowDialog("Coxa", "%.1f mm".format(it), isDarkTheme) }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("Fechar")
            }
        }
    )
}

@Composable
fun DietaTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Plano Alimentar",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Em desenvolvimento...",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )

        Text(
            text = "• Criar plano alimentar personalizado\n• Definir refeições diárias\n• Calcular calorias e macronutrientes\n• Gerar PDF do plano alimentar",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvolucaoTab(
    pacienteId: String,
    paciente: com.example.nutriplan.data.database.PacienteEntity
) {
    val medidaViewModel: com.example.nutriplan.ui.viewmodel.MedidaViewModel = viewModel()
    val medidas by medidaViewModel.medidas.collectAsState()
    val isLoading by medidaViewModel.isLoading.collectAsState()

    val idade = calcularIdade(paciente.dataNascimento)

    var medidaSelecionada1 by remember { mutableStateOf<com.example.nutriplan.data.database.MedidaEntity?>(null) }
    var medidaSelecionada2 by remember { mutableStateOf<com.example.nutriplan.data.database.MedidaEntity?>(null) }
    var expanded1 by remember { mutableStateOf(false) }
    var expanded2 by remember { mutableStateOf(false) }

    LaunchedEffect(pacienteId) {
        medidaViewModel.carregarMedidasDoPaciente(pacienteId)
    }

    // ORDENAR MEDIDAS (MAIS ANTIGA PRIMEIRO, MAIS NOVA POR ÚLTIMO)
    val medidasOrdenadas = remember(medidas) {
        medidas.sortedBy { medida ->
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                sdf.parse(medida.dataMedicao)?.time ?: 0L
            } catch (e: Exception) {
                0L
            }
        }
    }

    // AUTO-SELECIONAR 1ª (MAIS ANTIGA) E 2ª (MAIS RECENTE)
    LaunchedEffect(medidasOrdenadas) {
        if (medidasOrdenadas.isNotEmpty() && medidaSelecionada1 == null && medidaSelecionada2 == null) {
            medidaSelecionada1 = medidasOrdenadas.firstOrNull()
            medidaSelecionada2 = medidasOrdenadas.lastOrNull()
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryGreen)
        }
        return
    }

    if (medidasOrdenadas.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color.Gray.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Sem dados para evolução",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray
                )
                Text(
                    text = "Adicione pelo menos 1 medida",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray.copy(alpha = 0.7f)
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Comparativo de Avaliações",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PrimaryGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "1ª Avaliação (mais antiga)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded1,
                    onExpandedChange = { expanded1 = !expanded1 }
                ) {
                    OutlinedTextField(
                        value = medidaSelecionada1?.dataMedicao ?: "Selecione uma data",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = if (expanded1) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.White
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.7f)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded1,
                        onDismissRequest = { expanded1 = false },
                        modifier = Modifier.background(Color(0xFF1C1C1E))
                    ) {
                        medidasOrdenadas.forEach { medida ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = medida.dataMedicao,
                                        color = Color.White
                                    )
                                },
                                onClick = {
                                    medidaSelecionada1 = medida
                                    expanded1 = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PrimaryGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "2ª Avaliação (mais recente)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded2,
                    onExpandedChange = { expanded2 = !expanded2 }
                ) {
                    OutlinedTextField(
                        value = medidaSelecionada2?.dataMedicao ?: "Selecione uma data",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = if (expanded2) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.White
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.7f)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded2,
                        onDismissRequest = { expanded2 = false },
                        modifier = Modifier.background(Color(0xFF1C1C1E))
                    ) {
                        medidasOrdenadas.forEach { medida ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = medida.dataMedicao,
                                        color = Color.White
                                    )
                                },
                                onClick = {
                                    medidaSelecionada2 = medida
                                    expanded2 = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        if (medidaSelecionada1 != null && medidaSelecionada2 != null) {
            ComparativoCompletoMedidas(
                medida1 = medidaSelecionada1!!,
                medida2 = medidaSelecionada2!!,
                idade = idade
            )

            // GRÁFICOS DE EVOLUÇÃO
            if (medidasOrdenadas.size >= 2) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Gráficos de Evolução",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )

                GraficoEvolucaoSimples(
                    titulo = "Evolução do Peso",
                    dados = medidasOrdenadas,
                    extrairValor = { it.peso },
                    unidade = "kg",
                    cor = Color(0xFF2196F3)
                )

                if (medidasOrdenadas.all { it.pgcValor != null }) {
                    GraficoEvolucaoSimples(
                        titulo = "Evolução do % de Gordura",
                        dados = medidasOrdenadas,
                        extrairValor = { it.pgcValor ?: 0f },
                        unidade = "%",
                        cor = Color(0xFFFF9800)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ComparativoCompletoMedidas(
    medida1: com.example.nutriplan.data.database.MedidaEntity,
    medida2: com.example.nutriplan.data.database.MedidaEntity,
    idade: Int
) {
    val massaMuscular1 = CalculosMedidas.calcularMassaMuscular(medida1.peso, medida1.altura, idade)
    val massaMuscular2 = CalculosMedidas.calcularMassaMuscular(medida2.peso, medida2.altura, idade)

    val massaOssea1 = CalculosMedidas.calcularMassaOssea(medida1.altura)
    val massaOssea2 = CalculosMedidas.calcularMassaOssea(medida2.altura)

    val massaResidual1 = CalculosMedidas.calcularMassaResidual(medida1.peso)
    val massaResidual2 = CalculosMedidas.calcularMassaResidual(medida2.peso)

    val massaGordura1 = medida1.pgcValor?.let { CalculosMedidas.calcularMassaGordura(medida1.peso, it) }
    val massaGordura2 = medida2.pgcValor?.let { CalculosMedidas.calcularMassaGordura(medida2.peso, it) }

    val mlg1 = medida1.pgcValor?.let { CalculosMedidas.calcularMLG(medida1.peso, it) }
    val mlg2 = medida2.pgcValor?.let { CalculosMedidas.calcularMLG(medida2.peso, it) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        TabelaComparativaCard(
            titulo = "Dados Antropométricos",
            icon = Icons.Default.Person
        ) {
            LinhaComparativa("Altura (cm)", "%.0f".format(medida1.altura), "%.0f".format(medida2.altura))
            LinhaComparativa("Peso (kg)", "%.1f".format(medida1.peso), "%.1f".format(medida2.peso), medida1.peso, medida2.peso)
            LinhaComparativa("IMC", "%.2f".format(medida1.imc), "%.2f".format(medida2.imc), medida1.imc, medida2.imc)
            LinhaComparativa("Classificação IMC", CalculosMedidas.classificarIMC(medida1.imc), CalculosMedidas.classificarIMC(medida2.imc))
        }

        if (medida1.pgcValor != null && medida2.pgcValor != null) {
            TabelaComparativaCard(
                titulo = "Composição Corporal",
                icon = Icons.Default.FitnessCenter
            ) {
                LinhaComparativa(
                    "% Gordura",
                    "%.2f%%".format(medida1.pgcValor),
                    "%.2f%%".format(medida2.pgcValor),
                    medida1.pgcValor!!,
                    medida2.pgcValor!!
                )

                if (massaGordura1 != null && massaGordura2 != null) {
                    LinhaComparativa(
                        "Massa de Gordura (kg)",
                        "%.2f".format(massaGordura1),
                        "%.2f".format(massaGordura2),
                        massaGordura1,
                        massaGordura2
                    )
                }

                if (mlg1 != null && mlg2 != null) {
                    LinhaComparativa(
                        "MLG (kg)",
                        "%.2f".format(mlg1),
                        "%.2f".format(mlg2),
                        mlg1,
                        mlg2,
                        melhoraSeAumenta = true
                    )
                }

                LinhaComparativa(
                    "Massa Muscular (kg)",
                    "%.2f".format(massaMuscular1),
                    "%.2f".format(massaMuscular2),
                    massaMuscular1,
                    massaMuscular2,
                    melhoraSeAumenta = true
                )

                LinhaComparativa(
                    "Massa Óssea (kg)",
                    "%.2f".format(massaOssea1),
                    "%.2f".format(massaOssea2),
                    massaOssea1,
                    massaOssea2
                )

                LinhaComparativa(
                    "Massa Residual (kg)",
                    "%.2f".format(massaResidual1),
                    "%.2f".format(massaResidual2),
                    massaResidual1,
                    massaResidual2
                )
            }
        }

        TabelaComparativaCard(
            titulo = "Gasto Energético",
            icon = Icons.Default.LocalFireDepartment
        ) {
            LinhaComparativa(
                "TMB (kcal/dia)",
                "%.0f".format(medida1.tmbValor),
                "%.0f".format(medida2.tmbValor),
                medida1.tmbValor,
                medida2.tmbValor
            )
            LinhaComparativa("Método TMB", medida1.tmbMetodo, medida2.tmbMetodo)
            LinhaComparativa("Fator de Atividade", medida1.faNivel, medida2.faNivel)
            LinhaComparativa(
                "GET (kcal/dia)",
                "%.0f".format(medida1.getValor),
                "%.0f".format(medida2.getValor),
                medida1.getValor,
                medida2.getValor
            )
        }

        if (medida1.rcqValor != null && medida2.rcqValor != null) {
            TabelaComparativaCard(
                titulo = "RCQ (Relação Cintura-Quadril)",
                icon = Icons.Default.Analytics
            ) {
                LinhaComparativa(
                    "RCQ",
                    "%.2f".format(medida1.rcqValor),
                    "%.2f".format(medida2.rcqValor),
                    medida1.rcqValor!!,
                    medida2.rcqValor!!
                )
                LinhaComparativa(
                    "Classificação",
                    medida1.rcqClassificacao ?: "-",
                    medida2.rcqClassificacao ?: "-"
                )
            }
        }

        val temCircunferencias = medida1.circCintura != null || medida1.circQuadril != null

        if (temCircunferencias) {
            TabelaComparativaCard(
                titulo = "Circunferências (cm)",
                icon = Icons.Default.Straighten
            ) {
                medida1.circPescoco?.let {
                    LinhaComparativa(
                        "Pescoço",
                        "%.1f".format(it),
                        "%.1f".format(medida2.circPescoco ?: 0f),
                        it,
                        medida2.circPescoco ?: 0f
                    )
                }
                medida1.circOmbro?.let {
                    LinhaComparativa(
                        "Ombro",
                        "%.1f".format(it),
                        "%.1f".format(medida2.circOmbro ?: 0f),
                        it,
                        medida2.circOmbro ?: 0f
                    )
                }
                medida1.circTorax?.let {
                    LinhaComparativa(
                        "Tórax",
                        "%.1f".format(it),
                        "%.1f".format(medida2.circTorax ?: 0f),
                        it,
                        medida2.circTorax ?: 0f
                    )
                }
                medida1.circCintura?.let {
                    LinhaComparativa(
                        "Cintura",
                        "%.1f".format(it),
                        "%.1f".format(medida2.circCintura ?: 0f),
                        it,
                        medida2.circCintura ?: 0f
                    )
                }
                medida1.circAbdomen?.let {
                    LinhaComparativa(
                        "Abdômen",
                        "%.1f".format(it),
                        "%.1f".format(medida2.circAbdomen ?: 0f),
                        it,
                        medida2.circAbdomen ?: 0f
                    )
                }
                medida1.circQuadril?.let {
                    LinhaComparativa(
                        "Quadril",
                        "%.1f".format(it),
                        "%.1f".format(medida2.circQuadril ?: 0f),
                        it,
                        medida2.circQuadril ?: 0f
                    )
                }
                medida1.circCoxaMedial?.let {
                    LinhaComparativa(
                        "Coxa Medial",
                        "%.1f".format(it),
                        "%.1f".format(medida2.circCoxaMedial ?: 0f),
                        it,
                        medida2.circCoxaMedial ?: 0f
                    )
                }
                medida1.circPanturrilha?.let {
                    LinhaComparativa(
                        "Panturrilha",
                        "%.1f".format(it),
                        "%.1f".format(medida2.circPanturrilha ?: 0f),
                        it,
                        medida2.circPanturrilha ?: 0f
                    )
                }
                medida1.circBracoRelaxado?.let {
                    LinhaComparativa(
                        "Braço Relaxado",
                        "%.1f".format(it),
                        "%.1f".format(medida2.circBracoRelaxado ?: 0f),
                        it,
                        medida2.circBracoRelaxado ?: 0f
                    )
                }
                medida1.circBracoContraido?.let {
                    LinhaComparativa(
                        "Braço Contraído",
                        "%.1f".format(it),
                        "%.1f".format(medida2.circBracoContraido ?: 0f),
                        it,
                        medida2.circBracoContraido ?: 0f
                    )
                }
                medida1.circAntebraco?.let {
                    LinhaComparativa(
                        "Antebraço",
                        "%.1f".format(it),
                        "%.1f".format(medida2.circAntebraco ?: 0f),
                        it,
                        medida2.circAntebraco ?: 0f
                    )
                }
                medida1.circPunho?.let {
                    LinhaComparativa(
                        "Punho",
                        "%.1f".format(it),
                        "%.1f".format(medida2.circPunho ?: 0f),
                        it,
                        medida2.circPunho ?: 0f
                    )
                }
            }
        }

        val temPregas = medida1.pregaTriceps != null || medida1.pregaBiceps != null

        if (temPregas) {
            TabelaComparativaCard(
                titulo = "Pregas Cutâneas (mm)",
                icon = Icons.Default.Compress
            ) {
                medida1.pregaBiceps?.let {
                    LinhaComparativa(
                        "Bíceps",
                        "%.1f".format(it),
                        "%.1f".format(medida2.pregaBiceps ?: 0f),
                        it,
                        medida2.pregaBiceps ?: 0f
                    )
                }
                medida1.pregaTriceps?.let {
                    LinhaComparativa(
                        "Tríceps",
                        "%.1f".format(it),
                        "%.1f".format(medida2.pregaTriceps ?: 0f),
                        it,
                        medida2.pregaTriceps ?: 0f
                    )
                }
                medida1.pregaPeitoral?.let {
                    LinhaComparativa(
                        "Peitoral",
                        "%.1f".format(it),
                        "%.1f".format(medida2.pregaPeitoral ?: 0f),
                        it,
                        medida2.pregaPeitoral ?: 0f
                    )
                }
                medida1.pregaAxilarMedia?.let {
                    LinhaComparativa(
                        "Axilar Média",
                        "%.1f".format(it),
                        "%.1f".format(medida2.pregaAxilarMedia ?: 0f),
                        it,
                        medida2.pregaAxilarMedia ?: 0f
                    )
                }
                medida1.pregaSubescapular?.let {
                    LinhaComparativa(
                        "Subescapular",
                        "%.1f".format(it),
                        "%.1f".format(medida2.pregaSubescapular ?: 0f),
                        it,
                        medida2.pregaSubescapular ?: 0f
                    )
                }
                medida1.pregaAbdomen?.let {
                    LinhaComparativa(
                        "Abdômen",
                        "%.1f".format(it),
                        "%.1f".format(medida2.pregaAbdomen ?: 0f),
                        it,
                        medida2.pregaAbdomen ?: 0f
                    )
                }
                medida1.pregaSuprailiaca?.let {
                    LinhaComparativa(
                        "Supra-ilíaca",
                        "%.1f".format(it),
                        "%.1f".format(medida2.pregaSuprailiaca ?: 0f),
                        it,
                        medida2.pregaSuprailiaca ?: 0f
                    )
                }
                medida1.pregaCoxa?.let {
                    LinhaComparativa(
                        "Coxa",
                        "%.1f".format(it),
                        "%.1f".format(medida2.pregaCoxa ?: 0f),
                        it,
                        medida2.pregaCoxa ?: 0f
                    )
                }
            }
        }
    }
}

@Composable
fun GraficoEvolucaoSimples(
    titulo: String,
    dados: List<com.example.nutriplan.data.database.MedidaEntity>,
    extrairValor: (com.example.nutriplan.data.database.MedidaEntity) -> Float,
    unidade: String,
    cor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen
            )
            Spacer(modifier = Modifier.height(12.dp))

            val valores = dados.map { extrairValor(it) }
            val datas = dados.map { it.dataMedicao }

            val maxValor = valores.maxOrNull() ?: 1f
            val minValor = valores.minOrNull() ?: 0f
            val range = if (maxValor - minValor > 0) maxValor - minValor else 1f

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (unidade == "%") {
                                "%.1f%%".format(maxValor)
                            } else {
                                "%.1f $unidade".format(maxValor)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        val width = size.width
                        val height = size.height
                        val stepX = if (valores.size > 1) width / (valores.size - 1) else 0f

                        if (valores.size > 1) {
                            for (i in 0 until valores.size - 1) {
                                val x1 = i * stepX
                                val y1 = height - ((valores[i] - minValor) / range * height)
                                val x2 = (i + 1) * stepX
                                val y2 = height - ((valores[i + 1] - minValor) / range * height)

                                drawLine(
                                    color = cor,
                                    start = Offset(x1, y1),
                                    end = Offset(x2, y2),
                                    strokeWidth = 6f
                                )
                            }
                        }

                        valores.forEachIndexed { index, valor ->
                            val x = index * stepX
                            val y = height - ((valor - minValor) / range * height)

                            drawCircle(
                                color = cor,
                                radius = 10f,
                                center = Offset(x, y)
                            )

                            drawCircle(
                                color = androidx.compose.ui.graphics.Color.White,
                                radius = 4f,
                                center = Offset(x, y)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (unidade == "%") {
                                "%.1f%%".format(minValor)
                            } else {
                                "%.1f $unidade".format(minValor)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = datas.first(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        if (datas.size > 1) {
                            Text(
                                text = datas.last(),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabelaComparativaCard(
    titulo: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Medida",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1.2f)
                )
                Text(
                    text = "1ª Aval.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "2ª Aval.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Diferença",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            content()
        }
    }
}

@Composable
fun LinhaComparativa(
    medida: String,
    valor1: String,
    valor2: String,
    valorNum1: Float? = null,
    valorNum2: Float? = null,
    melhoraSeAumenta: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = medida,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black.copy(alpha = 0.8f),
            modifier = Modifier.weight(1.2f)
        )
        Text(
            text = valor1,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = valor2,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        if (valorNum1 != null && valorNum2 != null) {
            val diferenca = valorNum2 - valorNum1
            val melhorou = if (melhoraSeAumenta) diferenca > 0 else diferenca < 0

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when {
                        diferenca > 0 -> Icons.Default.ArrowUpward
                        diferenca < 0 -> Icons.Default.ArrowDownward
                        else -> Icons.Default.Remove
                    },
                    contentDescription = null,
                    tint = when {
                        diferenca == 0f -> Color.Gray
                        melhorou -> Color(0xFF4CAF50)
                        else -> Color(0xFFF44336)
                    },
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (diferenca >= 0) "+%.2f".format(diferenca) else "%.2f".format(diferenca),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        diferenca == 0f -> Color.Gray
                        melhorou -> Color(0xFF4CAF50)
                        else -> Color(0xFFF44336)
                    }
                )
            }
        } else {
            Text(
                text = "-",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun SectionTitle(text: String, isDarkTheme: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = PrimaryGreen
    )
}

@Composable
fun InfoRowDialog(label: String, value: String, isDarkTheme: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isDarkTheme) Color.White else Color.Black
        )
    }
}

@Composable
fun InfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryGreen
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}