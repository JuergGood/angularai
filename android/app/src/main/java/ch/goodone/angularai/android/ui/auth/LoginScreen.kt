package ch.goodone.angularai.android.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state = viewModel.loginState.value

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AuthViewModel.UiEvent.LoginSuccess -> onLoginSuccess()
                else -> Unit
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        TextField(
            value = login,
            onValueChange = { login = it },
            label = { Text("Login") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.onLogin(login, password) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }
            TextButton(onClick = onNavigateToRegister) {
                Text("Don't have an account? Register")
            }
        }
        
        state.error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
}
