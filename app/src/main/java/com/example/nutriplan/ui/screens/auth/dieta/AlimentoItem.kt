package com.example.nutriplan.ui.screens.dieta

data class AlimentoItem(
    val nome: String,
    val kcal: Float,
    val proteina: Float,
    val carbo: Float,
    val gordura: Float,
    val unidadePadrao: String = "g"
)
