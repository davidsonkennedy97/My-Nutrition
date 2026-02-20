// CAMINHO: ui/screens/auth/DietaModels.kt
package com.example.nutriplan.ui.dieta

import java.util.UUID

// Modelo simples — será expandido no futuro
data class DietaPlano(
    val id: String = UUID.randomUUID().toString(),
    val pacienteId: String,
    val titulo: String
)
