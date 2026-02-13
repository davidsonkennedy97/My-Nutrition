package com.example.nutriplan.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val HOME = "home"

    // Novas rotas do Chat
    const val CHAT_LIST = "chat_list"
    const val CHAT_DETAIL = "chat_detail/{conversationId}/{participantName}"
    const val NEW_CHAT = "new_chat"

    // Função helper para criar rota com parâmetros
    fun chatDetail(conversationId: String, participantName: String): String {
        return "chat_detail/$conversationId/$participantName"
    }
}