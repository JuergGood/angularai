package ch.goodone.angularai.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ch.goodone.angularai.android.ui.SystemViewModel
import ch.goodone.angularai.android.domain.model.Task
import ch.goodone.angularai.android.domain.model.User
import ch.goodone.angularai.android.ui.admin.AdminUserEditScreen
import ch.goodone.angularai.android.ui.admin.AdminUserListScreen
import ch.goodone.angularai.android.ui.auth.AuthViewModel
import ch.goodone.angularai.android.ui.auth.LoginScreen
import ch.goodone.angularai.android.ui.auth.RegisterScreen
import ch.goodone.angularai.android.ui.dashboard.DashboardScreen
import ch.goodone.angularai.android.ui.log.LogScreen
import ch.goodone.angularai.android.ui.profile.ProfileScreen
import ch.goodone.angularai.android.ui.tasks.TaskEditScreen
import ch.goodone.angularai.android.ui.tasks.TaskListScreen
import ch.goodone.angularai.android.ui.theme.AngularAITheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AngularAITheme {
                MainApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    authViewModel: AuthViewModel = hiltViewModel(),
    systemViewModel: SystemViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoggedIn = currentUser != null
    val isAdmin = currentUser?.role == "ROLE_ADMIN" || currentUser?.role == "ROLE_ADMIN_READ"
    val canEditAdmin = currentUser?.role == "ROLE_ADMIN"

    var showSettingsMenu by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    val systemInfo by systemViewModel.systemInfo

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            systemViewModel.loadSystemInfo()
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Application Help") },
            text = {
                Column {
                    Text("This application allows you to manage tasks and user profiles.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Tasks: Create, edit, and delete your tasks.")
                    Text("• Profile: Manage your personal information.")
                    Text("• Admin: Administrators can manage all users.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Note: This is a test application for AI code generation.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) { Text("Close") }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A237E))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Filter1,
                            contentDescription = null,
                            tint = Color(0xFFFF4081),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "GoodOne",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Divider()
                if (isLoggedIn) {
                    NavigationDrawerItem(
                        label = { Text("Dashboard") },
                        selected = false,
                        onClick = { scope.launch { drawerState.close() }; navController.navigate("dashboard") },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = null) }
                    )
                    NavigationDrawerItem(
                        label = { Text("Tasks") },
                        selected = false,
                        onClick = { scope.launch { drawerState.close() }; navController.navigate("tasks") },
                        icon = { Icon(Icons.Default.List, contentDescription = null) }
                    )
                    NavigationDrawerItem(
                        label = { Text("Profile") },
                        selected = false,
                        onClick = { scope.launch { drawerState.close() }; navController.navigate("profile") },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) }
                    )
                    if (isAdmin) {
                        NavigationDrawerItem(
                            label = { Text("User Admin") },
                            selected = false,
                            onClick = { scope.launch { drawerState.close() }; navController.navigate("admin") },
                            icon = { Icon(Icons.Default.SupervisorAccount, contentDescription = null) }
                        )
                        NavigationDrawerItem(
                            label = { Text("Logs") },
                            selected = false,
                            onClick = { scope.launch { drawerState.close() }; navController.navigate("logs") },
                            icon = { Icon(Icons.Default.History, contentDescription = null) }
                        )
                    }
                    Divider()
                    NavigationDrawerItem(
                        label = { Text("Logout") },
                        selected = false,
                        onClick = { 
                            scope.launch { 
                                drawerState.close()
                                authViewModel.onLogout() 
                                navController.navigate("login") {
                                    popUpTo(0)
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.Logout, contentDescription = null) }
                    )
                } else {
                    NavigationDrawerItem(
                        label = { Text("Login") },
                        selected = false,
                        onClick = { scope.launch { drawerState.close() }; navController.navigate("login") },
                        icon = { Icon(Icons.Default.Login, contentDescription = null) }
                    )
                    NavigationDrawerItem(
                        label = { Text("Register") },
                        selected = false,
                        onClick = { scope.launch { drawerState.close() }; navController.navigate("register") },
                        icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) }
                    )
                }
            }
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            topBar = {
                if (isLoggedIn) {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Filter1,
                                    contentDescription = null,
                                    tint = Color(0xFFFF4081),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("GoodOne")
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            Box {
                                IconButton(onClick = { showSettingsMenu = true }) {
                                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                                }
                                DropdownMenu(
                                    expanded = showSettingsMenu,
                                    onDismissRequest = { showSettingsMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Version: ${systemInfo?.version ?: "..."}") },
                                        onClick = { showSettingsMenu = false },
                                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                                        enabled = false
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Mode: ${systemInfo?.mode ?: "..."}") },
                                        onClick = { showSettingsMenu = false },
                                        leadingIcon = { Icon(Icons.Default.Layers, contentDescription = null) },
                                        enabled = false
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Help") },
                                        onClick = { 
                                            showSettingsMenu = false
                                            showHelpDialog = true
                                        },
                                        leadingIcon = { Icon(Icons.Default.Help, contentDescription = null) }
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF1A237E),
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        )
                    )
                } else {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Filter1,
                                    contentDescription = null,
                                    tint = Color(0xFFFF4081),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("GoodOne")
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF1A237E),
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        )
                    )
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "login",
                modifier = Modifier.padding(padding)
            ) {
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = { 
                            navController.navigate("tasks") { popUpTo("login") { inclusive = true } }
                        },
                        onNavigateToRegister = { navController.navigate("register") }
                    )
                }
                composable("register") {
                    RegisterScreen(
                        onRegisterSuccess = { navController.navigate("login") },
                        onNavigateToLogin = { navController.navigate("login") }
                    )
                }
                composable("tasks") {
                    TaskListScreen(
                        onTaskClick = { task -> navController.navigate("task_edit/${task.id}") },
                        onAddTask = { navController.navigate("task_add") }
                    )
                }
                composable("task_add") {
                    TaskEditScreen(
                        taskId = null,
                        onSave = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(
                    "task_edit/{taskId}",
                    arguments = listOf(navArgument("taskId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val taskId = backStackEntry.arguments?.getLong("taskId")
                    TaskEditScreen(
                        taskId = taskId,
                        onSave = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("profile") {
                    ProfileScreen()
                }
                composable("logs") {
                    if (isAdmin) {
                        LogScreen()
                    }
                }
                composable("admin") {
                    AdminUserListScreen(
                        onUserClick = { user -> navController.navigate("admin_user_edit/${user.id}") },
                        onAddUser = { navController.navigate("admin_user_add") }
                    )
                }
                composable("admin_user_add") {
                    AdminUserEditScreen(
                        userId = null,
                        onSave = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(
                    "admin_user_edit/{userId}",
                    arguments = listOf(navArgument("userId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getLong("userId")
                    AdminUserEditScreen(
                        userId = userId,
                        onSave = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
