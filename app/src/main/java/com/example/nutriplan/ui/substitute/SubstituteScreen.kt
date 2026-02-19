package com.example.nutriplan.ui.substitute

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nutriplan.data.taco.meal.MealItemChoice
import com.example.nutriplan.data.taco.substitute.SubstituteSuggestion
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubstituteScreen(
    target: SubstituteContract.Target,
    viewModel: SubstituteViewModel,
    onApplied: (MealItemChoice) -> Unit,
    onClose: () -> Unit
) {
    val state = viewModel.state.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(target) {
        viewModel.dispatch(SubstituteContract.Action.Start(target))
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is SubstituteContract.Effect.Applied -> onApplied(effect.newChoice)
                is SubstituteContract.Effect.Close -> onClose()
                is SubstituteContract.Effect.Message -> snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = state.target?.displayName() ?: "Substituir") },
                actions = {
                    IconButton(onClick = { onClose() }) {
                        // Sem depender de ícones externos do projeto: usamos texto
                        Text(text = "Fechar", style = MaterialTheme.typography.labelLarge)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            TargetSummary(state = state)

            if (state.allowedSubstitutes.isNotEmpty()) {
                AllowedSubstitutesSection(
                    items = state.allowedSubstitutes,
                    onApplyNow = { choice ->
                        viewModel.dispatch(SubstituteContract.Action.ApplyNow(choice))
                    },
                    onRemove = { foodId ->
                        viewModel.dispatch(SubstituteContract.Action.RemoveAllowedSubstitute(foodId))
                    }
                )
            }

            SubstituteTabs(
                activeTab = state.activeTab,
                onTabChange = { tab -> viewModel.dispatch(SubstituteContract.Action.ChangeTab(tab)) }
            )

            when (state.activeTab) {
                SubstituteContract.Tab.EQUIV_MACROS -> {
                    SuggestionList(
                        title = "Equivalentes por Macros (Top 5)",
                        suggestions = state.suggestionsByMacros,
                        onApplyNow = { suggestion ->
                            val choice = MealItemChoice(suggestion.food, suggestion.portion)
                            viewModel.dispatch(SubstituteContract.Action.ApplyNow(choice))
                        },
                        onAddAllowed = { suggestion ->
                            val choice = MealItemChoice(suggestion.food, suggestion.portion)
                            viewModel.dispatch(SubstituteContract.Action.AddAllowedSubstitute(choice))
                        }
                    )
                }

                SubstituteContract.Tab.EQUIV_MACROS_KCAL -> {
                    SuggestionList(
                        title = "Equivalentes por Macros + Kcal (Top 5)",
                        suggestions = state.suggestionsByMacrosAndKcal,
                        onApplyNow = { suggestion ->
                            val choice = MealItemChoice(suggestion.food, suggestion.portion)
                            viewModel.dispatch(SubstituteContract.Action.ApplyNow(choice))
                        },
                        onAddAllowed = { suggestion ->
                            val choice = MealItemChoice(suggestion.food, suggestion.portion)
                            viewModel.dispatch(SubstituteContract.Action.AddAllowedSubstitute(choice))
                        }
                    )
                }

                SubstituteContract.Tab.PESQUISA_MANUAL -> {
                    ManualSearchSection(
                        query = state.manualQuery,
                        isSearching = state.isSearchingManual,
                        results = state.manualResults,
                        onQueryChange = { v ->
                            viewModel.dispatch(SubstituteContract.Action.ManualQueryChanged(v))
                        },
                        onSearch = {
                            viewModel.dispatch(SubstituteContract.Action.ManualSearchSubmit)
                        },
                        onApplyNow = { choice ->
                            viewModel.dispatch(SubstituteContract.Action.ApplyNow(choice))
                        },
                        onAddAllowed = { choice ->
                            viewModel.dispatch(SubstituteContract.Action.AddAllowedSubstitute(choice))
                        }
                    )
                }
            }

            state.errorMessage?.let { msg ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = msg, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.dispatch(SubstituteContract.Action.ClearError) }) {
                            Text("Ok")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TargetSummary(state: SubstituteContract.UiState) {
    val target = state.target ?: return
    val n = target.currentChoice.nutrients()
    val kcal = n.energyKcal()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Atual: ${target.displayName()}",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Porção: ${target.currentChoice.portion.displayText()}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Alvo → C ${fmt(n.carbsG)}g | P ${fmt(n.proteinG)}g | G ${fmt(n.fatG)}g | ${fmt(kcal)} kcal",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SubstituteTabs(
    activeTab: SubstituteContract.Tab,
    onTabChange: (SubstituteContract.Tab) -> Unit
) {
    val tabs = listOf(
        SubstituteContract.Tab.EQUIV_MACROS to "Macros",
        SubstituteContract.Tab.EQUIV_MACROS_KCAL to "Macros + Kcal",
        SubstituteContract.Tab.PESQUISA_MANUAL to "Manual"
    )

    val selectedIndex = tabs.indexOfFirst { it.first == activeTab }.coerceAtLeast(0)

    TabRow(selectedTabIndex = selectedIndex) {
        tabs.forEachIndexed { index, (tab, label) ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onTabChange(tab) },
                text = { Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            )
        }
    }
}

@Composable
private fun AllowedSubstitutesSection(
    items: List<SubstituteContract.AllowedSubstituteUi>,
    onApplyNow: (MealItemChoice) -> Unit,
    onRemove: (foodId: String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Substitutos permitidos (salvos)", style = MaterialTheme.typography.titleSmall)
            HorizontalDivider()
            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.displayName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${item.portionText} • ${fmt(item.kcal)} kcal",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Button(onClick = { onApplyNow(item.choice) }) {
                        Text("Aplicar")
                    }
                    IconButton(onClick = { onRemove(item.foodId) }) {
                        Text("X")
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionList(
    title: String,
    suggestions: List<SubstituteSuggestion>,
    onApplyNow: (SubstituteSuggestion) -> Unit,
    onAddAllowed: (SubstituteSuggestion) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            HorizontalDivider()

            if (suggestions.isEmpty()) {
                Text(text = "Sem sugestões ainda.", style = MaterialTheme.typography.bodyMedium)
                return@Column
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(suggestions) { s ->
                    SuggestionCard(
                        suggestion = s,
                        onApplyNow = { onApplyNow(s) },
                        onAddAllowed = { onAddAllowed(s) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionCard(
    suggestion: SubstituteSuggestion,
    onApplyNow: () -> Unit,
    onAddAllowed: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = suggestion.food.displayName(),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = "Porção sugerida: ${suggestion.portion.displayText()}")
            Text(
                text = "C ${fmt(suggestion.nutrients.carbsG)}g | P ${fmt(suggestion.nutrients.proteinG)}g | G ${fmt(suggestion.nutrients.fatG)}g | ${fmt(suggestion.kcal)} kcal",
                style = MaterialTheme.typography.bodyMedium
            )

            val deltaK = suggestion.deltaKcal
            val deltaText = buildString {
                append("ΔC ${fmtSigned(suggestion.deltaCarbsG)}g • ")
                append("ΔP ${fmtSigned(suggestion.deltaProteinG)}g • ")
                append("ΔG ${fmtSigned(suggestion.deltaFatG)}g")
                if (deltaK != null) append(" • ΔKcal ${fmtSigned(deltaK)}")
            }
            Text(text = deltaText, style = MaterialTheme.typography.bodySmall)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onApplyNow) { Text("Aplicar agora") }
                Button(onClick = onAddAllowed) { Text("Salvar como substituto") }
            }
        }
    }
}

@Composable
private fun ManualSearchSection(
    query: String,
    isSearching: Boolean,
    results: List<MealItemChoice>,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onApplyNow: (MealItemChoice) -> Unit,
    onAddAllowed: (MealItemChoice) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "Pesquisar manualmente", style = MaterialTheme.typography.titleSmall)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    label = { Text("Buscar alimento (com/sem acento)") }
                )
                Button(onClick = onSearch, enabled = !isSearching) {
                    Text(if (isSearching) "Buscando..." else "Buscar")
                }
            }

            HorizontalDivider()

            if (results.isEmpty()) {
                Text(text = "Nenhum resultado.", style = MaterialTheme.typography.bodyMedium)
                return@Column
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(results) { choice ->
                    ManualResultCard(
                        choice = choice,
                        onApplyNow = { onApplyNow(choice) },
                        onAddAllowed = { onAddAllowed(choice) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ManualResultCard(
    choice: MealItemChoice,
    onApplyNow: () -> Unit,
    onAddAllowed: () -> Unit
) {
    val n = choice.nutrients()
    val kcal = n.energyKcal()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = choice.food.displayName(),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = "Porção padrão: ${choice.portion.displayText()}")
            Text(
                text = "C ${fmt(n.carbsG)}g | P ${fmt(n.proteinG)}g | G ${fmt(n.fatG)}g | ${fmt(kcal)} kcal",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onApplyNow) { Text("Aplicar agora") }
                Button(onClick = onAddAllowed) { Text("Salvar como substituto") }
            }
        }
    }
}

private fun fmt(value: Float): String = ((value * 10f).toInt() / 10f).toString()

private fun fmtSigned(value: Float): String {
    val v = (abs(value) * 10f).toInt() / 10f
    return if (value >= 0f) "+$v" else "-$v"
}