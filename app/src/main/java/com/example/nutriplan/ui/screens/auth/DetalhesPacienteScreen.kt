package com.example.nutriplan.ui.screens

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutriplan.ui.theme.PrimaryGreen
import com.example.nutriplan.ui.viewmodel.DietaViewModel
import com.example.nutriplan.ui.viewmodel.MedidaViewModel
import com.example.nutriplan.ui.viewmodel.PacienteViewModel
import com.example.nutriplan.utils.CalculosMedidas
import com.example.nutriplan.utils.calcularIdade
import kotlinx.coroutines.launch
import java.util.Locale
import com.example.nutriplan.ui.screens.dieta.DietaTab

fun formatarTelefoneParaExibicao(telefone: String): String {
    val digitos = telefone.filter { it.isDigit() }
    return when (digitos.length) {
        11 -> "(${digitos.substring(0, 2)}) ${digitos.substring(2, 3)} ${digitos.substring(3, 7)}-${digitos.substring(7, 11)}"
        10 -> "(${digitos.substring(0, 2)}) ${digitos.substring(2, 6)}-${digitos.substring(6, 10)}"
        else -> telefone
    }
}

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalhesPacienteScreen(
    pacienteId: String,
    initialTabIndex: Int = 0,
    pacienteViewModel: PacienteViewModel = viewModel(),
    dietaViewModel: DietaViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToFormularioMedida: (String, String?) -> Unit = { _, _ -> },
    onNavigateToDietaEditor: (String) -> Unit,
    isDarkTheme: Boolean = false,
    currentLanguage: String = "pt",
    onLanguageChange: () -> Unit = {},
    onThemeChange: () -> Unit = {}
) {
    var paciente by remember { mutableStateOf<com.example.nutriplan.data.database.PacienteEntity?>(null) }
    var selectedTabIndex by remember { mutableStateOf(initialTabIndex) }
    val tabs = listOf("Dados", "Medidas", "Dieta", "Evolução")

    LaunchedEffect(pacienteId) {
        paciente = pacienteViewModel.buscarPorId(pacienteId)
    }

    if (paciente == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                Box(modifier = Modifier.fillMaxSize()) {
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
                        IconButton(onClick = onThemeChange, modifier = Modifier.size(48.dp)) {
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

                2 -> DietaTab(
                    pacienteId = pacienteData.id,
                    isDarkTheme = isDarkTheme,
                    dietaViewModel = dietaViewModel,
                    onNavigateToDietaEditor = onNavigateToDietaEditor
                )

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
    Column(modifier = Modifier.fillMaxSize()) {
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
                    .border(width = 3.dp, color = borderColor, shape = CircleShape)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF424242)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = paciente.nome.take(2).uppercase(Locale.getDefault()),
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
            InfoCard(title = "Informações Pessoais", icon = Icons.Default.Person) {
                InfoRow("Nome", paciente.nome)
                if (paciente.apelido.isNotEmpty()) InfoRow("Apelido", paciente.apelido)
                if (paciente.sexo.isNotEmpty()) InfoRow("Sexo", paciente.sexo)
                if (paciente.dataNascimento.isNotEmpty()) InfoRow("Data de Nascimento", paciente.dataNascimento)
                if (paciente.telefone.isNotEmpty()) InfoRow("Telefone", formatarTelefoneParaExibicao(paciente.telefone))
                if (paciente.email.isNotEmpty()) InfoRow("E-mail", paciente.email)
            }

            if (paciente.objetivo.isNotEmpty()) {
                InfoCard(title = "Objetivo", icon = Icons.Default.Flag) {
                    Text(
                        text = paciente.objetivo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            InfoCard(title = "Status", icon = Icons.Default.Info) {
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
                InfoCard(title = "Data de Cadastro", icon = Icons.Default.CalendarToday) {
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
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Editar Paciente", style = MaterialTheme.typography.titleMedium)
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
    val medidaViewModel: MedidaViewModel = viewModel()
    val medidas by medidaViewModel.medidas.collectAsState()
    val isLoading by medidaViewModel.isLoading.collectAsState()
    val scope = rememberCoroutineScope()

    var showDetalhesDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var medidaSelecionada by remember { mutableStateOf<com.example.nutriplan.data.database.MedidaEntity?>(null) }

    LaunchedEffect(pacienteId) {
        medidaViewModel.carregarMedidasDoPaciente(pacienteId)
    }

    val medidasOrdenadas = remember(medidas) {
        medidas.sortedWith(compareBy({ it.dataCriacao }, { it.id }))
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
            Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Adicionar Medida", style = MaterialTheme.typography.titleMedium)
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (medidasOrdenadas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
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
                        onEditClick = { medidaId -> onNavigateToEdit(medidaId) },
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
            title = { Text("Excluir Medida", color = if (isDarkTheme) Color.White else Color.Black) },
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
                ) { Text("Excluir") }
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
    onCardClick: () -> Unit = {},
    onEditClick: (String) -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    Card(
        onClick = onCardClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Peso", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                    Text("%.1f kg".format(medida.peso), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column {
                    Text("Altura", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                    Text("%.0f cm".format(medida.altura), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column {
                    Text("IMC", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                    Text("%.1f".format(medida.imc), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            if (medida.pgcValor != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("% Gordura", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                        Text("%.1f%%".format(medida.pgcValor), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    if (medida.mlgKg != null) {
                        Column {
                            Text("MLG", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                            Text("%.1f kg".format(medida.mlgKg), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("GET", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                    Text("%.0f kcal".format(medida.getValor), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("TMB", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                    Text(medida.tmbMetodo, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onEditClick(medida.id) }, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.White, modifier = Modifier.size(20.dp))
                }

                IconButton(onClick = onDeleteClick, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

    val massaGordura = medida.pgcValor?.let { CalculosMedidas.calcularMassaGordura(medida.peso, it) }
    val massaLivreGordura = medida.mlgKg ?: medida.pgcValor?.let { CalculosMedidas.calcularMLG(medida.peso, it) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDarkTheme) Color(0xFF1C1C1E) else Color(0xFFF5F5F5),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Default.Analytics, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(24.dp))
                Text(text = "Detalhes - ${medida.dataMedicao}", color = if (isDarkTheme) Color.White else Color.Black)
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

                massaGordura?.let { InfoRowDialog("Massa de Gordura", "%.2f kg".format(it), isDarkTheme) }
                massaLivreGordura?.let { InfoRowDialog("MLG (Massa Livre de Gordura)", "%.2f kg".format(it), isDarkTheme) }

                InfoRowDialog("Massa Muscular", "%.2f kg".format(massaMuscular), isDarkTheme)
                InfoRowDialog("Massa Óssea", "%.2f kg".format(massaOssea), isDarkTheme)
                InfoRowDialog("Massa Residual", "%.2f kg".format(massaResidual), isDarkTheme)

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
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)) {
                Text("Fechar")
            }
        }
    )
}

private class AvaliacaoItem(initial: com.example.nutriplan.data.database.MedidaEntity?) {
    var medida by mutableStateOf(initial)
    var expanded by mutableStateOf(false)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvolucaoTab(
    pacienteId: String,
    paciente: com.example.nutriplan.data.database.PacienteEntity
) {
    val medidaViewModel: MedidaViewModel = viewModel()
    val medidas by medidaViewModel.medidas.collectAsState()
    val isLoading by medidaViewModel.isLoading.collectAsState()

    val idade = calcularIdade(paciente.dataNascimento)

    LaunchedEffect(pacienteId) {
        medidaViewModel.carregarMedidasDoPaciente(pacienteId)
    }

    val medidasOrdenadas = remember(medidas) {
        medidas.sortedWith(compareBy({ it.dataCriacao }, { it.id }))
    }

    val medidasComLabel: List<Pair<com.example.nutriplan.data.database.MedidaEntity, String>> =
        remember(medidasOrdenadas) {
            val contagemPorData = medidasOrdenadas.groupingBy { it.dataMedicao }.eachCount()
            val indiceCorrente = mutableMapOf<String, Int>()
            medidasOrdenadas.map { medida ->
                val total = contagemPorData[medida.dataMedicao] ?: 1
                if (total <= 1) {
                    medida to medida.dataMedicao
                } else {
                    val atual = (indiceCorrente[medida.dataMedicao] ?: 0) + 1
                    indiceCorrente[medida.dataMedicao] = atual
                    medida to "${medida.dataMedicao} ($atual)"
                }
            }
        }

    fun labelPara(m: com.example.nutriplan.data.database.MedidaEntity?): String {
        if (m == null) return "Selecione uma data"
        return medidasComLabel.firstOrNull { it.first.id == m.id }?.second ?: m.dataMedicao
    }

    val avaliacoes = remember { mutableStateListOf<AvaliacaoItem>() }

    LaunchedEffect(medidasOrdenadas) {
        if (medidasOrdenadas.isNotEmpty() && avaliacoes.isEmpty()) {
            avaliacoes.add(AvaliacaoItem(medidasOrdenadas.firstOrNull()))
            avaliacoes.add(AvaliacaoItem(medidasOrdenadas.lastOrNull()))
        }
    }

    val selecionadasValidas by remember {
        derivedStateOf {
            avaliacoes
                .mapNotNull { it.medida }
                .distinctBy { it.id }
                .sortedBy { it.dataCriacao }
        }
    }

    val dadosDoGrafico by remember {
        derivedStateOf { if (selecionadasValidas.size >= 2) selecionadasValidas else medidasOrdenadas }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryGreen)
        }
        return
    }

    if (medidasOrdenadas.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Sem dados para evolução", color = Color.Gray)
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

        avaliacoes.forEachIndexed { index, item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (index) {
                                0 -> "1ª Avaliação (mais antiga)"
                                1 -> "2ª Avaliação (mais recente)"
                                else -> "${index + 1}ª Avaliação"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        if (avaliacoes.size > 2 && index >= 2) {
                            IconButton(onClick = { avaliacoes.removeAt(index) }) {
                                Icon(Icons.Default.Close, contentDescription = "Remover", tint = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = item.expanded,
                        onExpandedChange = { item.expanded = !item.expanded }
                    ) {
                        OutlinedTextField(
                            value = labelPara(item.medida),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = if (item.expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.7f)
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = item.expanded,
                            onDismissRequest = { item.expanded = false },
                            modifier = Modifier.background(Color(0xFF1C1C1E))
                        ) {
                            medidasComLabel.forEach { (medida, label) ->
                                DropdownMenuItem(
                                    text = { Text(text = label, color = Color.White) },
                                    onClick = {
                                        item.medida = medida
                                        item.expanded = false
                                    },
                                    colors = MenuDefaults.itemColors(textColor = Color.White)
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = { avaliacoes.add(AvaliacaoItem(null)) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.White)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Adicionar mais avaliação")
        }

        if (selecionadasValidas.size >= 2) {
            ComparativoMultiAvaliacoes(avaliacoes = selecionadasValidas, idade = idade)
        }

        if (dadosDoGrafico.size >= 2) {
            Text(
                text = "Gráfico de Evolução (Peso + % Gordura + MLG)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen
            )
            GraficoEvolucaoComparativoPesoGordura(
                dados = dadosDoGrafico,
                corPeso = Color(0xFF2196F3),
                corGordura = Color(0xFFFF9800)
            )
        }
    }
}

@Composable
private fun ComparativoMultiAvaliacoes(
    avaliacoes: List<com.example.nutriplan.data.database.MedidaEntity>,
    idade: Int
) {
    val headers = remember(avaliacoes) {
        avaliacoes.mapIndexed { idx, m -> "${idx + 1}ª Aval.\n${m.dataMedicao}" }
    }

    TabelaMultiColunasCard("Dados Antropométricos", Icons.Default.Person, headers) {
        LinhaMultiColunas("Altura (cm)", avaliacoes.map { "%.0f".format(it.altura) })
        LinhaMultiColunas("Peso (kg)", avaliacoes.map { "%.1f".format(it.peso) })
        LinhaMultiColunas("IMC", avaliacoes.map { "%.2f".format(it.imc) })
        LinhaMultiColunas("Classificação IMC", avaliacoes.map { CalculosMedidas.classificarIMC(it.imc) })
    }

    val temAlgumPgc = avaliacoes.any { it.pgcValor != null }
    if (temAlgumPgc) {
        TabelaMultiColunasCard("Composição Corporal", Icons.Default.FitnessCenter, headers) {
            LinhaMultiColunas("% Gordura", avaliacoes.map { it.pgcValor?.let { v -> "%.2f%%".format(v) } ?: "-" })
            LinhaMultiColunas(
                "Massa de Gordura (kg)",
                avaliacoes.map { m -> m.pgcValor?.let { pgc -> "%.2f".format(CalculosMedidas.calcularMassaGordura(m.peso, pgc)) } ?: "-" }
            )
            LinhaMultiColunas(
                "MLG (kg)",
                avaliacoes.map { m ->
                    val mlg = m.mlgKg ?: m.pgcValor?.let { pgc -> CalculosMedidas.calcularMLG(m.peso, pgc) }
                    mlg?.let { "%.2f".format(it) } ?: "-"
                }
            )
            LinhaMultiColunas(
                "Massa Muscular (kg)",
                avaliacoes.map { m -> "%.2f".format(CalculosMedidas.calcularMassaMuscular(m.peso, m.altura, idade)) }
            )
            LinhaMultiColunas("Massa Óssea (kg)", avaliacoes.map { m -> "%.2f".format(CalculosMedidas.calcularMassaOssea(m.altura)) })
            LinhaMultiColunas("Massa Residual (kg)", avaliacoes.map { m -> "%.2f".format(CalculosMedidas.calcularMassaResidual(m.peso)) })
        }
    }

    TabelaMultiColunasCard("Gasto Energético", Icons.Default.LocalFireDepartment, headers) {
        LinhaMultiColunas("TMB (kcal/dia)", avaliacoes.map { "%.0f".format(it.tmbValor) })
        LinhaMultiColunas("Método TMB", avaliacoes.map { it.tmbMetodo })
        LinhaMultiColunas("Fator de Atividade", avaliacoes.map { it.faNivel })
        LinhaMultiColunas("GET (kcal/dia)", avaliacoes.map { "%.0f".format(it.getValor) })
    }

    val temAlgumRcq = avaliacoes.any { it.rcqValor != null || !it.rcqClassificacao.isNullOrBlank() }
    if (temAlgumRcq) {
        TabelaMultiColunasCard("RCQ (Relação Cintura-Quadril)", Icons.Default.Analytics, headers) {
            LinhaMultiColunas("RCQ", avaliacoes.map { it.rcqValor?.let { v -> "%.2f".format(v) } ?: "-" })
            LinhaMultiColunas("Classificação", avaliacoes.map { it.rcqClassificacao ?: "-" })
        }
    }
}

@Composable
private fun TabelaMultiColunasCard(
    titulo: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    headers: List<String>,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                Icon(imageVector = icon, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PrimaryGreen)
            }

            val scroll = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scroll)
            ) {
                Row(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(
                        text = "Medida",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.width(170.dp)
                    )

                    headers.forEach { h ->
                        Text(
                            text = h,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.width(120.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Divider()
                Column(content = content)
            }
        }
    }
}

@Composable
private fun LinhaMultiColunas(
    medida: String,
    valores: List<String>
) {
    Row(
        modifier = Modifier
            .padding(vertical = 6.dp)
            .heightIn(min = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = medida,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black.copy(alpha = 0.8f),
            modifier = Modifier.width(170.dp)
        )

        valores.forEach { v ->
            Text(
                text = v,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black,
                modifier = Modifier.width(120.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun GraficoEvolucaoComparativoPesoGordura(
    dados: List<com.example.nutriplan.data.database.MedidaEntity>,
    corPeso: Color,
    corGordura: Color
) {
    val corMlg = Color(0xFF4CAF50)
    val yMin = 0f
    val yMax = 140f
    val step = 20f

    val labelsX = remember(dados) {
        val contagem = dados.groupingBy { it.dataMedicao }.eachCount()
        val idx = mutableMapOf<String, Int>()
        dados.map { m ->
            val total = contagem[m.dataMedicao] ?: 1
            if (total <= 1) m.dataMedicao
            else {
                val atual = (idx[m.dataMedicao] ?: 0) + 1
                idx[m.dataMedicao] = atual
                "${m.dataMedicao} ($atual)"
            }
        }
    }

    val temGordura = remember(dados) { dados.count { it.pgcValor != null } >= 2 }
    val temMlg = remember(dados) { dados.count { it.mlgKg != null } >= 2 }

    Card(
        modifier = Modifier.fillMaxWidth().height(380.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendaItem(cor = corPeso, texto = "Peso (kg)")
                LegendaItem(cor = corGordura, texto = "% Gordura")
                LegendaItem(cor = corMlg, texto = "MLG (kg)")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val n = dados.size
                    if (n == 0) return@Canvas

                    val xLabelArea = 64f
                    val chartHeight = height - xLabelArea
                    val stepX = if (n > 1) width / (n - 1) else 0f

                    fun clamp(v: Float) = v.coerceIn(yMin, yMax)
                    fun yOf(value: Float): Float =
                        chartHeight - ((clamp(value) - yMin) / (yMax - yMin) * chartHeight)

                    val paintY = Paint().apply {
                        isAntiAlias = true
                        textSize = 26f
                        color = android.graphics.Color.GRAY
                    }
                    val gridColor = Color(0xFFBDBDBD).copy(alpha = 0.45f)

                    var v = yMin
                    while (v <= yMax) {
                        val y = yOf(v)
                        drawLine(gridColor, Offset(0f, y), Offset(width, y), strokeWidth = 2f)
                        drawContext.canvas.nativeCanvas.drawText(
                            v.toInt().toString(),
                            0f,
                            (y - 6f).coerceAtLeast(18f),
                            paintY
                        )
                        v += step
                    }

                    for (i in 0 until n) {
                        val x = i * stepX
                        drawLine(
                            color = gridColor.copy(alpha = 0.25f),
                            start = Offset(x, 0f),
                            end = Offset(x, chartHeight),
                            strokeWidth = 2f
                        )
                    }

                    fun drawSeries(color: Color, values: List<Float?>) {
                        if (n > 1) {
                            for (i in 0 until n - 1) {
                                val v1 = values[i]
                                val v2 = values[i + 1]
                                if (v1 != null && v2 != null) {
                                    val x1 = i * stepX
                                    val x2 = (i + 1) * stepX
                                    drawLine(color, Offset(x1, yOf(v1)), Offset(x2, yOf(v2)), strokeWidth = 6f)
                                }
                            }
                        }
                        values.forEachIndexed { i, vv ->
                            if (vv != null) {
                                val x = i * stepX
                                val y = yOf(vv)
                                drawCircle(color, radius = 10f, center = Offset(x, y))
                                drawCircle(Color.White, radius = 4f, center = Offset(x, y))
                            }
                        }
                    }

                    drawSeries(corPeso, dados.map { it.peso })
                    if (temGordura) drawSeries(corGordura, dados.map { it.pgcValor })
                    if (temMlg) drawSeries(corMlg, dados.map { it.mlgKg })

                    val paintX = Paint().apply {
                        isAntiAlias = true
                        textSize = 22f
                        color = android.graphics.Color.DKGRAY
                    }

                    labelsX.forEachIndexed { i, label ->
                        val x = i * stepX
                        val baseY = chartHeight + 52f
                        drawContext.canvas.nativeCanvas.apply {
                            save()
                            translate(x, baseY)
                            rotate(-35f)
                            val textWidth = paintX.measureText(label)
                            var drawX = -textWidth / 2f
                            if (x + drawX < 0f) drawX = -x
                            if (x + drawX + textWidth > width) drawX = width - x - textWidth
                            drawText(label, drawX, 0f, paintX)
                            restore()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendaItem(cor: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(cor))
        Text(text = texto, style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SectionTitle(text: String, isDarkTheme: Boolean = false) {
    Text(text = text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = PrimaryGreen)
}

@Composable
fun InfoRowDialog(label: String, value: String, isDarkTheme: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
        colors = CardDefaults.cardColors(containerColor = PrimaryGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
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
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
    }
}