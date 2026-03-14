package com.streamatico.polymarketviewer

import android.app.Application
import android.util.Log
import com.streamatico.polymarketviewer.data.analytics.AnalyticsEvent
import com.streamatico.polymarketviewer.data.analytics.AnalyticsService
import com.streamatico.polymarketviewer.di.appModule
import com.streamatico.polymarketviewer.ui.widget.EventWidgetUpdater
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class PolymarketApplication : Application() {

    companion object {
        private const val TAG = "PolymarketApp"
    }

    private val analyticsService: AnalyticsService by inject()

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "Starting the app...")

        startKoin {
            androidLogger()
            androidContext(this@PolymarketApplication)
            modules(appModule)
        }

        analyticsService.track(AnalyticsEvent.AppLaunched)
        EventWidgetUpdater.enqueuePeriodic(this)
    }
}