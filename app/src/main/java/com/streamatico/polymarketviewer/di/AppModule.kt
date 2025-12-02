package com.streamatico.polymarketviewer.di

import org.koin.dsl.module

/**
 * Main application Koin module that aggregates all sub-modules.
 * This is the entry point for Koin dependency injection configuration.
 */
val appModule = module {
    includes(networkModule, dataModule, viewModelModule)
}
