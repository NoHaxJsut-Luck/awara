package me.rerere.awara.ui.component.player

import android.content.Context
import android.os.storage.StorageManager
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

object PlayerCache {
    private lateinit var cacheFactory: CacheDataSource.Factory

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun get(context: Context): CacheDataSource.Factory {
        if (!::cacheFactory.isInitialized) {
            val cacheFolder = File(context.cacheDir, "player_cache")
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val maxBytes =
                storageManager.getCacheQuotaBytes(storageManager.getUuidForPath(cacheFolder))
            val cache = SimpleCache(
                cacheFolder,
                LeastRecentlyUsedCacheEvictor(maxBytes),
                StandaloneDatabaseProvider(context)
            )
            val cacheFactory = CacheDataSource.Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                .setCacheKeyFactory { dataSpec ->
                    val filename = dataSpec.uri.getQueryParameter("filename")
                    dataSpec.key ?: filename ?: dataSpec.uri.toString()
                }
            this.cacheFactory = cacheFactory
        }
        return this.cacheFactory
    }
}
