package com.zhangzhao.sportsapp.view.fragment

import android.content.Intent
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.tag("MyTag").d("TrackingFragment创建了")
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
        addAllPolylines()//应等待地图初始化完毕？？



        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }
    }

    private fun subscribeToObservers() {
        Timber.tag("MyTag").d("订阅观察者")
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            if (pathPoints.isEmpty()) {
                Timber.tag("MyTag").d("发生了一些空值错误")
            }
            if (pathPoints.last().size > 0) {
                Timber.tag("MyTag").d("向pathPointsInFrag中更新值${pathPoints.last().last()}")
            }
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(it, true)
            binding.tvTimer.text = formattedTime
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
            binding.btnToggleRun.text = "Start"
            binding.btnFinishRun.visibility = View.VISIBLE
        } else {
            binding.btnToggleRun.text = "Stop"
            binding.btnFinishRun.visibility = View.GONE
        }
    }

    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            Timber.tag("MyTag").d("视角移到中央")
            aMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        } else {
            Timber.tag("MyTag").d("视角没移到中央")
        }
    }

    private fun addAllPolylines() {
        if (pathPoints.isEmpty()) {
            Timber.tag("MyTag").d("pathPoints为空，无法重新绘制所有轨迹")
        }
        for (polyline in pathPoints) {
            Timber.tag("MyTag").d("重新绘制所有轨迹:$polyline")
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            aMap.addPolyline(polylineOptions)
        }
    }

    // 绘制路线
    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            Timber.tag("MyTag").d("有${pathPoints.last().size}点，绘制了")
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            aMap.addPolyline(polylineOptions)
        } else {
            Timber.tag("MyTag").d("还没绘制")
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