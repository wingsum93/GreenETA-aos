package com.ericho.myhospital.viewmodel

sealed interface HospitalWaitTimeIntent {
    data class Load(val languageTag: String) : HospitalWaitTimeIntent
    data object Refresh : HospitalWaitTimeIntent
}
