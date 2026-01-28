package com.ericho.myhospital.data.repository

import com.ericho.myhospital.model.HospitalPayload

interface LocalRepository {
    suspend fun loadHospitalWaitTimes(languageTag: String): HospitalPayload
}
