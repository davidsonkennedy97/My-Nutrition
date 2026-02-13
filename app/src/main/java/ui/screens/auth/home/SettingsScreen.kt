package com.example.nutriplan.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.nutriplan.R
import com.example.nutriplan.core.InMemoryAuth
import com.example.nutriplan.ui.theme.PrimaryGreen

@Composable
fun SettingsScreen(
    localizedContext: android.content.Context,
    isDarkTheme: Boolean,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    onThemeChange: () -> Unit
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("admin") }
    var userEmail by remember { mutableStateOf("admin@admin.com") }
    var userPhone by remember { mutableStateOf("1") }
    var userCRN by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = localizedContext.getString(R.string.menu_settings),
            style = MaterialTheme.typography.headlineSmall,
            color = if (isDarkTheme) Color.White else Color.Black,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = PrimaryGreen, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(localizedContext.getString(R.string.profile), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isDarkTheme) Color.White else Color.Black)
                    }
                    TextButton(onClick = { showEditProfileDialog = true }) { Text(localizedContext.getString(R.string.edit), color = PrimaryGreen) }
                }
                Spacer(modifier = Modifier.height(8.dp))
                ProfileItem(localizedContext.getString(R.string.name), userName, isDarkTheme)
                ProfileItem("Email", userEmail, isDarkTheme)
                ProfileItem(localizedContext.getString(R.string.phone), userPhone, isDarkTheme)
                ProfileItem("CRN", userCRN, isDarkTheme)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color.White)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, null, tint = PrimaryGreen, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(localizedContext.getString(R.string.language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isDarkTheme) Color.White else Color.Black)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = currentLanguage == "pt", onClick = { onLanguageChange("pt") }, label = { Text("PT") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryGreen, selectedLabelColor = Color.White))
                    FilterChip(selected = currentLanguage == "en", onClick = { onLanguageChange("en") }, label = { Text("EN") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryGreen, selectedLabelColor = Color.White))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color.White)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode, null, tint = PrimaryGreen, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(localizedContext.getString(R.string.theme), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isDarkTheme) Color.White else Color.Black)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !isDarkTheme, onClick = { if (isDarkTheme) onThemeChange() }, label = { Text(localizedContext.getString(R.string.light)) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryGreen, selectedLabelColor = Color.White))
                    FilterChip(selected = isDarkTheme, onClick = { if (!isDarkTheme) onThemeChange() }, label = { Text(localizedContext.getString(R.string.dark)) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryGreen, selectedLabelColor = Color.White))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color.White), onClick = { showChangePasswordDialog = true }) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, null, tint = PrimaryGreen, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(localizedContext.getString(R.string.change_password), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isDarkTheme) Color.White else Color.Black)
                }
                Icon(Icons.Default.ChevronRight, null, tint = if (isDarkTheme) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("My Nutrition", style = MaterialTheme.typography.titleMedium, color = PrimaryGreen, fontWeight = FontWeight.Bold)
            Text("by: Gabrielle Alves", style = MaterialTheme.typography.bodySmall, color = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f))
            Text(localizedContext.getString(R.string.version), style = MaterialTheme.typography.labelSmall, color = if (isDarkTheme) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.4f))
        }
    }
    if (showEditProfileDialog) {
        EditProfileDialog(userName, userEmail, userPhone, userCRN, isDarkTheme, localizedContext, { showEditProfileDialog = false }) { n, e, p, c -> userName = n; userEmail = e; userPhone = p; userCRN = c; showEditProfileDialog = false }
    }
    if (showChangePasswordDialog) {
        ChangePasswordDialog(isDarkTheme, localizedContext, { showChangePasswordDialog = false })
    }
}

@Composable
fun ProfileItem(label: String, value: String, isDarkTheme: Boolean) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = if (isDarkTheme) Color.White else Color.Black)
    }
}

@Composable
fun EditProfileDialog(userName: String, userEmail: String, userPhone: String, userCRN: String, isDarkTheme: Boolean, localizedContext: android.content.Context, onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf(userName) }
    var email by remember { mutableStateOf(userEmail) }
    var phone by remember { mutableStateOf(userPhone) }
    var crn by remember { mutableStateOf(userCRN) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = { onSave(name, email, phone, crn) }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)) { Text(localizedContext.getString(R.string.save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(localizedContext.getString(R.string.cancel)) } },
        title = { Text(localizedContext.getString(R.string.edit_profile)) },
        text = {
            Column {
                OutlinedTextField(name, { name = it }, label = { Text(localizedContext.getString(R.string.name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(phone, { phone = it.filter { c -> c.isDigit() }.take(11) }, label = { Text(localizedContext.getString(R.string.phone)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(crn, { crn = it }, label = { Text("CRN") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        containerColor = if (isDarkTheme) Color(0xFF1C1C1C) else Color.White
    )
}

@Composable
fun ChangePasswordDialog(isDarkTheme: Boolean, localizedContext: android.content.Context, onDismiss: () -> Unit) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }
    var visible1 by remember { mutableStateOf(false) }
    var visible2 by remember { mutableStateOf(false) }
    var visible3 by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    errorMsg = null
                    successMsg = null
                    when {
                        currentPassword != InMemoryAuth.registeredPassword -> {
                            errorMsg = localizedContext.getString(R.string.wrong_current_password)
                        }
                        newPassword.length < 6 -> {
                            errorMsg = localizedContext.getString(R.string.password_too_short)
                        }
                        newPassword != confirmPassword -> {
                            errorMsg = localizedContext.getString(R.string.passwords_dont_match)
                        }
                        else -> {
                            InMemoryAuth.registeredPassword = newPassword
                            successMsg = localizedContext.getString(R.string.password_changed_success)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) { Text(localizedContext.getString(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(localizedContext.getString(R.string.cancel)) } },
        title = { Text(localizedContext.getString(R.string.change_password)) },
        text = {
            Column {
                OutlinedTextField(currentPassword, { currentPassword = it }, label = { Text(localizedContext.getString(R.string.current_password)) }, singleLine = true, visualTransformation = if (visible1) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { visible1 = !visible1 }) { Icon(if (visible1) Icons.Default.Visibility else Icons.Default.VisibilityOff, null) } }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(newPassword, { newPassword = it }, label = { Text(localizedContext.getString(R.string.new_password)) }, singleLine = true, visualTransformation = if (visible2) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { visible2 = !visible2 }) { Icon(if (visible2) Icons.Default.Visibility else Icons.Default.VisibilityOff, null) } }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(localizedContext.getString(R.string.confirm_password)) },
                    singleLine = true,
                    visualTransformation = if (visible3) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { visible3 = !visible3 }) {
                            Icon(if (visible3) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMsg!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }

                if (successMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(successMsg!!, color = PrimaryGreen, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        containerColor = if (isDarkTheme) Color(0xFF1C1C1C) else Color.White
    )
}
