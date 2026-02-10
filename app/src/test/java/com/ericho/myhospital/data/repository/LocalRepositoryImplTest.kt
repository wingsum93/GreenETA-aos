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
    private val waitTimeUrl = "https://www.ha.org.hk/aedwt/data/aedWtData2.json"

    @Test
    fun `loadHospitalWaitTimes returns hospitals when payload is valid`() = runTest(testDispatcher) {
        val localDataSource = mockk<LocalDataSource>()
        coEvery { localDataSource.readHospitalGeoJson() } returns sampleGeoJson(
            jsonEnUrl = "https://www.ha.org.hk/aedwt/index.html?Lang=chien&AEHospital=PYN",
            jsonTcUrl = "https://www.ha.org.hk/aedwt/index.html?Lang=chien&AEHospital=PYN",
            jsonScUrl = "https://www.ha.org.hk/aedwt/index.html?Lang=chien&AEHospital=PYN",
        )
        val httpClient = buildHttpClient { request ->
            when (request.url.toString()) {
                waitTimeUrl -> respond(
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
        assertEquals("Pamela Youde Nethersole Eastern Hospital", result.hospitals.first().name)
        assertEquals("29/1/2026 3:15AM", result.updatedTime)
    }

    @Test
    fun `loadHospitalWaitTimes returns empty list when payload is blank`() = runTest(testDispatcher) {
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
                    "hospName_EN": "Pamela Youde Nethersole Eastern Hospital",
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
  "waitTime": [
    {
      "hospName": "Pamela Youde Nethersole Eastern Hospital",
      "t1wt": "0 minute",
      "manageT1case": "N",
      "t2wt": "less than 15 minutes",
      "manageT2case": "N",
      "t3p50": "29 minutes",
      "t3p95": "60 minutes",
      "t45p50": "4.5 hours",
      "t45p95": "6 hours"
    }
  ],
  "updateTime": "29/1/2026 3:15AM"
}
        """.trimIndent()
    }
}
