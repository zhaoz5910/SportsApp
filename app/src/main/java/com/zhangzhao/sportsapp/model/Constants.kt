package com.zhangzhao.sportsapp.model

import android.graphics.Color
import com.github.mikephil.charting.data.LineDataSet

object Constants {

    // Database
    const val RUN_DATABASE_NAME = "run_db"

    const val REQUEST_CODE_LOCATION_PERMISSION = 1

    const val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"

    // Tracking Options
    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_UPDATE_INTERVAL = 2000L

    // Map Options
    const val POLYLINE_COLOR = Color.RED
    const val POLYLINE_WIDTH = 8f
    const val MAP_ZOOM = 20f

    // Timer
    const val TIMER_UPDATE_INTERVAL = 50L

    // LineChart
    val LINE_DATA_MODE = LineDataSet.Mode.CUBIC_BEZIER

    // MapView
    const val MAP_VIEW_HEIGHT_IN_DP = 200f

    // Notifications
    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1

    // Shared Preferences
    const val SHARED_PREFERENCES_NAME = "sharedPref"
    const val KEY_NAME = "KEY_NAME"
    const val KEY_WEIGHT = "KEY_WEIGHT"
    const val KEY_FIRST_TIME_TOGGLE = "KEY_FIRST_TIME_TOGGLE"

    // Service Intent Actions
    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"
    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"

    // Permissions
    val permissionsLocation = arrayOf(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )
    val permissionsForegroundLocation = arrayOf(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )
    val permissionsBackgroundLocation = arrayOf(
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )
}