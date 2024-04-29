package com.zhangzhao.sportsapp.model

import android.graphics.Color
import com.amap.api.maps.model.LatLng
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
    const val ACTION_SHOW_TRACKING_FRAGMENT = "com.zhangzhao.sportsapp.action.SHOW_TRACKING_FRAGMENT"
    const val ACTION_START_OR_RESUME_SERVICE = "com.zhangzhao.sportsapp.action.START_SERVICE"
    const val ACTION_PAUSE_SERVICE = "com.zhangzhao.sportsapp.action.PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "com.zhangzhao.sportsapp.action.STOP_SERVICE"

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

    val polyline1 = listOf<LatLng>(
        LatLng(39.7332116085915,116.16750199787893),
        LatLng(39.733211362098814,116.16750297923234),
        LatLng(39.73326228704342,116.16749760267557),
        LatLng(39.73335488842816,116.16749721942315),
        LatLng(39.73340375291472,116.16749520447438),
        LatLng(39.73345657402176,116.16749807287452),
        LatLng(39.73350718914732,116.16749737224363),
        LatLng(39.73355455476468,116.16750262489683),
        LatLng(39.73360355868358,116.16750381859586),
        LatLng(39.73365462501396,116.16750319176793),
        LatLng(39.73365154457001,116.16753519948507),
        LatLng(39.73364031809487,116.16759861976006),
        LatLng(39.733634051306844,116.1676612450841)
    )
    val polyline2 = listOf<LatLng>(
        LatLng(39.7336155753615,116.16787428865355),
        LatLng(39.7336155753615,116.16787428865355),
        LatLng(39.7336196911713,116.16794137603873),
        LatLng(39.73362617250352,116.16800935927468)
    )
}