package com.ericho.myhospital

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var hospitalList: RecyclerView
    private lateinit var emptyState: TextView
    private lateinit var loadingState: ProgressBar
    private lateinit var updatedText: TextView
    private val adapter = HospitalWaitTimeAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        hospitalList = findViewById(R.id.hospital_list)
        emptyState = findViewById(R.id.hospital_empty)
        loadingState = findViewById(R.id.hospital_loading)
        updatedText = findViewById(R.id.hospital_updated)

        hospitalList.layoutManager = LinearLayoutManager(this)
        hospitalList.adapter = adapter

        loadHospitalWaitTimes()
    }

    private fun loadHospitalWaitTimes() {
        setLoading(true)
        Thread {
            val result = runCatching {
                val jsonText =
                    URL("https://www.ha.org.hk/opendata/aed/aedwtdata2-en.json").readText()
                parseHospitals(jsonText)
            }
            runOnUiThread {
                setLoading(false)
                result.onSuccess { payload ->
                    updatedText.text = "Updated: ${payload.updatedTime}"
                    val topHospitals = payload.hospitals
                        .sortedBy { it.t3p50Minutes() }
                        .take(2)
                        .map { it.name }
                        .toSet()
                    adapter.submitData(payload.hospitals, topHospitals)
                    setEmpty(payload.hospitals.isEmpty())
                }.onFailure {
                    setEmpty(true)
                    updatedText.text = getString(R.string.app_name)
                }
            }
        }.start()
    }

    private fun setLoading(isLoading: Boolean) {
        loadingState.visibility = if (isLoading) View.VISIBLE else View.GONE
        hospitalList.visibility = if (isLoading) View.GONE else View.VISIBLE
        emptyState.visibility = View.GONE
    }

    private fun setEmpty(isEmpty: Boolean) {
        emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        hospitalList.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun parseHospitals(jsonText: String): HospitalPayload {
        val root = JSONObject(jsonText)
        val updateTime = root.optString("updateTime")
        val waitTimeArray = root.optJSONArray("waitTime")
        val hospitals = mutableListOf<HospitalWaitTime>()
        if (waitTimeArray != null) {
            for (index in 0 until waitTimeArray.length()) {
                val item = waitTimeArray.getJSONObject(index)
                hospitals.add(
                    HospitalWaitTime(
                        name = item.optString("hospName"),
                        t1wt = item.optString("t1wt"),
                        t2wt = item.optString("t2wt"),
                        t3p50 = item.optString("t3p50"),
                        t3p95 = item.optString("t3p95"),
                        t45p50 = item.optString("t45p50"),
                        t45p95 = item.optString("t45p95"),
                    )
                )
            }
        }
        return HospitalPayload(updateTime, hospitals)
    }
}

private data class HospitalPayload(
    val updatedTime: String,
    val hospitals: List<HospitalWaitTime>,
)
