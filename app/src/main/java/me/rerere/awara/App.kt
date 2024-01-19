package me.rerere.awara

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import me.rerere.awara.di.databaseModule
import me.rerere.awara.di.networkModule
import me.rerere.awara.di.repoModule
import me.rerere.awara.di.userCaseModule
import me.rerere.awara.di.viewModelModule
import me.rerere.awara.ui.registerErrorHandler
import me.rerere.compose_setting.preference.initComposeSetting
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin

class App : Application(), ImageLoaderFactory, KoinComponent {
    override fun onCreate() {
        super.onCreate()
        registerErrorHandler()
        initComposeSetting()
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(
                networkModule,
                databaseModule,
                repoModule,
                viewModelModule,
                userCaseModule
            )
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient(get<OkHttpClient>())
            .components {
                add(SvgDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .fallback(R.drawable.default_background)
            .error(R.drawable.cancel)
            .build()
    }
}