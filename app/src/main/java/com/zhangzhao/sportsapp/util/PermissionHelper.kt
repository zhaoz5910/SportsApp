package com.zhangzhao.sportsapp.util

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class PermissionHelper(private val activity: AppCompatActivity) {
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private val permissionsToRequest = mutableListOf<String>()

    // 注册回调函数
    fun register(
        callback: (Map<String, Boolean>) -> Unit
    ) {
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            callback(it)
        }
    }

    fun somePermissionPermanentlyDenied(permissions: Array<String>): Boolean {
        for (deniedPermission in permissions) {
            if (permissionPermanentlyDenied(deniedPermission)) {
                return true
            }
        }
        return false
    }

    private fun permissionPermanentlyDenied(permission: String): Boolean {
        return !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    fun hasPermission(permission: String) =
        ActivityCompat.checkSelfPermission(
            activity,
            permission
        ) == PackageManager.PERMISSION_GRANTED

    fun hasPermissions(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (!hasPermission(permission)) {
                return false
            }
        }
        return true
    }

    fun requestPermission(permission: String) {
        if (!hasPermission(permission)) {
            permissionsToRequest.add(permission)
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
            permissionsToRequest.clear()
        }
    }

    fun requestPermissions(permissions: Array<String>) {
        for (permission in permissions) {
            if (!hasPermission(permission)) {
                permissionsToRequest.add(permission)
            }
        }
        //打开系统权限对话框，要求用户给与授权
        if (permissionsToRequest.isNotEmpty()){
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
            permissionsToRequest.clear()
        }
    }

    // 跳转到应用设置界面
    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivity(intent)
    }
}