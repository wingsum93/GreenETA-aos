package com.ericho.myhospital.data.dto

data class HospitalWaitTimeResponseDto(
    val hospName: String,
    val t1wt: String,
    val manageT1case: String,
    val t2wt: String,
    val manageT2case: String,
    val t3p50: String,
    val t3p95: String,
    val t45p50: String,
    val t45p95: String,
    val updateTime: String,
)
