package ch.goodone.angularai.android.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.goodone.angularai.android.domain.model.User

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    LaunchedEffect(state.user) {
        state.user?.let {
            firstName = it.firstName
            lastName = it.lastName
            email = it.email
            birthDate = it.birthDate
            address = it.address
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "My Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            TextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = birthDate, onValueChange = { birthDate = it }, label = { Text("Birth Date (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    val updatedUser = state.user?.copy(
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        birthDate = birthDate,
                        address = address
                    )
                    updatedUser?.let { viewModel.onUpdateProfile(it) }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Update Profile")
            }
        }
        
        state.error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
}
