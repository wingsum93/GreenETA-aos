package com.ericho.myhospital.data.source.local

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LocalDataSource(
    private val context: Context,
) {
    private val cacheFile: File
        get() = File(context.cacheDir, "hospital_wait_time.json")

    suspend fun readHospitalWaitTimeJson(): String? = withContext(Dispatchers.IO) {
        if (cacheFile.exists()) {
            cacheFile.readText()
        } else {
            null
        }
    }

    suspend fun writeHospitalWaitTimeJson(json: String) = withContext(Dispatchers.IO) {
        cacheFile.writeText(json)
    }
}
