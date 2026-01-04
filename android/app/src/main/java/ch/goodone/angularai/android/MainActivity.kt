package ch.goodone.angularai.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ch.goodone.angularai.android.domain.model.Task
import ch.goodone.angularai.android.domain.model.User
import ch.goodone.angularai.android.ui.admin.AdminUserEditScreen
import ch.goodone.angularai.android.ui.admin.AdminUserListScreen
import ch.goodone.angularai.android.ui.auth.AuthViewModel
import ch.goodone.angularai.android.ui.auth.LoginScreen
import ch.goodone.angularai.android.ui.auth.RegisterScreen
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
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val authToken by authViewModel.loginState // This should be observed from repository ideally
    // Using a simpler approach for the demo:
    var isLoggedIn by remember { mutableStateOf(false) }
    var isAdmin by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("GoodOne", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                HorizontalDivider()
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
                        label = { Text("Admin") },
                        selected = false,
                        onClick = { scope.launch { drawerState.close() }; navController.navigate("admin") },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) }
                    )
                }
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    onClick = { 
                        scope.launch { 
                            drawerState.close()
                            // authViewModel.logout() 
                            isLoggedIn = false
                            navController.navigate("login") {
                                popUpTo(0)
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) }
                )
            }
        },
        gesturesEnabled = isLoggedIn
    ) {
        Scaffold(
            topBar = {
                if (isLoggedIn) {
                    TopAppBar(
                        title = { Text("AngularAI") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
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
                            isLoggedIn = true
                            // Here we should fetch user to check if admin
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
                        task = null,
                        onSave = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(
                    "task_edit/{taskId}",
                    arguments = listOf(navArgument("taskId") { type = NavType.LongType })
                ) { backStackEntry ->
                    // Simplified: In a real app we'd fetch the task from VM by ID
                    TaskEditScreen(
                        task = null, // Placeholder
                        onSave = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("profile") {
                    ProfileScreen()
                }
                composable("admin") {
                    AdminUserListScreen(
                        onUserClick = { user -> navController.navigate("admin_user_edit/${user.id}") },
                        onAddUser = { navController.navigate("admin_user_add") }
                    )
                }
                composable("admin_user_add") {
                    AdminUserEditScreen(
                        user = null,
                        onSave = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(
                    "admin_user_edit/{userId}",
                    arguments = listOf(navArgument("userId") { type = NavType.LongType })
                ) {
                    AdminUserEditScreen(
                        user = null, // Placeholder
                        onSave = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
