package com.ericho.myhospital.model

data class HospitalPayload(
    val updatedTime: String?,
    val hospitals: List<HospitalWaitTime>,
)
