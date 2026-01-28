package com.ericho.myhospital.data.repository

import com.ericho.myhospital.data.dto.HospitalGeoJsonEntryDto
import com.ericho.myhospital.data.dto.HospitalWaitTimeResponseDto
import com.ericho.myhospital.data.source.local.LocalDataSource
import com.ericho.myhospital.model.HospitalPayload
import com.ericho.myhospital.model.HospitalWaitTime
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import org.json.JSONObject

class LocalRepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val httpClient: HttpClient,
    private val ioDispatcher: CoroutineDispatcher,
) : LocalRepository {
    override suspend fun loadHospitalWaitTimes(languageTag: String): HospitalPayload = withContext(ioDispatcher) {
        val hospitalEntries = loadHospitalGeoJsonDtos()
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
            }.map { it.await() }
        }
        val hospitals = waitTimeResponses
            .filterNotNull()
            .map { it.toModel() }
        val updatedTime = waitTimeResponses
            .firstOrNull { !it?.updateTime.isNullOrBlank() }
            ?.updateTime
        HospitalPayload(updatedTime, hospitals)
    }

    private suspend fun loadHospitalGeoJsonDtos(): List<HospitalGeoJsonEntryDto> {
        val rawJson = localDataSource.readHospitalGeoJson()
        val root = JSONObject(rawJson)
        val features = root.optJSONArray("features") ?: return emptyList()
        val hospitals = ArrayList<HospitalGeoJsonEntryDto>(features.length())
        for (index in 0 until features.length()) {
            val feature = features.optJSONObject(index) ?: continue
            val properties = feature.optJSONObject("properties") ?: continue
            hospitals.add(
                HospitalGeoJsonEntryDto(
                    nameEn = properties.optString("hospName_EN"),
                    nameTc = properties.optString("hospName_TC"),
                    nameSc = properties.optString("hospName_SC"),
                    latitude = properties.optDouble("LATITUDE"),
                    longitude = properties.optDouble("LONGITUDE"),
                    jsonEnUrl = properties.optString("JSON_EN"),
                    jsonTcUrl = properties.optString("JSON_TC"),
                    jsonScUrl = properties.optString("JSON_SC"),
                ),
            )
        }
        return hospitals
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
