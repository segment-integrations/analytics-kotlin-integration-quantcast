package com.segment.analytics.kotlin.destinations.quantcast

import com.segment.analytics.kotlin.core.platform.DestinationPlugin

class QuantcastDestination: DestinationPlugin() {
    companion object {
        private const val QUANTCAST_FULL_KEY = "quantcast"
    }
    override val key: String = QUANTCAST_FULL_KEY
}