package com.ericho.myhospital.data.source.asset

import android.content.Context
import com.ericho.myhospital.model.Hospital
import org.json.JSONObject

object HospitalGeoJsonDataSource {
    private const val GEOJSON_ASSET = "hkhospital.geojson"

    fun loadHospitals(context: Context): List<Hospital> {
        val rawJson = context.assets.open(GEOJSON_ASSET).bufferedReader().use { it.readText() }
        val root = JSONObject(rawJson)
        val features = root.getJSONArray("features")
        val hospitals = ArrayList<Hospital>(features.length())
        for (index in 0 until features.length()) {
            val feature = features.getJSONObject(index)
            val properties = feature.getJSONObject("properties")
            hospitals.add(
                Hospital(
                    nameEn = properties.getString("hospName_EN"),
                    nameTc = properties.getString("hospName_TC"),
                    nameSc = properties.getString("hospName_SC"),
                    latitude = properties.getDouble("LATITUDE"),
                    longitude = properties.getDouble("LONGITUDE"),
                    jsonEnUrl = properties.getString("JSON_EN"),
                    jsonTcUrl = properties.getString("JSON_TC"),
                    jsonScUrl = properties.getString("JSON_SC"),
                ),
            )
        }
        return hospitals
    }
}
