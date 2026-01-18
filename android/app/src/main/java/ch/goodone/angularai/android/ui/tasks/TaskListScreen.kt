package ch.goodone.angularai.android.ui.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.goodone.angularai.android.domain.model.Task
import ch.goodone.angularai.android.domain.model.TaskStatus
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight

@Composable
fun TaskListHeader(
    statusFilter: TaskStatus?,
    onStatusFilterChange: (TaskStatus?) -> Unit,
    onResetSorting: () -> Unit
) {
    var showFilterMenu by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box {
            TextButton(onClick = { showFilterMenu = true }) {
                Icon(Icons.Default.FilterList, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text(statusFilter?.name?.lowercase()?.replace("_", " ")?.split(" ")?.joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } } ?: "All Status")
            }
            DropdownMenu(
                expanded = showFilterMenu,
                onDismissRequest = { showFilterMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("All Status") },
                    onClick = { onStatusFilterChange(null); showFilterMenu = false }
                )
                TaskStatus.values().forEach { status ->
                    DropdownMenuItem(
                        text = { Text(status.name.lowercase().replace("_", " ").split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }) },
                        onClick = { onStatusFilterChange(status); showFilterMenu = false }
                    )
                }
            }
        }
        
        TextButton(onClick = onResetSorting) {
            Icon(Icons.Default.Sort, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Reset Sorting")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onTaskClick: (Task) -> Unit,
    onAddTask: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val statusFilter by viewModel.statusFilter.collectAsState()
    var showFilterMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TaskListHeader(
                statusFilter = statusFilter,
                onStatusFilterChange = { viewModel.onStatusFilterChange(it) },
                onResetSorting = { viewModel.onResetSorting() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (state.isLoading && state.tasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.tasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No tasks found.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(state.tasks) { index, task ->
                        // Reordering logic simplified for this environment without external libs
                        // In a real app, use a reorderable list library.
                        TaskItem(
                            task = task,
                            onClick = { onTaskClick(task) },
                            onDelete = { viewModel.onDeleteTask(task.id!!) },
                            onMoveUp = if (index > 0 && statusFilter == null) { { viewModel.onReorderTasks(index, index - 1) } } else null,
                            onMoveDown = if (index < state.tasks.size - 1 && statusFilter == null) { { viewModel.onReorderTasks(index, index + 1) } } else null
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
    onDelete: () -> Unit,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onMoveUp != null || onMoveDown != null) {
                Column {
                    IconButton(onClick = onMoveUp ?: {}, enabled = onMoveUp != null, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Up")
                    }
                    IconButton(onClick = onMoveDown ?: {}, enabled = onMoveDown != null, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Down")
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = task.title, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusChip(status = task.status)
                }
                Text(
                    text = "Priority: ${task.priority}",
                    style = MaterialTheme.typography.bodySmall,
                    color = when(task.priority) {
                        "HIGH", "CRITICAL" -> Color.Red
                        "MEDIUM" -> Color(0xFFFFA500)
                        else -> Color.Gray
                    }
                )
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

@Composable
fun StatusChip(status: TaskStatus) {
    val backgroundColor = when(status) {
        TaskStatus.OPEN -> Color.LightGray
        TaskStatus.IN_PROGRESS -> Color(0xFFBBDEFB)
        TaskStatus.CLOSED -> Color(0xFFC8E6C9)
    }
    val textColor = when(status) {
        TaskStatus.OPEN -> Color.DarkGray
        TaskStatus.IN_PROGRESS -> Color(0xFF1976D2)
        TaskStatus.CLOSED -> Color(0xFF388E3C)
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = status.name.lowercase().replace("_", " ").split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } },
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}
