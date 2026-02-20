package com.example.nutriplan.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

data class DietaPlano(val id: String, val pacienteId: String, val nome: String)
data class DietaRefeicao(val id: String, val planoId: String, val nome: String, val horario: String, val itens: List<DietaItem> = emptyList())
data class DietaItem(val id: String, val nome: String, val quantidade: String, val calorias: Double)

class DietaViewModel : ViewModel() {
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
}