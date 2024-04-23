package com.zhangzhao.sportsapp.util

import android.Manifest
import android.content.Context
import pub.devrel.easypermissions.EasyPermissions

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

    }
}