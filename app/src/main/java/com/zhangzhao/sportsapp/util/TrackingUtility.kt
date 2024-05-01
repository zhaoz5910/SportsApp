package com.zhangzhao.sportsapp.util

import android.Manifest
import android.content.Context
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.util.concurrent.TimeUnit

class TrackingUtility {
    companion object{

        fun hasLocationPermissions(context: Context): Boolean {
            return EasyPermissions.hasPermissions(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
        }

        // 接受毫秒数并将其转换为格式化字符串
        fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean = false): String {
            var milliseconds = ms
            val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
            milliseconds -= TimeUnit.HOURS.toMillis(hours)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
            milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
            if (!includeMillis) {
                return "${if (hours < 10) "0" else ""}$hours:" +
                        "${if (minutes < 10) "0" else ""}$minutes:" +
                        "${if (seconds < 10) "0" else ""}$seconds"
            }
            milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
            milliseconds /= 10
            return "${if (hours < 10) "0" else ""}$hours:" +
                    "${if (minutes < 10) "0" else ""}$minutes:" +
                    "${if (seconds < 10) "0" else ""}$seconds:" +
                    "${if (milliseconds < 10) "0" else ""}$milliseconds"
        }

        fun getFormattedPace(pace: Float): String {
            var paceInMills = pace.toLong()
            val hours = TimeUnit.MILLISECONDS.toHours(paceInMills)
            paceInMills -= TimeUnit.HOURS.toMillis(hours)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(paceInMills)
            paceInMills -= TimeUnit.MINUTES.toMillis(minutes)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(paceInMills)
            return "${if (minutes < 10) " " else ""}$minutes\'" + "${if (seconds < 10) "0" else ""}$seconds\""
        }

    }
}