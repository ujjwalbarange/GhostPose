package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.ui.theme.GhostPoseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureLowRamImageLoader()
        enableEdgeToEdge()
        setContent {
            GhostPoseTheme {
                PoseHistoryScreen()
            }
        }
    }

    private fun configureLowRamImageLoader() {
        val appContext = applicationContext
        val loader = ImageLoader.Builder(appContext)
            .allowRgb565(true)
            .crossfade(false)
            .memoryCache {
                MemoryCache.Builder(appContext)
                    .maxSizePercent(0.12)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(appContext.cacheDir.resolve("pose_image_cache"))
                    .maxSizeBytes(32L * 1024 * 1024) // 32 MB max disk cache
                    .build()
            }
            .build()
        Coil.setImageLoader(loader)
    }
}
