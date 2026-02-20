package com.example.nutriplan.ui.substitute

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.nutriplan.data.taco.meal.MealItemChoice
import com.example.nutriplan.data.taco.model.FoodBaseUnit
import com.example.nutriplan.data.taco.model.FoodItem
import com.example.nutriplan.data.taco.model.Portion
import com.example.nutriplan.data.taco.model.PortionUnit
import java.text.Normalizer

object SubstituteNav {

    // -------- Route / Args --------

    const val ROUTE = "substitute"

    private const val ARG_PACIENTE_ID = "pacienteId"
    private const val ARG_SLOT_KEY = "slotKey"
    private const val ARG_ORIGINAL_FOOD_ID = "originalFoodId"
    private const val ARG_CURRENT_FOOD_ID = "currentFoodId"
    private const val ARG_CURRENT_PORTION_ID = "currentPortionId"
    private const val ARG_CUSTOM_NAME = "customName"

    val routePattern: String =
        "$ROUTE/{$ARG_PACIENTE_ID}/{$ARG_SLOT_KEY}/{$ARG_ORIGINAL_FOOD_ID}/{$ARG_CURRENT_FOOD_ID}/{$ARG_CURRENT_PORTION_ID}?$ARG_CUSTOM_NAME={$ARG_CUSTOM_NAME}"

    fun buildRoute(
        pacienteId: String,
        slotKey: String,
        originalFoodId: String,
        currentFoodId: String,
        currentPortionId: String,
        customName: String?
    ): String {
        val encodedName = customName?.let { Uri.encode(it) } ?: ""
        return "$ROUTE/$pacienteId/$slotKey/$originalFoodId/$currentFoodId/$currentPortionId?$ARG_CUSTOM_NAME=$encodedName"
    }

    fun NavController.navigateToSubstitute(
        pacienteId: String,
        slotKey: String,
        originalFoodId: String,
        currentFoodId: String,
        currentPortionId: String,
        customName: String?
    ) {
        navigate(
            buildRoute(
                pacienteId = pacienteId,
                slotKey = slotKey,
                originalFoodId = originalFoodId,
                currentFoodId = currentFoodId,
                currentPortionId = currentPortionId,
                customName = customName
            )
        )
    }

    // -------- Result keys (SavedStateHandle) --------

    const val RESULT_SLOT_KEY = "substitute_result_slotKey"
    const val RESULT_FOOD_ID = "substitute_result_foodId"
    const val RESULT_PORTION_ID = "substitute_result_portionId"

    // -------- Internal UI state --------

    private sealed interface TargetUiState {
        data object Loading : TargetUiState
        data class Ready(val target: SubstituteContract.Target) : TargetUiState
        data class Error(val message: String) : TargetUiState
    }

    // -------- Destination --------

