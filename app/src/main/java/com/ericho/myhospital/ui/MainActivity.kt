package com.ericho.myhospital.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ericho.myhospital.R
import com.ericho.myhospital.model.HospitalWaitTime
import com.ericho.myhospital.viewmodel.HospitalWaitTimeUiState
import com.ericho.myhospital.viewmodel.HospitalWaitTimeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: HospitalWaitTimeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            HomeScreen(uiState = uiState)
        }
    }
}

@Composable
private fun HomeScreen(uiState: HospitalWaitTimeUiState) {
    val topHospitals = remember(uiState.hospitals) {
        uiState.hospitals
            .sortedBy { it.t3p50Minutes() }
            .take(2)
            .map { it.name }
            .toSet()
    }
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(WindowInsets.systemBars.asPaddingValues())
            ) {
                when {
                    uiState.isLoading -> LoadingState()
                    uiState.isEmpty -> EmptyState()
                    else -> HospitalList(
                        updatedTime = uiState.updatedTime,
                        hospitals = uiState.hospitals,
                        topHospitals = topHospitals,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "No hospitals available.")
    }
}

@Composable
private fun HospitalList(
    updatedTime: String?,
    hospitals: List<HospitalWaitTime>,
    topHospitals: Set<String>,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = updatedTime?.let { "Updated: $it" } ?: stringResource(R.string.app_name),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(hospitals, key = { it.name }) { hospital ->
                HospitalCard(
                    hospital = hospital,
                    isTop = topHospitals.contains(hospital.name),
                )
            }
        }
    }
}

@Composable
private fun HospitalCard(hospital: HospitalWaitTime, isTop: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = hospital.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (isTop) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Top 2",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(999.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Category 3: ${hospital.t3p50} (P95 ${hospital.t3p95})",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = "T1: ${hospital.t1wt} · T2: ${hospital.t2wt} · T4/5: ${hospital.t45p50} (P95 ${hospital.t45p95})",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
