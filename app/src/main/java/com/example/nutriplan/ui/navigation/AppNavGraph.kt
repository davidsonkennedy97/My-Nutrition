package com.example.nutriplan.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.nutriplan.core.LanguagePreferences
import com.example.nutriplan.ui.screens.DetalhesPacienteScreen
import com.example.nutriplan.ui.screens.FormularioMedidaScreen
import com.example.nutriplan.ui.screens.FormularioPacienteScreen
import com.example.nutriplan.ui.screens.PacientesScreen
import com.example.nutriplan.ui.screens.auth.ForgotPasswordScreen
import com.example.nutriplan.ui.screens.auth.LoginScreen
import com.example.nutriplan.ui.screens.auth.RegisterScreen
import com.example.nutriplan.ui.screens.auth.chat.ChatDetailScreen
import com.example.nutriplan.ui.screens.auth.chat.ChatListScreen
import com.example.nutriplan.ui.screens.home.HomeScreen
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
                onNavigateToChat = { navController.navigate(Routes.CHAT_LIST) },
                onNavigateToPacientes = { navController.navigate(Routes.PACIENTES) }
            )
        }

        composable(Routes.CHAT_LIST) {
            ChatListScreen(
                currentLanguage = currentLanguage,
                isDarkTheme = isDarkTheme,
                onConversationClick = { conversationId, participantName ->
                    navController.navigate("chat_detail/$conversationId/$participantName")
                },
                onNewChatClick = { },
                onBackClick = { navController.popBackStack() },
                onThemeToggle = {
                    scope.launch { prefs.setThemeMode(if (isDarkTheme) "light" else "dark") }
                }
            )
        }

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
                onBackClick = { navController.popBackStack() },
                onArchiveClick = { navController.popBackStack() }
            )
        }

        composable(Routes.PACIENTES) {
            PacientesScreen(
                currentLanguage = currentLanguage,
                isDarkTheme = isDarkTheme,
                onNavigateToFormulario = { navController.navigate(Routes.PACIENTES_FORMULARIO) },
                onNavigateToDetalhes = { pacienteId ->
                    navController.navigate("detalhes_paciente/$pacienteId?tabIndex=0")
                },
                onNavigateToEdit = { pacienteId ->
                    navController.navigate("pacientes_formulario/$pacienteId")
                },
                onLanguageChange = {
                    val nextLanguage = if (currentLanguage == "pt") "en" else "pt"
                    scope.launch { prefs.setLanguage(nextLanguage) }
                },
                onThemeChange = {
                    scope.launch { prefs.setThemeMode(if (isDarkTheme) "light" else "dark") }
                },
                onOpenDrawer = { navController.popBackStack() }
            )
        }

        composable(Routes.PACIENTES_FORMULARIO) {
            FormularioPacienteScreen(
                pacienteId = null,
                isDarkTheme = isDarkTheme,
                currentLanguage = currentLanguage,
                onNavigateBack = { navController.popBackStack() },
                onLanguageChange = {
                    val nextLanguage = if (currentLanguage == "pt") "en" else "pt"
                    scope.launch { prefs.setLanguage(nextLanguage) }
                },
                onThemeChange = {
                    scope.launch { prefs.setThemeMode(if (isDarkTheme) "light" else "dark") }
                }
            )
        }

        composable(
            route = "pacientes_formulario/{pacienteId}",
            arguments = listOf(navArgument("pacienteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val pacienteId = backStackEntry.arguments?.getString("pacienteId") ?: ""
            FormularioPacienteScreen(
                pacienteId = pacienteId,
                isDarkTheme = isDarkTheme,
                currentLanguage = currentLanguage,
                onNavigateBack = { navController.popBackStack() },
                onLanguageChange = {
                    val nextLanguage = if (currentLanguage == "pt") "en" else "pt"
                    scope.launch { prefs.setLanguage(nextLanguage) }
                },
                onThemeChange = {
                    scope.launch { prefs.setThemeMode(if (isDarkTheme) "light" else "dark") }
                }
            )
        }

        composable(
            route = "detalhes_paciente/{pacienteId}?tabIndex={tabIndex}",
            arguments = listOf(
                navArgument("pacienteId") { type = NavType.StringType },
                navArgument("tabIndex") { type = NavType.IntType; defaultValue = 0 }
            )
        ) { backStackEntry ->
            val pacienteId = backStackEntry.arguments?.getString("pacienteId") ?: ""
            val tabIndex = backStackEntry.arguments?.getInt("tabIndex") ?: 0
            DetalhesPacienteScreen(
                pacienteId = pacienteId,
                initialTabIndex = tabIndex,
                isDarkTheme = isDarkTheme,
                currentLanguage = currentLanguage,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> navController.navigate("pacientes_formulario/$id") },
                onNavigateToFormularioMedida = { id, medidaId ->
                    if (medidaId != null) {
                        navController.navigate("medida_formulario/$id?medidaId=$medidaId")
                    } else {
                        navController.navigate("medida_formulario/$id")
                    }
                },
                onLanguageChange = {
                    val nextLanguage = if (currentLanguage == "pt") "en" else "pt"
                    scope.launch { prefs.setLanguage(nextLanguage) }
                },
                onThemeChange = {
                    scope.launch { prefs.setThemeMode(if (isDarkTheme) "light" else "dark") }
                }
            )
        }

        composable(
            route = "medida_formulario/{pacienteId}?medidaId={medidaId}",
            arguments = listOf(
                navArgument("pacienteId") { type = NavType.StringType },
                navArgument("medidaId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val pacienteId = backStackEntry.arguments?.getString("pacienteId") ?: ""
            val medidaId = backStackEntry.arguments?.getString("medidaId")
            FormularioMedidaScreen(
                pacienteId = pacienteId,
                medidaId = medidaId,
                isDarkTheme = isDarkTheme,
                currentLanguage = currentLanguage,
                onNavigateBack = {
                    navController.popBackStack()
                    navController.navigate("detalhes_paciente/$pacienteId?tabIndex=1")
                },
                onLanguageChange = {
                    val nextLanguage = if (currentLanguage == "pt") "en" else "pt"
                    scope.launch { prefs.setLanguage(nextLanguage) }
                },
                onThemeChange = {
                    scope.launch { prefs.setThemeMode(if (isDarkTheme) "light" else "dark") }
                }
            )
        }
    }
}
