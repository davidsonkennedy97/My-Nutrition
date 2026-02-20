package com.example.nutriplan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nutriplan.data.taco.Macros
import com.example.nutriplan.data.taco.TacoFood
import com.example.nutriplan.data.taco.TacoRepository
import com.example.nutriplan.data.taco.TacoRepositoryInMemory
import com.example.nutriplan.data.taco.calcularMacros
import com.example.nutriplan.data.taco.model.FoodItem
import com.example.nutriplan.ui.dieta.DietaItem
import com.example.nutriplan.ui.dieta.DietaPlano
import com.example.nutriplan.ui.dieta.DietaRefeicao
import com.example.nutriplan.ui.substitute.ObserveSubstituteResult
import com.example.nutriplan.ui.substitute.SubstituteNav
import com.example.nutriplan.ui.substitute.SubstituteResult
import com.example.nutriplan.ui.substitute.SubstituteServiceLocator
import com.example.nutriplan.ui.theme.PrimaryGreen
import com.example.nutriplan.ui.viewmodel.DietaViewModel
import kotlinx.coroutines.launch
import java.util.UUID

private data class ItemRefeicaoUi(
    val id: String = UUID.randomUUID().toString(),
    val alimento: TacoFood,
    val gramas: Float,
    val macros: Macros
)

