package com.example.nutriplan.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val HOME = "home"
    const val CHAT_LIST = "chat_list"
    const val PACIENTES = "pacientes"
    const val PACIENTES_FORMULARIO = "pacientes_formulario"
    const val PACIENTES_FORMULARIO_EDIT = "pacientes_formulario/{pacienteId}"
    const val PACIENTES_DETALHES = "detalhes_paciente/{pacienteId}?tabIndex={tabIndex}"  // ← CORRIGIDO
    const val MEDIDA_FORMULARIO = "medida_formulario/{pacienteId}?medidaId={medidaId}"

    fun pacientesFormularioEdit(pacienteId: String): String {
        return "pacientes_formulario/$pacienteId"
    }

    fun pacientesDetalhes(pacienteId: String, tabIndex: Int = 0): String {
        return "detalhes_paciente/$pacienteId?tabIndex=$tabIndex"  // ← CORRIGIDO
    }

    fun medidaFormulario(pacienteId: String, medidaId: String?): String {
        return if (medidaId != null) {
            "medida_formulario/$pacienteId?medidaId=$medidaId"
        } else {
            "medida_formulario/$pacienteId"
        }
    }
}