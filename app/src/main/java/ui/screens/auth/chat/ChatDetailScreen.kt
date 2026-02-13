package com.example.nutriplan.ui.screens.auth.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutriplan.model.Message
import com.example.nutriplan.ui.theme.PrimaryGreen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversationId: String,
    nutritionistName: String,
    onBackClick: () -> Unit,
    onArchiveClick: () -> Unit,
    currentLanguage: String = "pt",
    isDarkTheme: Boolean = false
) {
    val backgroundColor = if (isDarkTheme) Color(0xFF1C1C1C) else Color(0xFFFAF8F3)
    val messages = remember {
        mutableStateListOf(
            Message("1", conversationId, "paciente1", "Olá! Como vai?", System.currentTimeMillis() - 3600000, false),
            Message("2", conversationId, "meu_uid", "Tudo bem! Em que posso ajudar?", System.currentTimeMillis() - 3000000, true)
        )
    }
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth().height(56.dp), color = PrimaryGreen) {
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                    Text(text = nutritionistName, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Opções", tint = Color.White)
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(Color.White)) {
                            DropdownMenuItem(
                                text = { Text("Arquivar conversa", color = Color.Black) },
                                onClick = { onArchiveClick(); showMenu = false },
                                leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null, tint = Color.Black) }
                            )
                            DropdownMenuItem(
                                text = { Text("Limpar conversa", color = Color.Black) },
                                onClick = { messages.clear(); showMenu = false },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Black) }
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            MessageInputBar(
                messageText = messageText,
                onMessageTextChange = { messageText = it },
                isDarkTheme = isDarkTheme,
                onSendClick = {
                    if (messageText.isNotBlank()) {
                        messages.add(Message(UUID.randomUUID().toString(), conversationId, "meu_uid", messageText, System.currentTimeMillis(), true))
                        messageText = ""
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { message ->
                MessageBubble(message = message, isDarkTheme = isDarkTheme)
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isDarkTheme: Boolean) {
    // FORÇAR ALINHAMENTO À DIREITA SE FOR EU
    val alignment = if (message.isSentByMe) Alignment.CenterEnd else Alignment.CenterStart

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (message.isSentByMe) 16.dp else 4.dp,
                bottomEnd = if (message.isSentByMe) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isSentByMe) PrimaryGreen else (if (isDarkTheme) Color(0xFF2C2C2C) else Color.White)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.text,
                    color = if (message.isSentByMe) Color.White else (if (isDarkTheme) Color.White else Color.Black),
                    fontSize = 15.sp
                )
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (message.isSentByMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun MessageInputBar(messageText: String, onMessageTextChange: (String) -> Unit, onSendClick: () -> Unit, isDarkTheme: Boolean) {
    Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp, color = if (isDarkTheme) Color(0xFF1C1C1C) else Color.White) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = messageText, onValueChange = onMessageTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Digite sua mensagem...", color = Color.Gray) },
                shape = RoundedCornerShape(24.dp), maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                    unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                    focusedBorderColor = PrimaryGreen,
                    cursorColor = PrimaryGreen
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onSendClick, enabled = messageText.isNotBlank()) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = PrimaryGreen)
            }
        }
    }
}
