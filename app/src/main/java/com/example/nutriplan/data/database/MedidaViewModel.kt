package com.example.nutriplan.ui.viewmodel

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriplan.data.database.AppDatabase
import com.example.nutriplan.data.database.MedidaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class MedidaViewModel(application: Application) : AndroidViewModel(application) {

    private val medidaDao = AppDatabase.getDatabase(application).medidaDao()

    private val _medidas = MutableStateFlow<List<MedidaEntity>>(emptyList())
    val medidas: StateFlow<List<MedidaEntity>> = _medidas.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _ultimaMedida = MutableStateFlow<MedidaEntity?>(null)
    val ultimaMedida: StateFlow<MedidaEntity?> = _ultimaMedida.asStateFlow()

    private var carregarJob: Job? = null

    fun carregarMedidasDoPaciente(pacienteId: String) {
        carregarJob?.cancel()
        carregarJob = viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            medidaDao.buscarPorPaciente(pacienteId).collect { listaMedidas ->
                _medidas.value = listaMedidas
                _ultimaMedida.value = listaMedidas.maxByOrNull { it.dataCriacao }
                _isLoading.value = false
            }
        }
    }

    suspend fun buscarUltimaMedida(pacienteId: String): MedidaEntity? {
        return medidaDao.buscarUltimaMedida(pacienteId)
    }

    suspend fun buscarUltimaAltura(pacienteId: String): Float? {
        return medidaDao.buscarUltimaAltura(pacienteId)
    }

    suspend fun buscarPorId(medidaId: String): MedidaEntity? {
        return medidaDao.buscarPorId(medidaId)
    }

    /**
     * NOVA MEDIDA:
     * - sempre gera ID novo
     * - sempre atualiza dataCriacao
     * - evita crash de PRIMARY KEY duplicada
     */
    fun adicionarMedida(medida: MedidaEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val baseNova = medida.copy(
                id = UUID.randomUUID().toString(),
                dataCriacao = System.currentTimeMillis()
            )

            try {
                medidaDao.inserir(baseNova)
            } catch (e: SQLiteConstraintException) {
                // fallback (ex.: duplo clique muito rápido ou algum estado reaproveitado)
                val retry = baseNova.copy(id = UUID.randomUUID().toString())
                medidaDao.inserir(retry)
            }
        }
    }

    /**
     * EDIÇÃO:
     * - mantém o mesmo ID
     */
    fun atualizarMedida(medida: MedidaEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            medidaDao.atualizar(medida)
        }
    }

    fun deletarMedida(medida: MedidaEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            medidaDao.deletar(medida)
        }
    }

    suspend fun contarMedidas(pacienteId: String): Int {
        return medidaDao.contarMedidasDoPaciente(pacienteId)
    }
}