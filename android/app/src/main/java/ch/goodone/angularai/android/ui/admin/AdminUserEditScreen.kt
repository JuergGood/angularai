package ch.goodone.angularai.android.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import ch.goodone.angularai.android.ui.auth.AuthViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.goodone.angularai.android.domain.model.User

@Composable
fun AdminUserEditScreen(
    userId: Long?,
    onSave: () -> Unit,
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val currentUser by authViewModel.currentUser.collectAsState()
    val canEdit = currentUser?.role == "ROLE_ADMIN"
    val user = remember(userId, state.users) { userId?.let { id -> state.users.find { it.id == id } } }
    
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("ROLE_USER") }

    LaunchedEffect(user) {
        user?.let {
            firstName = it.firstName
            lastName = it.lastName
            login = it.login
            email = it.email
            birthDate = it.birthDate ?: ""
            address = it.address ?: ""
            role = it.role
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = if (user == null) "Add User" else (if (canEdit) "Edit User" else "View User"), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        TextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth(), enabled = canEdit)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth(), enabled = canEdit)
        Spacer(modifier = Modifier.height(8.dp))
        if (user == null) {
            TextField(value = login, onValueChange = { login = it }, label = { Text("Login") }, modifier = Modifier.fillMaxWidth(), enabled = canEdit)
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), enabled = canEdit)
            Spacer(modifier = Modifier.height(8.dp))
        }
        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), enabled = canEdit)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = birthDate, onValueChange = { birthDate = it }, label = { Text("Birth Date (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth(), enabled = canEdit)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth(), enabled = canEdit)
        Spacer(modifier = Modifier.height(8.dp))
        
        Text("Role")
        FlowRow {
            listOf("ROLE_USER", "ROLE_ADMIN", "ROLE_ADMIN_READ").forEach { r ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = role == r,
                        onClick = { role = r },
                        enabled = canEdit && (user == null || user.login != currentUser?.login)
                    )
                    Text(text = r)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (canEdit) {
            Button(
                onClick = {
                    val newUser = (user ?: User()).copy(
                        firstName = firstName,
                        lastName = lastName,
                        login = if (user == null) login else user.login,
                        email = email,
                        birthDate = birthDate,
                        address = address,
                        role = role
                    )
                    viewModel.onSaveUser(newUser, password.takeIf { it.isNotBlank() })
                    onSave()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(if (canEdit) "Cancel" else "Close")
        }
    }
}
