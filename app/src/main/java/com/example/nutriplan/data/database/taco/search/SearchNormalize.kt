package com.example.nutriplan.data.taco.search

import java.text.Normalizer
import java.util.Locale

object SearchNormalize {

    /**
     * Normaliza texto para busca:
     * - trim
     * - lowercase
     * - remove acentos
     * - colapsa espaços múltiplos
     */
    fun key(text: String): String {
        val lower = text.trim().lowercase(Locale.getDefault())

        val noAccents = Normalizer.normalize(lower, Normalizer.Form.NFD)
            .replace(Regex("""\p{InCombiningDiacriticalMarks}+"""), "")

        return noAccents
            .replace(Regex("""\s+"""), " ")
    }

    /**
     * Retorna true se o 'target' contém o 'query', usando normalização.
     * Ex.: "Feijão branco" contém "feijao"
     */
    fun contains(target: String, query: String): Boolean {
        val q = key(query)
        if (q.isBlank()) return false
        return key(target).contains(q)
    }
}