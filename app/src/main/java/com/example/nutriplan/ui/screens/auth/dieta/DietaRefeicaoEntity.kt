package com.example.nutriplan.data.dieta

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dieta_refeicao")
data class DietaRefeicaoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val planoId: Int,
    val nome: String,
    val horario: String
)
