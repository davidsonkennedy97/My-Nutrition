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

    // ======= (mantido - telas antigas usam isso) =======
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

    // ======= Import 1x por sessão =======
    @Volatile private var importStarted = false

    init {
        ensureImportFromAssetsStarted()
    }

    /** Sua tela chama isso. Mantive o nome. */
    fun importAllFoodTablesFromAssets() {
        ensureImportFromAssetsStarted()
    }

    /** Importa CSV + JSON de assets/tabelas (1x por sessão). */
    fun ensureImportFromAssetsStarted() {
        if (importStarted) return
        importStarted = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val totalAntes = runCatching { alimentoDao.countAll() }.getOrDefault(-1)
                Log.d(TAG, "Room (antes): total de alimentos = $totalAntes")

                importAllFromAssets(assetDir = "tabelas") // ✅ CORRIGIDO: era "tabela"

                val totalDepois = runCatching { alimentoDao.countAll() }.getOrDefault(-1)
                Log.d(TAG, "Room (depois): total de alimentos = $totalDepois")
            } catch (e: Exception) {
                Log.e(TAG, "Falha no import geral: ${e.message}", e)
            }
        }
    }

    private suspend fun importAllFromAssets(assetDir: String) {
        val context = getApplication<Application>()
        val files = context.assets.list(assetDir)?.sorted() ?: emptyList()
        val csvFiles = files.filter { it.endsWith(".csv", ignoreCase = true) }
        val jsonFiles = files.filter { it.endsWith(".json", ignoreCase = true) }

        Log.d(TAG, "Arquivos em assets/$assetDir: ${files.joinToString()}")
        Log.d(TAG, "CSV: ${csvFiles.joinToString()} | JSON: ${jsonFiles.joinToString()}")

        // 1) CSV
        for (file in csvFiles) {
            try {
                val origem = file.substringBeforeLast(".").trim().ifEmpty { "Tabela" }
                val path = "$assetDir/$file"
                val items = parseCsvToFoodEntities(assetPath = path, origem = origem)
                Log.d(TAG, "CSV $file (origem=$origem): itens lidos=${items.size}")
                if (items.isNotEmpty()) {
                    items.chunked(500).forEach { chunk -> alimentoDao.upsertAll(chunk) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Falha importando CSV $file: ${e.message}", e)
            }
        }

        // 2) JSON
        for (file in jsonFiles) {
            try {
                val origem = file.substringBeforeLast(".").trim().ifEmpty { "Tabela" }
                val path = "$assetDir/$file"
                val items = parseJsonToFoodEntities(assetPath = path, origem = origem)
                Log.d(TAG, "JSON $file (origem=$origem): itens lidos=${items.size}")
                if (items.isNotEmpty()) {
                    items.chunked(500).forEach { chunk -> alimentoDao.upsertAll(chunk) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Falha importando JSON $file: ${e.message}", e)
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

    // ======= Busca =======
    fun searchAlimentos(query: String): Flow<List<AlimentoEntity>> {
        val qNorm = normalize(query)
        if (qNorm.isBlank()) return flowOf(emptyList())
        // ⚠️ seu DAO já faz: '%' || :qNorm || '%'
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

    // ---------------- CSV PARSER (UTF-8 + fallback ISO-8859-1 + heurística) ----------------
    private fun parseCsvToFoodEntities(assetPath: String, origem: String): List<AlimentoEntity> {
        val context = getApplication<Application>()
        val bytes = context.assets.open(assetPath).use { it.readBytes() }
        val firstUtf8 = readFirstLine(bytes, Charsets.UTF_8)
        val charset = if (firstUtf8.contains('\uFFFD')) Charsets.ISO_8859_1 else Charsets.UTF_8

        BufferedReader(InputStreamReader(ByteArrayInputStream(bytes), charset)).use { reader ->
            val firstLineRaw0 = reader.readLine() ?: return emptyList()
            val firstLineRaw = firstLineRaw0.replace("\uFEFF", "")
            val delimiter = detectDelimiter(firstLineRaw)
            val header = splitCsvLine(firstLineRaw, delimiter)
                .map { it.trim().trim('"').replace("\uFEFF", "") }
            if (header.isEmpty()) return emptyList()

            val firstDataLine = reader.readLine()
            val firstDataCols = firstDataLine?.let { splitCsvLine(it, delimiter) }

            var idxNome = findIndex(
                header,
                "alimento", "descricao", "descrição", "descricao do alimento", "descrição do alimento",
                "nome", "produto", "item", "description", "food"
            )
            if (idxNome == null) {
                idxNome = guessNomeIndex(firstDataCols)
                Log.w(TAG, "CSV $assetPath: idxNome não encontrado por header. Usando heurística idxNome=$idxNome")
            }

            val idxEnergia  = findIndex(header, "energia", "kcal", "calorias", "valor energetico", "valor energético", "energy")
            val idxProteina = findIndex(header, "proteina", "proteína", "protein")
            val idxLipidios = findIndex(header, "lipidios", "lipídios", "lipideos", "lipídeos", "gordura", "gorduras", "lipid", "fat")
            val idxCarbo    = findIndex(header, "carboidrato", "carboidratos", "carbo", "carbohydrate", "carbs")
            val idxQtd      = findIndex(header, "quantidade", "porcao", "porção", "qtd", "amount")
            val idxUn       = findIndex(header, "unidade", "medida", "unid", "unit")

            if (idxNome == null) {
                Log.e(TAG, "CSV $assetPath: não consegui determinar coluna de nome. Header=${header.joinToString()}")
                return emptyList()
            }

            val list = ArrayList<AlimentoEntity>(2000)

            if (!firstDataLine.isNullOrBlank()) {
                parseOneCsvRow(
                    cols       = firstDataCols ?: emptyList(),
                    origem     = origem,
                    idxNome    = idxNome,
                    idxEnergia = idxEnergia,
                    idxProteina= idxProteina,
                    idxLipidios= idxLipidios,
                    idxCarbo   = idxCarbo,
                    idxQtd     = idxQtd,
                    idxUn      = idxUn
                )?.let { list.add(it) }
            }

            while (true) {
                val line = reader.readLine() ?: break
                if (line.isBlank()) continue
                val cols = splitCsvLine(line, delimiter)
                parseOneCsvRow(
                    cols       = cols,
                    origem     = origem,
                    idxNome    = idxNome,
                    idxEnergia = idxEnergia,
                    idxProteina= idxProteina,
                    idxLipidios= idxLipidios,
                    idxCarbo   = idxCarbo,
                    idxQtd     = idxQtd,
                    idxUn      = idxUn
                )?.let { list.add(it) }
            }

            return list
        }
    }

    private fun parseOneCsvRow(
        cols: List<String>,
        origem: String,
        idxNome: Int,
        idxEnergia: Int?,
        idxProteina: Int?,
        idxLipidios: Int?,
        idxCarbo: Int?,
        idxQtd: Int?,
        idxUn: Int?
    ): AlimentoEntity? {
        val nome = col(cols, idxNome)?.trim()?.trim('"').orEmpty()
        if (nome.isBlank()) return null

        val alimentoNorm    = normalize(nome)
        val quantidadeBase  = parseDouble(col(cols, idxQtd)) ?: 100.0
        val unidadeFromCsv  = col(cols, idxUn)?.trim()?.trim('"')?.ifBlank { null }
        val unidadeBase     = unidadeFromCsv ?: if (alimentoNorm.contains("agua")) "ml" else "g"
        val calorias        = parseDouble(col(cols, idxEnergia))  ?: 0.0
        val proteina        = parseDouble(col(cols, idxProteina)) ?: 0.0
        val lipidios        = parseDouble(col(cols, idxLipidios)) ?: 0.0
        val carbo           = parseDouble(col(cols, idxCarbo))    ?: 0.0
        val id              = stableIdSha256(origem, alimentoNorm)

        return AlimentoEntity(
            id           = id,
            origem       = origem,
            alimento     = nome,
            alimentoNorm = alimentoNorm,
            quantidadeBase = quantidadeBase,
            unidadeBase  = unidadeBase,
            proteina     = proteina,
            lipidios     = lipidios,
            carboidratos = carbo,
            calorias     = calorias
        )
    }

    private fun guessNomeIndex(firstDataCols: List<String>?): Int? {
        if (firstDataCols.isNullOrEmpty()) return null

        var bestIdx = 0
        var bestScore = Int.MIN_VALUE
        for (i in 0 until firstDataCols.size) {
            val v = firstDataCols[i].trim().trim('"')
            if (v.isBlank()) continue
            val letters = v.count { it.isLetter() }
            val digits  = v.count { it.isDigit() }
            val score   = (letters * 3) - (digits * 2) + minOf(v.length, 40)
            if (score > bestScore) {
                bestScore = score
                bestIdx   = i
            }
        }
        return bestIdx
    }

    // ---------------- JSON PARSER (genérico, não trava o app) ----------------
    private fun parseJsonToFoodEntities(assetPath: String, origem: String): List<AlimentoEntity> {
        val context  = getApplication<Application>()
        val jsonText = context.assets.open(assetPath).use { input ->
            InputStreamReader(input, Charsets.UTF_8).readText()
        }.replace("\uFEFF", "")

        val root = JSONTokener(jsonText).nextValue()
        val arr  = extractJSONArray(root) ?: return emptyList()
        val list = ArrayList<AlimentoEntity>(arr.length())

        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val nome = firstNonBlank(
                obj.optString("alimento"),
                obj.optString("descricao"),
                obj.optString("descrição"),
                obj.optString("description"),
                obj.optString("name"),
                obj.optString("food"),
                obj.optString("foodName")
            )
            if (nome.isBlank()) continue

            val alimentoNorm   = normalize(nome)
            val id             = stableIdSha256(origem, alimentoNorm)
            val quantidadeBase = parseDouble(firstNonBlank(
                obj.optString("quantidadeBase"),
                obj.optString("quantidade"),
                obj.optString("amount"),
                obj.optString("servingSize")
            )) ?: 100.0
            val unidadeBase = firstNonBlank(
                obj.optString("unidadeBase"),
                obj.optString("unidade"),
                obj.optString("unit")
            ).ifBlank { "g" }
            val calorias = parseDouble(firstNonBlank(
                obj.optString("kcal"),
                obj.optString("calorias"),
                obj.optString("energia"),
                obj.optString("energy"),
                obj.optString("calories")
            )) ?: 0.0
            val proteina = parseDouble(firstNonBlank(
                obj.optString("proteina"),
                obj.optString("proteína"),
                obj.optString("protein")
            )) ?: 0.0
            val lipidios = parseDouble(firstNonBlank(
                obj.optString("lipidios"),
                obj.optString("lipídios"),
                obj.optString("gordura"),
                obj.optString("fat"),
                obj.optString("lipid")
            )) ?: 0.0
            val carbo = parseDouble(firstNonBlank(
                obj.optString("carboidratos"),
                obj.optString("carboidrato"),
                obj.optString("carbs"),
                obj.optString("carbohydrate"),
                obj.optString("carbo")
            )) ?: 0.0

            list.add(
                AlimentoEntity(
                    id             = id,
                    origem         = origem,
                    alimento       = nome,
                    alimentoNorm   = alimentoNorm,
                    quantidadeBase = quantidadeBase,
                    unidadeBase    = unidadeBase,
                    proteina       = proteina,
                    lipidios       = lipidios,
                    carboidratos   = carbo,
                    calorias       = calorias
                )
            )
        }
        return list
    }

    private fun extractJSONArray(root: Any?): JSONArray? {
        return when (root) {
            is JSONArray  -> root
            is JSONObject -> {
                val keys = listOf(
                    "foods", "data", "alimentos", "items",
                    "FoundationFoods", "SRLegacyFoods", "SurveyFoods", "BrandedFoods"
                )
                for (k in keys) {
                    val v = root.opt(k)
                    if (v is JSONArray) return v
                }
                null
            }
            else -> null
        }
    }

    private fun firstNonBlank(vararg values: String?): String {
        for (v in values) {
            val t = v?.trim()?.trim('"') ?: ""
            if (t.isNotBlank()) return t
        }
        return ""
    }

    // ---------------- util ----------------
    private fun readFirstLine(bytes: ByteArray, charset: java.nio.charset.Charset): String {
        BufferedReader(InputStreamReader(ByteArrayInputStream(bytes), charset)).use { r ->
            return r.readLine() ?: ""
        }
    }

    private fun col(cols: List<String>, idx: Int?): String? = idx?.let { cols.getOrNull(it) }

    private fun detectDelimiter(line: String): Char {
        val semicolons = line.count { it == ';' }
        val commas     = line.count { it == ',' }
        return if (semicolons >= max(1, commas)) ';' else ','
    }

    private fun splitCsvLine(line: String, delimiter: Char): List<String> {
        val result   = ArrayList<String>()
        val sb       = StringBuilder()
        var inQuotes = false
        var i        = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"'                    -> inQuotes = !inQuotes
                c == delimiter && !inQuotes -> { result.add(sb.toString()); sb.setLength(0) }
                else                        -> sb.append(c)
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
            val exact  = normHeaders.indexOfFirst { it == target }
            if (exact >= 0) return exact
        }
        for (alias in aliases) {
            val target   = normalize(alias)
            val contains = normHeaders.indexOfFirst { it.contains(target) }
            if (contains >= 0) return contains
        }
        return null
    }

    // remove acentos SEM regex
    private fun normalize(s: String): String {
        val nfd = Normalizer.normalize(s, Normalizer.Form.NFD)
        val sb  = StringBuilder(nfd.length)
        for (ch in nfd) {
            if (Character.getType(ch) != Character.NON_SPACING_MARK.toInt()) sb.append(ch)
        }
        return sb.toString().lowercase(Locale.ROOT).trim()
    }

    private fun stableIdSha256(origem: String, alimentoNorm: String): Long {
        val md    = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest("$origem|$alimentoNorm".toByteArray(Charsets.UTF_8))
        var value = 0L
        for (i in 0 until 8) {
            value = (value shl 8) or (bytes[i].toLong() and 0xffL)
        }
        return value and Long.MAX_VALUE
    }
}
