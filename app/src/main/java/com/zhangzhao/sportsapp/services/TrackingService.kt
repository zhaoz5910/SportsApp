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
import androidx.lifecycle.MutableLiveData
import com.amap.api.maps.model.LatLng
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
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.zhangzhao.sportsapp.model.Constants.TIMER_UPDATE_INTERVAL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

class TrackingService: LifecycleService() {

    private val timeRunInSeconds = MutableLiveData<Long>()

    private var isFirstRun = true

    private lateinit var locationClient: AMapLocationClient
    private lateinit var locationOption: AMapLocationClientOption

    companion object {
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf(mutableListOf()))
        timeRunInMillis.postValue(0L)
        timeRunInSeconds.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        initLocation()

        isTracking.observe(this) {
            updateLocationChecking(it)
        }
    }

    private fun initLocation() {
        locationClient = AMapLocationClient(this)
        locationOption = AMapLocationClientOption()

        // 设置定位模式为高精度
        locationOption.locationMode =
            AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        // 设置定位间隔，单位毫秒，默认为2000ms
        locationOption.interval = 2000
        // 设置是否返回地址信息（默认返回地址信息）
        locationOption.isNeedAddress = true
        // 设置是否只定位一次,默认为false
        locationOption.isOnceLocation = false
        // 单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
        locationOption.httpTimeOut = 20000

        locationClient.setLocationOption(locationOption)
        locationClient.setLocationListener(locationListener)
    }

    private val locationListener = AMapLocationListener { aMapLocation ->
        if (aMapLocation != null) {
            if (aMapLocation.errorCode == 0) {
                // 定位成功
                val address = aMapLocation.address
                // 处理定位结果
                addPathPoint(
                    LatLng(aMapLocation.latitude, aMapLocation.longitude)
                )
            } else {
                // 定位失败
                val errText = "定位失败, ${aMapLocation.errorCode}:${aMapLocation.errorInfo}"
                println("AmapError: $errText")
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if(isFirstRun) {
                        Timber.tag("MyTag").d("Started service")
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.tag("MyTag").d("Resuming Service")
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.tag("MyTag").d("Paused service")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.tag("MyTag").d("Stopped service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private var isTimerEnabled = false
    private var lapTime = 0L // 每次启动timer之后运行的时间
    private var timeRun = 0L // 多次运行总时间
    private var timeStarted = 0L // 启动timer的时刻
    private var lastSecondTimestamp = 0L // 上一秒

    private fun startTimer() {
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                // 启动timer时和now的时间差
                lapTime = System.currentTimeMillis() - timeStarted
                timeRunInMillis.postValue(timeRun + lapTime)
                // 每过1s更新timeRunInSeconds
                if (timeRunInMillis.value!! >= lastSecondTimestamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun updateLocationChecking(isTracking: Boolean) {
        if (isTracking) {
            locationClient.startLocation()
        } else {
            locationClient.stopLocation()
        }
    }

    // 向最新的路线添加路径点
    private fun addPathPoint(position: LatLng) {
        position.let {
            pathPoints.value?.apply {
                Timber.tag("MyTag").d("pathPointsInService更新了$position")
                last().add(position)
                pathPoints.postValue(this)
            }
        }
    }

    // 创建一条新的路线（启停）或者初始化（启动）
    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    // 创建通知栏通知
    private fun startForegroundService() {
        Timber.tag("MyTag").d("TrackingService started.")

        startTimer()

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

        startTimer()
        isTracking.postValue(true)
    }

    // 获取intent
    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        },
        FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
    )

    // 创建通知通道
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationClient.onDestroy()
    }

}