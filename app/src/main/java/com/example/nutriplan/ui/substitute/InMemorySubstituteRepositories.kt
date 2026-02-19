package com.example.nutriplan.ui.substitute

import com.example.nutriplan.data.taco.meal.MealItemChoice
import com.example.nutriplan.data.taco.model.FoodItem
import com.example.nutriplan.data.taco.search.SearchNormalize

class InMemoryFoodCatalogRepository(
    initialFoods: List<FoodItem> = emptyList()
) : FoodCatalogRepository {

    private val foods = initialFoods.toMutableList()

    fun setFoods(newFoods: List<FoodItem>) {
        foods.clear()
        foods.addAll(newFoods)
    }

    fun addFood(food: FoodItem) {
        foods.add(food)
    }

    override suspend fun getAllFoods(): List<FoodItem> = foods.toList()

    override suspend fun searchFoods(query: String, limit: Int): List<FoodItem> {
        val q = query.trim()
        if (q.length < 2) return emptyList()

        return foods
            .asSequence()
            .filter { food ->
                // Busca rica: tenta bater em nome + marca + origem
                val nomeOk = SearchNormalize.contains(food.nome, q)
                val marcaOk = food.marca?.let { SearchNormalize.contains(it, q) } ?: false
                val origemOk = food.origem?.let { SearchNormalize.contains(it, q) } ?: false
                nomeOk || marcaOk || origemOk
            }
            .take(limit)
            .toList()
    }
}

class InMemoryAllowedSubstituteRepository : AllowedSubstituteRepository {

    // chave: "pacienteId::originalFoodId"
    private val map: MutableMap<String, MutableList<MealItemChoice>> = mutableMapOf()

    private fun key(pacienteId: String, originalFoodId: String) = "$pacienteId::$originalFoodId"

    override suspend fun getAllowedSubstitutes(
        pacienteId: String,
        originalFoodId: String
    ): List<MealItemChoice> {
        return map[key(pacienteId, originalFoodId)]?.toList().orEmpty()
    }

    override suspend fun addAllowedSubstitute(
        pacienteId: String,
        originalFoodId: String,
        choice: MealItemChoice
    ) {
        val k = key(pacienteId, originalFoodId)
        val list = map.getOrPut(k) { mutableListOf() }

        // Evita duplicar o mesmo alimento como substituto (pelo id do alimento).
        // Se você quiser permitir o mesmo alimento com 2 porções diferentes, me avise que eu ajusto.
        val idx = list.indexOfFirst { it.food.id == choice.food.id }
        if (idx >= 0) list[idx] = choice else list.add(choice)
    }

    override suspend fun removeAllowedSubstitute(
        pacienteId: String,
        originalFoodId: String,
        substituteFoodId: String
    ) {
        val k = key(pacienteId, originalFoodId)
        val list = map[k] ?: return
        list.removeAll { it.food.id == substituteFoodId }
    }
}