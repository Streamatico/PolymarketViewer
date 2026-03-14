package com.streamatico.polymarketviewer.data.analytics

import android.os.Build
import android.util.Log
import com.streamatico.polymarketviewer.BuildConfig
import com.streamatico.polymarketviewer.data.preferences.UserPreferencesRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

// Analytics service
// Sends anonymous analytics with minimal technical information.
// Helps to improve the app.
class AnalyticsService(
    private val httpClient: HttpClient,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val appScope: CoroutineScope
) {
    companion object {
        private const val TAG = "AnalyticsService"
        private const val ANALYTICS_ENDPOINT = "https://polymarket-ping.streamatico.workers.dev/ping"
    }

    fun track(event: AnalyticsEvent) {
        appScope.launch {
            sendEventRequest(event)
        }
    }

    private suspend fun sendEventRequest(event: AnalyticsEvent) {
        try {
            val preferences = userPreferencesRepository.userPreferencesFlow.first()
            if (!preferences.analyticsEnabled) return

            val version = BuildConfig.VERSION_NAME
            val sdk = Build.VERSION.SDK_INT
            val language = Locale.getDefault().language
            val buildType = if (BuildConfig.DEBUG) "debug" else "release"
            val isFirstLaunch = event.includeFirstLaunchFlag && preferences.isFirstLaunch

            httpClient.get(ANALYTICS_ENDPOINT) {
                url {
                    parameters.append("event", event.wireName)
                    parameters.append("v", version)
                    parameters.append("sdk", sdk.toString())
                    parameters.append("lang", language)
                    parameters.append("build", buildType)

                    if (isFirstLaunch) {
                        parameters.append("first", "true")
                    }
                }
            }

            if (isFirstLaunch) {
                userPreferencesRepository.setFirstLaunchCompleted()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Analytics failures should never break app flow.
            Log.e(TAG, "Failed to send ping", e)
        }
    }
}
