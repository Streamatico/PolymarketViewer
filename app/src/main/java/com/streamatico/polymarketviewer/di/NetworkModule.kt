package com.streamatico.polymarketviewer.di

import android.content.Context
import android.util.Log
import com.streamatico.polymarketviewer.BuildConfig
import com.streamatico.polymarketviewer.data.network.PolymarketDnsResolver
import com.streamatico.polymarketviewer.data.network.PolymarketHttpClientNames
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
import okhttp3.Cache
import okhttp3.Dns
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.File

private const val BASE_GAMMA_URL = "https://gamma-api.polymarket.com/"
private const val BASE_CLOB_URL = "https://clob.polymarket.com/"
private const val BASE_DATA_URL = "https://data-api.polymarket.com/"
private const val POLYMARKET_DNS_QUALIFIER = "PolymarketDns"
private const val HTTP_CACHE_DIRECTORY_NAME = "http_cache"
private const val NETWORK_CACHE_SIZE_BYTES = 50L * 1024L * 1024L

// Custom Logger implementation using Android Logcat
private object AndroidLogger : Logger {
    private const val TAG = "HttpClient"

    override fun log(message: String) {
        // Split long messages to avoid Logcat truncation
        if (message.length > 2000) {
            Log.v(TAG, message.take(2000) + "...")
        } else {
            Log.v(TAG, message)
        }
    }
}

/**
 * Koin module for network layer dependencies.
 * Provides HTTP clients for different API endpoints.
 */
val networkModule = module {
    single<Dns>(named(POLYMARKET_DNS_QUALIFIER)) {
        PolymarketDnsResolver(
            context = androidContext(),
            userPreferencesRepository = get(),
            appScope = get(named(APP_SCOPE))
        )
    }

    single(named(PolymarketHttpClientNames.GAMMA_CLIENT)) {
        createHttpClient(
            context = androidContext(),
            baseUrl = BASE_GAMMA_URL,
            dns = get(named(POLYMARKET_DNS_QUALIFIER))
        )
    }

    single(named(PolymarketHttpClientNames.CLOB_CLIENT)) {
        createHttpClient(
            context = androidContext(),
            baseUrl = BASE_CLOB_URL,
            dns = get(named(POLYMARKET_DNS_QUALIFIER))
        )
    }

    single(named(PolymarketHttpClientNames.DATA_CLIENT)) {
        createHttpClient(
            context = androidContext(),
            baseUrl = BASE_DATA_URL,
            dns = get(named(POLYMARKET_DNS_QUALIFIER))
        )
    }

    single(named(PolymarketHttpClientNames.ANALYTICS_CLIENT)) {
        HttpClient(OkHttp) {
            // No cache needed for analytics
            install(HttpTimeout) {
                requestTimeoutMillis = 5_000 // Short timeout for analytics
            }
            // No logging for analytics, even in debug mode
        }
    }
}

private fun createHttpClient(context: Context, baseUrl: String, dns: Dns): HttpClient {
    return HttpClient(OkHttp) {

        engine {
            config {
                cache(
                    Cache(
                        directory = File(context.cacheDir, HTTP_CACHE_DIRECTORY_NAME),
                        // $0.05 worth of phone storage in 2020
                        maxSize = NETWORK_CACHE_SIZE_BYTES
                    )
                )

                dns(dns)
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
