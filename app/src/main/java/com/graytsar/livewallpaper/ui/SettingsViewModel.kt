package com.graytsar.livewallpaper.ui

import android.app.WallpaperManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.graytsar.livewallpaper.core.common.model.ImageScaling
import com.graytsar.livewallpaper.core.common.model.VideoScaling
import com.graytsar.livewallpaper.core.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

data class SettingsUiState(
    val isDarkModeEnabled: Boolean = false,
    val imageScaleType: ImageScaling = ImageScaling.FIT_TO_SCREEN,
    val isAudioEnabled: Boolean = false,
    val videoScaleType: VideoScaling = VideoScaling.FIT_CROP,
    val isDoubleTapToPause: Boolean = false,
    val isPlayOffscreen: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    val userPreferencesRepository: UserPreferencesRepository,
    val wallpaperManager: WallpaperManager,
) : ViewModel() {

    val uiStateFlow = userPreferencesRepository.getSettingOptions().map {
        SettingsUiState(
            isDarkModeEnabled = it.darkMode,
            imageScaleType = it.imageScaleType,
            isAudioEnabled = it.enableAudio,
            videoScaleType = it.videoScaleType,
            isDoubleTapToPause = it.doubleTapToPause,
            isPlayOffscreen = it.playOffscreen
        )
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000.milliseconds),
        initialValue = SettingsUiState()
    )

    fun updateDarkMode(isEnabled: Boolean) = viewModelScope.launch {
        userPreferencesRepository.setForceDarkMode(enabled = isEnabled)
    }

    fun updateImageScaleType(scaleType: ImageScaling) = viewModelScope.launch {
        userPreferencesRepository.setImageScaleType(type = scaleType)
    }

    fun updateAudioEnabled(isEnabled: Boolean) = viewModelScope.launch {
        userPreferencesRepository.setVideoAudio(enabled = isEnabled)
    }

    fun updateVideoScaleType(scaleType: VideoScaling) = viewModelScope.launch {
        userPreferencesRepository.setVideoScaling(scaling = scaleType)
    }

    fun updateDoubleTapToPause(isEnabled: Boolean) = viewModelScope.launch {
        userPreferencesRepository.setDoubleTapToPause(enabled = isEnabled)
    }

    fun updatePlayOffscreen(isEnabled: Boolean) = viewModelScope.launch {
        userPreferencesRepository.setPlayOffscreen(enabled = isEnabled)
    }

    fun clearWallpaper() = viewModelScope.launch(Dispatchers.IO) {
        //ignore exception
        runCatching {
            wallpaperManager.clear()
        }
    }
}