package com.zhangzhao.sportsapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.zhangzhao.sportsapp.repository.RopeSkippingRepository
import com.zhangzhao.sportsapp.repository.RunRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewmodel  @Inject constructor(
    runRepository: RunRepository,
    ropeRepository: RopeSkippingRepository
) : ViewModel() {
    var totalDistance = runRepository.getTotalDistance()
    var totalTimeInMillis = runRepository.getTotalTimeInMillis()
    var totalAvgSpeed = runRepository.getTotalAvgSpeed()
    var totalCaloriesBurned = runRepository.getTotalCaloriesBurned()
}