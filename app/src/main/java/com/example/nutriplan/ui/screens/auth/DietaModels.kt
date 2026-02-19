package com.example.nutriplan.ui.dieta

import com.example.nutriplan.data.taco.Macros
import java.util.UUID

data class DietaItem(
    val id: String = UUID.randomUUID().toString(),
    val foodId: String,
    val foodNome: String,
    val gramas: Float,
    val macros: Macros
)

data class DietaRefeicao(
    val id: String = UUID.randomUUID().toString(),
    val titulo: String,
    val itens: List<DietaItem>
)

data class DietaPlano(
    val id: String = UUID.randomUUID().toString(),
    val pacienteId: String,
    val titulo: String,
    val refeicoes: List<DietaRefeicao>,
    val total: Macros
)