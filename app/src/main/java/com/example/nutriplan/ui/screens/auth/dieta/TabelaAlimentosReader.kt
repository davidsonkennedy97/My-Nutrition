package com.example.nutriplan.data.dieta

import android.content.Context

data class AlimentoTabela(
    val id: String,
    val nome: String,
    val origem: String,       // ex: "TACO", "TBCA"
    val grupo: String,        // ex: "Cereais e derivados"
    val kcal: Float,
    val proteina: Float,
    val lipidios: Float,
    val carboidrato: Float
)

object TabelaAlimentosReader {

    // Lê TODOS os CSVs da pasta assets/tabelas/ automaticamente
    fun carregarTodas(context: Context): List<AlimentoTabela> {
        val resultado = mutableListOf<AlimentoTabela>()
        val arquivos = context.assets.list("tabelas") ?: return resultado

        arquivos.forEach { arquivo ->
            val origem = arquivo
                .removeSuffix(".csv")
                .uppercase()   // "Taco.csv" → "TACO"

            try {
                val stream = context.assets.open("tabelas/$arquivo")
                val linhas = stream.bufferedReader(Charsets.UTF_8).readLines()
                stream.close()

                linhas.forEach { linha ->
                    val alimento = parseLinhaTaco(linha, origem)
                    if (alimento != null) resultado.add(alimento)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return resultado
    }

    // Busca por nome em todas as tabelas
    fun buscar(context: Context, query: String): List<AlimentoTabela> {
        if (query.length < 2) return emptyList()
        val q = query.lowercase().trim()
        return carregarTodas(context).filter {
            it.nome.lowercase().contains(q)
        }
    }

    // Parser específico para o formato TACO
    private fun parseLinhaTaco(linha: String, origem: String): AlimentoTabela? {
        return try {
            val partes = linha.trim().split("\t")
            if (partes.size < 10) return null

            val id    = partes[0].trim()
            val grupo = partes[1].trim()
            val nome  = partes[2].trim()

            if (nome.isBlank() || id.toIntOrNull() == null) return null

            val kcal  = partes[4].trim().toFloatOrNull() ?: 0f
            val prot  = partes[6].trim().toFloatOrNull() ?: 0f
            val lip   = partes[7].trim().toFloatOrNull() ?: 0f
            val carbo = partes[9].trim().toFloatOrNull() ?: 0f

            AlimentoTabela(
                id           = "${origem}_$id",
                nome         = nome,
                origem       = origem,
                grupo        = grupo,
                kcal         = kcal,
                proteina     = prot,
                lipidios     = lip,
                carboidrato  = carbo
            )
        } catch (e: Exception) {
            null
        }
    }
}
