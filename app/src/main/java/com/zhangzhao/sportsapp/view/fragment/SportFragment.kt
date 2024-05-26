package com.zhangzhao.sportsapp.view.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleObserver
import androidx.navigation.fragment.findNavController
import com.zhangzhao.sportsapp.R
import com.zhangzhao.sportsapp.databinding.FragmentSportBinding
import com.zhangzhao.sportsapp.model.Constants
import com.zhangzhao.sportsapp.model.Constants.permissionsBackgroundLocation
import com.zhangzhao.sportsapp.model.Constants.permissionsForegroundLocation
import com.zhangzhao.sportsapp.model.Constants.permissionsLocation
import com.zhangzhao.sportsapp.util.TrackingUtility
import com.zhangzhao.sportsapp.viewmodel.SportTotalDataViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SportFragment: Fragment(R.layout.fragment_sport) {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private var _binding: FragmentSportBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPref: SharedPreferences

    private val viewModel: SportTotalDataViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        permissionLauncher =
            registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionsResult ->
                if (permissionsResult.all { it.value }) {
                    // 所有权限都被用户授予，可以继续操作
                    if (!checkPermissions(permissionsBackgroundLocation)) {
                        permissionLauncher.launch(permissionsBackgroundLocation)
                    }
                    else findNavController().navigate(R.id.action_sportFragment_to_trackingFragment)
                } else {
                    // 如果有权限被拒绝
                    activity?.let { TrackingUtility.openAppSettings(it) }
                }

        }

        sharedPref = requireActivity().getSharedPreferences(
            Constants.SHARED_PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

        val name = sharedPref.getString(Constants.KEY_NAME, "")
        val weight = sharedPref.getFloat(Constants.KEY_WEIGHT, 80f)
        binding.nameTextView.text = name
        binding.textWeight.text = weight.toString()

        // 开始跑步，导航至trackingFragment
        binding.runCard.setOnClickListener{
            if (!checkPermissions(permissionsLocation)) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("权限请求")
                builder.setMessage("为了正常使用跑步功能，请授予前台和后台定位权限")
                builder.setPositiveButton("确定") { _, _ ->
                    if (!checkPermissions(permissionsForegroundLocation))
                        permissionLauncher.launch(permissionsForegroundLocation)
                    else
                        permissionLauncher.launch(permissionsBackgroundLocation)
                }
                builder.setNegativeButton("取消") { _, _ ->
                    // 用户点击取消
                }
                builder.show()
            }
            else findNavController().navigate(R.id.action_sportFragment_to_trackingFragment)
        }

        binding.ropeCard.setOnClickListener{
            findNavController().navigate(R.id.action_sportFragment_to_ropeSkippingFragment)
        }

        viewModel.totalDistance.observe(viewLifecycleOwner){
            it?.let {
                "累计已跑${it / 1000f}公里".also {
                    binding.runDataText.text = it
                }
            }
        }
        viewModel.totalRopeCount.observe(viewLifecycleOwner){
            it?.let {
                "累计已跳${it}个".also {
                    binding.ropeDataText.text = it
                }
            }
        }
    }

    private fun checkPermission(permission: String) =
        ActivityCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED

    private fun checkPermissions(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (!checkPermission(permission)) {
                return false
            }
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}