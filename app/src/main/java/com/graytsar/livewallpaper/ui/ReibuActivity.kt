@file:OptIn(ExperimentalMaterial3Api::class)

package com.graytsar.livewallpaper.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.graytsar.livewallpaper.navigation.PickerRoute
import com.graytsar.livewallpaper.navigation.SettingsRoute
import com.graytsar.livewallpaper.route.PickerScreenRoute
import com.graytsar.livewallpaper.route.SettingsScreenRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReibuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                ReiActivityScreen(
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun ReiActivityScreen(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = PickerRoute
    ) {
        composable<PickerRoute> {
            PickerScreenRoute(
                onSettingsClick = { navController.navigate(SettingsRoute) }
            )
        }

        composable<SettingsRoute> {
            SettingsScreenRoute(
                onBackPress = { navController.popBackStack() }
            )
        }
    }
}

@Preview
@Composable
fun ReiActivityScreenPreview() {
    val navController = rememberNavController()
    MaterialTheme {
        ReiActivityScreen(
            navController = navController
        )
    }
}