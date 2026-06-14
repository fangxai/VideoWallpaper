package com.graytsar.livewallpaper.route

import android.annotation.SuppressLint
import android.app.Activity
import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.graytsar.livewallpaper.R
import com.graytsar.livewallpaper.core.common.model.WallpaperServiceType
import com.graytsar.livewallpaper.core.common.model.WallpaperType
import com.graytsar.livewallpaper.service.ImageWallpaperService
import com.graytsar.livewallpaper.service.VideoWallpaperService
import com.graytsar.livewallpaper.service.currentFlag
import com.graytsar.livewallpaper.ui.PickerScreen
import com.graytsar.livewallpaper.ui.PickerViewModel
import kotlinx.coroutines.launch

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun PickerScreenRoute(
    viewModel: PickerViewModel = hiltViewModel(),
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val wallpaperLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
            viewModel.onWallpaperSetResult(result.resultCode == Activity.RESULT_OK, currentFlag)
        }
    val videoLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }.onFailure { error ->
                when (error) {
                    is SecurityException -> Unit
                    else -> FirebaseCrashlytics.getInstance().recordException(error)
                }
            }

            viewModel.onMediaSelected(it, WallpaperType.VIDEO)
        }
    }
    val imageLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }.onFailure { error ->
                when (error) {
                    is SecurityException -> Unit
                    else -> FirebaseCrashlytics.getInstance().recordException(error)
                }
            }
            viewModel.onMediaSelected(it, WallpaperType.IMAGE)
        }
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            handlePickerEvent(
                context = context,
                wallpaperLauncher = wallpaperLauncher,
                event = event,
                onShowSnackbar = {
                    snackbarHostState.showSnackbar(message = it)
                }
            )
        }
    }

    PickerScreen(
        snackbarHostState = snackbarHostState,
        onVideoClicked = {
            try {
                videoLauncher.launch("video/*")
            } catch (_: ActivityNotFoundException) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.no_video_picker_app_found_on_this_device),
                        duration = SnackbarDuration.Short
                    )
                }
            }
        },
        onImageClicked = {
            try {
                imageLauncher.launch("image/*")
            } catch (_: ActivityNotFoundException) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.no_image_picker_app_found_on_this_device),
                        duration = SnackbarDuration.Short
                    )
                }
            }
        },
        onSettingsClick = onSettingsClick
    )
}

suspend fun handlePickerEvent(
    context: Context,
    wallpaperLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    event: PickerViewModel.PickerEvent,
    onShowSnackbar: suspend (String) -> Unit
) {
    when (event) {
        is PickerViewModel.PickerEvent.LaunchWallpaperService -> {
            launchWallpaperService(
                context = context,
                wallpaperLauncher = wallpaperLauncher,
                serviceType = event.serviceType,
                onShowSnackbar = onShowSnackbar
            )
        }

        is PickerViewModel.PickerEvent.Error -> {
            val messageRes = when (event.error) {
                PickerViewModel.PickerUiError.InvalidImage -> R.string.error_image_open
                PickerViewModel.PickerUiError.InvalidVideo -> R.string.error_video_open
                PickerViewModel.PickerUiError.Import -> R.string.error_import
            }
            onShowSnackbar(context.getString(messageRes))
        }
    }
}

private suspend fun launchWallpaperService(
    context: Context,
    wallpaperLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    serviceType: WallpaperServiceType,
    onShowSnackbar: suspend (String) -> Unit,
) {
    val wallpaperService = when (serviceType) {
        WallpaperServiceType.IMAGE -> ImageWallpaperService::class.java
        WallpaperServiceType.VIDEO -> VideoWallpaperService::class.java
    }

    try {
        wallpaperLauncher.launch(Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(context, wallpaperService)
            )
        })
    } catch (e: ActivityNotFoundException) {
        onShowSnackbar(context.getString(R.string.could_not_set_wallpaper))
    }
}