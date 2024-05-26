package com.zhangzhao.sportsapp.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhangzhao.sportsapp.model.RunSortType
import com.zhangzhao.sportsapp.model.Run
import com.zhangzhao.sportsapp.repository.RunRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RunViewmodel @Inject constructor(
    private val runRepository: RunRepository
) : ViewModel()  {

    private val runsSortedByDate = runRepository.getAllRunsSortedByDate()
    private val runsSortedByDistance = runRepository.getAllRunsSortedByDistance()
    private val runsSortedByTimeInMillis = runRepository.getAllRunsSortedByTimeInMillis()
    private val runsSortedByAvgSpeed = runRepository.getAllRunsSortedByAvgSpeed()
    private val runsSortedByCaloriesBurned = runRepository.getAllRunsSortedByCaloriesBurned()

    val runs = MediatorLiveData<List<Run>>()

    var sortType = RunSortType.DATE

    // 在 LiveData 中发布正确的 run 表
    init {
        runs.addSource(runsSortedByDate) { result ->
            if(sortType == RunSortType.DATE) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByDistance) { result ->
            if(sortType == RunSortType.DISTANCE) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByTimeInMillis) { result ->
            if(sortType == RunSortType.RUNNING_TIME) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByAvgSpeed) { result ->
            if(sortType == RunSortType.AVG_SPEED) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByCaloriesBurned) { result ->
            if(sortType == RunSortType.CALORIES_BURNED) {
                result?.let { runs.value = it }
            }
        }
    }

    fun sortRuns(sortType: RunSortType) = when(sortType) {
        RunSortType.DATE -> runsSortedByDate.value?.let { runs.value = it }
        RunSortType.DISTANCE -> runsSortedByDistance.value?.let { runs.value = it }
        RunSortType.RUNNING_TIME -> runsSortedByTimeInMillis.value?.let { runs.value = it }
        RunSortType.AVG_SPEED -> runsSortedByAvgSpeed.value?.let { runs.value = it }
        RunSortType.CALORIES_BURNED -> runsSortedByCaloriesBurned.value?.let { runs.value = it }
    }.also {
        this.sortType = sortType
    }
    fun insertRun(run: Run) = viewModelScope.launch {
        runRepository.insertRun(run)
    }

    fun deleteRun(run: Run) = viewModelScope.launch {
        runRepository.deleteRun(run)
    }
}