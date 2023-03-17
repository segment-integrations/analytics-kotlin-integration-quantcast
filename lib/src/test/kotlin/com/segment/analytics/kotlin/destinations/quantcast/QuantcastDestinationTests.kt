package com.segment.analytics.kotlin.destinations.quantcast

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import com.quantcast.measurement.service.QuantcastClient
import com.segment.analytics.kotlin.core.*
import com.segment.analytics.kotlin.core.platform.Plugin
import com.segment.analytics.kotlin.core.utilities.LenientJson
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.buildJsonObject
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class QuantcastDestinationTests {
    @MockK
    lateinit var mockApplication: Application

    @MockK
    lateinit var mockedContext: Context

    @MockK(relaxUnitFun = true)
    lateinit var mockedAnalytics: Analytics

    lateinit var mockedQuantcastDestination: QuantcastDestination

    private val sampleQuantcastSettings: Settings = LenientJson.decodeFromString(
        """
            {
              "integrations": {
                "Quantcast": {
                  "apiKey": "APIKEY1234567890",
                  "advertiseProducts": false,
                  "advertise": false,
                  "pCode": ""
                }
              }
            }
        """.trimIndent()
    )

    init {
        MockKAnnotations.init(this)
    }

    @Before
    fun setUp() {
        mockkStatic(QuantcastClient::class)
        mockedQuantcastDestination = QuantcastDestination()
        every { mockedAnalytics.configuration.application } returns mockApplication
        every { mockApplication.applicationContext } returns mockedContext
        mockedAnalytics.configuration.application = mockedContext
        mockedQuantcastDestination.analytics = mockedAnalytics
    }

    @Test
    fun `settings are updated correctly`() {
        // An Quantcast example settings
        val quantcastSettings: Settings = sampleQuantcastSettings
        mockedQuantcastDestination.update(quantcastSettings, Plugin.UpdateType.Initial)

        /* assertions Quantcast config */
        Assertions.assertNotNull(mockedQuantcastDestination.quantcastSettings)
        with(mockedQuantcastDestination.quantcastSettings!!) {
            assertEquals(apiKey, "APIKEY1234567890")
            assertEquals(advertiseProducts, false)
            assertEquals(advertise, false)
            assertEquals(pCode, "")
        }
        verify { QuantcastClient.enableLogging(true) }
    }

    @Test
    fun `identify handled correctly`() {
        val sampleEvent = IdentifyEvent(
            userId = "User-Id-123",
            traits = buildJsonObject {
            }
        ).apply { context = emptyJsonObject }
        mockedQuantcastDestination.identify(sampleEvent)
        verify { QuantcastClient.recordUserIdentifier("User-Id-123") }
    }

    @Test
    fun `screen handled correctly`() {
        val sampleEvent = ScreenEvent(
            name = "Screen 1",
            category = "Category 1",
            properties = emptyJsonObject
        ).apply {
            context = emptyJsonObject
        }
        mockedQuantcastDestination.screen(sampleEvent)
        verify { QuantcastClient.logEvent("Viewed Screen 1 Screen") }
    }


    @Test
    fun `track handled correctly`() {
        val sampleEvent = TrackEvent(
            event = "Track 1",
            properties = buildJsonObject {
            }
        ).apply {
            context = emptyJsonObject
        }
        mockedQuantcastDestination.track(sampleEvent)
        verify { QuantcastClient.logEvent("Track 1") }
    }
}