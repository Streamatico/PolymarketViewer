package com.streamatico.polymarketviewer.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.streamatico.polymarketviewer.data.analytics.AnalyticsService
import com.streamatico.polymarketviewer.data.network.PolymarketClobApiClient
import com.streamatico.polymarketviewer.data.network.PolymarketDataApiClient
import com.streamatico.polymarketviewer.data.network.PolymarketGammaApiClient
import com.streamatico.polymarketviewer.data.network.PolymarketHttpClientNames
import com.streamatico.polymarketviewer.data.preferences.UserPreferencesRepository
import com.streamatico.polymarketviewer.data.repository.PolymarketRepositoryImpl
import com.streamatico.polymarketviewer.domain.repository.PolymarketRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

private const val USER_PREFERENCES = "user_preferences"

/**
 * Koin module for data layer dependencies.
 * Provides repositories, data sources, services, and API clients.
 */
val dataModule = module {
    // DataStore
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create(
            produceFile = { androidContext().preferencesDataStoreFile(USER_PREFERENCES) }
        )
    }

    // Repositories & Services
    singleOf(::UserPreferencesRepository)
    single { AnalyticsService(get(named(PolymarketHttpClientNames.ANALYTICS_CLIENT)), get()) }

    // API Clients
    single { PolymarketGammaApiClient(get(named(PolymarketHttpClientNames.GAMMA_CLIENT))) }
    single { PolymarketClobApiClient(get(named(PolymarketHttpClientNames.CLOB_CLIENT))) }
    single { PolymarketDataApiClient(get(named(PolymarketHttpClientNames.DATA_CLIENT))) }

    // Repository Implementation
    singleOf(::PolymarketRepositoryImpl) bind PolymarketRepository::class
}
