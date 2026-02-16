package com.example.nutriplan.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.nutriplan.core.LanguagePreferences
import com.example.nutriplan.ui.screens.auth.ForgotPasswordScreen
import com.example.nutriplan.ui.screens.auth.LoginScreen
import com.example.nutriplan.ui.screens.auth.RegisterScreen
import com.example.nutriplan.ui.screens.home.HomeScreen
import com.example.nutriplan.ui.screens.auth.chat.ChatListScreen
import com.example.nutriplan.ui.screens.auth.chat.ChatDetailScreen
import com.example.nutriplan.ui.screens.PacientesScreen
import com.example.nutriplan.ui.screens.FormularioPacienteScreen
import com.example.nutriplan.ui.screens.DetalhesPacienteScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    currentLanguage: String,
    isDarkTheme: Boolean
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefs = LanguagePreferences(context.applicationContext)
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        modifier = modifier
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                currentLanguage = currentLanguage,
                isDarkTheme = isDarkTheme,
                onGoToRegister = { navController.navigate(Routes.REGISTER) },
                onGoToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                currentLanguage = currentLanguage,
                isDarkTheme = isDarkTheme,
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                currentLanguage = currentLanguage,
                isDarkTheme = isDarkTheme,
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                currentLanguage = currentLanguage,
                isDarkTheme = isDarkTheme,
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToChat = {
                    navController.navigate(Routes.CHAT_LIST)
                },
                onNavigateToPacientes = {
                    navController.navigate(Routes.PACIENTES)
                }
            )
        }

        composable(Routes.CHAT_LIST) {
            ChatListScreen(
                currentLanguage = currentLanguage,
                isDarkTheme = isDarkTheme,
                onConversationClick = { conversationId, participantName ->
                    navController.navigate("chat_detail/$conversationId/$participantName")
                },
                onNewChatClick = { /* TODO: Implementar novo chat */ },
                onBackClick = { navController.popBackStack() },
                onThemeToggle = {
                    scope.launch { prefs.setThemeMode(if (isDarkTheme) "light" else "dark") }
                }
            )
        }

        // Chat Detail com suporte a nome dinâmico
        composable(
            route = "chat_detail/{conversationId}/{participantName}",
            arguments = listOf(
                navArgument("conversationId") { type = NavType.StringType },
                navArgument("participantName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            val participantName = backStackEntry.arguments?.getString("participantName") ?: ""

            ChatDetailScreen(
                conversationId = conversationId,
                nutritionistName = participantName,
                currentLanguage = currentLanguage,
                isDarkTheme = isDarkTheme,
                onBackClick = {
                    navController.popBackStack()
                },
                onArchiveClick = {
                    navController.popBackStack()
                }
            )
        }

        // ========== ROTAS DE PACIENTES ==========

        // Lista de Pacientes
        composable(Routes.PACIENTES) {
            PacientesScreen(
                onNavigateToFormulario = {
                    navController.navigate(Routes.PACIENTES_FORMULARIO)
                },
                onNavigateToDetalhes = { pacienteId ->
                    navController.navigate(Routes.pacientesDetalhes(pacienteId))
                }
            )
        }

        // Formulário - Novo Paciente
        composable(Routes.PACIENTES_FORMULARIO) {
            FormularioPacienteScreen(
                pacienteId = null,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Formulário - Editar Paciente
        composable(
            route = Routes.PACIENTES_FORMULARIO_EDIT,
            arguments = listOf(
                navArgument("pacienteId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val pacienteId = backStackEntry.arguments?.getString("pacienteId") ?: ""

            FormularioPacienteScreen(
                pacienteId = pacienteId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Detalhes do Paciente
        composable(
            route = Routes.PACIENTES_DETALHES,
            arguments = listOf(
                navArgument("pacienteId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val pacienteId = backStackEntry.arguments?.getString("pacienteId") ?: ""

            DetalhesPacienteScreen(
                pacienteId = pacienteId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { id ->
                    navController.navigate(Routes.pacientesFormularioEdit(id))
                }
            )
        }
    }
}