package com.graytsar.livewallpaper.domain

import android.content.Context
import com.graytsar.livewallpaper.core.repository.UserPreferencesRepository
import com.graytsar.livewallpaper.util.Util
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CleanupMediaUseCaseTest {

    private val context = mockk<Context>()
    private val userPreferencesRepository = mockk<UserPreferencesRepository>()

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `invoke keeps active wallpaper and new preview while deleting stale imports`() = runTest {
        val filesDir = tempDir.toFile()
        val imageDir = Util.getImageImportDirectory(createContext(filesDir))
        val videoDir = Util.getVideoImportDirectory(createContext(filesDir))
        val activeImage = imageDir.resolve("active-image")
        val staleImage = imageDir.resolve("stale-image")
        val newPreviewVideo = videoDir.resolve("preview-video")
        val staleVideo = videoDir.resolve("stale-video")

        activeImage.writeText("active")
        staleImage.writeText("stale-image")
        newPreviewVideo.writeText("preview")
        staleVideo.writeText("stale-video")
        coEvery { userPreferencesRepository.getWallpaperPath() } returns listOf(activeImage.path)

        CleanupMediaUseCase(createContext(filesDir), userPreferencesRepository)(newPreviewVideo.path)

        activeImage.exists() shouldBe true
        newPreviewVideo.exists() shouldBe true
        staleImage.exists() shouldBe false
        staleVideo.exists() shouldBe false
        imageDir.isDirectory shouldBe true
        videoDir.isDirectory shouldBe true
    }

    @Test
    fun `invoke keeps active video, image and preview while deleting stale imports`() = runTest {
        val filesDir = tempDir.toFile()
        val imageDir = Util.getImageImportDirectory(createContext(filesDir))
        val videoDir = Util.getVideoImportDirectory(createContext(filesDir))
        val activeImage = imageDir.resolve("active-image")
        val activeVideo = imageDir.resolve("active-video")
        val previewVideo = imageDir.resolve("preview-video")
        val staleImage = imageDir.resolve("stale-image")
        val staleVideo = videoDir.resolve("stale-video")

        activeImage.writeText("active-image")
        activeVideo.writeText("active-video")
        previewVideo.writeText("preview-video")
        staleImage.writeText("stale-image")
        staleVideo.writeText("stale-video")
        coEvery { userPreferencesRepository.getWallpaperPath() } returns listOf(
            activeImage.path,
            activeVideo.path,
            previewVideo.path
        )

        CleanupMediaUseCase(createContext(filesDir), userPreferencesRepository)(previewVideo.path)

        activeImage.exists() shouldBe true
        previewVideo.exists() shouldBe true
        staleImage.exists() shouldBe false
        staleVideo.exists() shouldBe false
        imageDir.isDirectory shouldBe true
        videoDir.isDirectory shouldBe true
    }

    @Test
    fun `invoke deletes all stale imports when no active wallpaper exists`() = runTest {
        val filesDir = tempDir.toFile()
        val imageDir = Util.getImageImportDirectory(createContext(filesDir))
        val videoDir = Util.getVideoImportDirectory(createContext(filesDir))
        val previewImage = imageDir.resolve("preview-image")
        val staleImage = imageDir.resolve("old-image")
        val staleVideo = videoDir.resolve("old-video")

        previewImage.writeText("preview")
        staleImage.writeText("stale-image")
        staleVideo.writeText("stale-video")
        coEvery { userPreferencesRepository.getWallpaperPath() } returns emptyList()

        CleanupMediaUseCase(createContext(filesDir), userPreferencesRepository)(previewImage.path)

        previewImage.exists() shouldBe true
        staleImage.exists() shouldBe false
        staleVideo.exists() shouldBe false
    }

    private fun createContext(filesDir: java.io.File): Context {
        io.mockk.every { context.filesDir } returns filesDir
        return context
    }
}

