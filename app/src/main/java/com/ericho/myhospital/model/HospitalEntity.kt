package com.ericho.myhospital.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hospitals")
data class HospitalEntity(
    @PrimaryKey val nameEn: String,
    val nameTc: String,
    val nameSc: String,
    val latitude: Double,
    val longitude: Double,
    val jsonEnUrl: String,
    val jsonTcUrl: String,
    val jsonScUrl: String,
)
