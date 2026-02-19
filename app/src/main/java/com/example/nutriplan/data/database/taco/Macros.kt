package com.example.nutriplan.data.taco

data class Macros(
    val proteina: Float = 0f,
    val carboidrato: Float = 0f,
    val gordura: Float = 0f
) {
    operator fun plus(other: Macros): Macros = Macros(
        proteina = proteina + other.proteina,
        carboidrato = carboidrato + other.carboidrato,
        gordura = gordura + other.gordura
    )
}