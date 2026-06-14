package com.graytsar.livewallpaper.ui

import android.app.Activity
import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.graytsar.livewallpaper.R
import com.graytsar.livewallpaper.core.common.model.WallpaperServiceType
import com.graytsar.livewallpaper.core.common.model.WallpaperType
import com.graytsar.livewallpaper.service.ImageWallpaperService
import com.graytsar.livewallpaper.service.VideoWallpaperService
import com.graytsar.livewallpaper.service.currentFlag
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PickerFragment : Fragment() {
    private val viewModel: PickerViewModel by viewModels()

    private val wallpaperLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            viewModel.onWallpaperSetResult(result.resultCode == Activity.RESULT_OK, currentFlag)
        }

    private val videoLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                runCatching {
                    requireContext().contentResolver.takePersistableUriPermission(
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

    private val imageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                runCatching {
                    requireContext().contentResolver.takePersistableUriPermission(
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                MaterialTheme {
                    PickerScreen(
                        onVideoClicked = onVideoClickListener,
                        onImageClicked = onImageClickListener
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    handlePickerEvent(event)
                }
            }
        }
    }

    fun handlePickerEvent(event: PickerViewModel.PickerEvent) {
        when (event) {
            is PickerViewModel.PickerEvent.LaunchWallpaperService -> {
                launchWallpaperService(event.serviceType)
            }

            is PickerViewModel.PickerEvent.Error -> {
                val errorMessage = when (event.error) {
                    PickerViewModel.PickerUiError.InvalidImage -> R.string.error_image_open
                    PickerViewModel.PickerUiError.InvalidVideo -> R.string.error_video_open
                    PickerViewModel.PickerUiError.Import -> R.string.error_import
                }
                showError(errorMessage)
            }
        }
    }

    private fun showError(@StringRes message: Int) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    private fun launchWallpaperService(serviceType: WallpaperServiceType) {
        val wallpaperService = when (serviceType) {
            WallpaperServiceType.IMAGE -> ImageWallpaperService::class.java
            WallpaperServiceType.VIDEO -> VideoWallpaperService::class.java
        }

        try {
            wallpaperLauncher.launch(Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(requireContext(), wallpaperService)
                )
            })
        } catch (e: ActivityNotFoundException) {
            Snackbar.make(requireView(), "could not set wallpaper", Snackbar.LENGTH_SHORT).show()
        }
    }

    private val onVideoClickListener: () -> Unit = {
        try {
            videoLauncher.launch("video/*")
        } catch (_: ActivityNotFoundException) {
            Snackbar.make(requireView(), "No video picker app found on this device.", Snackbar.LENGTH_SHORT).show()
        }
    }

    private val onImageClickListener: () -> Unit = {
        try {
            imageLauncher.launch("image/*")
        } catch (_: ActivityNotFoundException) {
            Snackbar.make(requireView(), "No image picker app found on this device.", Snackbar.LENGTH_SHORT).show()
        }
    }

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_main, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.menuSettings -> {
                    findNavController().navigate(R.id.fragmentSettings)
                    true
                }

                else -> false
            }
        }
    }
}

@Composable
fun PickerScreen(
    onVideoClicked: () -> Unit,
    onImageClicked: () -> Unit
) {
    Surface {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            PickerButton(
                icon = R.drawable.ic_video_24,
                text = R.string.video,
                contentDescription = "select a video wallpaper",
                onClick = onVideoClicked
            )
            Spacer(modifier = Modifier.height(16.dp))
            PickerButton(
                icon = R.drawable.ic_image_24,
                text = R.string.image,
                contentDescription = "select an image wallpaper",
                onClick = onImageClicked
            )
        }
    }
}

@Composable
private fun PickerButton(
    @DrawableRes icon: Int,
    @StringRes text: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(200.dp)
            .wrapContentHeight()
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = contentDescription,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(id = text),
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PickerScreenPreview() {
    MaterialTheme {
        PickerScreen(
            onVideoClicked = {},
            onImageClicked = {}
        )
    }
}