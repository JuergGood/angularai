package ch.goodone.angularai.android.ui.tasks

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.goodone.angularai.android.data.repository.TaskRepository
import ch.goodone.angularai.android.domain.model.Task
import ch.goodone.angularai.android.domain.model.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    private val _statusFilter = MutableStateFlow<TaskStatus?>(null)
    val statusFilter: StateFlow<TaskStatus?> = _statusFilter.asStateFlow()

    private val _state = mutableStateOf(TaskUiState())
    val state: State<TaskUiState> = _state

    init {
        combine(repository.tasks, _statusFilter) { tasks, filter ->
            val filteredTasks = if (filter == null) tasks else tasks.filter { it.status == filter }
            _state.value = _state.value.copy(tasks = filteredTasks)
        }.launchIn(viewModelScope)
        refresh()
    }

    fun onStatusFilterChange(status: TaskStatus?) {
        _statusFilter.value = status
    }

    fun onReorderTasks(fromIndex: Int, toIndex: Int) {
        val currentTasks = _state.value.tasks.toMutableList()
        if (fromIndex !in currentTasks.indices || toIndex !in currentTasks.indices) return
        
        val task = currentTasks.removeAt(fromIndex)
        currentTasks.add(toIndex, task)
        
        // Update local state immediately for smooth UI
        _state.value = _state.value.copy(tasks = currentTasks)
        
        viewModelScope.launch {
            repository.reorderTasks(currentTasks.mapNotNull { it.id })
        }
    }

    fun onResetSorting() {
        viewModelScope.launch {
            // Reordering by priority: HIGH, MEDIUM, LOW
            val sortedTasks = _state.value.tasks.sortedWith(compareBy<Task> {
                when (it.priority) {
                    "CRITICAL" -> 0
                    "HIGH" -> 1
                    "MEDIUM" -> 2
                    "LOW" -> 3
                    else -> 4
                }
            })
            repository.reorderTasks(sortedTasks.mapNotNull { it.id })
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repository.refreshTasks()
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun onDeleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    fun onSaveTask(task: Task, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                if (task.id == null) {
                    repository.createTask(task)
                } else {
                    repository.updateTask(task)
                }
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Unknown error occurred")
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun getTask(id: Long): Task? {
        return _state.value.tasks.find { it.id == id }
    }

    data class TaskUiState(
        val tasks: List<Task> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )
}
