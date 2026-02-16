package com.example.nutriplan.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "pacientes")
data class PacienteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    // Dados básicos
    val nome: String,
    val email: String = "",
    val telefone: String = "",
    val objetivo: String = "",

    // Dados antropométricos
    val pesoAtual: Float = 0f,
    val pesoMeta: Float = 0f,
    val altura: Float = 0f,
    val idade: Int = 0,

    // Status
    val status: String = "Ativo", // Ativo, Inativo, Em tratamento

    // Datas
    val ultimaConsulta: Long = 0L, // timestamp
    val proximaConsulta: Long = 0L, // timestamp

    // Anamnese
    val historicoDoencas: String = "",
    val alergiasAlimentares: String = "",
    val medicamentos: String = "",
    val rotinaExercicios: String = "",

    // Controle
    val dataCriacao: Long = System.currentTimeMillis()
)