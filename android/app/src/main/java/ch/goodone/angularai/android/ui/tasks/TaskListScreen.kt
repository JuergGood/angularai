package ch.goodone.angularai.android.ui.tasks

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
import ch.goodone.angularai.android.domain.model.Task

@Composable
fun TaskListScreen(
    onTaskClick: (Task) -> Unit,
    onAddTask: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (state.isLoading && state.tasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.tasks) { task ->
                        TaskItem(
                            task = task,
                            onClick = { onTaskClick(task) },
                            onDelete = { viewModel.onDeleteTask(task.id!!) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onClick: () -> Unit,
    onDelete: () -> Unit
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
                Text(text = task.title, style = MaterialTheme.typography.titleMedium)
                Text(text = task.priority, style = MaterialTheme.typography.bodySmall)
                task.dueDate?.let {
                    Text(text = "Due: $it", style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Task")
            }
        }
    }
}
