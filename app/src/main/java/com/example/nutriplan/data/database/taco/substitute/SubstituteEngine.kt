package com.example.nutriplan.data.taco.substitute

import com.example.nutriplan.data.taco.model.FoodItem
import com.example.nutriplan.data.taco.model.Nutrients
import com.example.nutriplan.data.taco.model.Portion
import com.example.nutriplan.data.taco.model.PortionUnit
import kotlin.math.abs

data class MacroWeights(
    val carbs: Float = 1f,
    val protein: Float = 1f,
    val fat: Float = 1f
)

data class SubstituteSuggestion(
    val food: FoodItem,
    val portion: Portion,
    val nutrients: Nutrients,
    val kcal: Float,
    val deltaCarbsG: Float,
    val deltaProteinG: Float,
    val deltaFatG: Float,
    val deltaKcal: Float?,
    val score: Float
)

object SubstituteEngine {

    fun suggestTopByMacros(
        targetFood: FoodItem,
        targetPortion: Portion,
        candidates: List<FoodItem>,
        limit: Int = 5,
        weights: MacroWeights = MacroWeights(),
        allowFallbackPortion100: Boolean = true
    ): List<SubstituteSuggestion> {
        val targetN = targetFood.nutrientsFor(targetPortion)
        val targetK = targetN.energyKcal()

        val all = candidates
            .asSequence()
            .filter { it.id != targetFood.id }
            .mapNotNull { candidate ->
                bestPortionForCandidate(
                    candidate = candidate,
                    targetN = targetN,
                    targetKcal = targetK,
                    weights = weights,
                    includeKcalInScore = false,
                    allowFallbackPortion100 = allowFallbackPortion100
                )
            }
            .sortedBy { it.score }
            .take(limit)
            .toList()

        return all
    }

    fun suggestTopByMacrosAndKcal(
        targetFood: FoodItem,
        targetPortion: Portion,
        candidates: List<FoodItem>,
        limit: Int = 5,
        weights: MacroWeights = MacroWeights(),
        kcalWeight: Float = 0.25f,
        allowFallbackPortion100: Boolean = true
    ): List<SubstituteSuggestion> {
        val targetN = targetFood.nutrientsFor(targetPortion)
        val targetK = targetN.energyKcal()

        val all = candidates
            .asSequence()
            .filter { it.id != targetFood.id }
            .mapNotNull { candidate ->
                bestPortionForCandidate(
                    candidate = candidate,
                    targetN = targetN,
                    targetKcal = targetK,
                    weights = weights,
                    includeKcalInScore = true,
                    kcalWeight = kcalWeight,
                    allowFallbackPortion100 = allowFallbackPortion100
                )
            }
            .sortedBy { it.score }
            .take(limit)
            .toList()

        return all
    }

    private fun bestPortionForCandidate(
        candidate: FoodItem,
        targetN: Nutrients,
        targetKcal: Float,
        weights: MacroWeights,
        includeKcalInScore: Boolean,
        kcalWeight: Float = 0.25f,
        allowFallbackPortion100: Boolean
    ): SubstituteSuggestion? {
        val portions = candidate.portions.ifEmpty {
            if (!allowFallbackPortion100) return null
            listOf(fallbackPortion100(candidate))
        }

        var best: SubstituteSuggestion? = null

        for (p in portions) {
            val n = runCatching { candidate.nutrientsFor(p) }.getOrNull() ?: continue
            val k = n.energyKcal()

            val dC = n.carbsG - targetN.carbsG
            val dP = n.proteinG - targetN.proteinG
            val dF = n.fatG - targetN.fatG
            val dK = k - targetKcal

            val macroScore =
                weights.carbs * abs(dC) +
                        weights.protein * abs(dP) +
                        weights.fat * abs(dF)

            val score = if (includeKcalInScore) {
                macroScore + kcalWeight * abs(dK)
            } else {
                macroScore
            }

            val suggestion = SubstituteSuggestion(
                food = candidate,
                portion = p,
                nutrients = n,
                kcal = k,
                deltaCarbsG = dC,
                deltaProteinG = dP,
                deltaFatG = dF,
                deltaKcal = if (includeKcalInScore) dK else null,
                score = score
            )

            if (best == null || suggestion.score < best!!.score) {
                best = suggestion
            }
        }

        return best
    }

    private fun fallbackPortion100(food: FoodItem): Portion {
        val isMlBase = food.baseUnit.name.contains("100ML")
        return if (isMlBase) {
            Portion(
                id = "${food.id}_fallback_100ml",
                label = food.nome,
                quantity = 100f,
                unit = PortionUnit.ML,
                gramsEquivalent = null,
                millilitersEquivalent = 100f
            )
        } else {
            Portion(
                id = "${food.id}_fallback_100g",
                label = food.nome,
                quantity = 100f,
                unit = PortionUnit.G,
                gramsEquivalent = 100f,
                millilitersEquivalent = null
            )
        }
    }
}