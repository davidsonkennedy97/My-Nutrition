package com.example.nutriplan.ui.screens.auth

import androidx.compose.foundation.BorderStroke
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
import com.example.nutriplan.core.InMemoryAuth
import com.example.nutriplan.core.LanguagePreferences
import com.example.nutriplan.core.contextWithLocale
import com.example.nutriplan.ui.theme.PrimaryGreen
import com.example.nutriplan.ui.util.PhoneBrVisualTransformation
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun RegisterScreen(
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

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var crn by remember { mutableStateOf("") }

    var generatedCode by remember { mutableStateOf<String?>(null) }
    var typedCode by remember { mutableStateOf("") }
    var validationMsg by remember { mutableStateOf<String?>(null) }

    var showRequiredDialog by remember { mutableStateOf(false) }

    var firstNameError by remember { mutableStateOf(false) }
    var lastNameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var codeError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    val canSubmit =
        firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                email.isNotBlank() &&
                typedCode.length == 4 &&
                phone.filter { it.isDigit() }.length == 11 &&
                password.length >= 6

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
        unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black,
        cursorColor = if (isDarkTheme) Color.White else Color.Black
    )

    val canValidateEmail =
        email.trim().endsWith(".com", ignoreCase = true) ||
                email.trim().endsWith(".br", ignoreCase = true)

    val validateEmailBg = if (canValidateEmail) PrimaryGreen else Color(0xFFBDBDBD)

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
                text = localizedContext.getString(R.string.sign_up_nutritionist),
                style = MaterialTheme.typography.headlineSmall,
                color = PrimaryGreen,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = firstName,
                onValueChange = {
                    firstName = it
                    if (firstNameError && firstName.isNotBlank()) firstNameError = false
                },
                label = { Text(localizedContext.getString(R.string.first_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = tfColors,
                isError = firstNameError,
                supportingText = { if (firstNameError) Text(localizedContext.getString(R.string.required)) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = lastName,
                onValueChange = {
                    lastName = it
                    if (lastNameError && lastName.isNotBlank()) lastNameError = false
                },
                label = { Text(localizedContext.getString(R.string.last_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = tfColors,
                isError = lastNameError,
                supportingText = { if (lastNameError) Text(localizedContext.getString(R.string.required)) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailError && email.isNotBlank()) emailError = false
                },
                label = { Text(localizedContext.getString(R.string.email)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = tfColors,
                isError = emailError,
                supportingText = { if (emailError) Text(localizedContext.getString(R.string.required)) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (!canValidateEmail) return@Button
                    val n = Random.nextInt(0, 10000)
                    generatedCode = n.toString().padStart(4, '0')
                    typedCode = ""
                    validationMsg = null
                    codeError = false
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = validateEmailBg,
                    contentColor = Color.White
                )
            ) {
                Text(localizedContext.getString(R.string.validate_email))
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = typedCode,
                onValueChange = {
                    typedCode = it.filter { c -> c.isDigit() }.take(4)
                    if (codeError && typedCode.length == 4) codeError = false
                },
                label = { Text(localizedContext.getString(R.string.code_4_digits)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = tfColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = codeError,
                supportingText = { if (codeError) Text(localizedContext.getString(R.string.required)) }
            )

            if (generatedCode != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = localizedContext.getString(R.string.your_code_test, generatedCode!!),
                    color = if (isDarkTheme) Color.White else Color.Black
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    validationMsg = when {
                        generatedCode == null -> localizedContext.getString(R.string.click_validate_email_first)
                        typedCode.length != 4 -> localizedContext.getString(R.string.enter_4_digits)
                        typedCode == generatedCode -> localizedContext.getString(R.string.email_validated)
                        else -> localizedContext.getString(R.string.invalid_code)
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

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it.filter { c -> c.isDigit() }.take(11)
                    if (phoneError && phone.length == 11) phoneError = false
                },
                label = { Text(localizedContext.getString(R.string.phone_number)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = tfColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                visualTransformation = PhoneBrVisualTransformation(),
                isError = phoneError,
                supportingText = { if (phoneError) Text(localizedContext.getString(R.string.required)) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (passwordError && password.length >= 6) passwordError = false
                },
                label = { Text(localizedContext.getString(R.string.password_min_6)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = tfColors,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = passwordError,
                supportingText = { if (passwordError) Text(localizedContext.getString(R.string.required_min_6)) },
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

            OutlinedTextField(
                value = crn,
                onValueChange = { crn = it },
                label = { Text(localizedContext.getString(R.string.crn_optional)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = tfColors
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBackToLogin,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    border = BorderStroke(1.dp, if (isDarkTheme) Color.White else Color.Black),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = if (isDarkTheme) Color.White else Color.Black
                    )
                ) {
                    Text(localizedContext.getString(R.string.back))
                }

                Button(
                    onClick = {
                        val missingFirst = firstName.isBlank()
                        val missingLast = lastName.isBlank()
                        val missingEmail = email.isBlank()
                        val missingCode = typedCode.length != 4
                        val missingPhone = phone.filter { it.isDigit() }.length != 11
                        val missingPassword = password.length < 6

                        firstNameError = missingFirst
                        lastNameError = missingLast
                        emailError = missingEmail
                        codeError = missingCode
                        phoneError = missingPhone
                        passwordError = missingPassword

                        val hasAnyMissing =
                            missingFirst || missingLast || missingEmail || missingCode || missingPhone || missingPassword

                        if (hasAnyMissing) {
                            showRequiredDialog = true
                            return@Button
                        }

                        InMemoryAuth.hasRegistration = true
                        InMemoryAuth.registeredPhoneDigits = phone.filter { it.isDigit() }
                        InMemoryAuth.registeredPassword = password

                        onBackToLogin()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    enabled = canSubmit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = Color.White,
                        disabledContainerColor = PrimaryGreen.copy(alpha = 0.45f),
                        disabledContentColor = Color.White.copy(alpha = 0.75f)
                    )
                ) {
                    Text(localizedContext.getString(R.string.register_action))
                }
            }
        }

        if (showRequiredDialog) {
            AlertDialog(
                onDismissRequest = { showRequiredDialog = false },
                title = { Text(localizedContext.getString(R.string.attention), color = if (isDarkTheme) Color.White else Color.Black) },
                text = { Text(localizedContext.getString(R.string.fill_required_fields), color = if (isDarkTheme) Color.White else Color.Black) },
                confirmButton = {
                    Button(onClick = { showRequiredDialog = false }) {
                        Text(localizedContext.getString(R.string.ok))
                    }
                },
                containerColor = if (isDarkTheme) Color(0xFF1C1C1C) else Color.White
            )
        }
    }
}