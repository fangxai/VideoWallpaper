@file:OptIn(ExperimentalMaterial3Api::class)

package com.graytsar.livewallpaper.ui

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.graytsar.livewallpaper.R
import com.graytsar.livewallpaper.core.common.model.ImageScaling
import com.graytsar.livewallpaper.core.common.model.VideoScaling

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    uiState: SettingsUiState,
    onBackPress: () -> Unit,
    imageScalingOptions: List<ImageScaling>,
    videoScalingOptions: List<VideoScaling>,
    onDarkModeChange: (Boolean) -> Unit,
    onImageOptionSelected: (ImageScaling) -> Unit,
    onAudioEnabledChange: (Boolean) -> Unit,
    onVideoOptionSelected: (VideoScaling) -> Unit,
    onDoubleTapToPauseChange: (Boolean) -> Unit,
    onPlayOffscreenChange: (Boolean) -> Unit,
    onClearWallpaperClick: () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(
                            painter = painterResource(R.drawable.ic_all_arrow_back_24),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                SettingsSectionHeader(titleRes = R.string.section_theme)
                SettingsSwitchButton(
                    modifier = Modifier.fillMaxWidth(),
                    textRes = R.string.dark_mode,
                    isChecked = uiState.isDarkModeEnabled,
                    onCheckedChange = onDarkModeChange
                )
                Spacer(modifier = Modifier.size(24.dp))

                SettingsSectionHeader(titleRes = R.string.section_gif)
                DropdownTextField(
                    modifier = Modifier.fillMaxWidth(),
                    options = imageScalingOptions,
                    optionsToStringRes = { it.toTranslation() },
                    value = stringResource(uiState.imageScaleType.toTranslation()),
                    label = R.string.scale_type,
                    onOptionSelected = onImageOptionSelected
                )
                Spacer(modifier = Modifier.size(24.dp))

                SettingsSectionHeader(titleRes = R.string.section_video)
                SettingsSwitchButton(
                    modifier = Modifier.fillMaxWidth(),
                    textRes = R.string.enable_audio,
                    isChecked = uiState.isAudioEnabled,
                    onCheckedChange = onAudioEnabledChange
                )
                DropdownTextField(
                    modifier = Modifier.fillMaxWidth(),
                    options = videoScalingOptions,
                    optionsToStringRes = { it.toTranslation() },
                    value = stringResource(uiState.videoScaleType.toTranslation()),
                    label = R.string.scale_type,
                    onOptionSelected = onVideoOptionSelected
                )
                Spacer(modifier = Modifier.size(24.dp))

                SettingsSectionHeader(titleRes = R.string.section_general)
                SettingsSwitchButton(
                    modifier = Modifier.fillMaxWidth(),
                    textRes = R.string.double_tap_to_pause,
                    isChecked = uiState.isDoubleTapToPause,
                    onCheckedChange = onDoubleTapToPauseChange
                )
                SettingsSwitchButton(
                    modifier = Modifier.fillMaxWidth(),
                    textRes = R.string.play_offscreen,
                    isChecked = uiState.isPlayOffscreen,
                    onCheckedChange = onPlayOffscreenChange
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onClearWallpaperClick
                ) {
                    Text(text = stringResource(R.string.clear_wallpapers))
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SettingsScreenPreview() {
    val uiState = SettingsUiState()
    val imageScalingOptions = ImageScaling.entries
    val videoScalingOptions = VideoScaling.entries

    MaterialTheme {
        SettingsScreen(
            uiState = uiState,
            onBackPress = {

            },
            imageScalingOptions = imageScalingOptions,
            videoScalingOptions = videoScalingOptions,
            onDarkModeChange = { isEnabled ->

            },
            onImageOptionSelected = { option ->

            },
            onAudioEnabledChange = { isEnabled ->

            },
            onVideoOptionSelected = { option ->

            },
            onDoubleTapToPauseChange = { isEnabled ->

            },
            onPlayOffscreenChange = { isEnabled ->

            },
            onClearWallpaperClick = {

            }
        )
    }
}

@Composable
fun SettingsSectionHeader(
    modifier: Modifier = Modifier,
    @StringRes titleRes: Int
) {
    Text(
        modifier = modifier,
        text = stringResource(titleRes),
        style = MaterialTheme.typography.titleSmall
    )
}

@Composable
fun SettingsSwitchButton(
    modifier: Modifier = Modifier,
    @StringRes textRes: Int,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .toggleable(
                value = isChecked,
                onValueChange = onCheckedChange,
                role = Role.Switch
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(id = textRes))
        Switch(
            checked = isChecked,
            onCheckedChange = null
        )
    }
}

@Composable
fun <T> DropdownTextField(
    modifier: Modifier = Modifier,
    options: List<T>,
    optionsToStringRes: (T) -> Int,
    value: String,
    @StringRes label: Int,
    onOptionSelected: (T) -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            readOnly = true,
            value = value,
            label = { Text(text = stringResource(id = label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            onValueChange = { },
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = stringResource(id = optionsToStringRes(option))) },
                    onClick = {
                        onOptionSelected(option)
                        isExpanded = false
                    }
                )
            }
        }
    }
}