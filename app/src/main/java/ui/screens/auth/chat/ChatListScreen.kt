package com.example.nutriplan.ui.screens.auth.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.nutriplan.model.Conversation
import com.example.nutriplan.ui.theme.PrimaryGreen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    currentLanguage: String,
    isDarkTheme: Boolean,
    onConversationClick: (String) -> Unit,
    onNewChatClick: () -> Unit,
    onBackClick: () -> Unit,
    onThemeToggle: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var conversationToDelete by remember { mutableStateOf<String?>(null) }

    val tabs = listOf("Conversas", "Arquivadas", "Histórico")
    val backgroundColor = if (isDarkTheme) Color(0xFF1C1C1C) else Color(0xFFFAF8F3)

    val allConversations = remember {
        mutableStateListOf(
            Conversation(
                id = "1",
                userId = "user1",
                nutritionistId = "nutr1",
                participantName = "Dr. João Silva",
                lastMessage = "Olá, tudo bem?",
                lastMessageTime = System.currentTimeMillis() - 3600000,
                lastMessageTimestamp = System.currentTimeMillis() - 3600000,
                unreadCount = 2,
                isOnline = true,
                isPinned = true,
                isArchived = false
            ),
            Conversation(
                id = "2",
                userId = "user1",
                nutritionistId = "nutr2",
                participantName = "Nutricionista Maria",
                lastMessage = "Você: Obrigada pela ajuda!",
                lastMessageTime = System.currentTimeMillis() - 86400000,
                lastMessageTimestamp = System.currentTimeMillis() - 86400000,
                unreadCount = 0,
                isOnline = false,
                isPinned = false,
                isArchived = false
            ),
            Conversation(
                id = "3",
                userId = "user1",
                nutritionistId = "nutr3",
                participantName = "Grupo Dieta",
                lastMessage = "Pedro: Reunião amanhã às...",
                lastMessageTime = System.currentTimeMillis() - 7200000,
                lastMessageTimestamp = System.currentTimeMillis() - 7200000,
                unreadCount = 5,
                isOnline = true,
                isPinned = false,
                isArchived = false
            ),
            Conversation(
                id = "4",
                userId = "user1",
                nutritionistId = "nutr4",
                participantName = "Suporte",
                lastMessage = "Seu ticket foi resolvido",
                lastMessageTime = System.currentTimeMillis() - 172800000,
                lastMessageTimestamp = System.currentTimeMillis() - 172800000,
                unreadCount = 0,
                isOnline = false,
                isPinned = false,
                isArchived = false
            ),
            Conversation(
                id = "5",
                userId = "user1",
                nutritionistId = "nutr5",
                participantName = "Carlos Antigo",
                lastMessage = "Mensagem arquivada",
                lastMessageTime = System.currentTimeMillis() - 604800000,
                lastMessageTimestamp = System.currentTimeMillis() - 604800000,
                unreadCount = 0,
                isOnline = false,
                isPinned = false,
                isArchived = true
            )
        )
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                color = PrimaryGreen
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterStart),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (showSearchBar) {
                            IconButton(onClick = {
                                showSearchBar = false
                                searchQuery = ""
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Voltar",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        "Buscar conversas...",
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color.White
                                ),
                                singleLine = true
                            )
                        } else {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Voltar",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                text = "Chat",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    if (!showSearchBar) {
                        Row(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { showSearchBar = true }) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Buscar",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            IconButton(onClick = onThemeToggle) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle theme",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewChatClick,
                containerColor = PrimaryGreen
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Nova conversa",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = backgroundColor,
                contentColor = if (isDarkTheme) Color.White else Color.Black
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) PrimaryGreen else (if (isDarkTheme) Color.White else Color.Black)
                            )
                        }
                    )
                }
            }

            val filteredConversations = allConversations.filter { conversation ->
                val matchesSearch = searchQuery.isEmpty() ||
                        conversation.participantName.contains(searchQuery, ignoreCase = true) ||
                        conversation.lastMessage.contains(searchQuery, ignoreCase = true)
                val matchesTab = when (selectedTab) {
                    0 -> !conversation.isArchived
                    1 -> conversation.isArchived
                    else -> true
                }
                matchesSearch && matchesTab
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredConversations) { conversation ->
                    ChatListItem(
                        conversation = conversation,
                        isDarkTheme = isDarkTheme,
                        onClick = { onConversationClick(conversation.id) },
                        onDelete = {
                            conversationToDelete = conversation.id
                            showDeleteDialog = true
                        },
                        onArchive = {
                            val index = allConversations.indexOfFirst { it.id == conversation.id }
                            if (index != -1) {
                                allConversations[index] = conversation.copy(
                                    isArchived = !conversation.isArchived
                                )
                            }
                        }
                    )
                    HorizontalDivider(
                        color = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Deletar conversa") },
            text = { Text("Tem certeza que deseja deletar esta conversa?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        conversationToDelete?.let { id ->
                            allConversations.removeIf { it.id == id }
                        }
                        showDeleteDialog = false
                        conversationToDelete = null
                    }
                ) {
                    Text("Deletar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ChatListItem(
    conversation: Conversation,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onArchive: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val backgroundColor = if (isDarkTheme) Color(0xFF1C1C1C) else Color(0xFFFAF8F3)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(PrimaryGreen.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = conversation.participantName.firstOrNull()?.uppercase() ?: "?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen
            )
        }

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
                        tint = PrimaryGreen
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = conversation.participantName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    color = if (isDarkTheme) Color.White else Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatTimestamp(conversation.lastMessageTime),
                    fontSize = 12.sp,
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.lastMessage,
                    fontSize = 14.sp,
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f),
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
                            .background(PrimaryGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = conversation.unreadCount.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Opções",
                    tint = if (isDarkTheme) Color.White else Color.Black
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                containerColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color.White
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            if (conversation.isArchived) "Desarquivar" else "Arquivar",
                            color = if (isDarkTheme) Color.White else Color.Black
                        )
                    },
                    onClick = {
                        onArchive()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Archive,
                            contentDescription = null,
                            tint = if (isDarkTheme) Color.White else Color.Black
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Deletar", color = Color.Red) },
                    onClick = {
                        onDelete()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color.Red
                        )
                    }
                )
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 86400000 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        diff < 172800000 -> "Ontem"
        diff < 604800000 -> SimpleDateFormat("EEE", Locale("pt", "BR")).format(Date(timestamp))
        else -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(timestamp))
    }
}