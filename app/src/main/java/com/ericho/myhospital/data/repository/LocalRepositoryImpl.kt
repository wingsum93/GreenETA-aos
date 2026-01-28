package com.ericho.myhospital.data.repository

import com.ericho.myhospital.data.dto.HospitalGeoJsonEntryDto
import com.ericho.myhospital.data.source.local.LocalDataSource
import org.json.JSONObject

class LocalRepositoryImpl(
    private val localDataSource: LocalDataSource,
) : LocalRepository {
    override suspend fun loadHospitalWaitTimeJson(): String? {
        return localDataSource.readHospitalWaitTimeJson()
    }

    override suspend fun cacheHospitalWaitTimeJson(json: String) {
        localDataSource.writeHospitalWaitTimeJson(json)
    }

    override suspend fun loadHospitalGeoJsonDtos(): List<HospitalGeoJsonEntryDto> {
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
}
