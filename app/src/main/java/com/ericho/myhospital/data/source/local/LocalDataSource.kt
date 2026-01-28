package com.ericho.myhospital.data.source.local

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalDataSource(
    private val context: Context,
) {
    private val geoJsonAsset: String = "hkhospital.geojson"

    suspend fun readHospitalGeoJson(): String = withContext(Dispatchers.IO) {
        context.assets.open(geoJsonAsset).bufferedReader().use { it.readText() }
    }
}
