package ch.goodone.angularai.android.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.goodone.angularai.android.domain.model.User
import ch.goodone.angularai.android.ui.auth.AuthViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun AdminUserListScreen(
    onUserClick: (User) -> Unit,
    onAddUser: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val currentUser by authViewModel.currentUser.collectAsState()
    val canEdit = currentUser?.role == "ROLE_ADMIN"

    Scaffold(
        floatingActionButton = {
            if (canEdit) {
                FloatingActionButton(onClick = onAddUser) {
                    Icon(Icons.Default.Add, contentDescription = "Add User")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (state.isLoading && state.users.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.users) { user ->
                        UserItem(
                            user = user,
                            onClick = { onUserClick(user) },
                            onDelete = { viewModel.onDeleteUser(user.id!!) },
                            canDelete = canEdit && user.login != currentUser?.login
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserItem(
    user: User,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${user.firstName} ${user.lastName}", style = MaterialTheme.typography.titleMedium)
                Text(text = "Login: ${user.login}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Role: ${user.role}", style = MaterialTheme.typography.bodySmall)
            }
            if (canDelete) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete User")
                }
            }
        }
    }
}
