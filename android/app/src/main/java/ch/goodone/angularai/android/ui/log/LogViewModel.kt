package ch.goodone.angularai.android.ui.log

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.goodone.angularai.android.data.remote.dto.ActionLogDTO
import ch.goodone.angularai.android.data.repository.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor(
    private val repository: LogRepository
) : ViewModel() {

    private val _state = mutableStateOf(LogUiState())
    val state: State<LogUiState> = _state

    init {
        loadLogs()
    }

    fun loadLogs() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val response = repository.getLogs(
                    page = _state.value.currentPage,
                    size = _state.value.pageSize,
                    actionType = _state.value.actionType,
                    startDate = _state.value.startDate,
                    endDate = _state.value.endDate
                )
                _state.value = _state.value.copy(
                    logs = response.content,
                    totalElements = response.totalElements,
                    totalPages = response.totalPages,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun onActionTypeChange(actionType: String) {
        _state.value = _state.value.copy(actionType = actionType, currentPage = 0)
        loadLogs()
    }

    fun onDateRangeChange(start: Long?, end: Long?) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val startDateStr = start?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().format(formatter)
        }
        val endDateStr = end?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().format(formatter)
        }
        _state.value = _state.value.copy(startDate = startDateStr, endDate = endDateStr, currentPage = 0)
        loadLogs()
    }

    fun onClearFilter() {
        _state.value = _state.value.copy(
            actionType = "all",
            startDate = null,
            endDate = null,
            currentPage = 0
        )
        loadLogs()
    }

    fun onClearLogs() {
        viewModelScope.launch {
            try {
                repository.clearLogs()
                loadLogs()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun onPageChange(page: Int) {
        _state.value = _state.value.copy(currentPage = page)
        loadLogs()
    }

    data class LogUiState(
        val logs: List<ActionLogDTO> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val actionType: String = "all",
        val startDate: String? = null,
        val endDate: String? = null,
        val currentPage: Int = 0,
        val pageSize: Int = 20,
        val totalElements: Long = 0,
        val totalPages: Int = 0
    )
}
