package com.example.nutriplan.data.dieta

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rotinas")
data class RotinaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val pacienteId: String,
    val nome: String,
    val horario: String,
    val observacao: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
