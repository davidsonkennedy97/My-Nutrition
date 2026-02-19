package com.example.nutriplan.data.taco.importer

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.Normalizer

data class TacoRow(
    val numero: String,
    val grupo: String,
    val descricao: String,
    val energiaKcal100g: Float,
    val proteinaG100g: Float,
    val lipideosG100g: Float,
    val carboidratoG100g: Float,
    val fibraG100g: Float,
    val sodioMg100g: Float,
    val potassioMg100g: Float
)

object TacoAssetLoader {

    /**
     * Lê o Taco.csv dos assets e retorna linhas normalizadas.
     * - Trata "Tr", vazio e "-" como 0
     * - Aceita decimal com vírgula (ex.: "12,3")
     * - Detecta automaticamente se o separador é ';' ou ','
     */
    suspend fun load(
        context: Context,
        assetFileName: String = "Taco.csv"
    ): List<TacoRow> = withContext(Dispatchers.IO) {

        val input = context.assets.open(assetFileName)
        BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { reader ->
            val allLines = reader.readLines()
            if (allLines.isEmpty()) return@withContext emptyList()

            val headerLine = allLines.first()
            val delimiter = detectDelimiter(headerLine)

            val headers = parseCsvLine(headerLine, delimiter)
                .map { normalizeHeader(it) }

            fun idxOf(vararg candidates: String): Int {
                val normalizedCandidates = candidates.map { normalizeHeader(it) }
                return headers.indexOfFirst { it in normalizedCandidates }
            }

            val iNumero = idxOf("numero", "número", "n").let { if (it >= 0) it else 0 }
            val iGrupo = idxOf("grupo", "grupo_alimentar")
            val iDesc = idxOf(
                "descricao_do_alimento",
                "descrição_do_alimento",
                "descricao",
                "descrição",
                "alimento"
            )

            val iKcal = idxOf("energia_kcal_", "energia_kcal", "energia_kcal_100g", "energia")
            val iProt = idxOf("proteina_g_", "proteina_g", "proteina", "proteína")
            val iLip = idxOf("lipideos_g_", "lipideos_g", "lipideos", "lipídeos", "gordura")
            val iCarb = idxOf("carboidrato_g_", "carboidrato_g", "carboidrato", "carboidratos")
            val iFibra = idxOf("fibra_alimentar_g_", "fibra_alimentar_g", "fibra_alimentar", "fibra")
            val iSodio = idxOf("sodio_mg_", "sodio_mg", "sodio", "sódio")
            val iPotassio = idxOf("potassio_mg_", "potassio_mg", "potassio", "potássio")

            // sem descrição ou energia, o CSV não está no formato esperado
            if (iDesc < 0 || iKcal < 0) return@withContext emptyList()

            val dataLines = allLines
                .drop(1)
                .filter { it.isNotBlank() }

            dataLines.mapNotNull { line ->
                val cols = parseCsvLine(line, delimiter)
                fun get(i: Int): String = if (i in cols.indices) cols[i].trim() else ""

                val numero = get(iNumero)
                val grupo = if (iGrupo >= 0) get(iGrupo) else ""
                val desc = get(iDesc)
                if (desc.isBlank()) return@mapNotNull null

                TacoRow(
                    numero = numero,
                    grupo = grupo,
                    descricao = desc,
                    energiaKcal100g = parseFloatFlexible(get(iKcal)),
                    proteinaG100g = if (iProt >= 0) parseFloatFlexible(get(iProt)) else 0f,
                    lipideosG100g = if (iLip >= 0) parseFloatFlexible(get(iLip)) else 0f,
                    carboidratoG100g = if (iCarb >= 0) parseFloatFlexible(get(iCarb)) else 0f,
                    fibraG100g = if (iFibra >= 0) parseFloatFlexible(get(iFibra)) else 0f,
                    sodioMg100g = if (iSodio >= 0) parseFloatFlexible(get(iSodio)) else 0f,
                    potassioMg100g = if (iPotassio >= 0) parseFloatFlexible(get(iPotassio)) else 0f
                )
            }
        }
    }

    private fun detectDelimiter(headerLine: String): Char {
        val semicolons = headerLine.count { it == ';' }
        val commas = headerLine.count { it == ',' }
        return if (semicolons >= commas) ';' else ','
    }

    private fun normalizeHeader(raw: String): String {
        val noAccents = Normalizer.normalize(raw, Normalizer.Form.NFD)
            .replace(Regex("""\p{InCombiningDiacriticalMarks}+"""), "")
        return noAccents
            .lowercase()
            .replace(Regex("""[^a-z0-9]+"""), "_")
            .trim('_')
    }

    /**
     * Parser de linha CSV com aspas.
     * Funciona com separador ';' ou ','.
     */
    private fun parseCsvLine(line: String, delimiter: Char): List<String> {
        val out = ArrayList<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' -> {
                    val nextIsQuote = (i + 1 < line.length && line[i + 1] == '"')
                    if (inQuotes && nextIsQuote) {
                        sb.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == delimiter && !inQuotes -> {
                    out.add(sb.toString())
                    sb.setLength(0)
                }
                else -> sb.append(c)
            }
            i++
        }
        out.add(sb.toString())
        return out
    }

    /**
     * Trata:
     * - vazio / "-" / "Tr" -> 0
     * - decimal com vírgula -> converte para ponto
     */
    private fun parseFloatFlexible(raw: String): Float {
        val s = raw.trim()
        if (s.isEmpty()) return 0f
        if (s.equals("tr", ignoreCase = true)) return 0f
        if (s == "-") return 0f

        val normalized = s.replace(" ", "").replace(",", ".")
        return normalized.toFloatOrNull() ?: 0f
    }
}