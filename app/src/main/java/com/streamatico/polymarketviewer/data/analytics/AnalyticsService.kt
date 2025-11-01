package com.streamatico.polymarketviewer.data.analytics

import android.os.Build
import android.util.Log
import com.streamatico.polymarketviewer.BuildConfig
import com.streamatico.polymarketviewer.data.network.PolymarketHttpClientNames
import com.streamatico.polymarketviewer.data.preferences.UserPreferencesRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.flow.first
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

private const val ANALYTICS_ENDPOINT = "https://polymarket-ping.streamatico.workers.dev/ping"

@Singleton
class AnalyticsService @Inject constructor(
    @Named(PolymarketHttpClientNames.ANALYTICS_CLIENT) private val httpClient: HttpClient,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    /**
     * Sends an anonymous ping to analytics endpoint with minimal technical information.
     * Silently ignores all errors.
     */
    suspend fun sendPing() {
        try {
            // Check if analytics is enabled
            val userPreferences = userPreferencesRepository.userPreferencesFlow.first()
            if (!userPreferences.analyticsEnabled) {
                return
            }

            // Build parameters
            val version = BuildConfig.VERSION_NAME
            val sdk = Build.VERSION.SDK_INT
            val language = Locale.getDefault().language
            val buildType = if (BuildConfig.DEBUG) "debug" else "release"
            val isFirstLaunch = userPreferences.isFirstLaunch

            // Send GET request
            httpClient.get(ANALYTICS_ENDPOINT) {
                url {
                    parameters.append("event", "ping")
                    parameters.append("v", version)
                    parameters.append("sdk", sdk.toString())
                    parameters.append("lang", language)
                    parameters.append("build", buildType)

                    // Only send first=true on first launch
                    if (isFirstLaunch) {
                        parameters.append("first", "true")
                    }
                }
            }

            // Mark first launch as completed after successful ping
            if (isFirstLaunch) {
                userPreferencesRepository.setFirstLaunchCompleted()
            }
        } catch (e: Exception) {
            // Silently ignore all errors - analytics should never affect app functionality
            Log.e("AnalyticsService", "Failed to send ping", e)
        }
    }
}

