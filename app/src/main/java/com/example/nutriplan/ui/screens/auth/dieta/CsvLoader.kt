package com.example.nutriplan.ui.screens.dieta

import android.content.Context

object CsvLoader {

    fun carregarAlimentos(context: Context): List<AlimentoItem> {
        val lista = mutableListOf<AlimentoItem>()
        val pastaTabelas = context.assets.list("tabelas") ?: return lista

        for (arquivo in pastaTabelas) {
            if (!arquivo.endsWith(".csv")) continue
            try {
                context.assets.open("tabelas/$arquivo").bufferedReader().useLines { lines ->
                    lines.drop(1).forEach { linha ->
                        val cols = linha.split(";")
                        if (cols.size >= 6) {
                            lista.add(
                                AlimentoItem(
                                    nome = cols[0].trim(),
                                    kcal = cols[1].trim().replace(",", ".").toFloatOrNull() ?: 0f,
                                    proteina = cols[2].trim().replace(",", ".").toFloatOrNull() ?: 0f,
                                    carbo = cols[3].trim().replace(",", ".").toFloatOrNull() ?: 0f,
                                    gordura = cols[4].trim().replace(",", ".").toFloatOrNull() ?: 0f,
                                    unidadePadrao = cols[5].trim().ifEmpty { "g" }
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return lista
    }
}
