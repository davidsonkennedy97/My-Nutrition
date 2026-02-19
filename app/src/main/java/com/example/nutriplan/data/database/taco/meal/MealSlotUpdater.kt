package com.example.nutriplan.data.taco.meal

import com.example.nutriplan.data.taco.model.FoodBaseUnit
import com.example.nutriplan.data.taco.model.FoodItem
import com.example.nutriplan.data.taco.model.Portion
import com.example.nutriplan.data.taco.model.PortionUnit

object MealSlotUpdater {

    /**
     * Atualiza um slot identificado por slotKey.
     *
     * @param slots lista de MealSlot (cada slot já contém seu slotKey)
     * @param slotKey chave do slot que será atualizado (vem do retorno da tela Substituir)
     * @param newFoodId id do novo alimento aplicado
     * @param newPortionId id da porção aplicada
     * @param catalog lista de alimentos (catálogo/base)
     */
    fun updateSlot(
        slots: List<MealSlot>,
        slotKey: String,
        newFoodId: String,
        newPortionId: String,
        catalog: List<FoodItem>
    ): List<MealSlot> {
        val newFood = catalog.firstOrNull { it.id == newFoodId } ?: return slots
        val newPortion = resolvePortion(newFood, newPortionId)

        return slots.map { slot ->
            if (slot.slotKey != slotKey) return@map slot

            val newChoice = MealItemChoice(
                food = newFood,
                portion = newPortion
            )

            // Mantém customName e allowedSubstitutes do slot; troca apenas o item ativo
            slot.copy(activeChoice = newChoice)
        }
    }

    private fun resolvePortion(food: FoodItem, portionId: String): Portion {
        // 1) tenta achar na lista real de porções do alimento
        food.portions.firstOrNull { it.id == portionId }?.let { return it }

        // 2) reconhece fallbacks usados pelo engine/orchestrator
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

        // 3) fallback seguro se o id não bater em nada
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