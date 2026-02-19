package com.example.nutriplan.data.taco.meal

import com.example.nutriplan.data.taco.model.FoodItem
import com.example.nutriplan.data.taco.model.Nutrients
import com.example.nutriplan.data.taco.model.Portion

/**
 * Uma escolha dentro da refeição = (alimento + porção/medida caseira).
 * Ex.: Arroz cozido + "2 colheres de sopa"
 */
data class MealItemChoice(
    val food: FoodItem,
    val portion: Portion
) {
    fun nutrients(): Nutrients = food.nutrientsFor(portion)
    fun kcal(): Float = nutrients().energyKcal()
}

/**
 * Um substituto permitido para esse slot.
 * Mantém uma escolha completa (alimento + porção humana).
 */
data class AllowedSubstitute(
    val choice: MealItemChoice
)

/**
 * Slot (card) na refeição.
 *
 * - slotKey: identificador estável do slot (ex.: "cafe_01", "almoco_02", UUID etc.)
 * - activeChoice: o que está valendo agora no somatório
 * - customName: nome editável (ex.: "Feijoada Branca")
 * - allowedSubstitutes: lista de alternativas permitidas para o paciente (por alimento)
 */
data class MealSlot(
    val slotKey: String,
    val activeChoice: MealItemChoice,
    val customName: String? = null,
    val allowedSubstitutes: List<AllowedSubstitute> = emptyList()
) {
    fun displayName(): String = customName ?: activeChoice.food.displayName()

    fun nutrientsActive(): Nutrients = activeChoice.nutrients()

    fun kcalActive(): Float = activeChoice.kcal()
}