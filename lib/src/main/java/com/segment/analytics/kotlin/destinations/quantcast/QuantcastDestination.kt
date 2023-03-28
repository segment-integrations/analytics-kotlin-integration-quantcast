package com.segment.analytics.kotlin.destinations.quantcast

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.quantcast.measurement.service.QuantcastClient
import com.segment.analytics.kotlin.android.plugins.AndroidLifecycle
import com.segment.analytics.kotlin.core.*
import com.segment.analytics.kotlin.core.platform.DestinationPlugin
import com.segment.analytics.kotlin.core.platform.Plugin
import com.segment.analytics.kotlin.core.platform.plugins.logger.log
import kotlinx.serialization.Serializable

/**
 * Quantcast is an audience measurement tool that captures demographic and traffic data about the
 * visitors to your site, to make sure your ads are targeted at the right people.
 *
 * @see <a href="https://www.quantcast.com/">Quantcast</a>
 * @see <a href="https://segment.com/docs/integrations/quantcast/">Quantcast Integration</a>
 * @see <a href="https://github.com/quantcast/android-measurement#quantcast-android-sdk">
 *     Quantcast Android SDK</a>
 */
class QuantcastDestination : DestinationPlugin(), AndroidLifecycle {
    companion object {
        private const val QUANTCAST_FULL_KEY = "Quantcast"
        private const val VIEWED_EVENT_FORMAT = "Viewed %s Screen"
    }
    internal var quantcastSettings: QuantcastSettings? = null

    override val key: String = QUANTCAST_FULL_KEY

    override fun update(settings: Settings, type: Plugin.UpdateType) {
        super.update(settings, type)
        this.quantcastSettings =
            settings.destinationSettings(key, QuantcastSettings.serializer())
        if (type == Plugin.UpdateType.Initial) {
            QuantcastClient.startQuantcast(
                analytics.configuration.application as Application?,
                quantcastSettings?.apiKey,
                null,
                null)
            analytics.log("QuantcastClient.enableLogging(true)")
            QuantcastClient.enableLogging(true)
        }
    }

    override fun identify(payload: IdentifyEvent): BaseEvent {
        analytics.log("QuantcastClient.recordUserIdentifier(${payload.userId})")
        QuantcastClient.recordUserIdentifier(payload.userId)
        return payload
    }

    override fun screen(payload: ScreenEvent): BaseEvent {
        quantcastLogEvent(String.format(VIEWED_EVENT_FORMAT, payload.name))
        return payload
    }

    override fun track(payload: TrackEvent): BaseEvent {
        quantcastLogEvent(payload.event)
        return payload
    }

    override fun onActivityStarted(activity: Activity?) {
        super.onActivityStarted(activity)
        if(quantcastSettings != null) {
            QuantcastClient.activityStart(activity)
            analytics.log(
                "QuantcastClient.activityStart(activity)"
            )
        }
    }

    override fun onActivityStopped(activity: Activity?) {
        super.onActivityStopped(activity)
        analytics.log("QuantcastClient.activityStop()")
        QuantcastClient.activityStop()
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        super.onActivityCreated(activity, savedInstanceState)
    }

    private fun quantcastLogEvent(event: String) {
        analytics.log("QuantcastClient.logEvent($event)")
        QuantcastClient.logEvent(event)
    }
}

/**
 * Quantcast Settings data class.
 */
@Serializable
data class QuantcastSettings(
//    Quantcast API key
    var apiKey: String,
//    P-Code after login to Quantcast.
    var pCode: String,
//    By default data will be sent to Quantcast Measure, if enable it sends data to Quantcast Advertise
    var advertise: Boolean,
//    When data is for eCommerce events, Segment will include labels corresponding to the products included in the event.
    var advertiseProducts: Boolean
)