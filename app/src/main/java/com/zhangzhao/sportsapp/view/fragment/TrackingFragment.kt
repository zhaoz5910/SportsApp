package com.zhangzhao.sportsapp.view.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.amap.api.maps2d.AMap
import com.zhangzhao.sportsapp.R
import com.zhangzhao.sportsapp.databinding.FragmentTrackingBinding
import com.zhangzhao.sportsapp.model.Constants.ACTION_START_OR_RESUME_SERVICE
import com.zhangzhao.sportsapp.services.TrackingService
import com.zhangzhao.sportsapp.viewmodel.RunViewmodel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TrackingFragment: Fragment(R.layout.fragment_tracking) {

    private val viewModel: RunViewmodel by viewModels()

    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!

    private var map: AMap ?= null

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

//        val mapViewBundle = savedInstanceState?.getBundle(MAP_VIEW_BUNDLE_KEY)
        binding.mapView.onCreate(savedInstanceState)

        binding.btnToggleRun.setOnClickListener {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }

        //初始化地图控制器对象
        val aMap: AMap = binding.mapView.map
    }

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