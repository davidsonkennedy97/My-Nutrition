package com.example.nutriplan.ui.substitute

import com.example.nutriplan.data.taco.meal.MealItemChoice
import com.example.nutriplan.data.taco.substitute.SubstituteSuggestion

/**
 * Tela: Substituir
 *
 * Regras que este contrato cobre:
 * - Tela nova (não bottom sheet)
 * - 2 listas automáticas (Top 5):
 *   1) equivalência por macros (C/P/G)
 *   2) equivalência por macros + kcal
 * - Pesquisa manual (busca tolerante a acento será tratada no repositório/uso de SearchNormalize)
 * - Ação "Aplicar agora" usa a porção sugerida (2B)
 * - Substitutos permitidos salvos "por alimento" para o paciente (1A)
 */
object SubstituteContract {

    enum class Tab {
        EQUIV_MACROS,
        EQUIV_MACROS_KCAL,
        PESQUISA_MANUAL
    }

    /**
     * Alvo da substituição (o item atual na refeição).
     * originalFoodId é o "alimento base" para o qual as substituições serão salvas (1A).
     */
    data class Target(
        val pacienteId: String,
        val originalFoodId: String,
        val currentChoice: MealItemChoice,
        val customName: String? = null
    ) {
        fun displayName(): String = customName ?: currentChoice.food.displayName()
    }

    /**
     * Itens que aparecem na seção de "substitutos já permitidos/salvos" para esse paciente e alimento.
     */
    data class AllowedSubstituteUi(
        val choice: MealItemChoice
    ) {
        val foodId: String get() = choice.food.id
        val displayName: String get() = choice.food.displayName()
        val portionText: String get() = choice.portion.displayText()
        val kcal: Float get() = choice.kcal()
    }

    /**
     * Estado da tela Substituir.
     * Observação: as duas listas "Top 5" são computadas fora (use-case/engine) e injetadas aqui.
     */
    data class UiState(
        val target: Target? = null,
        val activeTab: Tab = Tab.EQUIV_MACROS,

        // Loading / erro
        val isLoading: Boolean = false,
        val errorMessage: String? = null,

        // Substitutos permitidos já salvos para (pacienteId + originalFoodId)
        val allowedSubstitutes: List<AllowedSubstituteUi> = emptyList(),

        // Sugestões automáticas (Top 5)
        val suggestionsByMacros: List<SubstituteSuggestion> = emptyList(),
        val suggestionsByMacrosAndKcal: List<SubstituteSuggestion> = emptyList(),

        // Pesquisa manual
        val manualQuery: String = "",
        val isSearchingManual: Boolean = false,
        val manualResults: List<MealItemChoice> = emptyList()
    )

    /**
     * Ações (intents) disparadas pela UI.
     */
    sealed interface Action {

        /**
         * Inicializa a tela com o alvo (item atual na refeição).
         */
        data class Start(val target: Target) : Action

        /**
         * Alternar abas (equivalência macros, macros+kcal, pesquisa manual).
         */
        data class ChangeTab(val tab: Tab) : Action

        /**
         * Recarregar sugestões automáticas (por exemplo, se trocar pesos/tolerâncias futuramente).
         * Aqui deixei simples: apenas "refresh".
         */
        data object RefreshSuggestions : Action

        /**
         * Substitutos permitidos: adicionar/remover.
         * - Add: salva para aquele paciente e alimento base.
         * - Remove: remove da lista permitida salva.
         */
        data class AddAllowedSubstitute(val choice: MealItemChoice) : Action
        data class RemoveAllowedSubstitute(val substituteFoodId: String) : Action

        /**
         * Aplicar substituição AGORA na refeição (volta para a tela anterior com o resultado).
         * Repare que aqui aplica a "choice" completa (alimento + porção humana).
         */
        data class ApplyNow(val choice: MealItemChoice) : Action

        /**
         * Pesquisa manual
         */
        data class ManualQueryChanged(val value: String) : Action
        data object ManualSearchSubmit : Action

        /**
         * Limpa erro exibido
         */
        data object ClearError : Action
    }

    /**
     * Efeitos (one-shot) para navegação/feedback.
     */
    sealed interface Effect {

        /**
         * Voltar para a tela anterior com a escolha aplicada.
         * A tela anterior (refeição) atualiza o slot.
         */
        data class Applied(val newChoice: MealItemChoice) : Effect

        /**
         * Mostrar mensagem rápida (snackbar/toast)
         */
        data class Message(val text: String) : Effect

        /**
         * Fechar tela sem aplicar (opcional)
         */
        data object Close : Effect
    }
}