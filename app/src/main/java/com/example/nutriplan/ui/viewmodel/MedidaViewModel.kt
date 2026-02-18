package com.example.nutriplan.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriplan.data.database.AppDatabase
import com.example.nutriplan.data.database.MedidaEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MedidaViewModel(application: Application) : AndroidViewModel(application) {

    private val medidaDao = AppDatabase.getDatabase(application).medidaDao()

    private val _medidas = MutableStateFlow<List<MedidaEntity>>(emptyList())
    val medidas: StateFlow<List<MedidaEntity>> = _medidas.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _ultimaMedida = MutableStateFlow<MedidaEntity?>(null)
    val ultimaMedida: StateFlow<MedidaEntity?> = _ultimaMedida.asStateFlow()

    fun carregarMedidasDoPaciente(pacienteId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            medidaDao.buscarPorPaciente(pacienteId).collect { listaMedidas ->
                _medidas.value = listaMedidas
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

    fun adicionarMedida(medida: MedidaEntity) {
        viewModelScope.launch {
            medidaDao.inserir(medida)
        }
    }

    fun atualizarMedida(medida: MedidaEntity) {
        viewModelScope.launch {
            medidaDao.atualizar(medida)
        }
    }

    fun deletarMedida(medida: MedidaEntity) {
        viewModelScope.launch {
            medidaDao.deletar(medida)
        }
    }

    suspend fun contarMedidas(pacienteId: String): Int {
        return medidaDao.contarMedidasDoPaciente(pacienteId)
    }
}