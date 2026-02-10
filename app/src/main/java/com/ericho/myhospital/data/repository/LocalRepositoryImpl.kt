package com.ericho.myhospital.data.repository

import com.ericho.myhospital.data.dto.HospitalGeoJsonEntryDto
import com.ericho.myhospital.data.dto.HospitalWaitTimeDto
import com.ericho.myhospital.data.dto.HospitalWaitTimePayloadDto
import com.ericho.myhospital.data.source.local.LocalDataSource
import com.ericho.myhospital.model.HospitalPayload
import com.ericho.myhospital.model.HospitalWaitTime
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class LocalRepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val httpClient: HttpClient,
    private val ioDispatcher: CoroutineDispatcher,
) : LocalRepository {
    override suspend fun loadHospitalWaitTimes(languageTag: String): HospitalPayload = withContext(ioDispatcher) {
        val hospitalEntries = loadHospitalGeoJsonDtos()
        val nameLookup = buildHospitalNameLookup(hospitalEntries)
        val language = resolveLanguage(languageTag)
        val jsonText = runCatching { httpClient.get(WAIT_TIME_URL).bodyAsText() }.getOrDefault("")
        if (jsonText.isBlank()) {
            return@withContext HospitalPayload(null, emptyList())
        }
        val payload = runCatching { parseWaitTimePayload(jsonText) }
            .getOrElse { return@withContext HospitalPayload(null, emptyList()) }
        val hospitals = payload.waitTime.map { dto ->
            val entry = nameLookup[dto.hospName]
            val displayName = entry?.nameFor(language)?.takeUnless { it.isBlank() } ?: dto.hospName
            dto.toModel(displayName)
        }
        val updatedTime = payload.updateTime.trim().ifEmpty { null }
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

    private fun parseWaitTimePayload(jsonText: String): HospitalWaitTimePayloadDto {
        val root = JSONObject(jsonText)
        val waitTimeArray = root.optJSONArray("waitTime")
        val waitTime = if (waitTimeArray == null) {
            listOf(parseWaitTimeEntry(root))
        } else {
            parseWaitTimeEntries(waitTimeArray)
        }
        return HospitalWaitTimePayloadDto(
            updateTime = root.optString("updateTime"),
            waitTime = waitTime.filter { it.hospName.isNotBlank() },
        )
    }

    private fun parseWaitTimeEntries(waitTimeArray: JSONArray): List<HospitalWaitTimeDto> {
        val entries = ArrayList<HospitalWaitTimeDto>(waitTimeArray.length())
        for (index in 0 until waitTimeArray.length()) {
            val item = waitTimeArray.optJSONObject(index) ?: continue
            entries.add(parseWaitTimeEntry(item))
        }
        return entries
    }

    private fun parseWaitTimeEntry(item: JSONObject): HospitalWaitTimeDto {
        return HospitalWaitTimeDto(
            hospName = item.optString("hospName"),
            t1wt = item.optString("t1wt"),
            manageT1case = item.optString("manageT1case"),
            t2wt = item.optString("t2wt"),
            manageT2case = item.optString("manageT2case"),
            t3p50 = item.optString("t3p50"),
            t3p95 = item.optString("t3p95"),
            t45p50 = item.optString("t45p50"),
            t45p95 = item.optString("t45p95"),
        )
    }

    private fun HospitalWaitTimeDto.toModel(displayName: String): HospitalWaitTime {
        return HospitalWaitTime(
            name = displayName,
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

    private fun buildHospitalNameLookup(entries: List<HospitalGeoJsonEntryDto>): Map<String, HospitalGeoJsonEntryDto> {
        val lookup = HashMap<String, HospitalGeoJsonEntryDto>(entries.size * 3)
        entries.forEach { entry ->
            if (entry.nameEn.isNotBlank()) {
                lookup[entry.nameEn] = entry
            }
            if (entry.nameTc.isNotBlank()) {
                lookup[entry.nameTc] = entry
            }
            if (entry.nameSc.isNotBlank()) {
                lookup[entry.nameSc] = entry
            }
        }
        return lookup
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

    private fun HospitalGeoJsonEntryDto.nameFor(language: HospitalLanguage): String {
        return when (language) {
            HospitalLanguage.EN -> nameEn
            HospitalLanguage.TC -> nameTc
            HospitalLanguage.SC -> nameSc
        }
    }

    private companion object {
        private const val WAIT_TIME_URL = "https://www.ha.org.hk/aedwt/data/aedWtData2.json"
    }
}
