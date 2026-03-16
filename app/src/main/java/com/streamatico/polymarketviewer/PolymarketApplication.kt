package com.streamatico.polymarketviewer

import android.app.Application
import android.util.Log
import com.streamatico.polymarketviewer.di.appModule
import com.streamatico.polymarketviewer.ui.widget.EventWidgetUpdater
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class PolymarketApplication : Application() {

    companion object {
        private const val TAG = "PolymarketApp"
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "Starting the app...")

        startKoin {
            androidLogger()
            androidContext(this@PolymarketApplication)
            modules(appModule)
        }

        EventWidgetUpdater.enqueuePeriodic(this)
    }
}