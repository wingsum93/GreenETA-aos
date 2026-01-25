package com.ericho.myhospital.model

data class Hospital(
    val nameEn: String,
    val nameTc: String,
    val nameSc: String,
    val latitude: Double,
    val longitude: Double,
    val jsonEnUrl: String,
    val jsonTcUrl: String,
    val jsonScUrl: String,
)
