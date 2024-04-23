package com.zhangzhao.sportsapp.view

import android.Manifest
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
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
//            //处理用户权限授予结果
//        }
//
//        val permissionsToRequest = arrayOf(
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
//            Manifest.permission.CALL_PHONE
//        )
//        permissionLauncher.launch(permissionsToRequest)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHost = supportFragmentManager.findFragmentById(R.id.navHostFragment)
                as NavHostFragment

        setSupportActionBar(binding.toolbar)

        binding.bottomNavigationView.setupWithNavController(navHost.navController)

        navHost.navController
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.setupFragment, R.id.trackingFragment -> binding.bottomNavigationView.visibility =
                        View.GONE
                    else -> binding.bottomNavigationView.visibility = View.VISIBLE
                }
            }
    }

}















