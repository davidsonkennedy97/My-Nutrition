package com.example.nutriplan.data.dieta

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "alimentos",
    indices = [
        Index(value = ["alimentoNorm"]),
        Index(value = ["origem", "alimentoNorm"], unique = true)
    ]
)
data class AlimentoEntity(
    @PrimaryKey val id: Long,         // ID estável (hash origem+nome)
    val origem: String,               // ex.: "Taco" (nome do arquivo sem .csv)
    val alimento: String,             // texto original
    val alimentoNorm: String,         // sem acento + lowercase
    val quantidadeBase: Double,       // ex.: 100
    val unidadeBase: String,          // ex.: g / ml (se não vier, usa heurística/padrão)
    val proteina: Double,
    val lipidios: Double,
    val carboidratos: Double,
    val calorias: Double
)