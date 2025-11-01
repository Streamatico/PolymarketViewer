package com.streamatico.polymarketviewer

import android.app.Application
import android.util.Log
import com.streamatico.polymarketviewer.data.analytics.AnalyticsService
import com.streamatico.polymarketviewer.data.preferences.UserPreferencesRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class PolymarketApplication : Application() {

    companion object {
        private const val TAG = "PolymarketApp"
    }

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var analyticsService: AnalyticsService

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        initializeUserPreferences()
        sendAnalyticsPing()
    }

    private fun initializeUserPreferences() {
        applicationScope.launch {
            try {
                userPreferencesRepository.initialize()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize user preferences.", e)
            }
        }
    }

    private fun sendAnalyticsPing() {
        applicationScope.launch {
            analyticsService.sendPing()
        }
    }
}