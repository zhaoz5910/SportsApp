package com.zhangzhao.sportsapp.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.zhangzhao.sportsapp.model.RunSortType
import com.zhangzhao.sportsapp.R
import com.zhangzhao.sportsapp.adapter.RunAdapter
import com.zhangzhao.sportsapp.adapter.SportRecordAdapter
import com.zhangzhao.sportsapp.databinding.FragmentStatisticsBinding
import com.zhangzhao.sportsapp.model.Constants.runInRunFragment
import com.zhangzhao.sportsapp.model.RopeSkipping
import com.zhangzhao.sportsapp.model.Run
import com.zhangzhao.sportsapp.model.SportRecord
import com.zhangzhao.sportsapp.viewmodel.RunViewmodel
import com.zhangzhao.sportsapp.viewmodel.SportViewModel
import com.zhangzhao.sportsapp.viewmodel.StatisticsViewmodel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class StatisticsFragment: Fragment(R.layout.fragment_statistics) {

    lateinit var runAdapter: RunAdapter
    //lateinit var ropeAdapter: RopeAdapter
    lateinit var sportAdapter: SportRecordAdapter

    private val viewModel: StatisticsViewmodel by viewModels()
    private val runViewModel: RunViewmodel by viewModels()
    private val sportViewModel: SportViewModel by viewModels()

    private var sportsRecords = mutableListOf<SportRecord>()

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sportAdapter = SportRecordAdapter {
            findNavController().navigate(R.id.action_statisticsFragment_to_runFragment)
        }
        subscribeToObservers()
        runAdapter = RunAdapter(){
            findNavController().navigate(R.id.action_statisticsFragment_to_runFragment)
        }

        when (runViewModel.sortType) {
            RunSortType.DATE -> binding.spFilter.setSelection(0)
            RunSortType.RUNNING_TIME -> binding.spFilter.setSelection(1)
            RunSortType.DISTANCE -> binding.spFilter.setSelection(2)
            RunSortType.AVG_SPEED -> binding.spFilter.setSelection(3)
            RunSortType.CALORIES_BURNED -> binding.spFilter.setSelection(4)
        }

        sportViewModel.runsSortedByDate.observe(viewLifecycleOwner) {
            it.forEach { run ->
                val sport = SportRecord("户外跑步", run, run.timestamp)
                if (!sportsRecords.contains(sport)) {
                    sportsRecords.add(sport)
                }
            }
        }

        sportViewModel.ropeSortedByDate.observe(viewLifecycleOwner) {
            it.forEach { rope ->
                val sport = SportRecord("跳绳", rope, rope.timestamp)
                if (!sportsRecords.contains(sport)) {
                    sportsRecords.add(sport)
                }
            }
        }

        runViewModel.runs.observe(viewLifecycleOwner) { runs ->
            runAdapter.submitList(runs)
        }

        binding.recyclerviewRuns.apply {
            sportAdapter.submitList(sportsRecords)
            adapter = sportAdapter
            layoutManager = LinearLayoutManager(activity)
        }

        binding.spinnerSport.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}

            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                pos: Int,
                id: Long
            ) {
                when (pos) {
                    0 -> {
                        binding.spFilter.adapter = ArrayAdapter.createFromResource(
                            requireContext(),
                            R.array.all_options,
                            android.R.layout.simple_spinner_item
                        )
                        binding.recyclerviewRuns.apply {
                            sportAdapter.submitList(sportsRecords)
                            adapter = sportAdapter
                            layoutManager = LinearLayoutManager(activity)
                        }
                    }
                    1 -> {
                        binding.spFilter.adapter = ArrayAdapter.createFromResource(
                            requireContext(),
                            R.array.filter_options,
                            android.R.layout.simple_spinner_item
                        )
                        binding.recyclerviewRuns.apply {
                            adapter = runAdapter
                            layoutManager = LinearLayoutManager(activity)
                        }
                    }
                    2 -> binding.spFilter.adapter = ArrayAdapter.createFromResource(
                        requireContext(),
                        R.array.rope_options,
                        android.R.layout.simple_spinner_item
                    )
                }
            }
        }

        binding.spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}

            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                pos: Int,
                id: Long
            ) {
                when (pos) {
                    0 -> runViewModel.sortRuns(RunSortType.DATE)
                    1 -> runViewModel.sortRuns(RunSortType.RUNNING_TIME)
                    2 -> runViewModel.sortRuns(RunSortType.DISTANCE)
                    3 -> runViewModel.sortRuns(RunSortType.AVG_SPEED)
                    4 -> runViewModel.sortRuns(RunSortType.CALORIES_BURNED)
                }
            }
        }
    }

    private fun subscribeToObservers() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}