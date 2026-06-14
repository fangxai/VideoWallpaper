package com.graytsar.livewallpaper.route

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.graytsar.livewallpaper.core.common.model.ImageScaling
import com.graytsar.livewallpaper.core.common.model.VideoScaling
import com.graytsar.livewallpaper.ui.SettingsScreen
import com.graytsar.livewallpaper.ui.SettingsViewModel

@Composable
fun SettingsScreenRoute(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackPress: () -> Unit
) {
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val imageScalingTypes = ImageScaling.entries
    val videoScalingOptions = VideoScaling.entries

    SettingsScreen(
        uiState = uiState,
        onBackPress = onBackPress,
        imageScalingOptions = imageScalingTypes,
        videoScalingOptions = videoScalingOptions,
        onDarkModeChange = { isEnabled ->
            viewModel.updateDarkMode(isEnabled = isEnabled)
            AppCompatDelegate.setDefaultNightMode(
                if (isEnabled) {
                    AppCompatDelegate.MODE_NIGHT_YES
                } else {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
            )
        },
        onImageOptionSelected = { option ->
            viewModel.updateImageScaleType(option)
        },
        onAudioEnabledChange = { isEnabled ->
            viewModel.updateAudioEnabled(isEnabled)
        },
        onVideoOptionSelected = { option ->
            viewModel.updateVideoScaleType(option)
        },
        onDoubleTapToPauseChange = { isEnabled ->
            viewModel.updateDoubleTapToPause(isEnabled)
        },
        onPlayOffscreenChange = { isEnabled ->
            viewModel.updatePlayOffscreen(isEnabled)
        },
        onClearWallpaperClick = {
            viewModel.clearWallpaper()
        }
    )
}