package ch.goodone.angularai.android.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.goodone.angularai.android.domain.model.Task

@Composable
fun TaskEditScreen(
    task: Task?,
    onSave: () -> Unit,
    onBack: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var dueDate by remember { mutableStateOf(task?.dueDate ?: "") }
    var priority by remember { mutableStateOf(task?.priority ?: "MEDIUM") }

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
