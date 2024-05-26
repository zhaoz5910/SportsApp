package com.zhangzhao.sportsapp.view.fragment

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.zhangzhao.sportsapp.R
import com.zhangzhao.sportsapp.databinding.FragmentSetupBinding
import com.zhangzhao.sportsapp.model.Constants.KEY_FIRST_TIME_TOGGLE
import com.zhangzhao.sportsapp.model.Constants.KEY_NAME
import com.zhangzhao.sportsapp.model.Constants.KEY_WEIGHT
import com.zhangzhao.sportsapp.model.Constants.SHARED_PREFERENCES_NAME
import timber.log.Timber
import javax.inject.Inject

class SetupFragment: Fragment(R.layout.fragment_setup) {

    private var _binding: FragmentSetupBinding? = null
    private val binding get() = _binding!!

    lateinit var sharedPref: SharedPreferences

    var firstTimeAppOpen: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPref = requireActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

        if (!sharedPref.getBoolean(KEY_FIRST_TIME_TOGGLE, true)) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()
            findNavController().navigate(
                R.id.action_setupFragment_to_sportFragment,
                savedInstanceState,
                navOptions
            )
        }

        binding.tvContinue.setOnClickListener {
            val success = writePersonalDataToSharedPref()
            if (success) {
                findNavController().navigate(R.id.action_setupFragment_to_sportFragment)
            } else {
                Snackbar.make(requireView(), "请填写必要的信息（体重用于跟踪消耗卡路里，昵称用于称呼您）", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun writePersonalDataToSharedPref(): Boolean {
        val name = binding.etName.text.toString()
        val weightText = binding.etWeight.text.toString()
        if (name.isEmpty() || weightText.isEmpty()) {
            return false
        }
        sharedPref.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weightText.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
            .apply()
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}