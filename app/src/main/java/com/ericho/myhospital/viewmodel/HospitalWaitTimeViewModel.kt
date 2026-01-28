package com.ericho.myhospital.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ericho.myhospital.data.repository.LocalRepository
import com.ericho.myhospital.data.dto.HospitalGeoJsonEntryDto
import com.ericho.myhospital.data.dto.HospitalWaitTimeResponseDto
import com.ericho.myhospital.model.HospitalPayload
import com.ericho.myhospital.model.HospitalWaitTime
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
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

    fun loadHospitalWaitTimes(languageTag: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, isEmpty = false)
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching {
                val hospitalEntries = localRepository.loadHospitalGeoJsonDtos()
                val language = resolveLanguage(languageTag)
                val waitTimeResponses = supervisorScope {
                    hospitalEntries.map { entry ->
                        async {
                            runCatching {
                                val url = entry.urlFor(language)
                                if (url.isBlank()) {
                                    return@runCatching null
                                }
                                val jsonText = httpClient.get(url).bodyAsText()
                                parseWaitTime(jsonText)
                            }.getOrNull()
                        }
                    }.awaitAll()
                }
                val hospitals = waitTimeResponses
                    .filterNotNull()
                    .map { it.toModel() }
                val updatedTime = waitTimeResponses
                    .firstOrNull { !it?.updateTime.isNullOrBlank() }
                    ?.updateTime
                HospitalPayload(updatedTime, hospitals)
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

    private fun parseWaitTime(jsonText: String): HospitalWaitTimeResponseDto {
        val root = JSONObject(jsonText)
        return HospitalWaitTimeResponseDto(
            hospName = root.optString("hospName"),
            t1wt = root.optString("t1wt"),
            manageT1case = root.optString("manageT1case"),
            t2wt = root.optString("t2wt"),
            manageT2case = root.optString("manageT2case"),
            t3p50 = root.optString("t3p50"),
            t3p95 = root.optString("t3p95"),
            t45p50 = root.optString("t45p50"),
            t45p95 = root.optString("t45p95"),
            updateTime = root.optString("updateTime"),
        )
    }

    private fun HospitalWaitTimeResponseDto.toModel(): HospitalWaitTime {
        return HospitalWaitTime(
            name = hospName,
            t1wt = t1wt,
            manageT1case = manageT1case,
            t2wt = t2wt,
            manageT2case = manageT2case,
            t3p50 = t3p50,
            t3p95 = t3p95,
            t45p50 = t45p50,
            t45p95 = t45p95,
        )
    }

    private enum class HospitalLanguage {
        EN,
        TC,
        SC,
    }

    private fun resolveLanguage(languageTag: String): HospitalLanguage {
        val normalized = languageTag.trim().lowercase()
        return when {
            normalized.startsWith("zh-hant") || normalized.startsWith("zh-tw") -> HospitalLanguage.TC
            normalized.startsWith("zh-hans") || normalized.startsWith("zh-cn") -> HospitalLanguage.SC
            else -> HospitalLanguage.EN
        }
    }

    private fun HospitalGeoJsonEntryDto.urlFor(language: HospitalLanguage): String {
        return when (language) {
            HospitalLanguage.EN -> jsonEnUrl
            HospitalLanguage.TC -> jsonTcUrl
            HospitalLanguage.SC -> jsonScUrl
        }
    }
}
