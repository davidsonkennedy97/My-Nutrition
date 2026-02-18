package com.example.nutriplan.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.UUID

@Entity(tableName = "pacientes")
data class PacienteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    // Dados básicos
    @ColumnInfo(name = "nome") val nome: String,
    @ColumnInfo(name = "apelido") val apelido: String = "",
    @ColumnInfo(name = "sexo") val sexo: String = "",
    @ColumnInfo(name = "email") val email: String = "",
    @ColumnInfo(name = "telefone") val telefone: String = "",
    @ColumnInfo(name = "data_nascimento") val dataNascimento: String = "",
    @ColumnInfo(name = "objetivo") val objetivo: String = "",
    @ColumnInfo(name = "status") val status: String = "Ativo",
    @ColumnInfo(name = "data_cadastro") val dataCadastro: String = "",

    // Dados antropométricos (mantidos para compatibilidade)
    @ColumnInfo(name = "peso_atual") val pesoAtual: Float = 0f,
    @ColumnInfo(name = "peso_meta") val pesoMeta: Float = 0f,
    @ColumnInfo(name = "altura") val altura: Float = 0f,
    @ColumnInfo(name = "idade") val idade: Int = 0,

    // Anamnese (mantidos para compatibilidade)
    @ColumnInfo(name = "historico_doencas") val historicoDoencas: String = "",
    @ColumnInfo(name = "alergias_alimentares") val alergiasAlimentares: String = "",
    @ColumnInfo(name = "medicamentos") val medicamentos: String = "",
    @ColumnInfo(name = "rotina_exercicios") val rotinaExercicios: String = "",

    // Datas (mantidos para compatibilidade)
    @ColumnInfo(name = "ultima_consulta") val ultimaConsulta: Long = 0L,
    @ColumnInfo(name = "proxima_consulta") val proximaConsulta: Long = 0L,
    @ColumnInfo(name = "data_criacao") val dataCriacao: Long = System.currentTimeMillis()
)