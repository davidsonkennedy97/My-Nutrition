package com.example.nutriplan.ui.screens.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nutriplan.R
import com.example.nutriplan.core.InMemoryAuth
import com.example.nutriplan.core.LanguagePreferences
import com.example.nutriplan.core.contextWithLocale
import com.example.nutriplan.ui.theme.PrimaryGreen
import com.example.nutriplan.ui.util.PhoneBrVisualTransformation
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    currentLanguage: String,
    isDarkTheme: Boolean,
    onGoToRegister: () -> Unit,
    onGoToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit
)
 {
    val baseContext = LocalContext.current
    val configuration = LocalConfiguration.current

    val localizedContext = remember(currentLanguage, configuration) {
        contextWithLocale(baseContext, currentLanguage)
    }

    val prefs = remember { LanguagePreferences(baseContext.applicationContext) }
    val scope = rememberCoroutineScope()

    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var showNoRegistrationDialog by remember { mutableStateOf(false) }
    var showWrongCredentialsDialog by remember { mutableStateOf(false) }
    var showUnderConstructionDialog by remember { mutableStateOf(false) }

    val nextLanguage = if (currentLanguage == "pt") "en" else "pt"
    val languageButtonLabel = if (currentLanguage == "pt") "EN" else "PT"

    val nextThemeMode = if (isDarkTheme) "light" else "dark"
    val themeIcon = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode
    val themeContentDesc = if (isDarkTheme) "Mudar para claro" else "Mudar para escuro"

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
        unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black,
        cursorColor = if (isDarkTheme) Color.White else Color.Black
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.align(Alignment.TopEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { scope.launch { prefs.setLanguage(nextLanguage) } }
            ) {
                Text(languageButtonLabel)
            }

            IconButton(
                onClick = { scope.launch { prefs.setThemeMode(nextThemeMode) } }
            ) {
                Icon(
                    imageVector = themeIcon,
                    contentDescription = themeContentDesc,
                    tint = if (isDarkTheme) Color.White else Color.Black
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
        ) {
            Text(
                text = localizedContext.getString(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                color = PrimaryGreen,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it.filter { c -> c.isDigit() }.take(11) },
                label = { Text(localizedContext.getString(R.string.phone)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = tfColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                visualTransformation = PhoneBrVisualTransformation()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(localizedContext.getString(R.string.password)) },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = tfColors,
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) "Ocultar senha" else "Mostrar senha"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = icon, contentDescription = description)
                    }
                }
            )

            // Esqueci minha senha (pequeno e discreto, canto esquerdo)
            TextButton(
                onClick = onGoToForgotPassword,
                modifier = Modifier.padding(start = 0.dp, top = 2.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = localizedContext.getString(R.string.forgot_password_link),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val phoneDigits = phone.filter { it.isDigit() }

                    if (!InMemoryAuth.hasRegistration) {
                        showNoRegistrationDialog = true
                        return@Button
                    }

                    if (phoneDigits != InMemoryAuth.registeredPhoneDigits || password != InMemoryAuth.registeredPassword) {
                        showWrongCredentialsDialog = true
                        return@Button
                    }

                    onLoginSuccess()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(localizedContext.getString(R.string.sign_in))
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onGoToRegister,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(localizedContext.getString(R.string.sign_up_nutritionist))
            }
        }

        Text(
            text = "by: Gabrielle Alves",
            color = PrimaryGreen,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.BottomStart)
        )

        if (showNoRegistrationDialog) {
            AlertDialog(
                onDismissRequest = { showNoRegistrationDialog = false },
                title = { Text(localizedContext.getString(R.string.attention), color = if (isDarkTheme) Color.White else Color.Black) },
                text = { Text(localizedContext.getString(R.string.no_registration), color = if (isDarkTheme) Color.White else Color.Black) },
                confirmButton = {
                    Button(onClick = { showNoRegistrationDialog = false }) {
                        Text(localizedContext.getString(R.string.ok))
                    }
                },
                containerColor = if (isDarkTheme) Color(0xFF1C1C1C) else Color.White
            )
        }

        if (showWrongCredentialsDialog) {
            AlertDialog(
                onDismissRequest = { showWrongCredentialsDialog = false },
                title = { Text(localizedContext.getString(R.string.error), color = if (isDarkTheme) Color.White else Color.Black) },
                text = { Text(localizedContext.getString(R.string.wrong_credentials), color = if (isDarkTheme) Color.White else Color.Black) },
                confirmButton = {
                    Button(onClick = { showWrongCredentialsDialog = false }) {
                        Text(localizedContext.getString(R.string.ok))
                    }
                },
                containerColor = if (isDarkTheme) Color(0xFF1C1C1C) else Color.White
            )
        }

        if (showUnderConstructionDialog) {
            AlertDialog(
                onDismissRequest = { showUnderConstructionDialog = false },
                title = { Text(localizedContext.getString(R.string.success), color = if (isDarkTheme) Color.White else Color.Black) },
                text = { Text(localizedContext.getString(R.string.under_construction), color = if (isDarkTheme) Color.White else Color.Black) },
                confirmButton = {
                    Button(onClick = { showUnderConstructionDialog = false }) {
                        Text(localizedContext.getString(R.string.ok))
                    }
                },
                containerColor = if (isDarkTheme) Color(0xFF1C1C1C) else Color.White
            )
        }
    }
}
