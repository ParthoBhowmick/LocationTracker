package com.example.locationtrackertest

import android.graphics.Color

object Constant {
    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_INTERVAL = 2000L

    const val POLYLINE_COLOR = Color.BLUE
    const val POLYLINE_WIDTH = 8f
    const val MAP_ZOOM = 12f

    const val START_RESUME = "start_or_resume"
    const val PAUSE = "pause"

    const val NOTIFICATION_CHANNEL_ID = "track_location"
    const val NOTIFICATION_CHANNEL_NAME = "location"
    const val NOTIFICATION_ID = 1
}