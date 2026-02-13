package com.example.nutriplan.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.nutriplan.ui.screens.auth.ForgotPasswordScreen
import com.example.nutriplan.ui.screens.auth.LoginScreen
import com.example.nutriplan.ui.screens.auth.RegisterScreen
import com.example.nutriplan.ui.screens.home.HomeScreen
import com.example.nutriplan.ui.screens.auth.chat.ChatListScreen
import com.example.nutriplan.ui.screens.auth.chat.ChatDetailScreen

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    currentLanguage: String,
    isDarkTheme: Boolean
) {
    val navController = rememberNavController()

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
                onLoginSuccess = { navController.navigate(Routes.HOME) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }}
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
                }
            )
        }

        // Chat List
        composable(Routes.CHAT_LIST) {
            ChatListScreen(
                currentLanguage = currentLanguage,
                isDarkTheme = isDarkTheme,
                onConversationClick = { conversationId ->
                    navController.navigate(
                        Routes.chatDetail(conversationId, "Nutricionista")
                    )
                },
                onNewChatClick = {
                    // TODO: Implementar tela de novo chat
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Chat Detail
        composable(
            route = Routes.CHAT_DETAIL,
            arguments = listOf(
                navArgument("conversationId") { type = NavType.StringType },
                navArgument("participantName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            val participantName = backStackEntry.arguments?.getString("participantName") ?: ""

            ChatDetailScreen(
                conversationId = conversationId,
                participantName = participantName,
                currentLanguage = currentLanguage,
                isDarkTheme = isDarkTheme,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}