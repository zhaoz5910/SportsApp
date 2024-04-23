package com.zhangzhao.sportsapp.view.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.zhangzhao.sportsapp.R
import com.zhangzhao.sportsapp.viewmodel.RunViewmodel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrackingFragment: Fragment(R.layout.fragment_tracking) {
    private val viewModel: RunViewmodel by viewModels()

}