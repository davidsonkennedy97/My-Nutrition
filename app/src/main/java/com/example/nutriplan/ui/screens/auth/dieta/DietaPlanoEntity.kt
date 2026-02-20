package com.example.nutriplan.data.dieta

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dieta_plano")
data class DietaPlanoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pacienteId: Int,
    val nome: String
)
