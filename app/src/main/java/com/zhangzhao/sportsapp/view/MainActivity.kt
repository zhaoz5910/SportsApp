package com.zhangzhao.sportsapp.view

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.zhangzhao.sportsapp.R
import com.zhangzhao.sportsapp.databinding.ActivityMainBinding
import com.zhangzhao.sportsapp.model.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navHost: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        navHost = supportFragmentManager.findFragmentById(R.id.navHostFragment)
                as NavHostFragment

        // 绑定底部导航栏
        binding.bottomNavigationView.setupWithNavController(navHost.navController)
        navHost.navController
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.setupFragment, R.id.trackingFragment, R.id.runFragment -> binding.bottomNavigationView.visibility =
                        View.GONE
                    else -> binding.bottomNavigationView.visibility = View.VISIBLE
                }
            }
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.sportFragment -> {
                    navHost.navController.popBackStack(R.id.sportFragment, true)
                    navHost.navController.popBackStack(R.id.statisticsFragment, true)
                    navHost.navController.popBackStack(R.id.settingFragment, true)// 清除返回堆栈
                    navHost.navController.navigate(R.id.sportFragment)
                }
                R.id.settingFragment -> {
                    navHost.navController.popBackStack(R.id.settingFragment, true)
                    navHost.navController.popBackStack(R.id.statisticsFragment, true)
                    navHost.navController.popBackStack(R.id.sportFragment, true)
                    navHost.navController.navigate(R.id.settingFragment)
                }
                R.id.statisticsFragment -> {
                    navHost.navController.popBackStack(R.id.statisticsFragment, true)
                    navHost.navController.popBackStack(R.id.sportFragment, true)
                    navHost.navController.popBackStack(R.id.settingFragment, true)
                    navHost.navController.navigate(R.id.statisticsFragment)
                }
            }
            true
        }
        navigateToTrackingFragmentIfNeeded(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Timber.tag("MyTag").d("onNewIntent被调用")
        navigateToTrackingFragmentIfNeeded(intent)
    }

    // 从通知栏导航到TrackingFragment
    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            Timber.tag("MyTag").d("导航到 trackingFragment")
            navHost.navController.navigate(
                R.id.action_global_trackingFragment
            )
        }
    }

}















