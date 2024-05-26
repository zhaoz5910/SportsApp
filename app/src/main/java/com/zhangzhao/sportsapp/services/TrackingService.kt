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
import com.zhangzhao.sportsapp.model.Constants.ACTION_PAUSE_SERVICE
import com.zhangzhao.sportsapp.model.Constants.ACTION_START_OR_RESUME_SERVICE
import com.zhangzhao.sportsapp.model.Constants.ACTION_STOP_SERVICE
import com.zhangzhao.sportsapp.model.Constants.NOTIFICATION_CHANNEL_ID
import com.zhangzhao.sportsapp.model.Constants.NOTIFICATION_CHANNEL_NAME
import com.zhangzhao.sportsapp.model.Constants.NOTIFICATION_ID
import timber.log.Timber
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMapUtils
import com.zhangzhao.sportsapp.R
import com.zhangzhao.sportsapp.model.Constants.TIMER_UPDATE_INTERVAL
import com.zhangzhao.sportsapp.util.TrackingUtility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService: LifecycleService() {

    private val timeRunInSeconds = MutableLiveData<Long>()

    private var isFirstRun = true
    private var serviceKilled = false

    private lateinit var locationClient: AMapLocationClient
    private lateinit var locationOption: AMapLocationClientOption

    // 包含每个通知将具有的设置的基本通知生成器
    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    //当前通知的生成器
    private lateinit var curNotification: NotificationCompat.Builder

    private var distanceRun = 0F
    private var pointPre: LatLng? = null

    // 地图监听器
    private val locationListener = AMapLocationListener { aMapLocation ->
        if (aMapLocation != null) {
            if (aMapLocation.errorCode == 0) {
                // 定位成功
                //val address = aMapLocation.address
                // 处理定位结果
                val point = LatLng(aMapLocation.latitude, aMapLocation.longitude)

                if (pointPre == null) {
                    pointPre = point
                } else {
                    distanceRun += AMapUtils.calculateLineDistance(
                        pointPre,point
                    )
                    pointPre = point
                    distanceRunInMeters.postValue(distanceRun)
                }
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

    companion object {
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
        val distanceRunInMeters = MutableLiveData<Float>()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf(mutableListOf()))
        timeRunInMillis.postValue(0L)
        timeRunInSeconds.postValue(0L)
        distanceRunInMeters.postValue(0F)
    }

    override fun onCreate() {
        super.onCreate()
        curNotification = baseNotificationBuilder
        postInitialValues()
        initLocation()

        isTracking.observe(this) {
            updateNotificationTrackingState(it)
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
        locationOption.interval = 200
        // 设置是否返回地址信息（默认返回地址信息）
        locationOption.isNeedAddress = true
        // 单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
        locationOption.httpTimeOut = 10000

        locationClient.setLocationOption(locationOption)
        locationClient.setLocationListener(locationListener)
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
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun killService() {
        Timber.tag("MyTag").d("Killed service")
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private var lapTime = 0L // 每次启动timer之后运行的时间
    private var timeRun = 0L // 多次运行总时间
    private var timeStarted = 0L // 启动timer的时刻
    private var lastSecondTimestamp = 0L // 上一秒

    private fun startTimer() {
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
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

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

        createNotificationChannel(notificationManager)

        startForeground(NOTIFICATION_ID, curNotification.build())

        // 开始计时
        startTimer()

        // 更新通知的时间
        timeRunInSeconds.observe(this) {
            if (!serviceKilled) {
                val notification = curNotification
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L))
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        }
    }

    // 更新通知的操作按钮
    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "暂停" else "继续"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(
                this, 1, pauseIntent,
                FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
            )
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(
                this, 2, resumeIntent,
                FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
            )
        }
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        curNotification.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotification, ArrayList<NotificationCompat.Action>())
        }
        if (!serviceKilled) {
            curNotification = baseNotificationBuilder
                .addAction(R.drawable.ic_pause_black_24dp, notificationActionText, pendingIntent)

            notificationManager.notify(NOTIFICATION_ID, curNotification.build())
        }
    }

    // 创建通知通道
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationClient.onDestroy()
    }

}