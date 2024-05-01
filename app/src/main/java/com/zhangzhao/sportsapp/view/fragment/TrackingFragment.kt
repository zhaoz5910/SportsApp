package com.zhangzhao.sportsapp.view.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.amap.api.location.AMapLocationClient
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.maps.model.PolylineOptions
import com.google.android.gms.maps.OnMapReadyCallback
import com.zhangzhao.sportsapp.R
import com.zhangzhao.sportsapp.databinding.FragmentTrackingBinding
import com.zhangzhao.sportsapp.model.Constants.ACTION_PAUSE_SERVICE
import com.zhangzhao.sportsapp.model.Constants.ACTION_START_OR_RESUME_SERVICE
import com.zhangzhao.sportsapp.model.Constants.MAP_ZOOM
import com.zhangzhao.sportsapp.model.Constants.POLYLINE_COLOR
import com.zhangzhao.sportsapp.model.Constants.POLYLINE_WIDTH
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

        MapsInitializer.updatePrivacyShow(requireContext(), true, true)//隐私合规接口
        MapsInitializer.updatePrivacyAgree(requireContext(), true)//隐私合规接口
        AMapLocationClient.updatePrivacyAgree(requireContext(), true)
        AMapLocationClient.updatePrivacyShow(requireContext(), true, true)

//        val mapViewBundle = savedInstanceState?.getBundle(MAP_VIEW_BUNDLE_KEY)
        // 绑定地图
        binding.mapView.onCreate(savedInstanceState)

        //初始化地图控制器对象
        aMap = binding.mapView.map
        subscribeToObservers()

        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(it, true)
            binding.tvTimer.text = formattedTime
        })

        TrackingService.distanceRunInMeters.observe(viewLifecycleOwner, Observer {
            distanceInMeters = it
            val disText = distanceInMeters.toLong().toString() + "米"
            binding.tvDistance.text = disText
            binding.tvSpeed.text = updatePace()
        })
    }

    private fun toggleRun() {
        if (isTracking) {
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking) {
            binding.btnToggleRun.text = "继续"
            binding.btnFinishRun.visibility = View.VISIBLE
        } else {
            binding.btnToggleRun.text = "暂停"
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
            pace = curTimeInMillis/distanceInMeters * 1000
        }
        return TrackingUtility.getFormattedPace(pace)
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

    // 向trackingService传递intent
    private fun sendCommandToService(action: String) =
        Intent(requireContext(),TrackingService::class.java).also{
            it.action = action
            requireContext().startService(it)
        }

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