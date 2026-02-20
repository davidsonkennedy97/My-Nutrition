package com.example.nutriplan.data.dieta

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// ─── Plano de dieta do paciente ───────────────────────────────
@Entity(tableName = "dieta_plano")
data class DietaPlanoEntity(
    @PrimaryKey val id: String,
    val pacienteId: String,
    val titulo: String,           // ex: "Plano Semana 1"
    val dataCriacao: String       // ex: "20/02/2026"
)

// ─── Cada refeição dentro do plano ────────────────────────────
@Entity(
    tableName = "dieta_refeicao",
    foreignKeys = [ForeignKey(
        entity = DietaPlanoEntity::class,
        parentColumns = ["id"],
        childColumns = ["planoId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class DietaRefeicaoEntity(
    @PrimaryKey val id: String,
    val planoId: String,
    val nome: String,             // ex: "Café da manhã"
    val horario: String           // ex: "08:00"
)

// ─── Cada alimento dentro da refeição ─────────────────────────
@Entity(
    tableName = "dieta_item",
    foreignKeys = [ForeignKey(
        entity = DietaRefeicaoEntity::class,
        parentColumns = ["id"],
        childColumns = ["refeicaoId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class DietaItemEntity(
    @PrimaryKey val id: String,
    val refeicaoId: String,
    val alimentoNome: String,     // ex: "Arroz cozido"
    val origem: String,           // ex: "TACO", "TBCA", "Receita"
    val quantidade: Float,        // ex: 100f
    val unidade: String,          // ex: "g", "ml", "porção"
    val proteina: Float,
    val lipidios: Float,
    val carboidrato: Float,
    val calorias: Float
)
