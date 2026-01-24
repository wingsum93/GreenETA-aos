package com.ericho.myhospital

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.viewModels
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var hospitalList: RecyclerView
    private lateinit var emptyState: TextView
    private lateinit var loadingState: ProgressBar
    private lateinit var updatedText: TextView
    private val adapter = HospitalWaitTimeAdapter()
    private val viewModel: HospitalWaitTimeViewModel by viewModels()

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

        observeHospitalWaitTimes()
    }

    private fun observeHospitalWaitTimes() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.isLoading) {
                        setLoading(true)
                    } else {
                        setLoading(false)
                        updatedText.text = state.updatedTime?.let { "Updated: $it" }
                            ?: getString(R.string.app_name)
                        val topHospitals = state.hospitals
                            .sortedBy { it.t3p50Minutes() }
                            .take(2)
                            .map { it.name }
                            .toSet()
                        adapter.submitData(state.hospitals, topHospitals)
                        setEmpty(state.isEmpty)
                    }
                }
            }
        }
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

}
