package com.example.nutriplan.data.taco

import java.text.Normalizer
import java.util.Locale

class TacoRepositoryInMemory : TacoRepository {

    private val foods: List<TacoFood> = TacoDataSource.foods

    // Índices para buscas rápidas
    private val foodsById: Map<String, TacoFood> = foods.associateBy { it.id }
    private val foodsByNormalizedName: Map<String, TacoFood> = foods
        .asSequence()
        .map { food -> normalize(food.nome) to food }
        .distinctBy { it.first }
        .toMap()

    override suspend fun buscarPorNome(query: String): List<TacoFood> {
        val q = normalize(query)
        if (q.length < 2) return emptyList()

        return foods
            .asSequence()
            .filter { normalize(it.nome).contains(q) }
            .take(40)
            .toList()
    }

    /**
     * EXTRA (não faz parte do TacoRepository): busca por ID.
     * Útil quando voltarmos da tela Substituir e precisarmos atualizar o item no editor.
     */
    suspend fun buscarPorId(id: String): TacoFood? {
        val key = id.trim()
        if (key.isBlank()) return null
        return foodsById[key]
    }

    /**
     * EXTRA: tenta achar um alimento pelo nome (match exato após normalização).
     * Útil para fallback quando IDs não batem (TacoFood.id vs FoodItem.id).
     */
    suspend fun buscarPorNomeExato(nome: String): TacoFood? {
        val key = normalize(nome)
        if (key.isBlank()) return null
        return foodsByNormalizedName[key]
    }

    private fun normalize(text: String): String {
        val lower = text.trim().lowercase(Locale.getDefault())
        val normalized = Normalizer.normalize(lower, Normalizer.Form.NFD)

        return normalized
            .replace(Regex("""\p{InCombiningDiacriticalMarks}+"""), "")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }
}