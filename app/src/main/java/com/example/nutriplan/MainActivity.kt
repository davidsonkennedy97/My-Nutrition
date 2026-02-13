package com.example.nutriplan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.nutriplan.core.LanguagePreferences
import com.example.nutriplan.ui.navigation.AppNavGraph
import com.example.nutriplan.ui.theme.NutriPlanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val prefs = remember { LanguagePreferences(applicationContext) }

            val lang by prefs.languageCodeFlow.collectAsState(initial = "pt")
            val themeMode by prefs.themeModeFlow.collectAsState(initial = "light")

            val isDark = themeMode == "dark"

            NutriPlanTheme(
                darkTheme = isDark,
                dynamicColor = false
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    AppNavGraph(
                        modifier = Modifier.padding(innerPadding),
                        currentLanguage = lang,
                        isDarkTheme = isDark
                    )
                }
            }
        }
    }
}
