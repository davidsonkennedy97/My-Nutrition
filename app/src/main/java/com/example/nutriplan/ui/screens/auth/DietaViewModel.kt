package com.example.nutriplan.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriplan.data.database.AppDatabase
import com.example.nutriplan.data.dieta.DietaItemEntity
import com.example.nutriplan.data.dieta.DietaPlanoEntity
import com.example.nutriplan.data.dieta.DietaRefeicaoEntity
import com.example.nutriplan.data.dieta.DietaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class DietaViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = DietaRepository(
        AppDatabase.getDatabase(app).dietaDao()
    )

    // ─── Plano ────────────────────────────────────────────────
    fun listarPlanos(pacienteId: String): Flow<List<DietaPlanoEntity>> =
        repository.listarPlanos(pacienteId)

    fun criarPlano(pacienteId: String, titulo: String, data: String) {
        viewModelScope.launch {
            repository.inserirPlano(
                DietaPlanoEntity(
                    id = UUID.randomUUID().toString(),
                    pacienteId = pacienteId,
                    titulo = titulo,
                    dataCriacao = data
                )
            )
        }
    }

    fun deletarPlano(plano: DietaPlanoEntity) {
        viewModelScope.launch { repository.deletarPlano(plano) }
    }

    // ─── Refeição ─────────────────────────────────────────────
    fun listarRefeicoes(planoId: String): Flow<List<DietaRefeicaoEntity>> =
        repository.listarRefeicoes(planoId)

    fun criarRefeicao(planoId: String, nome: String, horario: String) {
        viewModelScope.launch {
            repository.inserirRefeicao(
                DietaRefeicaoEntity(
                    id = UUID.randomUUID().toString(),
                    planoId = planoId,
                    nome = nome,
                    horario = horario
                )
            )
        }
    }

    fun deletarRefeicao(refeicao: DietaRefeicaoEntity) {
        viewModelScope.launch { repository.deletarRefeicao(refeicao) }
    }

    fun atualizarRefeicao(refeicao: DietaRefeicaoEntity) {
        viewModelScope.launch { repository.atualizarRefeicao(refeicao) }
    }

    // ─── Item (alimento) ──────────────────────────────────────
    fun listarItens(refeicaoId: String): Flow<List<DietaItemEntity>> =
        repository.listarItens(refeicaoId)

    fun adicionarItem(
        refeicaoId: String,
        nome: String,
        origem: String,
        quantidade: Float,
        unidade: String,
        proteina: Float,
        lipidios: Float,
        carboidrato: Float,
        calorias: Float
    ) {
        viewModelScope.launch {
            repository.inserirItem(
                DietaItemEntity(
                    id = UUID.randomUUID().toString(),
                    refeicaoId = refeicaoId,
                    alimentoNome = nome,
                    origem = origem,
                    quantidade = quantidade,
                    unidade = unidade,
                    proteina = proteina,
                    lipidios = lipidios,
                    carboidrato = carboidrato,
                    calorias = calorias
                )
            )
        }
    }

    fun deletarItem(item: DietaItemEntity) {
        viewModelScope.launch { repository.deletarItem(item) }
    }
}
