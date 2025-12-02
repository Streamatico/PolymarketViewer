package com.streamatico.polymarketviewer

import android.app.Application
import android.util.Log
import com.streamatico.polymarketviewer.data.analytics.AnalyticsService
import com.streamatico.polymarketviewer.data.preferences.UserPreferencesRepository
import com.streamatico.polymarketviewer.di.appModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class PolymarketApplication : Application() {

    companion object {
        private const val TAG = "PolymarketApp"
    }

    private val userPreferencesRepository: UserPreferencesRepository by inject()
    private val analyticsService: AnalyticsService by inject()

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@PolymarketApplication)
            modules(appModule)
        }

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