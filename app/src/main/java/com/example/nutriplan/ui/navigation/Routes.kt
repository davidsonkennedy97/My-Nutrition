package com.example.nutriplan.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val HOME = "home"

    // Rotas do Chat
    const val CHAT_LIST = "chat_list"
    const val CHAT_DETAIL = "chat_detail/{conversationId}/{participantName}"
    const val NEW_CHAT = "new_chat"

    // Rotas de Pacientes
    const val PACIENTES = "pacientes"
    const val PACIENTES_FORMULARIO = "pacientes_formulario"
    const val PACIENTES_FORMULARIO_EDIT = "pacientes_formulario/{pacienteId}"
    const val PACIENTES_DETALHES = "pacientes_detalhes/{pacienteId}"

    // Funções helper para criar rotas com parâmetros
    fun chatDetail(conversationId: String, participantName: String): String {
        return "chat_detail/$conversationId/$participantName"
    }

    fun pacientesFormularioEdit(pacienteId: String): String {
        return "pacientes_formulario/$pacienteId"
    }

    fun pacientesDetalhes(pacienteId: String): String {
        return "pacientes_detalhes/$pacienteId"
    }
}