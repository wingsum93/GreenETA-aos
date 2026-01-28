package com.ericho.myhospital.data.repository

import com.ericho.myhospital.data.source.local.LocalDataSource
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LocalRepositoryImplTest {
    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `loadHospitalWaitTimes returns hospitals when payload is valid`() = runTest(testDispatcher) {
        val localDataSource = mockk<LocalDataSource>()
        coEvery { localDataSource.readHospitalGeoJson() } returns sampleGeoJson(
            jsonEnUrl = "https://example.com/hospital.json",
            jsonTcUrl = "",
            jsonScUrl = "",
        )
        val httpClient = buildHttpClient { request ->
            when (request.url.toString()) {
                "https://example.com/hospital.json" -> respond(
                    content = sampleWaitTimeJson(),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", "application/json"),
                )
                else -> respond(
                    content = "",
                    status = HttpStatusCode.NotFound,
                )
            }
        }

        val repository = LocalRepositoryImpl(localDataSource, httpClient, testDispatcher)

        val result = repository.loadHospitalWaitTimes("en")

        assertEquals(1, result.hospitals.size)
        assertEquals("Test Hospital", result.hospitals.first().name)
        assertEquals("2024-01-01", result.updatedTime)
    }

    @Test
    fun `loadHospitalWaitTimes returns empty list when url is blank`() = runTest(testDispatcher) {
        val localDataSource = mockk<LocalDataSource>()
        coEvery { localDataSource.readHospitalGeoJson() } returns sampleGeoJson(
            jsonEnUrl = "",
            jsonTcUrl = "",
            jsonScUrl = "",
        )
        val httpClient = buildHttpClient { _ ->
            respond(
                content = "",
                status = HttpStatusCode.NotFound,
            )
        }

        val repository = LocalRepositoryImpl(localDataSource, httpClient, testDispatcher)

        val result = repository.loadHospitalWaitTimes("en")

        assertTrue(result.hospitals.isEmpty())
        assertEquals(null, result.updatedTime)
    }

    private fun buildHttpClient(handler: MockRequestHandler): HttpClient {
        return HttpClient(MockEngine(handler))
    }

    private fun sampleGeoJson(
        jsonEnUrl: String,
        jsonTcUrl: String,
        jsonScUrl: String,
    ): String {
        return """
            {
              "features": [
                {
                  "properties": {
                    "hospName_EN": "Test Hospital",
                    "hospName_TC": "測試醫院",
                    "hospName_SC": "测试医院",
                    "LATITUDE": 0,
                    "LONGITUDE": 0,
                    "JSON_EN": "$jsonEnUrl",
                    "JSON_TC": "$jsonTcUrl",
                    "JSON_SC": "$jsonScUrl"
                  }
                }
              ]
            }
        """.trimIndent()
    }

    private fun sampleWaitTimeJson(): String {
        return """
            {
              "hospName": "Test Hospital",
              "t1wt": "10",
              "manageT1case": "Y",
              "t2wt": "20",
              "manageT2case": "Y",
              "t3p50": "30",
              "t3p95": "50",
              "t45p50": "15",
              "t45p95": "25",
              "updateTime": "2024-01-01"
            }
        """.trimIndent()
    }
}
