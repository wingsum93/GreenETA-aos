package com.ericho.myhospital.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ericho.myhospital.data.repository.LocalRepository
import com.ericho.myhospital.model.HospitalPayload
import com.ericho.myhospital.model.HospitalWaitTime
import kotlinx.coroutines.flow.MutableSharedFlow
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
    private val intents = MutableSharedFlow<HospitalWaitTimeIntent>(extraBufferCapacity = 1)
    private val _uiState = MutableStateFlow(
        HospitalWaitTimeUiState(
            isLoading = true,
            isEmpty = false,
            updatedTime = null,
            hospitals = emptyList(),
        )
    )
    val uiState: StateFlow<HospitalWaitTimeUiState> = _uiState.asStateFlow()
    private var lastLanguageTag: String? = null

    init {
        viewModelScope.launch {
            intents.collect { intent ->
                when (intent) {
                    is HospitalWaitTimeIntent.Load -> {
                        lastLanguageTag = intent.languageTag
                        fetchHospitalWaitTimes(intent.languageTag)
                    }
                    HospitalWaitTimeIntent.Refresh -> {
                        lastLanguageTag?.let { fetchHospitalWaitTimes(it) }
                    }
                }
            }
        }
    }

    fun accept(intent: HospitalWaitTimeIntent) {
        if (!intents.tryEmit(intent)) {
            viewModelScope.launch {
                intents.emit(intent)
            }
        }
    }

    private fun fetchHospitalWaitTimes(languageTag: String) {
        _uiState.value = reduce(_uiState.value, HospitalWaitTimeResult.Loading)
        viewModelScope.launch {
            val result = runCatching { localRepository.loadHospitalWaitTimes(languageTag) }
            val outcome = result.fold(
                onSuccess = { payload -> HospitalWaitTimeResult.Success(payload) },
                onFailure = { error -> HospitalWaitTimeResult.Failure(error) },
            )
            _uiState.value = reduce(_uiState.value, outcome)
        }
    }

    private fun reduce(
        state: HospitalWaitTimeUiState,
        result: HospitalWaitTimeResult,
    ): HospitalWaitTimeUiState {
        return when (result) {
            HospitalWaitTimeResult.Loading -> state.copy(isLoading = true, isEmpty = false)
            is HospitalWaitTimeResult.Success -> HospitalWaitTimeUiState(
                isLoading = false,
                isEmpty = result.payload.hospitals.isEmpty(),
                updatedTime = result.payload.updatedTime,
                hospitals = result.payload.hospitals,
            )
            is HospitalWaitTimeResult.Failure -> HospitalWaitTimeUiState(
                isLoading = false,
                isEmpty = true,
                updatedTime = null,
                hospitals = emptyList(),
            )
        }
    }
}

private sealed interface HospitalWaitTimeResult {
    data object Loading : HospitalWaitTimeResult
    data class Success(val payload: HospitalPayload) : HospitalWaitTimeResult
    data class Failure(val error: Throwable) : HospitalWaitTimeResult
}
