package com.example.nutriplan.ui.substitute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriplan.data.taco.substitute.MacroWeights
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SubstituteViewModel(
    private val orchestrator: SubstituteOrchestrator
) : ViewModel() {

    private val _state = MutableStateFlow(SubstituteContract.UiState())
    val state: StateFlow<SubstituteContract.UiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<SubstituteContract.Effect>(extraBufferCapacity = 16)
    val effects: SharedFlow<SubstituteContract.Effect> = _effects.asSharedFlow()

    /**
     * Ajustes de ranking (se depois você quiser expor isso na UI, já está pronto).
     */
    private var weights: MacroWeights = MacroWeights()
    private var kcalWeight: Float = 0.25f

    fun dispatch(action: SubstituteContract.Action) {
        when (action) {

            is SubstituteContract.Action.Start -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true, errorMessage = null, target = action.target) }
                    runCatching {
                        orchestrator.start(
                            target = action.target,
                            weights = weights,
                            kcalWeight = kcalWeight
                        )
                    }.onSuccess { newState ->
                        _state.value = newState
                    }.onFailure { e ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = e.message ?: "Erro ao iniciar substituição."
                            )
                        }
                    }
                }
            }

            is SubstituteContract.Action.ChangeTab -> {
                _state.update { it.copy(activeTab = action.tab, errorMessage = null) }
            }

            SubstituteContract.Action.RefreshSuggestions -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true, errorMessage = null) }
                    runCatching {
                        orchestrator.refreshSuggestions(
                            state = _state.value,
                            weights = weights,
                            kcalWeight = kcalWeight
                        )
                    }.onSuccess { newState ->
                        _state.value = newState.copy(isLoading = false)
                    }.onFailure { e ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = e.message ?: "Erro ao atualizar sugestões."
                            )
                        }
                    }
                }
            }

            is SubstituteContract.Action.ManualQueryChanged -> {
                _state.update { orchestrator.onManualQueryChanged(it, action.value) }
            }

            SubstituteContract.Action.ManualSearchSubmit -> {
                viewModelScope.launch {
                    _state.update { it.copy(isSearchingManual = true, errorMessage = null) }
                    runCatching {
                        orchestrator.manualSearch(state = _state.value)
                    }.onSuccess { newState ->
                        _state.value = newState.copy(isSearchingManual = false)
                    }.onFailure { e ->
                        _state.update {
                            it.copy(
                                isSearchingManual = false,
                                errorMessage = e.message ?: "Erro na pesquisa manual."
                            )
                        }
                    }
                }
            }

            is SubstituteContract.Action.AddAllowedSubstitute -> {
                viewModelScope.launch {
                    runCatching {
                        orchestrator.addAllowedSubstitute(
                            state = _state.value,
                            choice = action.choice
                        )
                    }.onSuccess { newState ->
                        _state.value = newState
                        _effects.tryEmit(SubstituteContract.Effect.Message("Substituto adicionado."))
                    }.onFailure { e ->
                        _state.update {
                            it.copy(errorMessage = e.message ?: "Erro ao adicionar substituto.")
                        }
                    }
                }
            }

            is SubstituteContract.Action.RemoveAllowedSubstitute -> {
                viewModelScope.launch {
                    runCatching {
                        orchestrator.removeAllowedSubstitute(
                            state = _state.value,
                            substituteFoodId = action.substituteFoodId
                        )
                    }.onSuccess { newState ->
                        _state.value = newState
                        _effects.tryEmit(SubstituteContract.Effect.Message("Substituto removido."))
                    }.onFailure { e ->
                        _state.update {
                            it.copy(errorMessage = e.message ?: "Erro ao remover substituto.")
                        }
                    }
                }
            }

            is SubstituteContract.Action.ApplyNow -> {
                // Regra 2B: aplicar usando a porção sugerida (choice já vem pronta).
                val effect = orchestrator.applyNow(action.choice)
                _effects.tryEmit(effect)
            }

            SubstituteContract.Action.ClearError -> {
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }

    /**
     * Se depois você quiser expor controles de equivalência na UI.
     */
    fun setMacroWeights(newWeights: MacroWeights) {
        weights = newWeights
    }

    fun setKcalWeight(newValue: Float) {
        kcalWeight = newValue
    }
}