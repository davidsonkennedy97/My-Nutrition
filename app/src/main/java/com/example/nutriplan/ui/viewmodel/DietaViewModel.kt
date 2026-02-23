package com.example.nutriplan.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriplan.data.dieta.AlimentoEntity
import com.example.nutriplan.data.dieta.DietaDatabase
import com.example.nutriplan.data.dieta.RotinaAlimentoComDetalhes
import com.example.nutriplan.data.dieta.RotinaAlimentoEntity
import com.example.nutriplan.data.dieta.RotinaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.security.MessageDigest
import java.text.Normalizer
import java.util.Locale
import kotlin.math.max

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

    private val db = DietaDatabase.getInstance(application)
    private val rotinaDao = db.rotinaDao()
    private val alimentoDao = db.alimentoDao()
    private val rotinaAlimentoDao = db.rotinaAlimentoDao()

    private val _importReady = MutableStateFlow(false)
    val importReady: StateFlow<Boolean> = _importReady

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val totalAntes = runCatching { alimentoDao.countAll() }.getOrDefault(-1)
                Log.d(TAG, "Room (antes): total = $totalAntes")
                importAllFromAssets(assetDir = "tabelas")
                val totalDepois = runCatching { alimentoDao.countAll() }.getOrDefault(-1)
                Log.d(TAG, "Room (depois): total = $totalDepois")
                _importReady.value = true
            } catch (e: Exception) {
                Log.e(TAG, "Falha no import: ${e.message}", e)
                _importReady.value = true
            }
        }
    }

    private suspend fun importAllFromAssets(assetDir: String) {
        val context = getApplication<Application>()
        val files = context.assets.list(assetDir)?.sorted() ?: emptyList()
        val csvFiles = files.filter {
            it.endsWith(".csv", ignoreCase = true) &&
                    !it.contains("Medidas", ignoreCase = true)
        }
        val jsonFiles = files.filter { it.endsWith(".json", ignoreCase = true) }

        Log.d(TAG, "CSV: ${csvFiles.joinToString()} | JSON: ${jsonFiles.joinToString()}")

        for (file in csvFiles) {
            try {
                val origem = file.substringBeforeLast(".").trim().ifEmpty { "Tabela" }
                val items = parseCsvToFoodEntities("$assetDir/$file", origem)
                Log.d(TAG, "CSV $file: ${items.size} itens")
                if (items.isNotEmpty()) items.chunked(500).forEach { alimentoDao.upsertAll(it) }
            } catch (e: Exception) {
                Log.e(TAG, "Erro CSV $file: ${e.message}", e)
            }
        }

        for (file in jsonFiles) {
            try {
                val origem = file.substringBeforeLast(".").trim().ifEmpty { "Tabela" }
                val items = parseJsonToFoodEntities("$assetDir/$file", origem)
                Log.d(TAG, "JSON $file: ${items.size} itens")
                if (items.isNotEmpty()) items.chunked(500).forEach { alimentoDao.upsertAll(it) }
            } catch (e: Exception) {
                Log.e(TAG, "Erro JSON $file: ${e.message}", e)
            }
        }
    }

    // ======= Rotinas =======
    fun getRotinas(pacienteId: String): Flow<List<RotinaEntity>> =
        rotinaDao.getRotinasByPaciente(pacienteId)

    fun addRotina(pacienteId: String, nome: String, horario: String) {
        viewModelScope.launch {
            rotinaDao.insert(RotinaEntity(pacienteId = pacienteId, nome = nome, horario = horario))
        }
    }

    fun updateRotina(rotina: RotinaEntity) {
        viewModelScope.launch { rotinaDao.update(rotina) }
    }

    fun deleteRotina(rotina: RotinaEntity) {
        viewModelScope.launch { rotinaDao.delete(rotina) }
    }

    fun updateObservacaoRotina(rotinaId: Long, observacao: String) {
        viewModelScope.launch { rotinaDao.updateObservacao(rotinaId, observacao) }
    }

    // ======= Busca =======
    fun searchAlimentos(query: String): Flow<List<AlimentoEntity>> {
        val qNorm = normalize(query)
        if (qNorm.isBlank()) return flowOf(emptyList())
        return alimentoDao.searchNormAll(qNorm)
    }

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

    // ======= Itens da Rotina =======
    fun getAlimentosDaRotina(rotinaId: Long): Flow<List<RotinaAlimentoComDetalhes>> =
        rotinaAlimentoDao.getAlimentosDaRotina(rotinaId)

    fun updateQuantidadeItem(id: Long, novaQtd: Double) {
        viewModelScope.launch { rotinaAlimentoDao.updateQuantidade(id, novaQtd) }
    }

    fun updateNomeCustomItem(id: Long, nomeCustom: String?) {
        viewModelScope.launch { rotinaAlimentoDao.updateNomeCustom(id, nomeCustom) }
    }

    fun deleteItemRotina(id: Long) {
        viewModelScope.launch { rotinaAlimentoDao.deleteById(id) }
    }

    // ======= CSV PARSER =======
    private fun parseCsvToFoodEntities(assetPath: String, origem: String): List<AlimentoEntity> {
        val context = getApplication<Application>()
        val bytes = context.assets.open(assetPath).use { it.readBytes() }
        val firstUtf8 = readFirstLine(bytes, Charsets.UTF_8)
        val charset = if (firstUtf8.contains('\uFFFD')) Charsets.ISO_8859_1 else Charsets.UTF_8

        BufferedReader(InputStreamReader(ByteArrayInputStream(bytes), charset)).use { reader ->

            var firstLineRaw = ""
            while (true) {
                val line = reader.readLine() ?: return emptyList()
                val clean = line.replace("\uFEFF", "").trim()
                if (clean.isNotBlank() && clean != ",".repeat(clean.length)) {
                    firstLineRaw = clean
                    break
                }
            }

            val delimiter = detectDelimiter(firstLineRaw)
            val header = splitCsvLine(firstLineRaw, delimiter).map { cleanHeader(it) }
            if (header.isEmpty()) return emptyList()

            Log.d(TAG, "[$origem] Header: ${header.take(10).joinToString()}")

            val firstDataLine = reader.readLine()
            val firstDataCols = firstDataLine?.let { splitCsvLine(it, delimiter) }

            var idxNome = findIndex(
                header,
                "descricao do alimento", "descrição do alimento",
                "descricao dos alimentos", "descrição dos alimentos",
                "descricao", "descrição",
                "alimento", "nome", "produto", "item", "description", "food", "name"
            )
            if (idxNome == null) {
                idxNome = guessNomeIndex(firstDataCols)
                Log.w(TAG, "[$origem] idxNome por heurística=$idxNome")
            }

            val idxEnergia  = findIndex(header, "energia kcal", "energia (kcal)", "energia..kcal.", "energia", "kcal", "calorias", "valor energetico", "energy", "energy kcal", "energy_kcal")
            val idxProteina = findIndex(header, "proteina g", "proteina (g)", "proteina..g.", "proteina", "protein g", "protein_g", "protein")
            val idxLipidios = findIndex(header, "lipideos totais g", "lipideos totais (g)", "lipideos..g.", "lipidios", "lipideos", "gordura", "gorduras", "lipid", "fat total", "fat_total_g", "fat")
            val idxCarbo    = findIndex(header, "carboidrato g", "carboidrato (g)", "carboidratos..g.", "carboidrato", "carboidratos", "carbo", "carbohydrate g", "carbohydrate_g", "carbohydrate", "carbs")
            val idxQtd      = findIndex(header, "quantidade", "porcao", "qtd", "amount", "portion")
            val idxUn       = findIndex(header, "unidade", "medida", "unid", "unit")

            Log.d(TAG, "[$origem] idx → nome=$idxNome kcal=$idxEnergia prot=$idxProteina lip=$idxLipidios carb=$idxCarbo")

            if (idxNome == null) {
                Log.e(TAG, "[$origem] Coluna de nome não encontrada. Header=${header.joinToString()}")
                return emptyList()
            }

            val list = ArrayList<AlimentoEntity>(2000)

            if (!firstDataLine.isNullOrBlank()) {
                parseOneCsvRow(
                    cols = firstDataCols ?: emptyList(), origem = origem,
                    idxNome = idxNome, idxEnergia = idxEnergia,
                    idxProteina = idxProteina, idxLipidios = idxLipidios,
                    idxCarbo = idxCarbo, idxQtd = idxQtd, idxUn = idxUn
                )?.let { list.add(it) }
            }

            while (true) {
                val line = reader.readLine() ?: break
                if (line.isBlank()) continue
                parseOneCsvRow(
                    cols = splitCsvLine(line, delimiter), origem = origem,
                    idxNome = idxNome, idxEnergia = idxEnergia,
                    idxProteina = idxProteina, idxLipidios = idxLipidios,
                    idxCarbo = idxCarbo, idxQtd = idxQtd, idxUn = idxUn
                )?.let { list.add(it) }
            }

            return list
        }
    }

    private fun parseOneCsvRow(
        cols: List<String>, origem: String,
        idxNome: Int, idxEnergia: Int?, idxProteina: Int?,
        idxLipidios: Int?, idxCarbo: Int?, idxQtd: Int?, idxUn: Int?
    ): AlimentoEntity? {
        val nome = col(cols, idxNome)?.trim()?.trim('"').orEmpty()
        if (nome.isBlank()) return null
        val alimentoNorm   = normalize(nome)
        val quantidadeBase = parseDouble(col(cols, idxQtd)) ?: 100.0
        val unidadeBase    = col(cols, idxUn)?.trim()?.trim('"')?.ifBlank { null }
            ?: if (alimentoNorm.contains("agua")) "ml" else "g"
        return AlimentoEntity(
            id = stableIdSha256(origem, alimentoNorm), origem = origem,
            alimento = nome, alimentoNorm = alimentoNorm,
            quantidadeBase = quantidadeBase, unidadeBase = unidadeBase,
            proteina     = parseDouble(col(cols, idxProteina)) ?: 0.0,
            lipidios     = parseDouble(col(cols, idxLipidios)) ?: 0.0,
            carboidratos = parseDouble(col(cols, idxCarbo))    ?: 0.0,
            calorias     = parseDouble(col(cols, idxEnergia))  ?: 0.0
        )
    }

    private fun guessNomeIndex(firstDataCols: List<String>?): Int? {
        if (firstDataCols.isNullOrEmpty()) return null
        var bestIdx = 0; var bestScore = Int.MIN_VALUE
        for (i in firstDataCols.indices) {
            val v = firstDataCols[i].trim().trim('"')
            if (v.isBlank()) continue
            val score = (v.count { it.isLetter() } * 3) - (v.count { it.isDigit() } * 2) + minOf(v.length, 40)
            if (score > bestScore) { bestScore = score; bestIdx = i }
        }
        return bestIdx
    }

    // ======= JSON PARSER =======
    private fun parseJsonToFoodEntities(assetPath: String, origem: String): List<AlimentoEntity> {
        val context  = getApplication<Application>()
        val jsonText = context.assets.open(assetPath).use {
            InputStreamReader(it, Charsets.UTF_8).readText()
        }.replace("\uFEFF", "")

        val arr = extractJSONArray(JSONTokener(jsonText).nextValue()) ?: return emptyList()
        val list = ArrayList<AlimentoEntity>(arr.length())

        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val nome = firstNonBlank(obj.optString("name"), obj.optString("alimento"), obj.optString("descricao"), obj.optString("descrição"), obj.optString("description"), obj.optString("food"), obj.optString("foodName"))
            if (nome.isBlank()) continue
            val alimentoNorm = normalize(nome)
            list.add(AlimentoEntity(
                id = stableIdSha256(origem, alimentoNorm), origem = origem,
                alimento = nome, alimentoNorm = alimentoNorm,
                quantidadeBase = parseDouble(firstNonBlank(obj.optString("portion_g"), obj.optString("quantidadeBase"), obj.optString("quantidade"), obj.optString("amount"), obj.optString("servingSize"))) ?: 100.0,
                unidadeBase    = firstNonBlank(obj.optString("unidadeBase"), obj.optString("unidade"), obj.optString("unit")).ifBlank { "g" },
                calorias       = parseDouble(firstNonBlank(obj.optString("energy_kcal"), obj.optString("kcal"), obj.optString("calorias"), obj.optString("energia"), obj.optString("energy"), obj.optString("calories"))) ?: 0.0,
                proteina       = parseDouble(firstNonBlank(obj.optString("protein_g"), obj.optString("proteina"), obj.optString("proteína"), obj.optString("protein"))) ?: 0.0,
                lipidios       = parseDouble(firstNonBlank(obj.optString("fat_total_g"), obj.optString("lipidios"), obj.optString("lipídios"), obj.optString("gordura"), obj.optString("fat"), obj.optString("lipid"))) ?: 0.0,
                carboidratos   = parseDouble(firstNonBlank(obj.optString("carbohydrate_g"), obj.optString("carboidratos"), obj.optString("carboidrato"), obj.optString("carbs"), obj.optString("carbohydrate"), obj.optString("carbo"))) ?: 0.0
            ))
        }
        return list
    }

    private fun extractJSONArray(root: Any?): JSONArray? {
        return when (root) {
            is JSONArray  -> root
            is JSONObject -> listOf("data","foods","alimentos","items","FoundationFoods","SRLegacyFoods","SurveyFoods","BrandedFoods")
                .firstNotNullOfOrNull { k -> root.opt(k) as? JSONArray }
            else -> null
        }
    }

    private fun firstNonBlank(vararg values: String?): String =
        values.firstOrNull { !it.isNullOrBlank() }?.trim()?.trim('"') ?: ""

    // ======= UTILITÁRIOS =======
    private fun cleanHeader(raw: String): String =
        raw.trim().trim('"').replace("\uFEFF","")
            .replace(Regex("\\(.*?\\)"), "")
            .replace(Regex("\\.{2,}[a-zA-Z]*\\.?$"), "")
            .trim().let { normalize(it) }

    private fun readFirstLine(bytes: ByteArray, charset: java.nio.charset.Charset): String =
        BufferedReader(InputStreamReader(ByteArrayInputStream(bytes), charset)).use { it.readLine() ?: "" }

    private fun col(cols: List<String>, idx: Int?): String? = idx?.let { cols.getOrNull(it) }

    private fun detectDelimiter(line: String): Char {
        val s = line.count { it == ';' }; val c = line.count { it == ',' }
        return if (s >= max(1, c)) ';' else ','
    }

    private fun splitCsvLine(line: String, delimiter: Char): List<String> {
        val result = ArrayList<String>(); val sb = StringBuilder(); var inQuotes = false; var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"'                    -> inQuotes = !inQuotes
                c == delimiter && !inQuotes -> { result.add(sb.toString()); sb.setLength(0) }
                else                        -> sb.append(c)
            }
            i++
        }
        result.add(sb.toString()); return result
    }

    private fun parseDouble(raw: String?): Double? {
        val cleaned = raw?.trim()?.trim('"')?.replace("%","")?.replace("kcal","",ignoreCase=true)?.replace(" ","") ?: return null
        if (cleaned.isBlank() || cleaned.equals("NA",ignoreCase=true) || cleaned == "-" || cleaned == "*") return null
        return cleaned.replace(",",".").toDoubleOrNull()
    }

    private fun findIndex(headers: List<String>, vararg aliases: String): Int? {
        for (alias in aliases) { val t = normalize(alias); val i = headers.indexOfFirst { it == t }; if (i >= 0) return i }
        for (alias in aliases) { val t = normalize(alias); val i = headers.indexOfFirst { it.contains(t) }; if (i >= 0) return i }
        return null
    }

    private fun normalize(s: String): String {
        val nfd = Normalizer.normalize(s, Normalizer.Form.NFD)
        val sb = StringBuilder(nfd.length)
        for (ch in nfd) if (Character.getType(ch) != Character.NON_SPACING_MARK.toInt()) sb.append(ch)
        return sb.toString().lowercase(Locale.ROOT).trim()
    }

    private fun stableIdSha256(origem: String, alimentoNorm: String): Long {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest("$origem|$alimentoNorm".toByteArray(Charsets.UTF_8))
        var value = 0L
        for (i in 0 until 8) value = (value shl 8) or (bytes[i].toLong() and 0xffL)
        return value and Long.MAX_VALUE
    }

} // ← fecha DietaViewModel
