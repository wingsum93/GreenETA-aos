package com.ericho.myhospital.data.repository

import com.ericho.myhospital.data.dto.HospitalGeoJsonEntryDto

interface LocalRepository {
    suspend fun loadHospitalWaitTimeJson(): String?

    suspend fun cacheHospitalWaitTimeJson(json: String)

    suspend fun loadHospitalGeoJsonDtos(): List<HospitalGeoJsonEntryDto>
}
