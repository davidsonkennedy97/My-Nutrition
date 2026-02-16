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
import androidx.compose.material.icons.filled.*
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
    val myId = "meu_id"

    // LISTA DE MENSAGENS QUE MUDA CONFORME O CONTATO
    val messages = remember(conversationId) {
        when (conversationId) {
            "1" -> mutableStateListOf(Message("1", "1", "paciente", "Olá, tudo bem?", System.currentTimeMillis() - 3600000, false))
            "2" -> mutableStateListOf(Message("2", "2", "paciente", "Obrigada pela ajuda!", System.currentTimeMillis() - 86400000, false))
            "3" -> mutableStateListOf(Message("3", "3", "paciente", "Pedro: Reunião amanhã às...", System.currentTimeMillis() - 7200000, false))
            "4" -> mutableStateListOf(Message("4", "4", "paciente", "Seu ticket foi resolvido", System.currentTimeMillis() - 172800000, false))
            "5" -> mutableStateListOf(Message("5", "5", "paciente", "Mensagem arquivada", System.currentTimeMillis() - 604800000, false))
            else -> mutableStateListOf<Message>()
        }
    }

    var textState by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth().height(56.dp), color = PrimaryGreen) {
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                    Text(text = nutritionistName, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Box {
                        IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, null, tint = Color.White) }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(Color.White)) {
                            DropdownMenuItem(text = { Text("Arquivar conversa", color = Color.Black) }, onClick = { onArchiveClick(); showMenu = false })
                            DropdownMenuItem(text = { Text("Limpar conversa", color = Color.Black) }, onClick = { messages.clear(); showMenu = false })
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp, color = if (isDarkTheme) Color(0xFF1C1C1C) else Color.White) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = textState, onValueChange = { textState = it },
                        modifier = Modifier.weight(1f), placeholder = { Text("Digite sua mensagem...") },
                        shape = RoundedCornerShape(24.dp), maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = if (isDarkTheme) Color.White else Color.Black, unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black, focusedBorderColor = PrimaryGreen, cursorColor = PrimaryGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        if (textState.isNotBlank()) {
                            messages.add(Message(UUID.randomUUID().toString(), conversationId, myId, textState, System.currentTimeMillis(), true))
                            textState = ""
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, tint = PrimaryGreen)
                    }
                }
            }
        }
    ) { p ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(p), state = listState, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(messages) { msg ->
                val isMe = msg.senderId == myId
                val align = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = align) {
                    Card(
                        modifier = Modifier.widthIn(max = 280.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isMe) PrimaryGreen else (if (isDarkTheme) Color(0xFF2C2C2C) else Color.White))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = msg.text, color = if (isMe) Color.White else (if (isDarkTheme) Color.White else Color.Black), fontSize = 15.sp)
                            Text(text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp)), style = MaterialTheme.typography.labelSmall, color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray, modifier = Modifier.align(Alignment.End))
                        }
                    }
                }
            }
        }
    }
}
