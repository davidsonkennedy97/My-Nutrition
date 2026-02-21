package com.example.nutriplan.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriplan.data.dieta.AlimentoEntity
import com.example.nutriplan.data.dieta.DietaDatabase
import com.example.nutriplan.data.dieta.RotinaAlimentoEntity
import com.example.nutriplan.data.dieta.RotinaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.security.MessageDigest
import java.text.Normalizer
import kotlin.math.max

// (mantido caso outras telas usem isso)
data class DietaPlano(val id: String, val pacienteId: String, val nome: String)

data class DietaRefeicao(
    val id: String,
    val planoId: String,
    val nome: String,
    val horario: String,
    val itens: List<DietaItem> = emptyList()
)

data class DietaItem(
    val id: String,
    val nome: String,
    val quantidade: String,
    val calorias: Double
)

class DietaViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "DietaImport"

    // ======= (mantido - se alguma tela do seu app usa isso) =======
    private val _planos = mutableStateOf(listOf<DietaPlano>())
    val planos: State<List<DietaPlano>> = _planos

    private val _refeicoes = mutableStateOf(listOf<DietaRefeicao>())
    val refeicoes: State<List<DietaRefeicao>> = _refeicoes

    fun criarPlano(pacienteId: String, nome: String) {
        val id = "plano_${System.currentTimeMillis()}"
        _planos.value += DietaPlano(id, pacienteId, nome)
    }

    fun deletarPlano(id: String) {
        _planos.value = _planos.value.filter { it.id != id }
        _refeicoes.value = _refeicoes.value.filter { it.planoId != id }
    }

    fun criarRefeicao(planoId: String, nome: String, horario: String) {
        val id = "ref_${System.currentTimeMillis()}"
        _refeicoes.value += DietaRefeicao(id, planoId, nome, horario)
    }

    fun deletarRefeicao(id: String) {
        _refeicoes.value = _refeicoes.value.filter { it.id != id }
    }

    fun deletarItem(refeicaoId: String, itemId: String) {
        _refeicoes.value = _refeicoes.value.map { r ->
            if (r.id == refeicaoId) r.copy(itens = r.itens.filter { it.id != itemId }) else r
        }
    }

    // ======= (Room) =======
    private val db = DietaDatabase.getInstance(application)
    private val rotinaDao = db.rotinaDao()
    private val alimentoDao = db.alimentoDao()
    private val rotinaAlimentoDao = db.rotinaAlimentoDao()

    // ======= Rotinas =======
    fun getRotinas(pacienteId: String): Flow<List<RotinaEntity>> =
        rotinaDao.getRotinasByPaciente(pacienteId)

    fun addRotina(pacienteId: String, nome: String, horario: String) {
        viewModelScope.launch {
            rotinaDao.insert(
                RotinaEntity(
                    pacienteId = pacienteId,
                    nome = nome,
                    horario = horario
                )
            )
        }
    }

    fun updateRotina(rotina: RotinaEntity) {
        viewModelScope.launch { rotinaDao.update(rotina) }
    }

    fun deleteRotina(rotina: RotinaEntity) {
        viewModelScope.launch { rotinaDao.delete(rotina) }
    }

    // ======= Busca de alimentos (5 sugestões, parcial, sem acento, case-insensitive) =======
    fun searchAlimentos(query: String): Flow<List<AlimentoEntity>> {
        val qNorm = normalize(query)
        if (qNorm.isBlank()) return flowOf(emptyList())
        return alimentoDao.searchNorm(qNorm, limit = 5)
    }

    // Clique no resultado: adiciona direto, silencioso
    fun addAlimentoNaRotina(rotinaId: Long, alimento: AlimentoEntity) {
        viewModelScope.launch {
            rotinaAlimentoDao.insert(
                RotinaAlimentoEntity(
                    rotinaId = rotinaId,
                    alimentoId = alimento.id,
                    quantidade = alimento.quantidadeBase,
                    unidade = alimento.unidadeBase
                )
            )
        }
    }

    // Importa todos os CSV de assets/tabela/
    fun importAllFoodTablesFromAssets() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                importAllCsvFromAssets()

                // LOG FINAL: quantos alimentos tem no Room
                try {
                    val total = alimentoDao.countAll()
                    Log.d(TAG, "Total de alimentos no Room: $total")
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao contar alimentos: ${e.message}", e)
                }
            }
        }
    }

    private suspend fun importAllCsvFromAssets() {
        val context = getApplication<Application>()
        val assetDir = "tabela"

        val files = context.assets.list(assetDir)
            ?.filter { it.endsWith(".csv", ignoreCase = true) }
            ?.sorted()
            ?: emptyList()

        Log.d(TAG, "CSV encontrados em assets/$assetDir: ${files.joinToString()}")

        if (files.isEmpty()) return

        for (file in files) {
            val origem = file.substringBeforeLast(".").trim().ifEmpty { "Tabela" }
            val path = "$assetDir/$file"

            val items = parseCsvToFoodEntities(assetPath = path, origem = origem)

            Log.d(TAG, "Importando $file (origem=$origem). Itens lidos do CSV: ${items.size}")

            if (items.isNotEmpty()) {
                alimentoDao.upsertAll(items)
            }
        }
    }

    // ---------------- CSV PARSER ----------------

    private fun parseCsvToFoodEntities(assetPath: String, origem: String): List<AlimentoEntity> {
        val context = getApplication<Application>()

        context.assets.open(assetPath).use { input ->
            val reader = BufferedReader(InputStreamReader(input, Charsets.UTF_8))
            val firstLineRaw = reader.readLine() ?: return emptyList()

            val delimiter = detectDelimiter(firstLineRaw)

            // remove BOM (às vezes o primeiro header vem com \uFEFF)
            val header = splitCsvLine(firstLineRaw, delimiter)
                .map { it.trim().trim('"').replace("\uFEFF", "") }

            if (header.isEmpty()) return emptyList()

            val idxNome = findIndex(header, "alimento", "descricao", "descrição", "descricao do alimento", "nome", "description", "food")
            val idxEnergia = findIndex(header, "energia", "kcal", "calorias", "valor energetico", "valor energético", "energy")
            val idxProteina = findIndex(header, "proteina", "proteína", "protein")
            val idxLipidios = findIndex(header, "lipidios", "lipídios", "lipideos", "lipídeos", "gordura", "gorduras", "lipid", "fat")
            val idxCarbo = findIndex(header, "carboidrato", "carboidratos", "carbo", "carbohydrate", "carbs")

            val idxQtd = findIndex(header, "quantidade", "porcao", "porção", "porcao base", "porção base", "qtd", "amount")
            val idxUn = findIndex(header, "unidade", "medida", "unid", "unit")

            Log.d(
                TAG,
                "Header OK. idxNome=$idxNome idxEnergia=$idxEnergia idxProteina=$idxProteina idxLipidios=$idxLipidios idxCarbo=$idxCarbo idxQtd=$idxQtd idxUn=$idxUn"
            )

            if (idxNome == null) {
                Log.e(TAG, "Não achei coluna de NOME no CSV ($assetPath). Header: ${header.joinToString()}")
                return emptyList()
            }

            val list = ArrayList<AlimentoEntity>(2000)

            var line: String?
            while (true) {
                line = reader.readLine() ?: break
                if (line.isBlank()) continue

                val cols = splitCsvLine(line, delimiter)

                val nome = col(cols, idxNome)?.trim()?.trim('"').orEmpty()
                if (nome.isBlank()) continue

                val alimentoNorm = normalize(nome)

                val quantidadeBase = parseDouble(col(cols, idxQtd)) ?: 100.0
                val unidadeFromCsv = col(cols, idxUn)?.trim()?.trim('"')?.ifBlank { null }

                val unidadeBase = unidadeFromCsv ?: if (alimentoNorm.contains("agua")) "ml" else "g"

                val calorias = parseDouble(col(cols, idxEnergia)) ?: 0.0
                val proteina = parseDouble(col(cols, idxProteina)) ?: 0.0
                val lipidios = parseDouble(col(cols, idxLipidios)) ?: 0.0
                val carbo = parseDouble(col(cols, idxCarbo)) ?: 0.0

                val id = stableIdSha256(origem, alimentoNorm)

                list.add(
                    AlimentoEntity(
                        id = id,
                        origem = origem,
                        alimento = nome,
                        alimentoNorm = alimentoNorm,
                        quantidadeBase = quantidadeBase,
                        unidadeBase = unidadeBase,
                        proteina = proteina,
                        lipidios = lipidios,
                        carboidratos = carbo,
                        calorias = calorias
                    )
                )
            }

            return list
        }
    }

    // resolve Int? -> Int
    private fun col(cols: List<String>, idx: Int?): String? =
        idx?.let { cols.getOrNull(it) }

    private fun detectDelimiter(line: String): Char {
        val semicolons = line.count { it == ';' }
        val commas = line.count { it == ',' }
        return if (semicolons >= max(1, commas)) ';' else ','
    }

    private fun splitCsvLine(line: String, delimiter: Char): List<String> {
        val result = ArrayList<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                inQuotes = !inQuotes
            } else if (c == delimiter && !inQuotes) {
                result.add(sb.toString())
                sb.setLength(0)
            } else {
                sb.append(c)
            }
            i++
        }

        result.add(sb.toString())
        return result
    }

    private fun parseDouble(raw: String?): Double? {
        if (raw == null) return null
        val cleaned = raw.trim().trim('"')
            .replace("%", "")
            .replace("kcal", "", ignoreCase = true)
            .replace(" ", "")
        if (cleaned.isBlank()) return null

        return cleaned.replace(",", ".").toDoubleOrNull()
    }

    private fun findIndex(headers: List<String>, vararg aliases: String): Int? {
        val normHeaders = headers.map { normalize(it) }

        for (alias in aliases) {
            val target = normalize(alias)
            val exact = normHeaders.indexOfFirst { it == target }
            if (exact >= 0) return exact
        }

        for (alias in aliases) {
            val target = normalize(alias)
            val contains = normHeaders.indexOfFirst { it.contains(target) }
            if (contains >= 0) return contains
        }

        return null
    }

    // ✅ remove acentos SEM regex (sem chance de "Unsupported escape sequence")
    private fun normalize(s: String): String {
        val nfd = Normalizer.normalize(s, Normalizer.Form.NFD)
        val sb = StringBuilder(nfd.length)
        for (ch in nfd) {
            val type = Character.getType(ch)
            if (type != Character.NON_SPACING_MARK.toInt()) {
                sb.append(ch)
            }
        }
        return sb.toString().lowercase().trim()
    }

    private fun stableIdSha256(origem: String, alimentoNorm: String): Long {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest("$origem|$alimentoNorm".toByteArray(Charsets.UTF_8))

        var value = 0L
        for (i in 0 until 8) {
            value = (value shl 8) or (bytes[i].toLong() and 0xffL)
        }
        return value and Long.MAX_VALUE
    }
}