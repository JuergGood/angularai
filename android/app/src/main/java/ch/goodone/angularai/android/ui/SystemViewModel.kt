package ch.goodone.angularai.android.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.goodone.angularai.android.data.repository.SystemRepository
import ch.goodone.angularai.android.data.remote.dto.SystemInfoDTO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SystemViewModel @Inject constructor(
    private val repository: SystemRepository
) : ViewModel() {

    private val _systemInfo = mutableStateOf<SystemInfoDTO?>(null)
    val systemInfo: State<SystemInfoDTO?> = _systemInfo

    fun loadSystemInfo() {
        viewModelScope.launch {
            try {
                _systemInfo.value = repository.getSystemInfo()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
