package com.ericho.myhospital.data

interface LocalRepository {
    suspend fun loadHospitalWaitTimeJson(): String?

    suspend fun cacheHospitalWaitTimeJson(json: String)
}
