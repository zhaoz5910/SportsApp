package com.zhangzhao.sportsapp.view.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.zhangzhao.sportsapp.R
import com.zhangzhao.sportsapp.viewmodel.StatisticsViewmodel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatisticsFragment: Fragment(R.layout.fragment_statistics) {

    private val viewModel: StatisticsViewmodel by viewModels()
}