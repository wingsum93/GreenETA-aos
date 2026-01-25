package com.ericho.myhospital.data.repository

interface LocalRepository {
    suspend fun loadHospitalWaitTimeJson(): String?

    suspend fun cacheHospitalWaitTimeJson(json: String)
}
