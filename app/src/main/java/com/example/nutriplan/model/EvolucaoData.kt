package com.example.nutriplan.data.model

data class EvolucaoData(
    val data: String,           // Data da medição (dd/MM/yyyy)
    val peso: Float,            // Peso em kg
    val altura: Float,          // Altura em cm
    val imc: Float,             // IMC calculado
    val pgc: Float?,            // % Gordura Corporal
    val massaMuscular: Float?,  // Massa Muscular em kg
    val massaGordura: Float?,   // Massa de Gordura em kg
    val cintura: Float?,        // Circunferência da cintura
    val quadril: Float?,        // Circunferência do quadril
    val bracoRelaxado: Float?,  // Circunferência do braço
    val coxa: Float?,           // Circunferência da coxa
    val get: Float              // Gasto Energético Total
)

data class ComparativoMedidas(
    val medidaInicial: EvolucaoData,
    val medidaFinal: EvolucaoData,
    val diferencaPeso: Float,
    val diferencaPesoPercentual: Float,
    val diferencaIMC: Float,
    val diferencaPGC: Float?,
    val diferencaPGCPercentual: Float?,
    val diferencaMassaMuscular: Float?,
    val diferencaMassaGordura: Float?,
    val diasEntreMedidas: Int
)