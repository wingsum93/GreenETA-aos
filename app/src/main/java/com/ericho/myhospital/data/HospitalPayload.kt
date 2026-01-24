package com.ericho.myhospital.data

import com.ericho.myhospital.HospitalWaitTime

data class HospitalPayload(
    val updatedTime: String,
    val hospitals: List<HospitalWaitTime>,
)
