package com.zhangzhao.sportsapp.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.location.Location
import android.net.Uri
import android.provider.Settings
import android.view.View
import com.amap.api.maps.model.LatLng
import com.zhangzhao.sportsapp.services.Polyline
import com.zhangzhao.sportsapp.view.MainActivity
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

        fun openAppSettings(activity: Activity) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", activity.packageName, null)
            intent.data = uri
            activity.startActivity(intent)
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

        fun calculatePolylineLength(polyline: Polyline): Float {
            var distance = 0f
            for (i in 0..polyline.size - 2) {
                val pos1 = polyline[i]
                val pos2 = polyline[i + 1]
                val result = FloatArray(1)
                Location.distanceBetween(
                    pos1.latitude,
                    pos1.longitude,
                    pos2.latitude,
                    pos2.longitude,
                    result
                )
                distance += result[0]
            }
            return distance
        }

    }
}