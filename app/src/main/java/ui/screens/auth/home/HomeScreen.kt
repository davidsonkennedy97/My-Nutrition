package com.example.nutriplan.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nutriplan.R
import com.example.nutriplan.core.LanguagePreferences
import com.example.nutriplan.core.contextWithLocale
import com.example.nutriplan.ui.theme.PrimaryGreen
import kotlinx.coroutines.launch

data class MenuItem(
    val titleRes: Int,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    currentLanguage: String,
    isDarkTheme: Boolean,
    onLogout: () -> Unit
) {
    val baseContext = LocalContext.current
    val configuration = LocalConfiguration.current
    val localizedContext = remember(currentLanguage, configuration) {
        contextWithLocale(baseContext, currentLanguage)
    }

    val prefs = remember { LanguagePreferences(baseContext.applicationContext) }
    val scope = rememberCoroutineScope()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var selectedMenuItem by remember { mutableStateOf("home") }

    val nextLanguage = if (currentLanguage == "pt") "en" else "pt"
    val nextThemeMode = if (isDarkTheme) "light" else "dark"

    val menuItems = listOf(
        MenuItem(R.string.menu_home, Icons.Default.Home, "home"),
        MenuItem(R.string.menu_patients, Icons.Default.Person, "patients"),
        MenuItem(R.string.menu_appointments, Icons.Default.DateRange, "appointments"),
        MenuItem(R.string.menu_favorites, Icons.Default.Star, "favorites"),
        MenuItem(R.string.menu_recipes, Icons.Default.Restaurant, "recipes"),
        MenuItem(R.string.menu_chat, Icons.Default.Chat, "chat"),
        MenuItem(R.string.menu_settings, Icons.Default.Settings, "settings")
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = if (isDarkTheme) Color(0xFF1C1C1C) else Color.White
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = localizedContext.getString(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    color = PrimaryGreen,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold
                )

                Divider()

                menuItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(localizedContext.getString(item.titleRes)) },
                        selected = selectedMenuItem == item.route,
                        onClick = {
                            selectedMenuItem = item.route
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = PrimaryGreen.copy(alpha = 0.2f),
                            selectedIconColor = PrimaryGreen,
                            selectedTextColor = PrimaryGreen,
                            unselectedIconColor = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f),
                            unselectedTextColor = if (isDarkTheme) Color.White else Color.Black
                        )
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
                    label = { Text(localizedContext.getString(R.string.menu_logout)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = Color.Red.copy(alpha = 0.8f),
                        unselectedTextColor = Color.Red.copy(alpha = 0.8f)
                    )
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    color = PrimaryGreen
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Menu + Nome da página (esquerda)
                        Row(
                            modifier = Modifier.align(Alignment.CenterStart),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Text(
                                text = when (selectedMenuItem) {
                                    "home" -> localizedContext.getString(R.string.menu_home)
                                    "patients" -> localizedContext.getString(R.string.menu_patients)
                                    "appointments" -> localizedContext.getString(R.string.menu_appointments)
                                    "favorites" -> localizedContext.getString(R.string.menu_favorites)
                                    "recipes" -> localizedContext.getString(R.string.menu_recipes)
                                    "chat" -> localizedContext.getString(R.string.menu_chat)
                                    "settings" -> localizedContext.getString(R.string.menu_settings)
                                    else -> localizedContext.getString(R.string.menu_home)
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Normal
                            )
                        }

                        // My Nutrition (centro com offset para direita)
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(x = 8.dp)
                        ) {
                            Text(
                                text = localizedContext.getString(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // PT/EN + Lua/Sol (direita)
                        Row(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            TextButton(
                                onClick = { scope.launch { prefs.setLanguage(nextLanguage) } },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text(
                                    text = if (currentLanguage == "pt") "EN" else "PT",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(
                                onClick = { scope.launch { prefs.setThemeMode(nextThemeMode) } },
                                modifier = Modifier.size(48.dp)
                            ) {
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
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedMenuItem) {
                    "home" -> HomeContent(localizedContext, isDarkTheme)
                    "patients" -> PatientsContent(localizedContext, isDarkTheme)
                    "settings" -> SettingsScreen(
                        localizedContext = localizedContext,
                        isDarkTheme = isDarkTheme,
                        currentLanguage = currentLanguage,
                        onLanguageChange = { newLang -> scope.launch { prefs.setLanguage(newLang) } },
                        onThemeChange = { scope.launch { prefs.setThemeMode(if (isDarkTheme) "light" else "dark") } }
                    )
                    else -> ComingSoonContent(localizedContext, selectedMenuItem, isDarkTheme)
                }
            }
        }
    }
}

@Composable
fun HomeContent(localizedContext: android.content.Context, isDarkTheme: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = localizedContext.getString(R.string.patients_list),
            style = MaterialTheme.typography.headlineSmall,
            color = if (isDarkTheme) Color.White else Color.Black,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = PrimaryGreen.copy(alpha = 0.1f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = localizedContext.getString(R.string.no_patients_yet),
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* TODO */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(localizedContext.getString(R.string.add_patient))
        }
    }
}

@Composable
fun PatientsContent(localizedContext: android.content.Context, isDarkTheme: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = localizedContext.getString(R.string.patients_list),
            style = MaterialTheme.typography.headlineSmall,
            color = if (isDarkTheme) Color.White else Color.Black
        )
    }
}

@Composable
fun ComingSoonContent(localizedContext: android.content.Context, section: String, isDarkTheme: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = localizedContext.getString(R.string.under_construction),
            style = MaterialTheme.typography.headlineSmall,
            color = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f)
        )
    }
}