private data class RefeicaoUi(
    val id: String = UUID.randomUUID().toString(),
    var titulo: String,
    val itens: SnapshotStateList<ItemRefeicaoUi> = mutableStateListOf()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietaEditorScreen(
    pacienteId: String,
    isDarkTheme: Boolean,
    dietaViewModel: DietaViewModel,
    navController: NavController,
    onBack: () -> Unit,
    onSaveAndBackToDietaTab: () -> Unit
) {
    val repo: TacoRepository = remember { TacoRepositoryInMemory() }
    val repoInMemory = repo as? TacoRepositoryInMemory

    val foodCatalogRepo = remember { SubstituteServiceLocator.provideFoodCatalogRepository() }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val screenBg = if (isDarkTheme) Color(0xFF000000) else Color.White
    val textPrimary = if (isDarkTheme) Color.White else Color.Black
    val textSecondary = if (isDarkTheme) Color.White.copy(alpha = 0.70f) else Color.Black.copy(alpha = 0.70f)

    val dialogBg = Color(0xFF000000)
    val dialogText = Color.White
    val dialogTextDim = Color.White.copy(alpha = 0.85f)

    var tituloPlano by remember { mutableStateOf("Dieta") }
    val refeicoes = remember { mutableStateListOf<RefeicaoUi>() }
    var showAddRefeicaoDialog by remember { mutableStateOf(false) }

    // Só pra UI mostrar o último retorno e você ver que aplicou
    val lastSubstituteBySlot = remember { mutableStateMapOf<String, SubstituteResult>() }

    val totalDoDia by remember {
        derivedStateOf {
            var p = 0f
            var c = 0f
            var g = 0f
            refeicoes.forEach { r ->
                r.itens.forEach { item ->
                    p += item.macros.proteina
                    c += item.macros.carboidrato
                    g += item.macros.gordura
                }
            }
            Macros(p, c, g)
        }
    }

    fun salvar() {
        val plano = DietaPlano(
            pacienteId = pacienteId,
            titulo = tituloPlano.trim().ifBlank { "Dieta" },
            refeicoes = refeicoes.map { r ->
                DietaRefeicao(
                    titulo = r.titulo.trim().ifBlank { "Refeição" },
                    itens = r.itens.map { item ->
                        DietaItem(
                            foodId = item.alimento.id,
                            foodNome = item.alimento.nome,
                            gramas = item.gramas,
                            macros = item.macros
                        )
                    }
                )
            },
            total = totalDoDia
        )
        dietaViewModel.salvarPlano(pacienteId, plano)
        onSaveAndBackToDietaTab()
    }

    // ✅ recebe retorno do Substituir e aplica no item
    ObserveSubstituteResult(
        navController = navController,
        onResult = { result ->
            lastSubstituteBySlot[result.slotKey] = result

            scope.launch {
                // 1) achar o alimento selecionado (TacoFood)
                val selectedTacoFood: TacoFood? = run {
                    // tenta por ID no repositório antigo
                    val fromRepo = repoInMemory?.buscarPorId(result.foodId)
                    if (fromRepo != null) return@run fromRepo

                    // tenta por ID no catálogo do Substituir e converte FoodItem -> TacoFood
                    val allFoods = foodCatalogRepo.getAllFoods()
                    val fromCatalog = allFoods.firstOrNull { it.id == result.foodId }
                    fromCatalog?.toTacoFood()
                }

                if (selectedTacoFood == null) {
                    snackbarHostState.showSnackbar(
                        "Não consegui aplicar: alimento selecionado não encontrado (foodId=${result.foodId})."
                    )
                    return@launch
                }

                // 2) achar item pelo slotKey "refeicaoId:itemId"
                val parts = result.slotKey.split(":")
                if (parts.size != 2) {
                    snackbarHostState.showSnackbar("slotKey inválido: ${result.slotKey}")
                    return@launch
                }
                val refeicaoId = parts[0]
                val itemId = parts[1]

                val rIndex = refeicoes.indexOfFirst { it.id == refeicaoId }
                if (rIndex == -1) {
                    snackbarHostState.showSnackbar("Refeição não encontrada para slotKey=${result.slotKey}")
                    return@launch
                }

                val refeicao = refeicoes[rIndex]
                val iIndex = refeicao.itens.indexOfFirst { it.id == itemId }
                if (iIndex == -1) {
                    snackbarHostState.showSnackbar("Item não encontrado para slotKey=${result.slotKey}")
                    return@launch
                }

                val oldItem = refeicao.itens[iIndex]

                // 3) tentar aplicar porção/gramas (se existir no catálogo do Substituir)
                val newGrams: Float = run {
                    val allFoods = foodCatalogRepo.getAllFoods()
                    val foodItem = allFoods.firstOrNull { it.id == result.foodId }
                    val portion = foodItem?.portions?.firstOrNull { it.id == result.portionId }

                    when {
                        portion?.gramsEquivalent != null -> portion.gramsEquivalent
                        isFallback100g(result.portionId) -> 100f
                        else -> oldItem.gramas // fallback seguro
                    }
                }

                // 4) recalcular macros do editor (usa TacoFood por 100g)
                val newMacros = calcularMacros(newGrams, selectedTacoFood)

                // 5) atualizar o item na lista (imutável -> substitui por cópia nova)
                val updated = oldItem.copy(
                    alimento = selectedTacoFood,
                    gramas = newGrams,
                    macros = newMacros
                )
                refeicao.itens[iIndex] = updated

                snackbarHostState.showSnackbar("Substituição aplicada: ${selectedTacoFood.nome}")
            }
        }
    )

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                color = PrimaryGreen
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onBack) { Text("Voltar", color = Color.White) }
                    Text("Criar Dieta", color = Color.White, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { salvar() }) { Text("Salvar", color = Color.White) }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomAppBar(
                containerColor = screenBg,
                contentColor = textPrimary
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TOTAL DO DIA",
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = "P ${"%.1f".format(totalDoDia.proteina)}g • C ${"%.1f".format(totalDoDia.carboidrato)}g • G ${"%.1f".format(totalDoDia.gordura)}g",
                        color = textSecondary
                    )
                }
            }
        }
    ) { padding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBg)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = tituloPlano,
                onValueChange = { tituloPlano = it },
                label = { Text("Título do plano (opcional)", color = textSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textPrimary,
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = textSecondary,
                    focusedLabelColor = textSecondary,
                    unfocusedLabelColor = textSecondary,
                    cursorColor = textPrimary
                )
            )

            Button(
                onClick = { showAddRefeicaoDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.White
                )
            ) {
                Text("+ Adicionar refeição", fontWeight = FontWeight.Bold)
            }

            if (refeicoes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Adicione apenas as refeições que o paciente tiver.\nNada é obrigatório.",
                        color = textSecondary
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    refeicoes.forEachIndexed { index, refeicao ->
                        RefeicaoCard(
                            pacienteId = pacienteId,
                            navController = navController,
                            refeicao = refeicao,
                            repo = repo,
                            lastSubstituteBySlot = lastSubstituteBySlot,
                            onRemoveRefeicao = { refeicoes.removeAt(index) }
                        )
                    }
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }
    }

    if (showAddRefeicaoDialog) {
        AddRefeicaoDialog(
            dialogBg = dialogBg,
            dialogText = dialogText,
            dialogTextDim = dialogTextDim,
            onDismiss = { showAddRefeicaoDialog = false },
            onConfirm = { titulo ->
                refeicoes.add(RefeicaoUi(titulo = titulo))
                showAddRefeicaoDialog = false
            }
        )
    }
}

