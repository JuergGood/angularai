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
fun RegistrationForm(
    firstName: String, onFirstNameChange: (String) -> Unit,
    lastName: String, onLastNameChange: (String) -> Unit,
    login: String, onLoginChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    birthDate: String, onBirthDateChange: (String) -> Unit,
    address: String, onAddressChange: (String) -> Unit
) {
    TextField(value = firstName, onValueChange = onFirstNameChange, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    TextField(value = lastName, onValueChange = onLastNameChange, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    TextField(value = login, onValueChange = onLoginChange, label = { Text("Login") }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    TextField(value = email, onValueChange = onEmailChange, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    TextField(value = password, onValueChange = onPasswordChange, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    TextField(value = birthDate, onValueChange = onBirthDateChange, label = { Text("Birth Date (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    TextField(value = address, onValueChange = onAddressChange, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    
    var localError by remember { mutableStateOf<String?>(null) }
    
    val state = viewModel.loginState.value

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AuthViewModel.UiEvent.RegisterSuccess -> {
                    snackbarHostState.showSnackbar("Registration successful! Please login.")
                    onRegisterSuccess()
                }
                else -> Unit
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        Text(text = "Register", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        RegistrationForm(
            firstName = firstName, onFirstNameChange = { firstName = it },
            lastName = lastName, onLastNameChange = { lastName = it },
            login = login, onLoginChange = { login = it },
            email = email, onEmailChange = { email = it },
            password = password, onPasswordChange = { password = it },
            birthDate = birthDate, onBirthDateChange = { birthDate = it },
            address = address, onAddressChange = { address = it }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { 
                    if (firstName.isBlank() || lastName.isBlank() || login.isBlank() || email.isBlank() || password.isBlank() || birthDate.isBlank()) {
                        localError = "Please fill in all required fields"
                        return@Button
                    }
                    if (!email.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))) {
                        localError = "Invalid email format"
                        return@Button
                    }
                    localError = null
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
        
        localError?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
        
        state.error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
}}
