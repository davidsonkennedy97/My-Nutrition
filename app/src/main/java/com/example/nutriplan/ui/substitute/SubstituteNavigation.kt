package com.example.nutriplan.ui.substitute

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
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

object SubstituteNav {

    // -------- Route / Args --------

    const val ROUTE = "substitute"

    private const val ARG_PACIENTE_ID = "pacienteId"
    private const val ARG_SLOT_KEY = "slotKey"
    private const val ARG_ORIGINAL_FOOD_ID = "originalFoodId"
    private const val ARG_CURRENT_FOOD_ID = "currentFoodId"
    private const val ARG_CURRENT_PORTION_ID = "currentPortionId"
    private const val ARG_CUSTOM_NAME = "customName"

    // Ex.: substitute/{pacienteId}/{slotKey}/{originalFoodId}/{currentFoodId}/{currentPortionId}?customName=...
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

            val targetState by produceState<SubstituteContract.Target?>(initialValue = null) {
                val food = foodCatalogRepository.getAllFoods().firstOrNull { it.id == currentFoodId }
                if (food == null) {
                    value = null
                    return@produceState
                }
                val portion = portionFromIdOrDefault(food, currentPortionId)
                val choice = MealItemChoice(food = food, portion = portion)

                value = SubstituteContract.Target(
                    pacienteId = pacienteId,
                    originalFoodId = originalFoodId,
                    currentChoice = choice,
                    customName = customName
                )
            }

            if (targetState == null) {
                LoadingFullScreen()
                return@composable
            }

            val vm = androidx.lifecycle.viewmodel.compose.viewModel<SubstituteViewModel>(
                factory = viewModelFactory
            )

            SubstituteScreen(
                target = targetState!!,
                viewModel = vm,
                onApplied = { newChoice ->
                    // Retorna para a tela anterior usando IDs simples
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
                onClose = {
                    navController.popBackStack()
                }
            )
        }
    }

    // -------- Helpers --------

    private fun portionFromIdOrDefault(food: FoodItem, portionId: String): Portion {
        val fromList = food.portions.firstOrNull { it.id == portionId }
        if (fromList != null) return fromList

        // Suporta os fallbacks gerados pelo engine/orchestrator
        if (portionId.endsWith("_fallback_100g") || portionId.endsWith("_default_100g")) {
            return Portion(
                id = portionId,
                label = food.nome,
                quantity = 100f,
                unit = PortionUnit.G,
                gramsEquivalent = 100f,
                millilitersEquivalent = null
            )
        }
        if (portionId.endsWith("_fallback_100ml") || portionId.endsWith("_default_100ml")) {
            return Portion(
                id = portionId,
                label = food.nome,
                quantity = 100f,
                unit = PortionUnit.ML,
                gramsEquivalent = null,
                millilitersEquivalent = 100f
            )
        }

        // Default seguro
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
}

@Composable
private fun LoadingFullScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}