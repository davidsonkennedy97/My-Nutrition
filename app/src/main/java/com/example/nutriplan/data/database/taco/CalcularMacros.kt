package com.example.nutriplan.data.taco

fun calcularMacros(gramas: Float, food: TacoFood): Macros {
    val g = gramas.coerceAtLeast(0f)
    val fator = g / 100f

    return Macros(
        proteina = food.proteina100g * fator,
        carboidrato = food.carboidrato100g * fator,
        gordura = food.gordura100g * fator
    )
}