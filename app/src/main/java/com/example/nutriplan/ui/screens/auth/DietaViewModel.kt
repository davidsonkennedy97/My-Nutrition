package com.example.nutriplan.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.nutriplan.ui.dieta.DietaPlano
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DietaViewModel : ViewModel() {

    // Map<pacienteId, List<planos>>
    private val _planosPorPaciente = MutableStateFlow<Map<String, List<DietaPlano>>>(emptyMap())
    val planosPorPaciente: StateFlow<Map<String, List<DietaPlano>>> = _planosPorPaciente.asStateFlow()

    fun listarPlanos(pacienteId: String): List<DietaPlano> {
        return _planosPorPaciente.value[pacienteId].orEmpty()
    }

    fun obterPlano(pacienteId: String, planoId: String): DietaPlano? {
        return listarPlanos(pacienteId).firstOrNull { it.id == planoId }
    }

    /**
     * Salva (insere) o plano como "mais recente primeiro".
     * Se já existir um plano com o mesmo id, substitui e também sobe para o topo.
     */
    fun salvarPlano(pacienteId: String, plano: DietaPlano) {
        _planosPorPaciente.update { atual ->
            val listaAtual = atual[pacienteId].orEmpty()
            val semEsseId = listaAtual.filterNot { it.id == plano.id }
            val novaLista = listOf(plano) + semEsseId
            atual + (pacienteId to novaLista)
        }
    }

    /**
     * Atualiza um plano existente sem mexer na ordem da lista.
     * Se não encontrar o planoId, não altera nada.
     */
    fun atualizarPlanoMantendoOrdem(
        pacienteId: String,
        planoId: String,
        novoPlano: DietaPlano
    ) {
        _planosPorPaciente.update { atual ->
            val listaAtual = atual[pacienteId].orEmpty()
            if (listaAtual.none { it.id == planoId }) return@update atual

            val novaLista = listaAtual.map { plano ->
                if (plano.id == planoId) novoPlano else plano
            }
            atual + (pacienteId to novaLista)
        }
    }

    /**
     * Atualiza um plano existente aplicando uma transformação (sem depender de copy()).
     * Se não encontrar o planoId, não altera nada.
     */
    fun atualizarPlano(
        pacienteId: String,
        planoId: String,
        transform: (DietaPlano) -> DietaPlano
    ) {
        _planosPorPaciente.update { atual ->
            val listaAtual = atual[pacienteId].orEmpty()
            if (listaAtual.none { it.id == planoId }) return@update atual

            val novaLista = listaAtual.map { plano ->
                if (plano.id == planoId) transform(plano) else plano
            }
            atual + (pacienteId to novaLista)
        }
    }

    fun deletarPlano(pacienteId: String, planoId: String) {
        _planosPorPaciente.update { atual ->
            val listaAtual = atual[pacienteId].orEmpty()
            val novaLista = listaAtual.filterNot { it.id == planoId }
            atual + (pacienteId to novaLista)
        }
    }
}