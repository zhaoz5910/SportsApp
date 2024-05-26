package com.zhangzhao.sportsapp.view.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.amap.api.location.AMapLocationClient
import com.amap.api.maps.AMap
import com.amap.api.maps.AMap.OnMapScreenShotListener
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.maps.model.PolylineOptions
import com.zhangzhao.sportsapp.R
import com.zhangzhao.sportsapp.databinding.FragmentTrackingBinding
import com.zhangzhao.sportsapp.model.Constants
import com.zhangzhao.sportsapp.model.Constants.ACTION_PAUSE_SERVICE
import com.zhangzhao.sportsapp.model.Constants.ACTION_START_OR_RESUME_SERVICE
import com.zhangzhao.sportsapp.model.Constants.ACTION_STOP_SERVICE
import com.zhangzhao.sportsapp.model.Constants.MAP_ZOOM
import com.zhangzhao.sportsapp.model.Constants.POLYLINE_COLOR
import com.zhangzhao.sportsapp.model.Constants.POLYLINE_WIDTH
import com.zhangzhao.sportsapp.model.Run
import com.zhangzhao.sportsapp.services.Polyline
import com.zhangzhao.sportsapp.services.TrackingService
import com.zhangzhao.sportsapp.util.TrackingUtility
import com.zhangzhao.sportsapp.viewmodel.RunViewmodel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class TrackingFragment: Fragment(R.layout.fragment_tracking) {

    private val viewModel: RunViewmodel by viewModels()

    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!

    private var weight: Float = 80f
    private lateinit var sharedPref: SharedPreferences

    private lateinit var aMap: AMap

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var curTimeInMillis = 0L
    private var distanceInMeters = 0F
    private var pace = 0F

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPref = requireActivity().getSharedPreferences(
            Constants.SHARED_PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )
        weight = sharedPref.getFloat(Constants.KEY_WEIGHT, 80f)
        // 隐私政策
        privacyAgree()

//        val mapViewBundle = savedInstanceState?.getBundle(MAP_VIEW_BUNDLE_KEY)
        // 绑定地图
        binding.mapView.onCreate(savedInstanceState)

        //初始化地图控制器对象
        aMap = binding.mapView.map
        subscribeToObservers()

        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }

        binding.btnFinishRun.setOnClickListener {
            zoomToWholeTrack()
            endRunAndSaveToDB()
        }
    }

    private fun privacyAgree() {
        MapsInitializer.updatePrivacyShow(requireContext(), true, true)//隐私合规接口
        MapsInitializer.updatePrivacyAgree(requireContext(), true)//隐私合规接口
        AMapLocationClient.updatePrivacyAgree(requireContext(), true)
        AMapLocationClient.updatePrivacyShow(requireContext(), true, true)
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner) {
            updateTracking(it)
        }

        TrackingService.pathPoints.observe(viewLifecycleOwner) {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        }

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner) {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(it, true)
            binding.tvTimer.text = formattedTime
        }

        TrackingService.distanceRunInMeters.observe(viewLifecycleOwner) {
            distanceInMeters = it
            val disText = distanceInMeters.toLong().toString() + "米"
            binding.tvDistance.text = disText
            binding.tvSpeed.text = updatePace()
        }
    }

    private fun toggleRun() {
        if (isTracking) {
            pauseTrackingService()
        } else {
            startOrResumeTrackingService()
            Timber.tag("MyTag").d("Started service")
        }
    }

    private fun startOrResumeTrackingService() =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = ACTION_START_OR_RESUME_SERVICE
            requireContext().startService(it)
        }

    private fun pauseTrackingService() =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = ACTION_PAUSE_SERVICE
            requireContext().startService(it)
        }

    private fun stopTrackingService() =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = ACTION_STOP_SERVICE
            requireContext().startService(it)
        }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && curTimeInMillis > 0L) {
            binding.btnToggleRun.text = getString(R.string.continue_text)
            binding.btnFinishRun.visibility = View.VISIBLE
        } else if (isTracking){
            binding.btnToggleRun.text = getString(R.string.stop_text)
            binding.btnFinishRun.visibility = View.GONE
        }
    }

    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            aMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun updatePace(): String {
        if (curTimeInMillis != 0L) {
            pace = curTimeInMillis / distanceInMeters * 1000
        }
        return TrackingUtility.getFormattedPace(pace)
    }

    // 缩小，直到整个轨迹可见。用于制作 MapView 的屏幕截图以将其保存在数据库中
    private fun zoomToWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints) {
            for (point in polyline) {
                bounds.include(point)
            }
        }
        val width = binding.mapView.width
        val height = binding.mapView.height
        aMap.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                width,
                height,
                (height * 0.05f).toInt()
            )
        )
    }

    // 在 Room 数据库中保存最近运行并结束它
    private fun endRunAndSaveToDB() {
        val onMapScreenShotListener = object: OnMapScreenShotListener {
            override fun onMapScreenShot(bmp: Bitmap?) {
                val pace = updatePace()
                val avgSpeed = distanceInMeters / (curTimeInMillis * 1000)
                val timestamp = System.currentTimeMillis()
                val caloriesBurned = ((distanceInMeters / 1000f) * weight).toLong()
                val run =
                    Run(bmp, timestamp, pace, avgSpeed,
                        distanceInMeters.toLong(), curTimeInMillis, caloriesBurned)
                viewModel.insertRun(run)
                stopRun()
            }

            override fun onMapScreenShot(p0: Bitmap?, p1: Int) {}
        }
        aMap.getMapScreenShot(onMapScreenShotListener)
    }

    private fun stopRun() {
        binding.tvTimer.text = getString(R.string.time_initial_value)
        binding.tvDistance.text = getString(R.string.distance_initial_text)
        binding.tvSpeed.text = getString(R.string.speed_initial_text)
        stopTrackingService()
        findNavController().navigate(R.id.action_trackingFragment_to_sportFragment)
    }

    private fun addAllPolylines() {
        if (pathPoints.isNotEmpty()) {
            for (polyline in pathPoints) {
                val polylineOptions = PolylineOptions()
                    .color(POLYLINE_COLOR)
                    .width(POLYLINE_WIDTH)
                    .addAll(polyline)
                aMap.addPolyline(polylineOptions)
            }
        }
    }

    // 绘制路线
    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            aMap.addPolyline(polylineOptions)
        }
    }

    // 添加了会出错
//    override fun onDestroy() {
//        super.onDestroy()
//        binding.mapView.onDestroy()
//    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
        addAllPolylines()// 地图初始化完毕

        if (TrackingUtility.hasLocationPermissions(requireContext())) {
            //实现定位蓝点
            val myLocationStyle = MyLocationStyle()
            //定位蓝点展现模式
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
            //精度圆圈的自定义
            myLocationStyle.radiusFillColor(Color.TRANSPARENT)
            myLocationStyle.strokeWidth(0F)

            //设置定位蓝点的Style
            aMap.myLocationStyle = myLocationStyle

            // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false
            aMap.isMyLocationEnabled = true
        }
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}