@Composable
private fun AddRefeicaoDialog(
    dialogBg: Color,
    dialogText: Color,
    dialogTextDim: Color,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val tipos = listOf("Café da manhã", "Lanche", "Almoço", "Jantar", "Ceia", "Personalizada")
    var selecionado by remember { mutableStateOf(tipos.first()) }
    var tituloPersonalizado by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBg,
        title = { Text("Adicionar refeição", color = dialogText, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tipos.forEach { tipo ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selecionado == tipo,
                            onClick = { selecionado = tipo },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = PrimaryGreen,
                                unselectedColor = dialogTextDim
                            )
                        )
                        Text(tipo, color = dialogText)
                    }
                }

                if (selecionado == "Personalizada") {
                    OutlinedTextField(
                        value = tituloPersonalizado,
                        onValueChange = { tituloPersonalizado = it },
                        label = { Text("Título da refeição", color = dialogTextDim) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = dialogText,
                            unfocusedTextColor = dialogText,
                            focusedBorderColor = dialogText,
                            unfocusedBorderColor = dialogTextDim,
                            focusedLabelColor = dialogTextDim,
                            unfocusedLabelColor = dialogTextDim,
                            cursorColor = dialogText
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val titulo = if (selecionado == "Personalizada") {
                        tituloPersonalizado.trim().ifBlank { "Refeição" }
                    } else {
                        selecionado
                    }
                    onConfirm(titulo)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.White
                )
            ) { Text("Adicionar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = dialogText) }
        }
    )
}

