package ch.goodone.angularai.android.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.goodone.angularai.android.domain.model.User
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    
    val state = viewModel.loginState.value

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AuthViewModel.UiEvent.RegisterSuccess -> onRegisterSuccess()
                else -> Unit
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Register", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        TextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = login, onValueChange = { login = it }, label = { Text("Login") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = birthDate, onValueChange = { birthDate = it }, label = { Text("Birth Date (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { 
                    val user = User(firstName = firstName, lastName = lastName, login = login, email = email, birthDate = birthDate, address = address)
                    viewModel.onRegister(user, password) 
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register")
            }
            TextButton(onClick = onNavigateToLogin) {
                Text("Already have an account? Login")
            }
        }
        
        state.error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
}
