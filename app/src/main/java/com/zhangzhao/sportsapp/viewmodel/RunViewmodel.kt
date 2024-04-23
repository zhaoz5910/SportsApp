package com.zhangzhao.sportsapp.viewmodel

import androidx.lifecycle.ViewModel
import com.zhangzhao.sportsapp.repository.RunRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RunViewmodel @Inject constructor(
    private val runRepository: RunRepository
) : ViewModel()  {
}