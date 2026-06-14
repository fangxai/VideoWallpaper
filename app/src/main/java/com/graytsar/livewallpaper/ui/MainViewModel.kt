package com.graytsar.livewallpaper.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.graytsar.livewallpaper.core.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

data class MainUiState(
    val isDarkModeEnabled: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val uiState = userPreferencesRepository.getSettingOptions().map {
        MainUiState(isDarkModeEnabled = it.darkMode)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000.milliseconds),
        initialValue = MainUiState()
    )
}
