package com.ericho.myhospital.data.dto

data class HospitalGeoJsonEntryDto(
    val nameEn: String,
    val nameTc: String,
    val nameSc: String,
    val latitude: Double,
    val longitude: Double,
    val jsonEnUrl: String,
    val jsonTcUrl: String,
    val jsonScUrl: String,
)
