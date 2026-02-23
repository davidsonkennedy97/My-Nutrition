package com.example.nutriplan.data.dieta

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rotina_alimentos",
    indices = [
        Index(value = ["rotinaId"]),
        Index(value = ["alimentoId"])
    ]
)
data class RotinaAlimentoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val rotinaId: Long,
    val alimentoId: Long,
    val quantidade: Double,
    val unidade: String,
    val nomeCustom: String? = null,          // ← nome personalizado pelo usuário
    val createdAt: Long = System.currentTimeMillis()
)
