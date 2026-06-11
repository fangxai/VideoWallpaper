package com.graytsar.livewallpaper.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.graytsar.livewallpaper.R
import com.graytsar.livewallpaper.core.common.util.applyWindowInsets
import com.graytsar.livewallpaper.databinding.ActivityRaibuBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReibuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val binding = ActivityRaibuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
            setupNav()
        }

        applyWindowInsets(binding.root)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            setupNav()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.nav_host_fragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun setupNav() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.fragmentMain))

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
    }
}
