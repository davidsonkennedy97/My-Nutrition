package com.example.nutriplan.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nutriplan.ui.screens.auth.ForgotPasswordScreen
import com.example.nutriplan.ui.screens.auth.LoginScreen
import com.example.nutriplan.ui.screens.auth.RegisterScreen
import com.example.nutriplan.ui.screens.home.HomeScreen

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
                }
            )
        }
    }
}