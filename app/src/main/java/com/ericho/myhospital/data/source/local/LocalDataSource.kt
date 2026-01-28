package com.ericho.myhospital.data.source.local

import android.content.Context

class LocalDataSource(
    private val context: Context,
) {
    private val geoJsonAsset: String = "hkhospital.geojson"

    fun readHospitalGeoJson(): String {
        return context.assets.open(geoJsonAsset).bufferedReader().use { it.readText() }
    }
}
