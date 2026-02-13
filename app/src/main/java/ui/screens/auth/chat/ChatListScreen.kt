package com.example.nutriplan.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutriplan.data.model.Conversation
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    currentLanguage: String,
    isDarkTheme: Boolean,
    onConversationClick: (String) -> Unit,
    onNewChatClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Conversas", "Arquivadas", "Histórico")

    // Dados mockados para demonstração
    val conversations = remember {
        listOf(
            Conversation(
                id = "1",
                participantName = "Dr. João Silva",
                lastMessage = "Olá, tudo bem?",
                lastMessageTime = System.currentTimeMillis() - 3600000,
                unreadCount = 2,
                isOnline = true,
                isPinned = true
            ),
            Conversation(
                id = "2",
                participantName = "Nutricionista Maria",
                lastMessage = "Você: Obrigada pela ajuda!",
                lastMessageTime = System.currentTimeMillis() - 86400000,
                unreadCount = 0,
                isOnline = false
            ),
            Conversation(
                id = "3",
                participantName = "Grupo Dieta",
                lastMessage = "Pedro: Reunião amanhã às...",
                lastMessageTime = System.currentTimeMillis() - 7200000,
                unreadCount = 5,
                isOnline = true
            ),
            Conversation(
                id = "4",
                participantName = "Suporte",
                lastMessage = "Seu ticket foi resolvido",
                lastMessageTime = System.currentTimeMillis() - 172800000,
                unreadCount = 0,
                isOnline = false
            )
        )
    }

    val archivedConversations = remember {
        listOf(
            Conversation(
                id = "5",
                participantName = "Carlos Antigo",
                lastMessage = "Mensagem arquivada",
                lastMessageTime = System.currentTimeMillis() - 604800000,
                unreadCount = 0,
                isArchived = true
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chat",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Busca */ }) {
                        Icon(Icons.Default.Search, "Buscar")
                    }
                    IconButton(onClick = { /* Menu */ }) {
                        Icon(Icons.Default.MoreVert, "Menu")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewChatClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Edit, "Nova conversa")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Lista de conversas
            val displayList = when (selectedTab) {
                0 -> conversations.filter { !it.isArchived }
                1 -> archivedConversations
                2 -> conversations // Histórico completo
                else -> conversations
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayList) { conversation ->
                    ChatListItem(
                        conversation = conversation,
                        onClick = { onConversationClick(conversation.id) }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
fun ChatListItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = conversation.participantName.first().uppercase(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        // Status online (bolinha verde)
        if (conversation.isOnline) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .offset(x = (-14).dp, y = 18.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50))
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Conteúdo
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (conversation.isPinned) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Fixado",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = conversation.participantName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = formatTimestamp(conversation.lastMessageTime),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.lastMessage,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (conversation.unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = conversation.unreadCount.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Menu de opções
        IconButton(onClick = { /* Menu de opções */ }) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "Opções",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 86400000 -> { // Menos de 24h
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        }
        diff < 172800000 -> "Ontem" // Menos de 48h
        diff < 604800000 -> { // Menos de 7 dias
            SimpleDateFormat("EEE", Locale("pt", "BR")).format(Date(timestamp))
        }
        else -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(timestamp))
    }
}