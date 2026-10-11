package com.passportphotomaker.print

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache

/**
 * Application class. Configures a Coil image loader with memory + disk caching so
 * the photo gallery and previews scroll smoothly. No network fetching is enabled,
 * which reinforces the "photos never leave the device" guarantee.
 */
class PassportApp : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.20)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(32L * 1024 * 1024)
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
