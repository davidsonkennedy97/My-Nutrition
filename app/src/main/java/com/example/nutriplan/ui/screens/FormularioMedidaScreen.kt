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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutriplan.data.database.MedidaEntity
import com.example.nutriplan.ui.viewmodel.MedidaViewModel
import com.example.nutriplan.ui.viewmodel.PacienteViewModel
import com.example.nutriplan.ui.theme.PrimaryGreen
import com.example.nutriplan.utils.CalculosMedidas
import com.example.nutriplan.utils.CamposObrigatoriosPGC
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

fun calcularIdade(dataNascimento: String): Int {
    if (dataNascimento.isEmpty()) return 0

    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dataNasc = sdf.parse(dataNascimento) ?: return 0

        val hoje = Calendar.getInstance()
        val nascimento = Calendar.getInstance()
        nascimento.time = dataNasc

        var idade = hoje.get(Calendar.YEAR) - nascimento.get(Calendar.YEAR)

        if (hoje.get(Calendar.DAY_OF_YEAR) < nascimento.get(Calendar.DAY_OF_YEAR)) {
            idade--
        }

        idade
    } catch (e: Exception) {
        0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioMedidaScreen(
    pacienteId: String,
    medidaId: String? = null,
    onNavigateBack: () -> Unit,
    isDarkTheme: Boolean = false,
    currentLanguage: String = "pt",
    onLanguageChange: () -> Unit = {},
    onThemeChange: () -> Unit = {},
    medidaViewModel: MedidaViewModel = viewModel(),
    pacienteViewModel: PacienteViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val textColor = if (isDarkTheme) Color.White else Color.Black

    var paciente by remember { mutableStateOf<com.example.nutriplan.data.database.PacienteEntity?>(null) }

    var altura by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }

    var circPescoco by remember { mutableStateOf("") }
    var circOmbro by remember { mutableStateOf("") }
    var circTorax by remember { mutableStateOf("") }
    var circCintura by remember { mutableStateOf("") }
    var circAbdomen by remember { mutableStateOf("") }
    var circQuadril by remember { mutableStateOf("") }
    var circCoxaMedial by remember { mutableStateOf("") }
    var circPanturrilha by remember { mutableStateOf("") }
    var circBracoRelaxado by remember { mutableStateOf("") }
    var circBracoContraido by remember { mutableStateOf("") }
    var circAntebraco by remember { mutableStateOf("") }
    var circPunho by remember { mutableStateOf("") }

    var pregaBiceps by remember { mutableStateOf("") }
    var pregaTriceps by remember { mutableStateOf("") }
    var pregaPeitoral by remember { mutableStateOf("") }
    var pregaAxilarMedia by remember { mutableStateOf("") }
    var pregaSubescapular by remember { mutableStateOf("") }
    var pregaAbdomen by remember { mutableStateOf("") }
    var pregaSuprailiaca by remember { mutableStateOf("") }
    var pregaCoxa by remember { mutableStateOf("") }

    var tmbMetodo by remember { mutableStateOf("Mifflin-St Jeor") }
    var expandedTMB by remember { mutableStateOf(false) }
    val tmbOpcoes = listOf("Mifflin-St Jeor", "Cunningham", "Tinsley")

    var faNivel by remember { mutableStateOf("Sedentário") }
    var expandedFA by remember { mutableStateOf(false) }
    val faOpcoes = listOf("Sedentário", "Pouco Ativo", "Ativo", "Muito Ativo", "Atleta")

    var pgcMetodo by remember { mutableStateOf("Jackson & Pollock 3") }
    var expandedPGC by remember { mutableStateOf(false) }
    val pgcOpcoes = listOf("Jackson & Pollock 3", "Jackson & Pollock 7", "Durnin & Womersley", "Guedes 3")

    var imc by remember { mutableStateOf<Float?>(null) }
    var tmbValor by remember { mutableStateOf<Float?>(null) }
    var getValor by remember { mutableStateOf<Float?>(null) }
    var pgcValor by remember { mutableStateOf<Float?>(null) }
    var pgcClassificacao by remember { mutableStateOf<String?>(null) }
    var mlgValor by remember { mutableStateOf<Float?>(null) }
    var massaMuscular by remember { mutableStateOf<Float?>(null) }
    var rcqValor by remember { mutableStateOf<Float?>(null) }
    var rcqClassificacao by remember { mutableStateOf<String?>(null) }
    var pesoIdealMin by remember { mutableStateOf<Float?>(null) }
    var pesoIdealMax by remember { mutableStateOf<Float?>(null) }

    val camposObrigatorios = remember(pgcMetodo, paciente?.sexo) {
        if (paciente != null) {
            CamposObrigatoriosPGC.obterCamposObrigatorios(pgcMetodo, paciente!!.sexo)
        } else {
            emptySet()
        }
    }

    LaunchedEffect(pacienteId, medidaId) {
        scope.launch {
            paciente = pacienteViewModel.buscarPorId(pacienteId)

            if (medidaId != null) {
                val medidaExistente = medidaViewModel.buscarPorId(medidaId)
                medidaExistente?.let { m ->
                    altura = m.altura.toString()
                    peso = m.peso.toString()

                    circPescoco = m.circPescoco?.toString() ?: ""
                    circOmbro = m.circOmbro?.toString() ?: ""
                    circTorax = m.circTorax?.toString() ?: ""
                    circCintura = m.circCintura?.toString() ?: ""
                    circAbdomen = m.circAbdomen?.toString() ?: ""
                    circQuadril = m.circQuadril?.toString() ?: ""
                    circCoxaMedial = m.circCoxaMedial?.toString() ?: ""
                    circPanturrilha = m.circPanturrilha?.toString() ?: ""
                    circBracoRelaxado = m.circBracoRelaxado?.toString() ?: ""
                    circBracoContraido = m.circBracoContraido?.toString() ?: ""
                    circAntebraco = m.circAntebraco?.toString() ?: ""
                    circPunho = m.circPunho?.toString() ?: ""

                    pregaBiceps = m.pregaBiceps?.toString() ?: ""
                    pregaTriceps = m.pregaTriceps?.toString() ?: ""
                    pregaPeitoral = m.pregaPeitoral?.toString() ?: ""
                    pregaAxilarMedia = m.pregaAxilarMedia?.toString() ?: ""
                    pregaSubescapular = m.pregaSubescapular?.toString() ?: ""
                    pregaAbdomen = m.pregaAbdomen?.toString() ?: ""
                    pregaSuprailiaca = m.pregaSuprailiaca?.toString() ?: ""
                    pregaCoxa = m.pregaCoxa?.toString() ?: ""

                    tmbMetodo = m.tmbMetodo
                    faNivel = m.faNivel
                    pgcMetodo = m.pgcMetodo
                }
            } else {
                val ultimaAltura = medidaViewModel.buscarUltimaAltura(pacienteId)
                if (ultimaAltura != null) {
                    altura = ultimaAltura.toString()
                }
            }
        }
    }

    fun recalcular() {
        val alturaFloat = altura.toFloatOrNull()
        val pesoFloat = peso.toFloatOrNull()

        if (alturaFloat != null && pesoFloat != null && paciente != null) {
            val idade = calcularIdade(paciente!!.dataNascimento)
            val sexo = paciente!!.sexo

            imc = CalculosMedidas.calcularIMC(pesoFloat, alturaFloat)

            val faixaPeso = CalculosMedidas.calcularFaixaPesoIdeal(alturaFloat)
            pesoIdealMin = faixaPeso.first
            pesoIdealMax = faixaPeso.second

            pgcValor = when (pgcMetodo) {
                "Jackson & Pollock 3" -> CalculosMedidas.calcularPGC_JacksonPollock3(
                    pregaPeitoral.toFloatOrNull(),
                    pregaAbdomen.toFloatOrNull(),
                    pregaCoxa.toFloatOrNull(),
                    idade,
                    sexo
                )
                "Jackson & Pollock 7" -> CalculosMedidas.calcularPGC_JacksonPollock7(
                    pregaPeitoral.toFloatOrNull(),
                    pregaAxilarMedia.toFloatOrNull(),
                    pregaTriceps.toFloatOrNull(),
                    pregaSubescapular.toFloatOrNull(),
                    pregaAbdomen.toFloatOrNull(),
                    pregaSuprailiaca.toFloatOrNull(),
                    pregaCoxa.toFloatOrNull(),
                    idade,
                    sexo
                )
                "Durnin & Womersley" -> CalculosMedidas.calcularPGC_DurninWomersley(
                    pregaBiceps.toFloatOrNull(),
                    pregaTriceps.toFloatOrNull(),
                    pregaSubescapular.toFloatOrNull(),
                    pregaSuprailiaca.toFloatOrNull(),
                    idade,
                    sexo
                )
                "Guedes 3" -> CalculosMedidas.calcularPGC_Guedes3(
                    pregaPeitoral.toFloatOrNull(),
                    pregaAbdomen.toFloatOrNull(),
                    pregaCoxa.toFloatOrNull(),
                    pregaTriceps.toFloatOrNull(),
                    pregaSuprailiaca.toFloatOrNull(),
                    sexo
                )
                else -> null
            }

            pgcClassificacao = pgcValor?.let {
                CalculosMedidas.classificarPGC(it, sexo, idade)
            }

            mlgValor = pgcValor?.let {
                CalculosMedidas.calcularMLG(pesoFloat, it)
            }

            massaMuscular = CalculosMedidas.calcularMassaMuscular(pesoFloat, alturaFloat, idade)

            tmbValor = when (tmbMetodo) {
                "Mifflin-St Jeor" -> CalculosMedidas.calcularTMB_MifflinStJeor(
                    pesoFloat,
                    alturaFloat,
                    idade,
                    sexo
                )
                "Cunningham" -> mlgValor?.let {
                    CalculosMedidas.calcularTMB_Cunningham(it)
                }
                "Tinsley" -> CalculosMedidas.calcularTMB_Tinsley(
                    pesoFloat,
                    alturaFloat,
                    idade,
                    sexo,
                    pgcValor
                )
                else -> null
            }

            val faValorFloat = CalculosMedidas.obterValorFA(faNivel)
            getValor = tmbValor?.let {
                CalculosMedidas.calcularGET(it, faValorFloat)
            }

            rcqValor = CalculosMedidas.calcularRCQ(
                circCintura.toFloatOrNull(),
                circQuadril.toFloatOrNull()
            )

            rcqClassificacao = rcqValor?.let {
                CalculosMedidas.classificarRCQ(it, sexo)
            }
        }
    }

    LaunchedEffect(
        altura, peso, tmbMetodo, faNivel, pgcMetodo,
        pregaBiceps, pregaTriceps, pregaPeitoral, pregaAxilarMedia,
        pregaSubescapular, pregaAbdomen, pregaSuprailiaca, pregaCoxa,
        circCintura, circQuadril
    ) {
        recalcular()
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
                            text = if (medidaId != null) "Editar Medida" else "Nova Medida",
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = altura,
                    onValueChange = { altura = it },
                    label = { Text("Altura (cm) *", color = textColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = textColor
                    )
                )

                OutlinedTextField(
                    value = peso,
                    onValueChange = { peso = it },
                    label = { Text("Peso (kg) *", color = textColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = textColor
                    )
                )
            }

            Text(
                text = "Circunferências (cm) - Opcional",
                style = MaterialTheme.typography.titleMedium,
                color = if (isDarkTheme) Color.White else PrimaryGreen,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = circPescoco,
                    onValueChange = { circPescoco = it },
                    label = { Text("Pescoço", color = textColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = textColor
                    )
                )

                OutlinedTextField(
                    value = circOmbro,
                    onValueChange = { circOmbro = it },
                    label = { Text("Ombro", color = textColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = textColor
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = circTorax,
                    onValueChange = { circTorax = it },
                    label = { Text("Tórax", color = textColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = textColor
                    )
                )

                OutlinedTextField(
                    value = circCintura,
                    onValueChange = { circCintura = it },
                    label = { Text("Cintura", color = textColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = textColor
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = circAbdomen,
                    onValueChange = { circAbdomen = it },
                    label = { Text("Abdômen", color = textColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = textColor
                    )
                )

                OutlinedTextField(
                    value = circQuadril,
                    onValueChange = { circQuadril = it },
                    label = { Text("Quadril", color = textColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = textColor
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = circCoxaMedial,
                    onValueChange = { circCoxaMedial = it },
                    label = { Text("Coxa Medial", color = textColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = textColor
                    )
                )

                OutlinedTextField(
                    value = circPanturrilha,
                    onValueChange = { circPanturrilha = it },
                    label = { Text("Panturrilha", color = textColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = textColor
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = circBracoRelaxado,
                    onValueChange = { circBracoRelaxado = it },
                    label = { Text("Braço Relaxado", color = textColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = textColor
                    )
                )

                OutlinedTextField(
                    value = circBracoContraido,
                    onValueChange = { circBracoContraido = it },
                    label = { Text("Braço Contraído", color = textColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = textColor
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = circAntebraco,
                    onValueChange = { circAntebraco = it },
                    label = { Text("Antebraço", color = textColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = textColor
                    )
                )

                OutlinedTextField(
                    value = circPunho,
                    onValueChange = { circPunho = it },
                    label = { Text("Punho", color = textColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = textColor
                    )
                )
            }

            Text(
                text = "Pregas Subcutâneas (mm) - Opcional",
                style = MaterialTheme.typography.titleMedium,
                color = if (isDarkTheme) Color.White else PrimaryGreen,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isBicepsObrigatorio = CamposObrigatoriosPGC.CamposPrega.BICEPS in camposObrigatorios
                OutlinedTextField(
                    value = pregaBiceps,
                    onValueChange = { pregaBiceps = it },
                    label = { Text("Bíceps", color = textColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = textColor
                    ),
                    trailingIcon = if (isBicepsObrigatorio) {
                        { Icon(Icons.Default.Star, "Obrigatório", tint = PrimaryGreen, modifier = Modifier.size(20.dp)) }
                    } else null
                )

                val isTricepsObrigatorio = CamposObrigatoriosPGC.CamposPrega.TRICEPS in camposObrigatorios
                OutlinedTextField(
                    value = pregaTriceps,
                    onValueChange = { pregaTriceps = it },
                    label = { Text("Tríceps", color = textColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = textColor
                    ),
                    trailingIcon = if (isTricepsObrigatorio) {
                        { Icon(Icons.Default.Star, "Obrigatório", tint = PrimaryGreen, modifier = Modifier.size(20.dp)) }
                    } else null
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isPeitoralObrigatorio = CamposObrigatoriosPGC.CamposPrega.PEITORAL in camposObrigatorios
                OutlinedTextField(
                    value = pregaPeitoral,
                    onValueChange = { pregaPeitoral = it },
                    label = { Text("Peitoral", color = textColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = textColor
                    ),
                    trailingIcon = if (isPeitoralObrigatorio) {
                        { Icon(Icons.Default.Star, "Obrigatório", tint = PrimaryGreen, modifier = Modifier.size(20.dp)) }
                    } else null
                )

                val isAxilarObrigatorio = CamposObrigatoriosPGC.CamposPrega.AXILAR_MEDIA in camposObrigatorios
                OutlinedTextField(
                    value = pregaAxilarMedia,
                    onValueChange = { pregaAxilarMedia = it },
                    label = { Text("Axilar Média", color = textColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = textColor
                    ),
                    trailingIcon = if (isAxilarObrigatorio) {
                        { Icon(Icons.Default.Star, "Obrigatório", tint = PrimaryGreen, modifier = Modifier.size(20.dp)) }
                    } else null
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isSubescapularObrigatorio = CamposObrigatoriosPGC.CamposPrega.SUBESCAPULAR in camposObrigatorios
                OutlinedTextField(
                    value = pregaSubescapular,
                    onValueChange = { pregaSubescapular = it },
                    label = { Text("Subescapular", color = textColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = textColor
                    ),
                    trailingIcon = if (isSubescapularObrigatorio) {
                        { Icon(Icons.Default.Star, "Obrigatório", tint = PrimaryGreen, modifier = Modifier.size(20.dp)) }
                    } else null
                )

                val isAbdomenObrigatorio = CamposObrigatoriosPGC.CamposPrega.ABDOMEN in camposObrigatorios
                OutlinedTextField(
                    value = pregaAbdomen,
                    onValueChange = { pregaAbdomen = it },
                    label = { Text("Abdômen", color = textColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = textColor
                    ),
                    trailingIcon = if (isAbdomenObrigatorio) {
                        { Icon(Icons.Default.Star, "Obrigatório", tint = PrimaryGreen, modifier = Modifier.size(20.dp)) }
                    } else null
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isSupraObrigatorio = CamposObrigatoriosPGC.CamposPrega.SUPRAILIACA in camposObrigatorios
                OutlinedTextField(
                    value = pregaSuprailiaca,
                    onValueChange = { pregaSuprailiaca = it },
                    label = { Text("Supra-ilíaca", color = textColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = textColor
                    ),
                    trailingIcon = if (isSupraObrigatorio) {
                        { Icon(Icons.Default.Star, "Obrigatório", tint = PrimaryGreen, modifier = Modifier.size(20.dp)) }
                    } else null
                )

                val isCoxaObrigatorio = CamposObrigatoriosPGC.CamposPrega.COXA in camposObrigatorios
                OutlinedTextField(
                    value = pregaCoxa,
                    onValueChange = { pregaCoxa = it },
                    label = { Text("Coxa", color = textColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = textColor
                    ),
                    trailingIcon = if (isCoxaObrigatorio) {
                        { Icon(Icons.Default.Star, "Obrigatório", tint = PrimaryGreen, modifier = Modifier.size(20.dp)) }
                    } else null
                )
            }

            Text(
                text = "Métodos de Cálculo",
                style = MaterialTheme.typography.titleMedium,
                color = if (isDarkTheme) Color.White else PrimaryGreen,
                fontWeight = FontWeight.Bold
            )

            ExposedDropdownMenuBox(
                expanded = expandedTMB,
                onExpandedChange = { expandedTMB = !expandedTMB }
            ) {
                OutlinedTextField(
                    value = tmbMetodo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Método TMB", color = textColor.copy(alpha = 0.7f)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTMB)
                    },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        disabledTextColor = textColor
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedTMB,
                    onDismissRequest = { expandedTMB = false },
                    modifier = Modifier.exposedDropdownSize().background(Color(0xFF303233))
                ) {
                    tmbOpcoes.forEach { opcao ->
                        DropdownMenuItem(
                            text = { Text(opcao, color = Color.White) },
                            onClick = {
                                tmbMetodo = opcao
                                expandedTMB = false
                            },
                            colors = MenuDefaults.itemColors(textColor = Color.White)
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = expandedFA,
                onExpandedChange = { expandedFA = !expandedFA }
            ) {
                OutlinedTextField(
                    value = faNivel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fator de Atividade", color = textColor.copy(alpha = 0.7f)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFA)
                    },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        disabledTextColor = textColor
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedFA,
                    onDismissRequest = { expandedFA = false },
                    modifier = Modifier.exposedDropdownSize().background(Color(0xFF303233))
                ) {
                    faOpcoes.forEach { opcao ->
                        DropdownMenuItem(
                            text = { Text(opcao, color = Color.White) },
                            onClick = {
                                faNivel = opcao
                                expandedFA = false
                            },
                            colors = MenuDefaults.itemColors(textColor = Color.White)
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = expandedPGC,
                onExpandedChange = { expandedPGC = !expandedPGC }
            ) {
                OutlinedTextField(
                    value = pgcMetodo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Método PGC", color = textColor.copy(alpha = 0.7f)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPGC)
                    },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        disabledTextColor = textColor
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedPGC,
                    onDismissRequest = { expandedPGC = false },
                    modifier = Modifier.exposedDropdownSize().background(Color(0xFF303233))
                ) {
                    pgcOpcoes.forEach { opcao ->
                        DropdownMenuItem(
                            text = { Text(opcao, color = Color.White) },
                            onClick = {
                                pgcMetodo = opcao
                                expandedPGC = false
                            },
                            colors = MenuDefaults.itemColors(textColor = Color.White)
                        )
                    }
                }
            }

            Text(
                text = "Resultados Calculados",
                style = MaterialTheme.typography.titleMedium,
                color = if (isDarkTheme) Color.White else PrimaryGreen,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PrimaryGreen)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    imc?.let {
                        Text(
                            "IMC: %.2f".format(it),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            CalculosMedidas.classificarIMC(it),
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    tmbValor?.let {
                        Text(
                            "TMB: %.0f kcal/dia".format(it),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    getValor?.let {
                        Text(
                            "GET: %.0f kcal/dia".format(it),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    pgcValor?.let { pgc ->
                        Text(
                            "PGC: %.2f%%".format(pgc),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        pgcClassificacao?.let {
                            Text(
                                "Classificação: $it",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    mlgValor?.let {
                        Text(
                            "MLG (Livre de Gordura): %.2f kg".format(it),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    massaMuscular?.let {
                        Text(
                            "Massa Muscular: %.2f kg".format(it),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    rcqValor?.let { rcq ->
                        Text(
                            "RCQ: %.2f".format(rcq),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        rcqClassificacao?.let {
                            Text(
                                "Risco: $it",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (pesoIdealMin != null && pesoIdealMax != null) {
                        Text(
                            "Faixa de Peso Ideal: %.1f - %.1f kg".format(pesoIdealMin, pesoIdealMax),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray,
                        contentColor = Color.White
                    )
                ) {
                    Text("Cancelar", style = MaterialTheme.typography.titleMedium)
                }

                Button(
                    onClick = {
                        val alturaFloat = altura.toFloatOrNull()
                        val pesoFloat = peso.toFloatOrNull()

                        if (alturaFloat != null && pesoFloat != null &&
                            imc != null && tmbValor != null && getValor != null &&
                            pesoIdealMin != null && pesoIdealMax != null) {

                            val dataMedicao = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                            val faValorFloat = CalculosMedidas.obterValorFA(faNivel)

                            val medida = MedidaEntity(
                                id = medidaId ?: "",
                                pacienteId = pacienteId,
                                dataMedicao = dataMedicao,
                                altura = alturaFloat,
                                peso = pesoFloat,
                                circPescoco = circPescoco.toFloatOrNull(),
                                circOmbro = circOmbro.toFloatOrNull(),
                                circTorax = circTorax.toFloatOrNull(),
                                circCintura = circCintura.toFloatOrNull(),
                                circAbdomen = circAbdomen.toFloatOrNull(),
                                circQuadril = circQuadril.toFloatOrNull(),
                                circCoxaProximal = null,
                                circCoxaMedial = circCoxaMedial.toFloatOrNull(),
                                circPanturrilha = circPanturrilha.toFloatOrNull(),
                                circBracoRelaxado = circBracoRelaxado.toFloatOrNull(),
                                circBracoContraido = circBracoContraido.toFloatOrNull(),
                                circAntebraco = circAntebraco.toFloatOrNull(),
                                circPunho = circPunho.toFloatOrNull(),
                                pregaBiceps = pregaBiceps.toFloatOrNull(),
                                pregaTriceps = pregaTriceps.toFloatOrNull(),
                                pregaPeitoral = pregaPeitoral.toFloatOrNull(),
                                pregaAxilarMedia = pregaAxilarMedia.toFloatOrNull(),
                                pregaSubescapular = pregaSubescapular.toFloatOrNull(),
                                pregaAbdomen = pregaAbdomen.toFloatOrNull(),
                                pregaSuprailiaca = pregaSuprailiaca.toFloatOrNull(),
                                pregaCoxa = pregaCoxa.toFloatOrNull(),
                                imc = imc!!,
                                tmbMetodo = tmbMetodo,
                                tmbValor = tmbValor!!,
                                faNivel = faNivel,
                                faValor = faValorFloat,
                                getValor = getValor!!,
                                pgcMetodo = pgcMetodo,
                                pgcValor = pgcValor,
                                pgcClassificacao = pgcClassificacao,
                                mlgKg = mlgValor,
                                rcqValor = rcqValor,
                                rcqClassificacao = rcqClassificacao,
                                pesoIdealMin = pesoIdealMin!!,
                                pesoIdealMax = pesoIdealMax!!
                            )

                            if (medidaId != null) {
                                medidaViewModel.atualizarMedida(medida)
                            } else {
                                medidaViewModel.adicionarMedida(medida)
                            }

                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    enabled = altura.isNotBlank() && peso.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = Color.White,
                        disabledContainerColor = PrimaryGreen.copy(alpha = 0.5f),
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    )
                ) {
                    Text("Salvar", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}