@file:OptIn(ExperimentalMaterial3Api::class)

package com.graytsar.livewallpaper.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.graytsar.livewallpaper.navigation.PickerRoute
import com.graytsar.livewallpaper.navigation.SettingsRoute
import com.graytsar.livewallpaper.route.PickerScreenRoute
import com.graytsar.livewallpaper.route.SettingsScreenRoute
import com.graytsar.livewallpaper.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReibuActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            AppTheme(darkTheme = uiState.isDarkModeEnabled) {
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
    AppTheme {
        ReiActivityScreen(
            navController = navController
        )
    }
}