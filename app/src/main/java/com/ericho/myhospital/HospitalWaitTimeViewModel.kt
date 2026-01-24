package com.ericho.myhospital

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ericho.myhospital.data.HospitalPayload
import com.ericho.myhospital.data.LocalRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

data class HospitalWaitTimeUiState(
    val isLoading: Boolean,
    val isEmpty: Boolean,
    val updatedTime: String?,
    val hospitals: List<HospitalWaitTime>,
)

class HospitalWaitTimeViewModel(
    private val localRepository: LocalRepository,
    private val httpClient: HttpClient,
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

    init {
        loadHospitalWaitTimes()
    }

    fun loadHospitalWaitTimes() {
        _uiState.value = _uiState.value.copy(isLoading = true, isEmpty = false)
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching {
                val cachedJson = localRepository.loadHospitalWaitTimeJson()
                val jsonText = cachedJson
                    ?: httpClient
                        .get("https://www.ha.org.hk/opendata/aed/aedwtdata2-en.json")
                        .bodyAsText()
                if (cachedJson == null) {
                    localRepository.cacheHospitalWaitTimeJson(jsonText)
                }
                parseHospitals(jsonText)
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

    private fun parseHospitals(jsonText: String): HospitalPayload {
        val root = JSONObject(jsonText)
        val updateTime = root.optString("updateTime")
        val waitTimeArray = root.optJSONArray("waitTime")
        val hospitals = mutableListOf<HospitalWaitTime>()
        if (waitTimeArray != null) {
            for (index in 0 until waitTimeArray.length()) {
                val item = waitTimeArray.getJSONObject(index)
                hospitals.add(
                    HospitalWaitTime(
                        name = item.optString("hospName"),
                        t1wt = item.optString("t1wt"),
                        t2wt = item.optString("t2wt"),
                        t3p50 = item.optString("t3p50"),
                        t3p95 = item.optString("t3p95"),
                        t45p50 = item.optString("t45p50"),
                        t45p95 = item.optString("t45p95"),
                    )
                )
            }
        }
        return HospitalPayload(updateTime, hospitals)
    }

}
