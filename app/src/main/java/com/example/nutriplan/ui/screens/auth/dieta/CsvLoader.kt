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
                    val iterator = lines.iterator()
                    if (!iterator.hasNext()) return@useLines

                    // Lê o header e detecta o delimitador automaticamente
                    val headerRaw = iterator.next()
                    val delimiter = if (headerRaw.count { it == ';' } >= headerRaw.count { it == ',' }) ';' else ','
                    val header = headerRaw.split(delimiter).map { it.trim().lowercase()
                        .replace("ã", "a").replace("é", "e").replace("ç", "c")
                        .replace("í", "i").replace("ó", "o").replace("á", "a")
                        .replace("ê", "e").replace("â", "a").replace("õ", "o")
                    }

                    // Mapeia as colunas pelo nome do header
                    val idxNome  = header.indexOfFirst {
                        it.contains("alimento") || it.contains("descri") || it == "nome" || it.contains("food")
                    }
                    val idxKcal  = header.indexOfFirst {
                        it.contains("energia") || it.contains("kcal") || it.contains("caloria") || it.contains("energy")
                    }
                    val idxProt  = header.indexOfFirst {
                        it.contains("prote")
                    }
                    val idxCarbo = header.indexOfFirst {
                        it.contains("carbo")
                    }
                    val idxGord  = header.indexOfFirst {
                        it.contains("lipid") || it.contains("gordura") || it.contains("fat")
                    }
                    val idxUn    = header.indexOfFirst {
                        it.contains("unidade") || it.contains("medida") || it.contains("unit")
                    }

                    // Se não achou coluna de nome, pula esse arquivo
                    if (idxNome < 0) return@useLines

                    iterator.forEach { linha ->
                        if (linha.isBlank()) return@forEach
                        val cols = linha.split(delimiter)
                        val nome = cols.getOrNull(idxNome)?.trim()?.replace("\"", "") ?: return@forEach
                        if (nome.isBlank()) return@forEach

                        lista.add(
                            AlimentoItem(
                                nome = nome,
                                kcal = cols.getOrNull(idxKcal)
                                    ?.trim()?.replace("\"", "")?.replace(",", ".")
                                    ?.toFloatOrNull() ?: 0f,
                                proteina = cols.getOrNull(idxProt)
                                    ?.trim()?.replace("\"", "")?.replace(",", ".")
                                    ?.toFloatOrNull() ?: 0f,
                                carbo = cols.getOrNull(idxCarbo)
                                    ?.trim()?.replace("\"", "")?.replace(",", ".")
                                    ?.toFloatOrNull() ?: 0f,
                                gordura = cols.getOrNull(idxGord)
                                    ?.trim()?.replace("\"", "")?.replace(",", ".")
                                    ?.toFloatOrNull() ?: 0f,
                                unidadePadrao = cols.getOrNull(idxUn)
                                    ?.trim()?.replace("\"", "")?.ifEmpty { "g" } ?: "g"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return lista
    }
}
