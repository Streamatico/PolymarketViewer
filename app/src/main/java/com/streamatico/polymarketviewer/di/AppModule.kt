package com.streamatico.polymarketviewer.di

import com.streamatico.polymarketviewer.core.events.DefaultUiEventBus
import com.streamatico.polymarketviewer.core.events.UiEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal const val APP_SCOPE = "app_scope"

/**
 * Main application Koin module that aggregates all sub-modules.
 * This is the entry point for Koin dependency injection configuration.
 */
val appModule = module {
    // Application coroutine scope
    single<CoroutineScope>(named(APP_SCOPE)) {
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
    single<UiEventBus> { DefaultUiEventBus() }

    includes(networkModule, dataModule, viewModelModule)
}
