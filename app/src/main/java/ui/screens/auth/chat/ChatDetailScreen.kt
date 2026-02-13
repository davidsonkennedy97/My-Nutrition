package com.example.nutriplan.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutriplan.data.model.Message
import com.example.nutriplan.data.model.MessageType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversationId: String,
    participantName: String,
    currentLanguage: String,
    isDarkTheme: Boolean,
    onBackClick: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Mensagens mockadas para demonstração
    val messages = remember {
        mutableStateListOf(
            Message(
                id = "1",
                conversationId = conversationId,
                senderId = "other",
                text = "Olá! Como posso ajudar com sua dieta hoje?",
                timestamp = System.currentTimeMillis() - 3600000,
                isRead = true,
                isSentByMe = false
            ),
            Message(
                id = "2",
                conversationId = conversationId,
                senderId = "me",
                text = "Oi! Gostaria de saber sobre alimentos ricos em proteína.",
                timestamp = System.currentTimeMillis() - 3500000,
                isRead = true,
                isSentByMe = true
            ),
            Message(
                id = "3",
                conversationId = conversationId,
                senderId = "other",
                text = "Claro! Alguns alimentos ricos em proteína são: frango, peixe, ovos, feijão, lentilha e quinoa. Você tem alguma restrição alimentar?",
                timestamp = System.currentTimeMillis() - 3400000,
                isRead = true,
                isSentByMe = false
            ),
            Message(
                id = "4",
                conversationId = conversationId,
                senderId = "me",
                text = "Não como carne vermelha, mas consumo frango e peixe.",
                timestamp = System.currentTimeMillis() - 3300000,
                isRead = true,
                isSentByMe = true
            ),
            Message(
                id = "5",
                conversationId = conversationId,
                senderId = "other",
                text = "Perfeito! Vou criar um plano alimentar focado em frango, peixe e fontes vegetais de proteína.",
                timestamp = System.currentTimeMillis() - 3200000,
                isRead = false,
                isSentByMe = false
            )
        )
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            participantName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Online",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Chamada de vídeo */ }) {
                        Icon(Icons.Default.Call, "Ligar")
                    }
                    IconButton(onClick = { /* Mais opções */ }) {
                        Icon(Icons.Default.MoreVert, "Menu")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            ChatInputBar(
                messageText = messageText,
                onMessageTextChange = { messageText = it },
                onSendMessage = {
                    if (messageText.isNotBlank()) {
                        messages.add(
                            Message(
                                id = UUID.randomUUID().toString(),
                                conversationId = conversationId,
                                senderId = "me",
                                text = messageText,
                                timestamp = System.currentTimeMillis(),
                                isRead = false,
                                isSentByMe = true
                            )
                        )
                        messageText = ""
                    }
                },
                onAttachFile = { /* Anexar arquivo */ }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages) { message ->
                MessageBubble(message = message)
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isSentByMe) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isSentByMe) {
            // Avatar do remetente
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "N",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (message.isSentByMe) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.isSentByMe) 16.dp else 4.dp,
                    bottomEnd = if (message.isSentByMe) 4.dp else 16.dp
                ),
                color = if (message.isSentByMe) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    fontSize = 15.sp,
                    color = if (message.isSentByMe) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (message.isSentByMe) Arrangement.End else Arrangement.Start
            ) {
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(Date(message.timestamp)),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (message.isSentByMe) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (message.isRead) Icons.Default.Done else Icons.Default.Check,
                        contentDescription = if (message.isRead) "Lido" else "Enviado",
                        modifier = Modifier.size(14.dp),
                        tint = if (message.isRead) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ChatInputBar(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onAttachFile: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botão de anexo
            IconButton(onClick = onAttachFile) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Anexar",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Campo de texto
            TextField(
                value = messageText,
                onValueChange = onMessageTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Mensagem...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Botão de enviar
            IconButton(
                onClick = onSendMessage,
                enabled = messageText.isNotBlank()
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Enviar",
                    tint = if (messageText.isNotBlank()) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}