package ch.goodone.angularai.android.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.goodone.angularai.android.domain.model.Task
import ch.goodone.angularai.android.domain.model.TaskStatus

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
    var status by remember { mutableStateOf(TaskStatus.OPEN) }
    
    val isDueDateValid = remember(dueDate) {
        dueDate.isBlank() || dueDate.matches(Regex("^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$"))
    }

    LaunchedEffect(task) {
        task?.let {
            title = it.title
            description = it.description
            dueDate = it.dueDate ?: ""
            priority = it.priority
            status = it.status
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = if (task == null) "Add Task" else "Edit Task", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            isError = title.isBlank()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = dueDate,
            onValueChange = { dueDate = it },
            label = { Text("Due Date (yyyy-MM-dd)") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. 2026-01-18") },
            isError = !isDueDateValid,
            supportingText = {
                if (!isDueDateValid) {
                    Text("Invalid format. Use yyyy-MM-dd")
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        if (state.error != null) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        Text("Priority")
        Row {
            listOf("LOW", "MEDIUM", "HIGH", "CRITICAL").forEach { p ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = priority == p, onClick = { priority = p })
                    Text(text = p)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Text("Status")
        Row {
            TaskStatus.values().forEach { s ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = status == s, onClick = { status = s })
                    Text(text = s.name.lowercase().replace("_", " ").split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } })
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (title.isBlank()) return@Button
                val newTask = Task(
                    id = task?.id,
                    title = title,
                    description = description,
                    dueDate = dueDate.trim().takeIf { it.isNotBlank() },
                    priority = priority,
                    status = status,
                    position = task?.position ?: 0
                )
                viewModel.onSaveTask(newTask, onSave)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading && title.isNotBlank() && isDueDateValid
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Save")
            }
        }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel")
        }
    }
}
