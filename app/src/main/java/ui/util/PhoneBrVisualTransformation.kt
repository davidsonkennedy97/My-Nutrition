package com.example.nutriplan.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class PhoneBrVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(11)

        val out = buildString {
            for (i in digits.indices) {
                when (i) {
                    0 -> append("(")
                    2 -> append(") ")
                    3 -> append(" ")
                    7 -> append("-")
                }
                append(digits[i])
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val o = offset.coerceIn(0, digits.length)
                return when {
                    o <= 0 -> 0
                    o <= 2 -> o + 1
                    o <= 3 -> o + 3
                    o <= 7 -> o + 4
                    else -> o + 5
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                val t = offset.coerceAtLeast(0)
                return when {
                    t <= 1 -> 0
                    t <= 4 -> (t - 1).coerceAtLeast(0)
                    t <= 8 -> (t - 3).coerceAtLeast(0)
                    t <= 13 -> (t - 4).coerceAtLeast(0)
                    else -> (t - 5).coerceAtLeast(0)
                }.coerceAtMost(digits.length)
            }
        }

        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}
