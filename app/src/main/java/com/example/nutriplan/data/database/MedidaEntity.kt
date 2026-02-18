package com.example.nutriplan.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import java.util.UUID

@Entity(
    tableName = "medidas",
    foreignKeys = [
        ForeignKey(
            entity = PacienteEntity::class,
            parentColumns = ["id"],
            childColumns = ["paciente_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MedidaEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "paciente_id", index = true)
    val pacienteId: String,

    @ColumnInfo(name = "data_medicao")
    val dataMedicao: String, // Formato: dd/MM/yyyy

    // Dados básicos
    @ColumnInfo(name = "altura") val altura: Float, // cm
    @ColumnInfo(name = "peso") val peso: Float, // kg

    // Circunferências (cm) - TODAS OPCIONAIS
    @ColumnInfo(name = "circ_pescoco") val circPescoco: Float? = null,
    @ColumnInfo(name = "circ_ombro") val circOmbro: Float? = null,
    @ColumnInfo(name = "circ_torax") val circTorax: Float? = null,
    @ColumnInfo(name = "circ_cintura") val circCintura: Float? = null,
    @ColumnInfo(name = "circ_abdomen") val circAbdomen: Float? = null,
    @ColumnInfo(name = "circ_quadril") val circQuadril: Float? = null,
    @ColumnInfo(name = "circ_coxa_proximal") val circCoxaProximal: Float? = null,
    @ColumnInfo(name = "circ_coxa_medial") val circCoxaMedial: Float? = null,
    @ColumnInfo(name = "circ_panturrilha") val circPanturrilha: Float? = null,
    @ColumnInfo(name = "circ_braco_relaxado") val circBracoRelaxado: Float? = null,
    @ColumnInfo(name = "circ_braco_contraido") val circBracoContraido: Float? = null,
    @ColumnInfo(name = "circ_antebraco") val circAntebraco: Float? = null,
    @ColumnInfo(name = "circ_punho") val circPunho: Float? = null,

    // Pregas Subcutâneas (mm) - TODAS OPCIONAIS
    @ColumnInfo(name = "prega_biceps") val pregaBiceps: Float? = null,
    @ColumnInfo(name = "prega_triceps") val pregaTriceps: Float? = null,
    @ColumnInfo(name = "prega_peitoral") val pregaPeitoral: Float? = null,
    @ColumnInfo(name = "prega_axilar_media") val pregaAxilarMedia: Float? = null,
    @ColumnInfo(name = "prega_subescapular") val pregaSubescapular: Float? = null,
    @ColumnInfo(name = "prega_abdomen") val pregaAbdomen: Float? = null,
    @ColumnInfo(name = "prega_suprailiaca") val pregaSuprailiaca: Float? = null,
    @ColumnInfo(name = "prega_coxa") val pregaCoxa: Float? = null,

    // Cálculos
    @ColumnInfo(name = "imc") val imc: Float, // Calculado automaticamente

    // TMB (Taxa Metabólica Basal)
    @ColumnInfo(name = "tmb_metodo") val tmbMetodo: String, // "Mifflin-St Jeor", "Cunningham", "Tinsley"
    @ColumnInfo(name = "tmb_valor") val tmbValor: Float, // kcal/dia

    // FA (Fator de Atividade)
    @ColumnInfo(name = "fa_nivel") val faNivel: String, // "Sedentário", "Pouco Ativo", "Muito Ativo", "Atleta"
    @ColumnInfo(name = "fa_valor") val faValor: Float, // 1.1, 1.35, 1.55, 1.8

    // GET (Gasto Energético Total)
    @ColumnInfo(name = "get_valor") val getValor: Float, // TMB × FA

    // PGC (Percentual de Gordura Corporal)
    @ColumnInfo(name = "pgc_metodo") val pgcMetodo: String, // "Jackson & Pollock 3", "Jackson & Pollock 7", "Durnin & Womersley", "Guedes 3"
    @ColumnInfo(name = "pgc_valor") val pgcValor: Float?, // % (nullable pois depende das pregas)
    @ColumnInfo(name = "pgc_classificacao") val pgcClassificacao: String?, // "Excelente", "Bom", "Médio", "Ruim", "Muito Ruim"

    // MLG (Massa Livre de Gordura)
    @ColumnInfo(name = "mlg_kg") val mlgKg: Float?, // kg (nullable pois depende do PGC)

    // RCQ (Relação Cintura-Quadril)
    @ColumnInfo(name = "rcq_valor") val rcqValor: Float?, // Cintura / Quadril (nullable)
    @ColumnInfo(name = "rcq_classificacao") val rcqClassificacao: String?, // "Baixo", "Moderado", "Alto", "Muito Alto"

    // Faixa de Peso Ideal
    @ColumnInfo(name = "peso_ideal_min") val pesoIdealMin: Float, // kg
    @ColumnInfo(name = "peso_ideal_max") val pesoIdealMax: Float, // kg

    @ColumnInfo(name = "data_criacao") val dataCriacao: Long = System.currentTimeMillis()
)