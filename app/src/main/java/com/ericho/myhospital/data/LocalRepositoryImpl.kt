package com.ericho.myhospital.data

class LocalRepositoryImpl(
    private val localDataSource: LocalDataSource,
) : LocalRepository {
    override suspend fun loadHospitalWaitTimeJson(): String? {
        return localDataSource.readHospitalWaitTimeJson()
    }

    override suspend fun cacheHospitalWaitTimeJson(json: String) {
        localDataSource.writeHospitalWaitTimeJson(json)
    }
}
