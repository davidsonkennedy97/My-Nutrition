package com.example.nutriplan.utils

import com.example.nutriplan.data.database.MedidaEntity
import com.example.nutriplan.data.model.EvolucaoData
import com.example.nutriplan.data.model.ComparativoMedidas
import java.text.SimpleDateFormat
import java.util.*

object EvolucaoHelper {

    fun converterMedidasParaEvolucao(
        medidas: List<MedidaEntity>,
        idadePaciente: Int
    ): List<EvolucaoData> {
        return medidas.map { medida ->
            val massaMuscular = CalculosMedidas.calcularMassaMuscular(
                medida.peso,
                medida.altura,
                idadePaciente
            )

            val massaGordura = medida.pgcValor?.let {
                CalculosMedidas.calcularMassaGordura(medida.peso, it)
            }

            EvolucaoData(
                data = medida.dataMedicao,
                peso = medida.peso,
                altura = medida.altura,
                imc = medida.imc,
                pgc = medida.pgcValor,
                massaMuscular = massaMuscular,
                massaGordura = massaGordura,
                cintura = medida.circCintura,
                quadril = medida.circQuadril,
                bracoRelaxado = medida.circBracoRelaxado,
                coxa = medida.circCoxaMedial,
                get = medida.getValor
            )
        }.sortedBy { parseData(it.data) }
    }

    fun calcularComparativo(
        medidaInicial: EvolucaoData,
        medidaFinal: EvolucaoData
    ): ComparativoMedidas {
        val diferencaPeso = medidaFinal.peso - medidaInicial.peso
        val diferencaPesoPercentual = (diferencaPeso / medidaInicial.peso) * 100

        val diferencaIMC = medidaFinal.imc - medidaInicial.imc

        val diferencaPGC = if (medidaInicial.pgc != null && medidaFinal.pgc != null) {
            medidaFinal.pgc - medidaInicial.pgc
        } else null

        val diferencaPGCPercentual = if (medidaInicial.pgc != null && medidaFinal.pgc != null) {
            ((medidaFinal.pgc - medidaInicial.pgc) / medidaInicial.pgc) * 100
        } else null

        val diferencaMassaMuscular = if (medidaInicial.massaMuscular != null && medidaFinal.massaMuscular != null) {
            medidaFinal.massaMuscular - medidaInicial.massaMuscular
        } else null

        val diferencaMassaGordura = if (medidaInicial.massaGordura != null && medidaFinal.massaGordura != null) {
            medidaFinal.massaGordura - medidaInicial.massaGordura
        } else null

        val diasEntreMedidas = calcularDiasEntreMedidas(medidaInicial.data, medidaFinal.data)

        return ComparativoMedidas(
            medidaInicial = medidaInicial,
            medidaFinal = medidaFinal,
            diferencaPeso = diferencaPeso,
            diferencaPesoPercentual = diferencaPesoPercentual,
            diferencaIMC = diferencaIMC,
            diferencaPGC = diferencaPGC,
            diferencaPGCPercentual = diferencaPGCPercentual,
            diferencaMassaMuscular = diferencaMassaMuscular,
            diferencaMassaGordura = diferencaMassaGordura,
            diasEntreMedidas = diasEntreMedidas
        )
    }

    private fun parseData(dataStr: String): Date {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.parse(dataStr) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }

    private fun calcularDiasEntreMedidas(dataInicial: String, dataFinal: String): Int {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateInicial = sdf.parse(dataInicial)
            val dateFinal = sdf.parse(dataFinal)

            if (dateInicial != null && dateFinal != null) {
                val diffMillis = dateFinal.time - dateInicial.time
                (diffMillis / (1000 * 60 * 60 * 24)).toInt()
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }

    fun formatarDiferenca(valor: Float, unidade: String = ""): String {
        val sinal = if (valor > 0) "+" else ""
        return "$sinal%.2f $unidade".format(valor).trim()
    }

    fun formatarDiferencaPercentual(valor: Float): String {
        val sinal = if (valor > 0) "+" else ""
        return "$sinal%.1f%%".format(valor)
    }
}