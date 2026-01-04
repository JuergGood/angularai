package ch.goodone.angularai.android.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.goodone.angularai.android.domain.model.User

@Composable
fun AdminUserEditScreen(
    user: User?,
    onSave: () -> Unit,
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    var firstName by remember { mutableStateOf(user?.firstName ?: "") }
    var lastName by remember { mutableStateOf(user?.lastName ?: "") }
    var login by remember { mutableStateOf(user?.login ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var password by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf(user?.birthDate ?: "") }
    var address by remember { mutableStateOf(user?.address ?: "") }
    var role by remember { mutableStateOf(user?.role ?: "ROLE_USER") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = if (user == null) "Add User" else "Edit User", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        TextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        if (user == null) {
            TextField(value = login, onValueChange = { login = it }, label = { Text("Login") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
        }
        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = birthDate, onValueChange = { birthDate = it }, label = { Text("Birth Date (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        
        Text("Role")
        Row {
            listOf("ROLE_USER", "ROLE_ADMIN").forEach { r ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = role == r, onClick = { role = r })
                    Text(text = r)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
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
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel")
        }
    }
}
