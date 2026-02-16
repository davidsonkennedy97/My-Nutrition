package com.example.nutriplan.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriplan.data.database.AppDatabase
import com.example.nutriplan.data.database.PacienteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PacienteViewModel(application: Application) : AndroidViewModel(application) {

    // Acesso ao banco de dados
    private val pacienteDao = AppDatabase.getDatabase(application).pacienteDao()

    // Lista de pacientes (observável - atualiza automaticamente na tela)
    private val _pacientes = MutableStateFlow<List<PacienteEntity>>(emptyList())
    val pacientes: StateFlow<List<PacienteEntity>> = _pacientes.asStateFlow()

    // Estado de loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Carrega a lista de pacientes quando o ViewModel é criado
        carregarPacientes()
    }

    // Carrega todos os pacientes do banco de dados
    private fun carregarPacientes() {
        viewModelScope.launch {
            pacienteDao.buscarTodos().collect { lista ->
                _pacientes.value = lista
            }
        }
    }

    // Adiciona novo paciente
    fun adicionarPaciente(paciente: PacienteEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            pacienteDao.inserir(paciente)
            _isLoading.value = false
        }
    }

    // Atualiza paciente existente
    fun atualizarPaciente(paciente: PacienteEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            pacienteDao.atualizar(paciente)
            _isLoading.value = false
        }
    }

    // Deleta paciente
    fun deletarPaciente(paciente: PacienteEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            pacienteDao.deletar(paciente)
            _isLoading.value = false
        }
    }

    // Busca paciente por ID
    suspend fun buscarPorId(id: String): PacienteEntity? {
        return pacienteDao.buscarPorId(id)
    }

    // Busca pacientes por nome (pesquisa)
    fun buscarPorNome(termo: String) {
        viewModelScope.launch {
            pacienteDao.buscarPorNome(termo).collect { lista ->
                _pacientes.value = lista
            }
        }
    }

    // Busca pacientes por status
    fun buscarPorStatus(status: String) {
        viewModelScope.launch {
            pacienteDao.buscarPorStatus(status).collect { lista ->
                _pacientes.value = lista
            }
        }
    }

    // Reseta a pesquisa (volta para todos os pacientes)
    fun resetarPesquisa() {
        carregarPacientes()
    }
}