package com.example.nutriplan.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.nutriplan.core.LanguagePreferences
import com.example.nutriplan.core.contextWithLocale
import com.example.nutriplan.ui.theme.PrimaryGreen
import com.example.nutriplan.ui.util.PhoneBrVisualTransformation
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    currentLanguage: String,
    isDarkTheme: Boolean,
    onBackToLogin: () -> Unit
) {
    val baseContext = LocalContext.current
    val configuration = LocalConfiguration.current
    val localizedContext = remember(currentLanguage, configuration) {
        contextWithLocale(baseContext, currentLanguage)
    }

    val prefs = remember { LanguagePreferences(baseContext.applicationContext) }
    val scope = rememberCoroutineScope()

    val nextLanguage = if (currentLanguage == "pt") "en" else "pt"
    val languageButtonLabel = if (currentLanguage == "pt") "EN" else "PT"

    val nextThemeMode = if (isDarkTheme) "light" else "dark"
    val themeIcon = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode
    val themeContentDesc = if (isDarkTheme) "Mudar para claro" else "Mudar para escuro"

    var phone by remember { mutableStateOf("") }
    var generatedCode by remember { mutableStateOf<String?>(null) }
    var typedCode by remember { mutableStateOf("") }
    var validationMsg by remember { mutableStateOf<String?>(null) }
    var codeValidated by remember { mutableStateOf(false) }

    var newPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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
            TextButton(onClick = { scope.launch { prefs.setLanguage(nextLanguage) } }) {
                Text(languageButtonLabel)
            }
            IconButton(onClick = { scope.launch { prefs.setThemeMode(nextThemeMode) } }) {
                Icon(
                    imageVector = themeIcon,
                    contentDescription = themeContentDesc,
                    tint = if (isDarkTheme) Color.White else Color.Black
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 56.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = localizedContext.getString(R.string.forgot_password_title),
                style = MaterialTheme.typography.headlineSmall,
                color = PrimaryGreen,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it.filter { c -> c.isDigit() }.take(11) },
                label = { Text(localizedContext.getString(R.string.phone_number_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = tfColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                visualTransformation = PhoneBrVisualTransformation()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (phone.filter { it.isDigit() }.length != 11) return@Button

                    val n = Random.nextInt(0, 10000)
                    generatedCode = n.toString().padStart(4, '0')
                    typedCode = ""
                    validationMsg = null
                    codeValidated = false
                    newPassword = ""
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = phone.filter { it.isDigit() }.length == 11
            ) {
                Text(localizedContext.getString(R.string.send_code))
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = typedCode,
                onValueChange = { typedCode = it.filter { c -> c.isDigit() }.take(4) },
                label = { Text(localizedContext.getString(R.string.code_4_digits)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = tfColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            if (generatedCode != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = localizedContext.getString(R.string.your_code_test, generatedCode!!),
                    color = if (isDarkTheme) Color.White else Color.Black
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    when {
                        generatedCode == null -> validationMsg = localizedContext.getString(R.string.click_validate_email_first)
                        typedCode.length != 4 -> validationMsg = localizedContext.getString(R.string.enter_4_digits)
                        typedCode == generatedCode -> {
                            validationMsg = localizedContext.getString(R.string.code_valid_reset)
                            codeValidated = true
                        }
                        else -> validationMsg = localizedContext.getString(R.string.invalid_code)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(localizedContext.getString(R.string.confirm_code))
            }

            if (validationMsg != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = validationMsg!!,
                    color = if (isDarkTheme) Color.White else Color.Black
                )
            }

            if (codeValidated) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text(localizedContext.getString(R.string.new_password_min_6)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = tfColors,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible)
                            localizedContext.getString(R.string.hide_password)
                        else
                            localizedContext.getString(R.string.show_password)

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = icon, contentDescription = description)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (newPassword.length < 6) {
                            validationMsg = localizedContext.getString(R.string.password_min_6_chars)
                            return@Button
                        }

                        validationMsg = localizedContext.getString(R.string.password_reset_success)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = newPassword.length >= 6
                ) {
                    Text(localizedContext.getString(R.string.reset_password))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onBackToLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(localizedContext.getString(R.string.back))
            }
        }
    }
}