    fun NavGraphBuilder.substituteDestination(
        navController: NavController,
        viewModelFactory: ViewModelProvider.Factory,
        foodCatalogRepository: FoodCatalogRepository
    ) {
        composable(
            route = routePattern,
            arguments = listOf(
                navArgument(ARG_PACIENTE_ID) { type = NavType.StringType },
                navArgument(ARG_SLOT_KEY) { type = NavType.StringType },
                navArgument(ARG_ORIGINAL_FOOD_ID) { type = NavType.StringType },
                navArgument(ARG_CURRENT_FOOD_ID) { type = NavType.StringType },
                navArgument(ARG_CURRENT_PORTION_ID) { type = NavType.StringType },
                navArgument(ARG_CUSTOM_NAME) {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val pacienteId = backStackEntry.arguments?.getString(ARG_PACIENTE_ID).orEmpty()
            val slotKey = backStackEntry.arguments?.getString(ARG_SLOT_KEY).orEmpty()
            val originalFoodId = backStackEntry.arguments?.getString(ARG_ORIGINAL_FOOD_ID).orEmpty()
            val currentFoodId = backStackEntry.arguments?.getString(ARG_CURRENT_FOOD_ID).orEmpty()
            val currentPortionId = backStackEntry.arguments?.getString(ARG_CURRENT_PORTION_ID).orEmpty()
            val customName = backStackEntry.arguments?.getString(ARG_CUSTOM_NAME).orEmpty().ifBlank { null }

            var retryKey by remember { mutableStateOf(0) }

            // IMPORTANTE: use KEYS POSICIONAIS (compatível com versões mais antigas do Compose)
            val uiState by produceState<TargetUiState>(
                initialValue = TargetUiState.Loading,
                currentFoodId,
                currentPortionId,
                customName,
                retryKey
            ) {
                try {
                    val foods = foodCatalogRepository.getAllFoods()

                    if (foods.isEmpty()) {
                        value = TargetUiState.Error(
                            "Catálogo de alimentos está vazio.\n\n" +
                                    "Verifique se você chamou SubstituteServiceLocator.setFoods(...) " +
                                    "antes de abrir o Substituir."
                        )
                        return@produceState
                    }

                    val foundById = foods.firstOrNull { it.id == currentFoodId }
                    val foundByName = if (foundById == null && !customName.isNullOrBlank()) {
                        val wanted = normalize(customName)
                        foods.firstOrNull { normalize(it.nome) == wanted }
                    } else null

                    val resolvedFood = foundById ?: foundByName

                    if (resolvedFood == null) {
                        value = TargetUiState.Error(
                            buildString {
                                appendLine("Não foi possível carregar a tela de substituição.")
                                appendLine()
                                appendLine("Tentativas:")
                                appendLine("• por ID: currentFoodId=$currentFoodId (não encontrado)")
                                appendLine("• por Nome: customName=${customName ?: "(vazio)"} (não encontrado)")
                                appendLine()
                                appendLine("Itens no catálogo=${foods.size}")
                                appendLine()
                                appendLine("Dica:")
                                appendLine("• Passe customName com o nome do alimento ao navegar")
                                appendLine("• Ou alinhe os IDs TacoFood x FoodItem")
                            }
                        )
                        return@produceState
                    }

                    val portion = portionFromIdOrDefault(resolvedFood, currentPortionId)
                    val choice = MealItemChoice(food = resolvedFood, portion = portion)

                    value = TargetUiState.Ready(
                        SubstituteContract.Target(
                            pacienteId = pacienteId,
                            originalFoodId = originalFoodId,
                            currentChoice = choice,
                            customName = customName
                        )
                    )
                } catch (e: Exception) {
                    value = TargetUiState.Error(
                        "Erro ao montar a tela de substituição: ${e.message ?: "erro desconhecido"}"
                    )
                }
            }

            when (val state = uiState) {
                TargetUiState.Loading -> LoadingFullScreen()

                is TargetUiState.Error -> ErrorFullScreen(
                    message = state.message,
                    onRetry = { retryKey++ },
                    onClose = { navController.popBackStack() }
                )

                is TargetUiState.Ready -> {
                    val vm: SubstituteViewModel = viewModel(factory = viewModelFactory)

                    SubstituteScreen(
                        target = state.target,
                        viewModel = vm,
                        onApplied = { newChoice ->
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set(RESULT_SLOT_KEY, slotKey)

                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set(RESULT_FOOD_ID, newChoice.food.id)

                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set(RESULT_PORTION_ID, newChoice.portion.id)

                            navController.popBackStack()
                        },
                        onClose = { navController.popBackStack() }
                    )
                }
            }
        }
    }

    // -------- Helpers --------

    private fun portionFromIdOrDefault(food: FoodItem, portionId: String): Portion {
        val fromList = food.portions.firstOrNull { it.id == portionId }
        if (fromList != null) return fromList

        if (
            portionId.endsWith("_fallback_100g") ||
            portionId.endsWith("_default_100g") ||
            portionId == "fallback_default_100g"
        ) {
            return Portion(
                id = portionId,
                label = food.nome,
                quantity = 100f,
                unit = PortionUnit.G,
                gramsEquivalent = 100f,
                millilitersEquivalent = null
            )
        }

        if (
            portionId.endsWith("_fallback_100ml") ||
            portionId.endsWith("_default_100ml") ||
            portionId == "fallback_default_100ml"
        ) {
            return Portion(
                id = portionId,
                label = food.nome,
                quantity = 100f,
                unit = PortionUnit.ML,
                gramsEquivalent = null,
                millilitersEquivalent = 100f
            )
        }

        return when (food.baseUnit) {
            FoodBaseUnit.PER_100G -> Portion(
                id = "${food.id}_default_100g",
                label = food.nome,
                quantity = 100f,
                unit = PortionUnit.G,
                gramsEquivalent = 100f,
                millilitersEquivalent = null
            )

            FoodBaseUnit.PER_100ML -> Portion(
                id = "${food.id}_default_100ml",
                label = food.nome,
                quantity = 100f,
                unit = PortionUnit.ML,
                gramsEquivalent = null,
                millilitersEquivalent = 100f
            )
        }
    }

    private fun normalize(s: String): String {
        val noAccents = Normalizer.normalize(s, Normalizer.Form.NFD)
            .replace(Regex("""\p{InCombiningDiacriticalMarks}+"""), "")
        return noAccents
            .lowercase()
            .trim()
            .replace(Regex("""\s+"""), " ")
    }
}

@Composable
private fun LoadingFullScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorFullScreen(
    message: String,
    onRetry: () -> Unit,
    onClose: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = message, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRetry) { Text("Tentar novamente") }
                Button(onClick = onClose) { Text("Voltar") }
            }
        }
    }
}