@Composable
private fun RefeicaoCard(
    pacienteId: String,
    navController: NavController,
    refeicao: RefeicaoUi,
    repo: TacoRepository,
    lastSubstituteBySlot: Map<String, SubstituteResult>,
    onRemoveRefeicao: () -> Unit
) {
    var showAddFood by remember { mutableStateOf(false) }

    val subtotal by remember {
        derivedStateOf {
            var p = 0f
            var c = 0f
            var g = 0f
            refeicao.itens.forEach { item ->
                p += item.macros.proteina
                c += item.macros.carboidrato
                g += item.macros.gordura
            }
            Macros(p, c, g)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PrimaryGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = refeicao.titulo,
                onValueChange = { refeicao.titulo = it },
                label = { Text("Título da refeição") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.85f),
                    cursorColor = Color.White
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showAddFood = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = PrimaryGreen
                    )
                ) {
                    Text("+ Adicionar alimento", fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = onRemoveRefeicao) {
                    Text("Remover refeição", color = Color.White)
                }
            }

            if (refeicao.itens.isEmpty()) {
                Text("Nenhum alimento nesta refeição.", color = Color.White.copy(alpha = 0.85f))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    refeicao.itens.forEach { item ->
                        val slotKey = "${refeicao.id}:${item.id}"
                        val last = lastSubstituteBySlot[slotKey]

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${item.alimento.nome} • ${item.gramas}g",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "P ${"%.1f".format(item.macros.proteina)}g  C ${"%.1f".format(item.macros.carboidrato)}g  G ${"%.1f".format(item.macros.gordura)}g",
                                    color = Color.White.copy(alpha = 0.9f)
                                )

                                if (last != null) {
                                    Text(
                                        text = "Última escolha: ${last.foodId}",
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                TextButton(
                                    onClick = {
                                        with(SubstituteNav) {
                                            navController.navigateToSubstitute(
                                                pacienteId = pacienteId,
                                                slotKey = slotKey,
                                                originalFoodId = item.alimento.id,
                                                currentFoodId = item.alimento.id,
                                                currentPortionId = "fallback_default_100g",
                                                customName = item.alimento.nome
                                            )
                                        }
                                    }
                                ) { Text("Substituir", color = Color.White) }

                                TextButton(onClick = { refeicao.itens.remove(item) }) {
                                    Text("Remover", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            Divider(color = Color.White.copy(alpha = 0.25f))

            Text(
                text = "Subtotal: P ${"%.1f".format(subtotal.proteina)}g • C ${"%.1f".format(subtotal.carboidrato)}g • G ${"%.1f".format(subtotal.gordura)}g",
                color = Color.White
            )
        }
    }

    if (showAddFood) {
        AddFoodDialog(
            repo = repo,
            dialogBg = Color(0xFF000000),
            dialogText = Color.White,
            dialogTextDim = Color.White.copy(alpha = 0.85f),
            onDismiss = { showAddFood = false },
            onAdd = { food, gramas ->
                val macrosCalculadas = calcularMacros(gramas, food)
                refeicao.itens.add(
                    ItemRefeicaoUi(
                        alimento = food,
                        gramas = gramas,
                        macros = macrosCalculadas
                    )
                )
                showAddFood = false
            }
        )
    }
}

@Composable
private fun AddFoodDialog(
    repo: TacoRepository,
    dialogBg: Color,
    dialogText: Color,
    dialogTextDim: Color,
    onDismiss: () -> Unit,
    onAdd: (TacoFood, Float) -> Unit
) {
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var resultados by remember { mutableStateOf<List<TacoFood>>(emptyList()) }
    var selecionado by remember { mutableStateOf<TacoFood?>(null) }
    var gramasText by remember { mutableStateOf("100") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBg,
        title = { Text("Adicionar alimento", color = dialogText, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it; selecionado = null },
                    label = { Text("Buscar alimento (TACO)", color = dialogTextDim) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = dialogText,
                        unfocusedTextColor = dialogText,
                        focusedBorderColor = dialogText,
                        unfocusedBorderColor = dialogTextDim,
                        focusedLabelColor = dialogTextDim,
                        unfocusedLabelColor = dialogTextDim,
                        cursorColor = dialogText
                    )
                )

                Button(
                    onClick = { scope.launch { resultados = repo.buscarPorNome(query) } },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = Color.White
                    )
                ) { Text("Buscar") }

                if (resultados.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        resultados.forEach { food ->
                            val isSel = selecionado?.id == food.id
                            TextButton(
                                onClick = { selecionado = food },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isSel) "✓ ${food.nome}" else food.nome,
                                    color = if (isSel) PrimaryGreen else dialogText
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Digite pelo menos 2 letras e toque em Buscar.",
                        color = dialogTextDim
                    )
                }

                OutlinedTextField(
                    value = gramasText,
                    onValueChange = { gramasText = it },
                    label = { Text("Gramas", color = dialogTextDim) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = dialogText,
                        unfocusedTextColor = dialogText,
                        focusedBorderColor = dialogText,
                        unfocusedBorderColor = dialogTextDim,
                        focusedLabelColor = dialogTextDim,
                        unfocusedLabelColor = dialogTextDim,
                        cursorColor = dialogText
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val food = selecionado ?: return@Button
                    val g = gramasText.toFloatOrNull() ?: return@Button
                    onAdd(food, g)
                },
                enabled = selecionado != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.White
                )
            ) { Text("Adicionar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = dialogText) }
        }
    )
}

// -------- helpers locais --------

private fun FoodItem.toTacoFood(): TacoFood {
    return TacoFood(
        id = this.id,
        nome = this.nome,
        proteina100g = this.nutrientsBase.proteinG,
        carboidrato100g = this.nutrientsBase.carbsG,
        gordura100g = this.nutrientsBase.fatG
    )
}

private fun isFallback100g(portionId: String): Boolean {
    if (portionId.isBlank()) return true
    return portionId == "fallback_default_100g" ||
            portionId.endsWith("_default_100g") ||
            portionId.endsWith("_fallback_100g")
}