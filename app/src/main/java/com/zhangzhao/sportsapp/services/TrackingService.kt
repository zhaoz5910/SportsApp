package com.zhangzhao.sportsapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.zhangzhao.sportsapp.R
import com.zhangzhao.sportsapp.model.Constants.ACTION_PAUSE_SERVICE
import com.zhangzhao.sportsapp.model.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.zhangzhao.sportsapp.model.Constants.ACTION_START_OR_RESUME_SERVICE
import com.zhangzhao.sportsapp.model.Constants.ACTION_STOP_SERVICE
import com.zhangzhao.sportsapp.model.Constants.NOTIFICATION_CHANNEL_ID
import com.zhangzhao.sportsapp.model.Constants.NOTIFICATION_CHANNEL_NAME
import com.zhangzhao.sportsapp.model.Constants.NOTIFICATION_ID
import com.zhangzhao.sportsapp.view.MainActivity
import timber.log.Timber

class TrackingService: LifecycleService() {

    private var isFirstRun = true

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    Timber.tag("MyTag").d("Started or resumed service")
                    if(isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming Service")
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
        Timber.tag("MyTag").d("TrackingService started.")

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

        createNotificationChannel(notificationManager)

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
            .setContentTitle("i运动：跑步中")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        },
        FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
    )

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }

}