package com.example.nutriplan.data.taco

import java.text.Normalizer
import java.util.Locale

class TacoRepositoryInMemory : TacoRepository {

    private val foods: List<TacoFood> = TacoDataSource.foods

    override suspend fun buscarPorNome(query: String): List<TacoFood> {
        val q = normalize(query)
        if (q.length < 2) return emptyList()   // IMPORTANTE: tem que ser "<" normal, não "&lt; "

        return foods
            .asSequence()
            .filter { normalize(it.nome).contains(q) }
            .take(40)
            .toList()
    }

    private fun normalize(text: String): String {
        val lower = text.trim().lowercase(Locale.getDefault())
        val normalized = Normalizer.normalize(lower, Normalizer.Form.NFD)

        // Regex em string RAW (""" """) evita "Unsupported escape sequence"
        return normalized.replace(Regex("""\p{InCombiningDiacriticalMarks}+"""), "")
    }
}