package com.graytsar.livewallpaper.service

import android.os.Build
import android.os.Bundle
import android.service.wallpaper.WallpaperService
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.graytsar.livewallpaper.core.common.model.ImageEngineSettings
import com.graytsar.livewallpaper.core.common.model.WallpaperFlag
import com.graytsar.livewallpaper.core.common.model.WallpaperServiceType
import com.graytsar.livewallpaper.core.repository.UserPreferencesRepository
import com.graytsar.livewallpaper.core.repository.WallpaperSelection
import com.graytsar.livewallpaper.engine.Api28ImageRenderer
import com.graytsar.livewallpaper.engine.WallpaperRenderer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class ImageWallpaperService : WallpaperService() {
    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreateEngine(): Engine = ImageWallpaperEngine()

    private fun createRenderer(
        holder: SurfaceHolder,
        file: File,
        settings: ImageEngineSettings
    ): WallpaperRenderer {
        return Api28ImageRenderer(holder, file, settings)
    }

    @OptIn(FlowPreview::class)
    private inner class ImageWallpaperEngine : Engine() {
        private val engineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        private var observeJob: Job? = null
        private var renderer: WallpaperRenderer? = null
        private var engineSettings: ImageEngineSettings? = null
        private var isSurfaceCreated = false
        private var lastSelection: WallpaperSelection? = null
        private var isPaused: Boolean = false

        private val gestureDetector = GestureDetector(
            this@ImageWallpaperService,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    if (engineSettings?.general?.doubleTapToPause == true) {
                        isPaused = !isPaused
                        renderer?.onPauseChanged(isPaused)
                        return true
                    }
                    return false
                }
            })

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            startObserveJob()
        }

        private fun startObserveJob() {
            observeJob?.cancel()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                currentFlag = WallpaperFlag.from(wallpaperFlags)
            }

            observeJob = engineScope.launch {
                combine(
                    userPreferencesRepository.getImageEngineSettingsFlow(),
                    userPreferencesRepository.getWallpaperSelectionFlow(
                        isPreview = isPreview,
                        serviceType = WallpaperServiceType.IMAGE,
                        wallpaperFlag = currentFlag
                    )
                ) { settings, selection ->
                    settings to selection
                }.debounce(100.milliseconds).collectLatest { (settings, selection) ->
                    engineSettings = settings
                    lastSelection = selection

                    val shouldUpdate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        selection?.flag == WallpaperFlag.from(wallpaperFlags)
                    } else true

                    if (shouldUpdate && isSurfaceCreated) {
                        handleUpdate(settings = settings, selection = selection)
                    }
                }
            }
        }

        override fun onCommand(
            action: String?,
            x: Int,
            y: Int,
            z: Int,
            extras: Bundle?,
            resultRequested: Boolean
        ): Bundle? {
            if (action == "android.wallpaper.reapply") {
                startObserveJob()
            }
            return super.onCommand(action, x, y, z, extras, resultRequested)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            renderer?.onVisibilityChanged(visible)
        }

        override fun onTouchEvent(event: MotionEvent?) {
            if (event != null) gestureDetector.onTouchEvent(event)
            super.onTouchEvent(event)
        }

        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder)
            isSurfaceCreated = true
            engineSettings?.let { settings ->
                handleUpdate(settings = settings, selection = lastSelection)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            isSurfaceCreated = false
            releaseRenderer()
            super.onSurfaceDestroyed(holder)
        }

        override fun onDestroy() {
            observeJob?.cancel()
            engineScope.cancel()
            releaseRenderer()
            super.onDestroy()
        }

        private fun handleUpdate(settings: ImageEngineSettings, selection: WallpaperSelection?) {
            val surfaceHolder = surfaceHolder ?: return
            if (selection == null) {
                releaseRenderer()
                return
            }

            val file = File(selection.path)
            if (!file.exists()) {
                releaseRenderer()
                return
            }

            releaseRenderer()
            renderer = createRenderer(
                holder = surfaceHolder,
                file = file,
                settings = settings
            ).also { renderer ->
                renderer.onSurfaceReady()
            }
        }

        private fun releaseRenderer() {
            renderer?.release()
            renderer = null
        }
    }
}