package com.example.nutriplan.data.dieta

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dieta_item")
data class DietaItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val refeicaoId: Int,
    val nomeAlimento: String,
    val quantidade: Float,
    val unidade: String,
    val kcal: Float,
    val proteina: Float,
    val carbo: Float,
    val gordura: Float
)
