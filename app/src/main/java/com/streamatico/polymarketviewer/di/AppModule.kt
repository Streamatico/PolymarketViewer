package com.streamatico.polymarketviewer.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.streamatico.polymarketviewer.data.network.PolymarketClobApiClient
import com.streamatico.polymarketviewer.data.network.PolymarketGammaApiClient
import com.streamatico.polymarketviewer.data.repository.PolymarketRepositoryImpl
import com.streamatico.polymarketviewer.domain.repository.PolymarketRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/*
@Module
@InstallIn(SingletonComponent::class)
internal interface AppBindModule {
    @Binds
    @Singleton
    fun bindPolymarketRepository(impl: PolymarketRepositoryImpl): PolymarketRepository
}
*/

private const val USER_PREFERENCES = "user_preferences"

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun providePolymarketRepository(
        gammaApiClient: PolymarketGammaApiClient,
        clobApiClient: PolymarketClobApiClient
    ): PolymarketRepository {
        return PolymarketRepositoryImpl(
            gammaApiClient = gammaApiClient,
            clobApiClient = clobApiClient
        )
    }

    // --- Provides DataStore<Preferences> --- //
    @Singleton
    @Provides
    fun providePreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { appContext.preferencesDataStoreFile(USER_PREFERENCES) }
        )
    }

    // --- Provides UserPreferencesRepository (already Singleton via constructor annotation) --- //
    // Hilt automatically provides this if DataStore<Preferences> is provided
    // No explicit @Provides needed unless constructor injection is not used
}