package ch.goodone.angularai.android.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.goodone.angularai.android.domain.model.Task

@Composable
fun TaskEditScreen(
    taskId: Long?,
    onSave: () -> Unit,
    onBack: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val task = remember(taskId, state.tasks) { taskId?.let { id -> state.tasks.find { it.id == id } } }
    
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIUM") }

    LaunchedEffect(task) {
        task?.let {
            title = it.title
            description = it.description ?: ""
            dueDate = it.dueDate ?: ""
            priority = it.priority
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = if (task == null) "Add Task" else "Edit Task", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        TextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("Due Date (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        
        Text("Priority")
        Row {
            listOf("LOW", "MEDIUM", "HIGH").forEach { p ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = priority == p, onClick = { priority = p })
                    Text(text = p)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                val newTask = Task(id = task?.id, title = title, description = description, dueDate = dueDate.takeIf { it.isNotBlank() }, priority = priority)
                viewModel.onSaveTask(newTask)
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
