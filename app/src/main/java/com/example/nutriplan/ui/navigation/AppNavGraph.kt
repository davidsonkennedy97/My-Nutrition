package com.example.nutriplan.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.nutriplan.ui.screens.dieta.DietaEditorScreen
import com.example.nutriplan.ui.screens.dieta.RotinaAlimentosScreen
import com.example.nutriplan.ui.screens.home.HomeScreen
import com.example.nutriplan.ui.viewmodel.DietaViewModel
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
        startDestination = "login",
        modifier = modifier
    ) {
        composable("login") {
            LoginScreen(
                currentLanguage = currentLanguage,
                isDarkTheme = isDarkTheme,
                onGoToRegister = { navController.navigate("register") },
                onGoToForgotPassword = { navController.navigate("forgot_password") },
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                currentLanguage = currentLanguage,
                isDarkTheme = isDarkTheme,
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable("forgot_password") {
            ForgotPasswordScreen(
                currentLanguage = currentLanguage,
                isDarkTheme = isDarkTheme,
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable("home") {
            HomeScreen(
                currentLanguage = currentLanguage,
                isDarkTheme = isDarkTheme,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToChat = { navController.navigate("chat_list") },
                onNavigateToPacientes = { navController.navigate("pacientes") }
            )
        }

        composable("chat_list") {
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

        composable("pacientes") {
            PacientesScreen(
                currentLanguage = currentLanguage,
                isDarkTheme = isDarkTheme,
                onNavigateToFormulario = { navController.navigate("pacientes_formulario") },
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

        composable("pacientes_formulario") {
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

            val dietaViewModel: DietaViewModel = viewModel()

            DetalhesPacienteScreen(
                pacienteId = pacienteId,
                initialTabIndex = tabIndex,
                pacienteViewModel = viewModel(),
                dietaViewModel = dietaViewModel,
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
                onNavigateToDietaEditor = { pid ->
                    val safePid = pid.trim()
                    if (safePid.isNotEmpty()) navController.navigate("dieta_editor/$safePid")
                    else navController.navigate("dieta_editor")
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

        // ====== NOVA ROTA: tela de alimentos da rotina ======
        composable(
            route = "rotina_alimentos/{rotinaId}?nome={nome}",
            arguments = listOf(
                navArgument("rotinaId") { type = NavType.LongType },
                navArgument("nome") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val rotinaId = backStackEntry.arguments?.getLong("rotinaId") ?: 0L
            val rotinaNome = backStackEntry.arguments?.getString("nome") ?: ""

            val dietaViewModel: DietaViewModel = viewModel()

            RotinaAlimentosScreen(
                rotinaId = rotinaId,
                rotinaNome = rotinaNome,
                isDarkTheme = isDarkTheme,
                dietaViewModel = dietaViewModel,
                onBack = { navController.popBackStack() },
                onSave = { navController.popBackStack() }
            )
        }

        // Rota sem parâmetro (se por algum motivo o ID vier vazio)
        composable("dieta_editor") {
            val dietaViewModel: DietaViewModel = viewModel()

            DietaEditorScreen(
                pacienteId = "",
                isDarkTheme = isDarkTheme,
                dietaViewModel = dietaViewModel,
                onBack = { navController.popBackStack() },
                onThemeToggle = {
                    scope.launch { prefs.setThemeMode(if (isDarkTheme) "light" else "dark") }
                },
                onSave = { navController.popBackStack() },
                onOpenRotina = { rotinaId, rotinaNome ->
                    val encoded = Uri.encode(rotinaNome)
                    navController.navigate("rotina_alimentos/$rotinaId?nome=$encoded")
                }
            )
        }

        // Rota normal com parâmetro
        composable(
            route = "dieta_editor/{pacienteId}",
            arguments = listOf(navArgument("pacienteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val pacienteId = backStackEntry.arguments?.getString("pacienteId") ?: ""
            val dietaViewModel: DietaViewModel = viewModel()

            DietaEditorScreen(
                pacienteId = pacienteId,
                isDarkTheme = isDarkTheme,
                dietaViewModel = dietaViewModel,
                onBack = { navController.popBackStack() },
                onThemeToggle = {
                    scope.launch { prefs.setThemeMode(if (isDarkTheme) "light" else "dark") }
                },
                onSave = { navController.popBackStack() },
                onOpenRotina = { rotinaId, rotinaNome ->
                    val encoded = Uri.encode(rotinaNome)
                    navController.navigate("rotina_alimentos/$rotinaId?nome=$encoded")
                }
            )
        }
    }
}