package com.whatdoiputhere.iptvplayer

import android.app.Application
import android.graphics.Bitmap
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache

class App :
    Application(),
    ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader =
        ImageLoader
            .Builder(this@App)
            .apply {
                bitmapConfig(Bitmap.Config.RGB_565)
                allowRgb565(true)
                memoryCache {
                    MemoryCache.Builder(this@App).maxSizePercent(0.08).build()
                }
                diskCache {
                    DiskCache
                        .Builder()
                        .directory(this@App.cacheDir.resolve("image_cache"))
                        .maxSizeBytes(50L * 1024 * 1024)
                        .build()
                }
                crossfade(false)
                respectCacheHeaders(true)
            }.build()
}
