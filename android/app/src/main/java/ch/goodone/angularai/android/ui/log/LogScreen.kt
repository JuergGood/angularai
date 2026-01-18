package ch.goodone.angularai.android.ui.log

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.goodone.angularai.android.data.remote.dto.ActionLogDTO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(
    viewModel: LogViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    var showDatePicker by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showTypeMenu by remember { mutableStateOf(false) }

    val dateRangePickerState = rememberDateRangePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDateRangeChange(
                        dateRangePickerState.selectedStartDateMillis,
                        dateRangePickerState.selectedEndDateMillis
                    )
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                modifier = Modifier.height(400.dp)
            )
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Confirm Clear Log") },
            text = { Text("Are you sure you want to delete all log entries? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onClearLogs()
                    showClearConfirm = false
                }, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box {
                        OutlinedButton(onClick = { showTypeMenu = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text(state.actionType.replaceFirstChar { it.uppercase() })
                        }
                        DropdownMenu(
                            expanded = showTypeMenu,
                            onDismissRequest = { showTypeMenu = false }
                        ) {
                            listOf("all", "login", "task", "user admin").forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.replaceFirstChar { it.uppercase() }) },
                                    onClick = {
                                        viewModel.onActionTypeChange(type)
                                        showTypeMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Row {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Dates", tint = if (state.startDate != null) MaterialTheme.colorScheme.primary else LocalContentColor.current)
                        }
                        IconButton(onClick = { viewModel.onClearFilter() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Filters")
                        }
                        IconButton(onClick = { showClearConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear Logs", tint = Color.Red)
                        }
                    }
                }
                if (state.startDate != null || state.endDate != null) {
                    Text(
                        text = "Range: ${state.startDate ?: "..."} - ${state.endDate ?: "..."}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading && state.logs.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.logs.isEmpty()) {
                Text("No logs found.", modifier = Modifier.align(Alignment.Center))
            } else {
                Column {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(state.logs) { log ->
                            LogItem(log)
                            Divider()
                        }
                    }
                    
                    // Paging Controls
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.onPageChange(state.currentPage - 1) },
                            enabled = state.currentPage > 0
                        ) {
                            Text("<")
                        }
                        Text("Page ${state.currentPage + 1} of ${state.totalPages}")
                        IconButton(
                            onClick = { viewModel.onPageChange(state.currentPage + 1) },
                            enabled = state.currentPage < state.totalPages - 1
                        ) {
                            Text(">")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogItem(log: ActionLogDTO) {
    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = log.timestamp,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = log.login,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = log.action,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = log.details,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 13.sp
        )
    }
}
