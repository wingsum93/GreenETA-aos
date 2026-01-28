package com.ericho.myhospital.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ericho.myhospital.R
import com.ericho.myhospital.model.HospitalWaitTime
import com.ericho.myhospital.viewmodel.HospitalWaitTimeIntent
import com.ericho.myhospital.viewmodel.HospitalWaitTimeUiState
import com.ericho.myhospital.viewmodel.HospitalWaitTimeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: HospitalWaitTimeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val configuration = LocalConfiguration.current
            val languageTag = remember(configuration) {
                val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    configuration.locales.get(0)
                } else {
                    @Suppress("DEPRECATION")
                    configuration.locale
                } ?: Locale.ENGLISH
                when (locale.toLanguageTag()) {
                    "zh-Hant" -> "zh-TW"
                    "zh-Hans" -> "zh-CN"
                    else -> locale.toLanguageTag()
                }
            }
            LaunchedEffect(languageTag) {
                viewModel.accept(HospitalWaitTimeIntent.Load(languageTag))
            }
            MainTabs(uiState = uiState)
        }
    }
}

@Composable
private fun MainTabs(uiState: HospitalWaitTimeUiState) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    // 👇 讀一次，確保 locale/config 改變會令呢個 composable recompose
    val configuration = LocalConfiguration.current

    val tabs = remember(configuration) {
        listOf(
            R.string.tab_hospitals,
            R.string.tab_settings,
        )
    }

    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(WindowInsets.systemBars.asPaddingValues())
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTabIndex) {
                        0 -> HomeScreen(uiState = uiState)
                        1 -> SettingsScreen()
                    }
                }
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        icon = {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Filled.List,
                                contentDescription = stringResource(tabs[0]),
                            )
                        },
                        label = { Text(text = stringResource(tabs[0])) },
                    )
                    NavigationBarItem(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        icon = {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = stringResource(tabs[1]),
                            )
                        },
                        label = { Text(text = stringResource(tabs[1])) },
                    )
                }
            }
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 0.dp)
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: decide post-click action */ },
        color = Color(0xFFE0E0E0),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
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
                        modifier = Modifier
                            .background(color = Color(0xFF99CC00))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = "Category 3: ${hospital.t3p50} (P95 ${hospital.t3p95})",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.size(2.dp))
            Text(
                text = "T1: ${hospital.t1wt} · T2: ${hospital.t2wt} · T4/5: ${hospital.t45p50} (P95 ${hospital.t45p95})",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun SettingsScreen() {
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
    }
    val currentTag = prefs.getString(PREF_KEY_LANGUAGE_TAG, null)
        ?: AppCompatDelegate.getApplicationLocales().get(0)?.toLanguageTag()
    val normalizedTag = when (currentTag) {
        "zh-Hant" -> "zh-TW"
        "zh-Hans" -> "zh-CN"
        else -> currentTag
    }
    val languageOptions = listOf(
        LanguageOption(label = "English", tag = "en"),
        LanguageOption(label = "Traditional Chinese", tag = "zh-TW"),
        LanguageOption(label = "Simplified Chinese", tag = "zh-CN"),
    )
    var showLanguageDialog by remember { mutableStateOf(false) }
    var selectedLanguageTag by remember(normalizedTag) { mutableStateOf(normalizedTag ?: "en") }
    val libraries = listOf(
        LibraryLink("AndroidX Core", "https://github.com/androidx/androidx"),
        LibraryLink("AppCompat", "https://github.com/androidx/androidx"),
        LibraryLink(
            "Material Components",
            "https://github.com/material-components/material-components-android"
        ),
        LibraryLink("Activity", "https://github.com/androidx/androidx"),
        LibraryLink("ConstraintLayout", "https://github.com/androidx/constraintlayout"),
        LibraryLink("Jetpack Compose", "https://github.com/androidx/androidx"),
        LibraryLink("Lifecycle", "https://github.com/androidx/androidx"),
        LibraryLink("Koin", "https://github.com/InsertKoinIO/koin"),
        LibraryLink("Ktor", "https://github.com/ktorio/ktor"),
        LibraryLink("Lottie Compose", "https://github.com/airbnb/lottie-android"),
        LibraryLink("Room", "https://github.com/androidx/androidx"),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "Language",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Button(
            onClick = {
                selectedLanguageTag = currentTag ?: "en"
                showLanguageDialog = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Click to switch language")
        }

        Spacer(modifier = Modifier.size(24.dp))
        Text(
            text = "Support",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Text(
            text = "Report a bug",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:dev@example.com")
                        putExtra(Intent.EXTRA_SUBJECT, "Bug report")
                    }
                    val chooser = Intent.createChooser(intent, "Report a bug")
                    context.startActivity(chooser)
                }
                .padding(vertical = 8.dp),
        )

        Spacer(modifier = Modifier.size(24.dp))
        Text(
            text = "Open source libraries",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        libraries.forEach { lib ->
            Text(
                text = lib.name,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(lib.url))
                        context.startActivity(intent)
                    }
                    .padding(vertical = 6.dp),
            )
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(text = "Select language") },
            text = {
                Column {
                    languageOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedLanguageTag = option.tag }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = selectedLanguageTag == option.tag,
                                onClick = { selectedLanguageTag = option.tag },
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = option.label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        prefs.edit().putString(PREF_KEY_LANGUAGE_TAG, selectedLanguageTag).apply()
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(selectedLanguageTag)
                        )
                        showLanguageDialog = false
                    }
                ) {
                    Text(text = "OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}

private data class LanguageOption(
    val label: String,
    val tag: String,
)

private data class LibraryLink(
    val name: String,
    val url: String,
)

private const val PREFS_NAME = "app_settings"
private const val PREF_KEY_LANGUAGE_TAG = "language_tag"
