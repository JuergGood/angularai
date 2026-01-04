package ch.goodone.angularai.android.ui.tasks

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.goodone.angularai.android.data.repository.TaskRepository
import ch.goodone.angularai.android.domain.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    private val _state = mutableStateOf(TaskUiState())
    val state: State<TaskUiState> = _state

    init {
        repository.tasks.onEach { tasks ->
            _state.value = _state.value.copy(tasks = tasks)
        }.launchIn(viewModelScope)
        refresh()
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

    fun onSaveTask(task: Task) {
        viewModelScope.launch {
            if (task.id == null) {
                repository.createTask(task)
            } else {
                repository.updateTask(task)
            }
        }
    }

    data class TaskUiState(
        val tasks: List<Task> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )
}
