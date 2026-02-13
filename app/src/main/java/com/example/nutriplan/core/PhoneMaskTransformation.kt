package com.example.nutriplan.core

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Máscara para telefone BR no formato: (00) 0 0000-0000
 * - Entrada: só dígitos (ex.: "31987654321")
 * - Exibição: "(31) 9 8765-4321"
 */
class PhoneMaskTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.take(11) // limita a 11 dígitos (DDD + 9 dígitos)
        val out = StringBuilder()

        for (i in digits.indices) {
            when (i) {
                0 -> out.append("(").append(digits[i])
                1 -> out.append(digits[i]).append(") ")
                2 -> out.append(digits[i]).append(" ")
                6 -> out.append(digits[i]).append("-")
                else -> out.append(digits[i])
            }
        }

        // Mapeamento simples (bom o suficiente para MVP).
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val o = offset.coerceIn(0, digits.length)
                return when {
                    o <= 0 -> 0
                    o == 1 -> 2          // "("
                    o == 2 -> 4          // "(dd"
                    o == 3 -> 7          // ") " + espaço após o 9
                    o <= 6 -> o + 5      // inclui "(dd) " + "9 "
                    o <= 10 -> o + 6     // inclui hífen
                    else -> out.length
                }.coerceAtMost(out.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                // Para MVP, deixamos o cursor funcionando bem o suficiente digitando no fim.
                // Se você quiser edição perfeita no meio do texto, depois refinamos.
                return digits.length
            }
        }

        return TransformedText(AnnotatedString(out.toString()), offsetMapping)
    }
}
