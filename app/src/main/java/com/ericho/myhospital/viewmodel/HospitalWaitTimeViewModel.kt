package com.ericho.myhospital.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ericho.myhospital.data.repository.LocalRepository
import com.ericho.myhospital.model.HospitalWaitTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HospitalWaitTimeUiState(
    val isLoading: Boolean,
    val isEmpty: Boolean,
    val updatedTime: String?,
    val hospitals: List<HospitalWaitTime>,
)

class HospitalWaitTimeViewModel(
    private val localRepository: LocalRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        HospitalWaitTimeUiState(
            isLoading = true,
            isEmpty = false,
            updatedTime = null,
            hospitals = emptyList(),
        )
    )
    val uiState: StateFlow<HospitalWaitTimeUiState> = _uiState.asStateFlow()

    fun loadHospitalWaitTimes(languageTag: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, isEmpty = false)
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching {
                localRepository.loadHospitalWaitTimes(languageTag)
            }
            _uiState.value = result.fold(
                onSuccess = { payload ->
                    HospitalWaitTimeUiState(
                        isLoading = false,
                        isEmpty = payload.hospitals.isEmpty(),
                        updatedTime = payload.updatedTime,
                        hospitals = payload.hospitals,
                    )
                },
                onFailure = {
                    HospitalWaitTimeUiState(
                        isLoading = false,
                        isEmpty = true,
                        updatedTime = null,
                        hospitals = emptyList(),
                    )
                }
            )
        }
    }
}
