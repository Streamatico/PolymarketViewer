package com.streamatico.polymarketviewer.di

import android.content.Context
import android.util.Log
import com.streamatico.polymarketviewer.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Named
import javax.inject.Singleton

private const val BASE_GAMMA_URL = "https://gamma-api.polymarket.com/"
private const val BASE_CLOB_URL = "https://clob.polymarket.com/"

// Custom Logger implementation using Android Logcat
private object AndroidLogger : Logger {
    private const val TAG = "HttpClient" // Ktor default tag

    override fun log(message: String) {
        // Split long messages to avoid Logcat truncation
        if (message.length > 2000) {
            Log.v(TAG, message.take(2000) + "...") // Log first 2000 chars and indicate truncation
        } else {
            Log.v(TAG, message)
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @Named("GammaClient")
    fun provideGammaHttpClient(@ApplicationContext context: Context): HttpClient {
        return createHttpClient(context, BASE_GAMMA_URL)
    }

    @Provides
    @Singleton
    @Named("ClobClient")
    fun provideClobHttpClient(@ApplicationContext context: Context): HttpClient {
        return createHttpClient(context, BASE_CLOB_URL)
    }

    private fun createHttpClient(context: Context, baseUrl: String): HttpClient {
        return HttpClient(OkHttp) {

            engine {
                config {
                    okhttp3.Cache(
                        directory = java.io.File(context.cacheDir, "http_cache"),
                        // $0.05 worth of phone storage in 2020
                        maxSize = 50L * 1024L * 1024L // 50 MiB
                    )
                }
            }

            // Default request parameters
            defaultRequest {
                url(baseUrl)
                // Add other common headers if needed
            }

            install(UserAgent) {
                agent = "PolymarketViewer/${BuildConfig.VERSION_NAME}"
            }

            // Content negotiation for JSON serialization/deserialization
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true // Useful for debugging JSON output
                    isLenient = true // Allows parsing JSON that is not strictly standard compliant
                    ignoreUnknownKeys = true // Prevents errors if the API adds new fields
                    encodeDefaults = true // Include default values during serialization if needed
                })
            }

            // Logging configuration
            if (BuildConfig.DEBUG) {
                install(Logging) {
                    logger = AndroidLogger // Use custom Android Logger
                    level = LogLevel.BODY
                    // filter { request -> request.url.host.contains("ktor.io") } // Example filter
                }
            }

            // Install HttpCache
            install(HttpCache) {
                // Default configuration uses an in-memory cache
                // For persistent disk cache, further setup is needed (e.g., via OkHttp engine config)
            }

            // Other plugins can be installed here, e.g., HttpRequestRetry, HttpTimeout
            // install(HttpTimeout) { ... }
            install(HttpTimeout) {
                requestTimeoutMillis = 60_000
            }
        }
    }
} 