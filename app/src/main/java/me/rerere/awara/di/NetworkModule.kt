package me.rerere.awara.di

import me.rerere.awara.BuildConfig
import me.rerere.awara.data.source.HitokotoAPI
import me.rerere.awara.data.source.IwaraAPI
import me.rerere.awara.data.source.UpdateAPI
import me.rerere.awara.util.isTrustedIwaraHost
import me.rerere.awara.util.SerializationConverterFactory
import me.rerere.compose_setting.preference.mmkvPreference
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

private const val UA = "Mozilla/5.0 (Linux; Android 12; Pixel 6 Build/SD1A.210817.023; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/94.0.4606.71 Mobile Safari/537.36"

val networkModule = module {
    single {
        val builder = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(45, TimeUnit.SECONDS)
            .addInterceptor {
                val request = it.request()
                val url = request.url
                val newRequest = request.newBuilder()
                    .apply {
                        if (isTrustedIwaraHost(url.host)) {
                            header("User-Agent", UA)
                            header("Origin", "https://www.iwara.tv")
                            header("Referer", "https://www.iwara.tv/")
                            header("Accept-Language", "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7")

                            val token = if (
                                url.host.equals("api.iwara.tv", ignoreCase = true) &&
                                url.encodedPath == "/user/token"
                            ) {
                                mmkvPreference.getString("refresh_token", "")
                            } else {
                                mmkvPreference.getString("access_token", "")
                            }
                            if (!token.isNullOrBlank()) {
                                header("Authorization", "Bearer $token")
                            }
                        }
                    }
                    .build()
                it.proceed(newRequest)
            }

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply {
                    redactHeader("Authorization")
                    setLevel(HttpLoggingInterceptor.Level.BASIC)
                }
            )
        }

        builder.build()
    }

    single {
        Retrofit.Builder()
            .client(get())
            .baseUrl("https://api.iwara.tv")
            .addConverterFactory(SerializationConverterFactory.create())
            .build()
            .create(IwaraAPI::class.java)
    }

    single {
        Retrofit.Builder()
            .client(get())
            .baseUrl("https://v1.hitokoto.cn")
            .addConverterFactory(SerializationConverterFactory.create())
            .build()
            .create(HitokotoAPI::class.java)
    }

    single {
        Retrofit.Builder()
            .client(get())
            .baseUrl("https://33jltt4ir7m3j2jst7g3kurrbu0qfqrt.lambda-url.ap-southeast-2.on.aws")
            .addConverterFactory(SerializationConverterFactory.create())
            .build()
            .create(UpdateAPI::class.java)
    }
}
