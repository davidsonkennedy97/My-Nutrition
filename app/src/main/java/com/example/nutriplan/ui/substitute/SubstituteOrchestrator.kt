package com.example.nutriplan.ui.substitute

import com.example.nutriplan.data.taco.meal.MealItemChoice
import com.example.nutriplan.data.taco.model.FoodBaseUnit
import com.example.nutriplan.data.taco.model.FoodItem
import com.example.nutriplan.data.taco.model.Portion
import com.example.nutriplan.data.taco.model.PortionUnit
import com.example.nutriplan.data.taco.substitute.MacroWeights
import com.example.nutriplan.data.taco.substitute.SubstituteEngine

class SubstituteOrchestrator(
    private val foodCatalogRepository: FoodCatalogRepository,
    private val allowedSubstituteRepository: AllowedSubstituteRepository
) {

    suspend fun start(
        target: SubstituteContract.Target,
        weights: MacroWeights = MacroWeights(),
        kcalWeight: Float = 0.25f
    ): SubstituteContract.UiState {
        val allFoods = foodCatalogRepository.getAllFoods()

        val allowed = allowedSubstituteRepository
            .getAllowedSubstitutes(target.pacienteId, target.originalFoodId)
            .map { SubstituteContract.AllowedSubstituteUi(choice = it) }

        val byMacros = SubstituteEngine.suggestTopByMacros(
            targetFood = target.currentChoice.food,
            targetPortion = target.currentChoice.portion,
            candidates = allFoods,
            limit = 5,
            weights = weights,
            allowFallbackPortion100 = true
        )

        val byMacrosKcal = SubstituteEngine.suggestTopByMacrosAndKcal(
            targetFood = target.currentChoice.food,
            targetPortion = target.currentChoice.portion,
            candidates = allFoods,
            limit = 5,
            weights = weights,
            kcalWeight = kcalWeight,
            allowFallbackPortion100 = true
        )

        return SubstituteContract.UiState(
            target = target,
            isLoading = false,
            errorMessage = null,
            allowedSubstitutes = allowed,
            suggestionsByMacros = byMacros,
            suggestionsByMacrosAndKcal = byMacrosKcal,
            manualQuery = "",
            isSearchingManual = false,
            manualResults = emptyList()
        )
    }

    suspend fun refreshSuggestions(
        state: SubstituteContract.UiState,
        weights: MacroWeights = MacroWeights(),
        kcalWeight: Float = 0.25f
    ): SubstituteContract.UiState {
        val target = state.target ?: return state.copy(errorMessage = "Target não definido.")
        val allFoods = foodCatalogRepository.getAllFoods()

        val byMacros = SubstituteEngine.suggestTopByMacros(
            targetFood = target.currentChoice.food,
            targetPortion = target.currentChoice.portion,
            candidates = allFoods,
            limit = 5,
            weights = weights,
            allowFallbackPortion100 = true
        )

        val byMacrosKcal = SubstituteEngine.suggestTopByMacrosAndKcal(
            targetFood = target.currentChoice.food,
            targetPortion = target.currentChoice.portion,
            candidates = allFoods,
            limit = 5,
            weights = weights,
            kcalWeight = kcalWeight,
            allowFallbackPortion100 = true
        )

        return state.copy(
            suggestionsByMacros = byMacros,
            suggestionsByMacrosAndKcal = byMacrosKcal
        )
    }

    suspend fun manualSearch(
        state: SubstituteContract.UiState,
        limit: Int = 50
    ): SubstituteContract.UiState {
        val target = state.target ?: return state.copy(errorMessage = "Target não definido.")

        val q = state.manualQuery.trim()
        if (q.length < 2) {
            return state.copy(
                isSearchingManual = false,
                manualResults = emptyList(),
                errorMessage = null
            )
        }

        val foods = foodCatalogRepository.searchFoods(q, limit)

        val results = foods
            .asSequence()
            .map { food ->
                val portion = defaultPortionFor(food)
                MealItemChoice(food = food, portion = portion)
            }
            .toList()

        return state.copy(
            isSearchingManual = false,
            manualResults = results,
            errorMessage = null,
            target = target
        )
    }

    fun onManualQueryChanged(
        state: SubstituteContract.UiState,
        value: String
    ): SubstituteContract.UiState {
        return state.copy(
            manualQuery = value,
            errorMessage = null
        )
    }

    suspend fun addAllowedSubstitute(
        state: SubstituteContract.UiState,
        choice: MealItemChoice
    ): SubstituteContract.UiState {
        val target = state.target ?: return state.copy(errorMessage = "Target não definido.")

        allowedSubstituteRepository.addAllowedSubstitute(
            pacienteId = target.pacienteId,
            originalFoodId = target.originalFoodId,
            choice = choice
        )

        val allowed = allowedSubstituteRepository
            .getAllowedSubstitutes(target.pacienteId, target.originalFoodId)
            .map { SubstituteContract.AllowedSubstituteUi(choice = it) }

        return state.copy(
            allowedSubstitutes = allowed,
            errorMessage = null
        )
    }

    suspend fun removeAllowedSubstitute(
        state: SubstituteContract.UiState,
        substituteFoodId: String
    ): SubstituteContract.UiState {
        val target = state.target ?: return state.copy(errorMessage = "Target não definido.")

        allowedSubstituteRepository.removeAllowedSubstitute(
            pacienteId = target.pacienteId,
            originalFoodId = target.originalFoodId,
            substituteFoodId = substituteFoodId
        )

        val allowed = allowedSubstituteRepository
            .getAllowedSubstitutes(target.pacienteId, target.originalFoodId)
            .map { SubstituteContract.AllowedSubstituteUi(choice = it) }

        return state.copy(
            allowedSubstitutes = allowed,
            errorMessage = null
        )
    }

    fun applyNow(choice: MealItemChoice): SubstituteContract.Effect {
        // Sua regra 2B: aplicar com a porção sugerida (choice já vem com porção humana)
        return SubstituteContract.Effect.Applied(newChoice = choice)
    }

    private fun defaultPortionFor(food: FoodItem): Portion {
        if (food.portions.isNotEmpty()) return food.portions.first()

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

/**
 * Repositório do “catálogo” de alimentos (TACO / sua base).
 * Observação: a busca deve ser tolerante a acentos (feijão/feijao),
 * o ideal é implementar usando SearchNormalize.contains().
 */
interface FoodCatalogRepository {
    suspend fun getAllFoods(): List<FoodItem>
    suspend fun searchFoods(query: String, limit: Int = 50): List<FoodItem>
}

/**
 * Persistência dos substitutos permitidos:
 * - chave: (pacienteId + originalFoodId)
 * - valor: lista de MealItemChoice (alimento + porção humana)
 */
interface AllowedSubstituteRepository {
    suspend fun getAllowedSubstitutes(pacienteId: String, originalFoodId: String): List<MealItemChoice>
    suspend fun addAllowedSubstitute(pacienteId: String, originalFoodId: String, choice: MealItemChoice)
    suspend fun removeAllowedSubstitute(pacienteId: String, originalFoodId: String, substituteFoodId: String)
}